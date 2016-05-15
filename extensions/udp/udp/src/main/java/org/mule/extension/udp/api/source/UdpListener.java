/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.udp.api.source;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.extension.tcp.internals.TcpInputStream;
import org.mule.extension.udp.api.client.UdpClient;
import org.mule.extension.udp.api.config.UdpConfig;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.api.message.NullPayload;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.FlowConstructAware;
import org.mule.runtime.core.transformer.types.DataTypeFactory;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.source.Source;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("udp-listener")
public class UdpListener extends Source<Object, UdpAttributes> implements FlowConstructAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpListener.class);
    private ExecutorService executorService;
    private FlowConstruct flowConstruct;


    @UseConfig
    private UdpConfig config;

    @Inject
    private MuleContext muleContext;

    @Connection
    private UdpClient client;

    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    @Override
    public void start() throws Exception
    {
        executorService = newSingleThreadExecutor(r -> new Thread(r, format("%s%s.tcp.listener", getPrefix(muleContext), flowConstruct.getName())));
        executorService.execute(this::listen);
    }

    private void listen()
    {
        LOGGER.debug("Started listener");
        Socket socket = null;
        for (; ; )
        {
            if (isRequestedToStop())
            {
                return;
            }

            try
            {
                socket = client.connect();
                if (socket == null)
                {
                    // socket.closed() was called
                    return;
                }
                processNewConnection(socket);
            }
            catch (ConnectionException e)
            {
                e.printStackTrace();
                sourceContext.getExceptionCallback().onException(e);
            }

        }
    }

    private MuleMessage<Object, UdpAttributes> createMessage(Socket socket, UdpAttributes
            attributes) throws IOException, ConnectionException
    {
        Object payload = NullPayload.getInstance();
        DataType dataType = DataTypeFactory.create(NullPayload.class);
        MuleMessage<Object, UdpAttributes> message;


        if (socket.isConnected() && socket.isBound())
        {
            payload = receiveFromSocket(socket, config.getUdpSocketProperties().getTimeout());
            if (payload == null)
            {
                return (MuleMessage) new DefaultMuleMessage(NullPayload.getInstance(), dataType, attributes, muleContext);
            }

            dataType = getTcpMessageDataType(DataTypeFactory.create(Object.class), attributes);
        }


        message = (MuleMessage) new DefaultMuleMessage(payload, dataType, attributes, muleContext);
        return message;
    }

    private Object receiveFromSocket(DatagramSocket socket, int timeout) throws ConnectionException, IOException
    {

        UdpSocketProperties properties = config.getUdpSocketProperties();
        try
        {
            DatagramPacket packet = new DatagramPacket(new byte[properties.getReceiveBufferSize()],
                                                       properties.getReceiveBufferSize());

            if (timeout > 0 && timeout != socket.getSoTimeout())
            {
                socket.setSoTimeout(timeout);
            }
            socket.receive(packet);
            return packet.getData();
        }
    }

    private DataType<Object> getTcpMessageDataType(DataType<?> originalDataType, UdpAttributes attributes)
    {
        DataType<Object> newDataType = DataTypeFactory.create(Object.class);
        newDataType.setEncoding(originalDataType.getEncoding());

        //String presumedMimeType = mimetypesFileTypeMap.getContentType(attributes.getPath());
        //newDataType.setMimeType(presumedMimeType != null ? presumedMimeType : originalDataType.getMimeType());

        return newDataType;
    }

    private void processNewConnection(Socket socket)
    {
        LOGGER.debug("Processing new connection");
        if (isRequestedToStop())
        {
            return;
        }

        // TODO new thread with reading and creating message
        try
        {
            sourceContext.getMessageHandler().handle(createMessage(socket, new UdpAttributes()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        try
        {
            client.disconnect();
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
        }
        stopRequested.set(true);
        shutdownExecutor();
    }

    private boolean isRequestedToStop()
    {
        return stopRequested.get() || Thread.currentThread().isInterrupted();
    }


    private void shutdownExecutor()
    {
        if (executorService == null)
        {
            return;
        }

        executorService.shutdownNow();
        try
        {
            if (!executorService.awaitTermination(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS))
            {
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("Could not properly terminate pending events for directory listener on flow " + flowConstruct.getName());
                }
            }
        }
        catch (InterruptedException e)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("Got interrupted while trying to terminate pending events for directory listener on flow " + flowConstruct.getName());
            }
        }
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
