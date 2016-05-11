/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.provider;

import org.mule.extension.tcp.api.client.TcpRequesterClient;
import org.mule.extension.tcp.api.config.TcpRequesterConfig;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionExceptionCode;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;

@Alias("requester")
public class TcpRequesterProvider implements ConnectionProvider<TcpRequesterConfig, TcpRequesterClient>
{

    @Override
    public TcpRequesterClient connect(TcpRequesterConfig tcpRequesterConfig) throws ConnectionException
    {
        TcpRequesterClient client = new TcpRequesterClient(tcpRequesterConfig);
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
            return ConnectionValidationResult.failure("Socket is not connected", ConnectionExceptionCode.UNKNOWN);
        }
    }

    @Override
    public ConnectionHandlingStrategy<TcpRequesterClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<TcpRequesterConfig, TcpRequesterClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.supportsPooling();
    }
}
