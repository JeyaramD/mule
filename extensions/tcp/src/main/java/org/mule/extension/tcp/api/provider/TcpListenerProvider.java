/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.provider;

import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.module.socket.api.TcpClientSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.IOException;

@Alias("listener")
public class TcpListenerProvider implements ConnectionProvider<TcpListenerConfig, TcpListenerClient>
{
    @Parameter
    private String host;

    @Parameter
    private Integer port;

    @Parameter
    @Optional
    private TcpClientSocketProperties clientSocketProperties;

    @Override
    public TcpListenerClient connect(TcpListenerConfig tcpListenerConfig) throws ConnectionException
    {
        TcpListenerClient tcpListenerClient = null;
        try
        {
            tcpListenerClient = new TcpListenerClient(tcpListenerConfig, host, port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return tcpListenerClient;
    }

    @Override
    public void disconnect(TcpListenerClient tcpConnection)
    {

    }

    @Override
    public ConnectionValidationResult validate(TcpListenerClient tcpListenerClient)
    {
        //TODO
        return ConnectionValidationResult.success();
    }

    @Override
    public ConnectionHandlingStrategy<TcpListenerClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<TcpListenerConfig, TcpListenerClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.none();
    }
}
