/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.config;

import org.mule.extension.tcp.api.protocol.SafeProtocol;
import org.mule.extension.tcp.api.protocol.TcpProtocol;
import org.mule.extension.tcp.api.provider.TcpListenerProvider;
import org.mule.extension.tcp.api.source.TcpListener;
import org.mule.module.socket.api.TcpServerSocketProperties;
import org.mule.module.socket.internal.DefaultTcpServerSocketProperties;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connector.Providers;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Configuration(name = "listener-config")
@Providers({TcpListenerProvider.class})
@Sources({TcpListener.class})
public class TcpListenerConfig
{

    @Parameter
    @Optional
    TcpServerSocketProperties tcpServerSocketProperties = new DefaultTcpServerSocketProperties();


    @Parameter
    @Optional
    TcpProtocol protocol = new SafeProtocol();

    public TcpServerSocketProperties getTcpServerSocketProperties()
    {
        return tcpServerSocketProperties;
    }

    public TcpProtocol getProtocol()
    {
        return protocol;
    }
}
