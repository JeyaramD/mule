/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.provider;

import org.mule.extension.tcp.api.client.TcpRequesterClient;
import org.mule.extension.tcp.api.config.TcpRequesterConfig;
import org.mule.extension.tcp.internals.ConnectionSettings;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionExceptionCode;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ParameterGroup;

@Alias("requester")
public class TcpRequesterProvider implements ConnectionProvider<TcpRequesterConfig, TcpRequesterClient>
{

    @ParameterGroup
    ConnectionSettings settings;

    @Override
    public TcpRequesterClient connect(TcpRequesterConfig tcpRequesterConfig) throws ConnectionException
    {
        TcpRequesterClient client = new TcpRequesterClient(tcpRequesterConfig, settings.getHost(), settings.getPort());
        client.connect();
        return client;
    }

    @Override
    public void disconnect(TcpRequesterClient tcpRequesterClient)
    {
        try
        {
            tcpRequesterClient.disconnect();
        }
        catch (ConnectionException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ConnectionValidationResult validate(TcpRequesterClient tcpRequesterClient)
    {
        if (tcpRequesterClient.isValid())
        {
            return ConnectionValidationResult.success();
        }
        else
        {
            String msg = "Socket is not connected";
            return ConnectionValidationResult.failure(msg, ConnectionExceptionCode.UNKNOWN, new ConnectionException(msg));
        }
    }

    @Override
    public ConnectionHandlingStrategy<TcpRequesterClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<TcpRequesterConfig, TcpRequesterClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.supportsPooling();
    }
}
