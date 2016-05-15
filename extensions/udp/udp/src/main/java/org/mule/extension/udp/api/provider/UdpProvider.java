/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.udp.api.provider;

import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.extension.tcp.internals.ConnectionSettings;
import org.mule.extension.udp.api.client.UdpClient;
import org.mule.extension.udp.api.config.UdpConfig;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionExceptionCode;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ParameterGroup;

@Alias("listener")
public class UdpProvider implements ConnectionProvider<UdpConfig, UdpClient>
{

    @ParameterGroup
    ConnectionSettings settings;

    @Override
    public UdpClient connect(UdpConfig tcpListenerConfig) throws ConnectionException
    {
        return null;//new UdpClient(tcpListenerConfig, settings.getHost(), settings.getPort());
    }

    @Override
    public void disconnect(UdpClient tcpConnection)
    {
        // no op
    }

    @Override
    public ConnectionValidationResult validate(UdpClient tcpListenerClient)
    {
       return null;
    }

    @Override
    public ConnectionHandlingStrategy<UdpClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<UdpConfig, UdpClient> handlingStrategyFactory)
    {
        return handlingStrategyFactory.none();
    }
}
