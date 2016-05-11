/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.source;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

@Alias("listener")
public class TcpListener extends Source<InputStream, ListenerTcpAttributes> implements FlowConstructAware
{

    private ExecutorService executorService;
    private FlowConstruct flowConstruct;


    @UseConfig
    private TcpListenerConfig config;

    @Inject
    private MuleContext muleContext;

    @Connection
    private TcpListenerClient client;

    @Override
    public void start() throws Exception
    {
        executorService = newSingleThreadExecutor(r -> new Thread(r, format("%s%s.tcp.listener", getPrefix(muleContext), flowConstruct.getName())));
        executorService.execute(this::listen);
    }

    private void listen()
    {
        Socket socket = client.connect();
        processNewConnection(socket);
    }

    private MuleMessage<InputStream, ListenerTcpAttributes> createMessage(Socket socket, ListenerTcpAttributes attributes)
    {
        Object payload = NullPayload.getInstance();
        DataType dataType = DataTypeFactory.create(NullPayload.class);
        MuleMessage<InputStream, ListenerTcpAttributes> message;

        try
        {
            if (socket.isConnected())
            {
                payload = config.getProtocol().read(socket.getInputStream());
                dataType = DataTypeFactory.create(InputStream.class);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        message = (MuleMessage) new DefaultMuleMessage(payload, dataType, attributes, muleContext);
        return message;
    }

    private void processNewConnection(Socket socket)
    {
        sourceContext.getMessageHandler().handle(createMessage(socket, new ListenerTcpAttributes()));
    }

    @Override
    public void stop() throws Exception
    {

    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }
}
