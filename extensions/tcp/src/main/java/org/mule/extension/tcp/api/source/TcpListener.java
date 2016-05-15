/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.source;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.extension.tcp.internals.TcpInputStream;
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
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("listener")
public class TcpListener extends Source<Object, TcpAttributes> implements FlowConstructAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpListener.class);
    private ExecutorService executorService;
    private FlowConstruct flowConstruct;


    @UseConfig
    private TcpListenerConfig config;

    @Inject
    private MuleContext muleContext;

    @Connection
    private TcpListenerClient client;

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

    private MuleMessage<Object, TcpAttributes> createMessage(Socket socket, TcpAttributes
            attributes) throws IOException, ConnectionException
    {
        Object payload = NullPayload.getInstance();
        DataType dataType = DataTypeFactory.create(NullPayload.class);
        MuleMessage<Object, TcpAttributes> message;


        if (socket.isConnected() && socket.isBound())
        {
            payload = receiveFromSocket(socket, config.getTcpServerSocketProperties().getTimeout());
            if (payload == null)
            {
                return (MuleMessage) new DefaultMuleMessage(NullPayload.getInstance(), dataType, attributes, muleContext);
            }

            dataType = getTcpMessageDataType(DataTypeFactory.create(Object.class), attributes);
        }


        message = (MuleMessage) new DefaultMuleMessage(payload, dataType, attributes, muleContext);
        return message;
    }

    private Object receiveFromSocket(Socket socket, int timeout) throws ConnectionException, IOException
    {

        DataInputStream underlyingIs = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        TcpInputStream tis = new TcpInputStream(underlyingIs);

        try
        {
            return config.getProtocol().read(tis);
        }
        catch (IOException e)
        {
            if (config.getProtocol().getRethrowExceptionOnRead())
            {
                throw e;
            }

            return null;
        }
        finally
        {
            if (!tis.isStreaming())
            {
                tis.close();
            }
        }
    }

    private DataType<Object> getTcpMessageDataType(DataType<?> originalDataType, TcpAttributes attributes)
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
            sourceContext.getMessageHandler().handle(createMessage(socket, new TcpAttributes()));
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
