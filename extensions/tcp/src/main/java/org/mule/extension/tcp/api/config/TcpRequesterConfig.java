/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.config;

import org.mule.extension.tcp.api.TcpRequesterOperations;
import org.mule.extension.tcp.api.protocol.SafeProtocol;
import org.mule.extension.tcp.api.protocol.TcpProtocol;
import org.mule.extension.tcp.api.provider.TcpRequesterProvider;
import org.mule.module.socket.api.TcpClientSocketProperties;
import org.mule.module.socket.internal.DefaultTcpClientSocketProperties;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.connector.Providers;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Configuration(name = "requester-config")
@Operations({TcpRequesterOperations.class})
@Providers({TcpRequesterProvider.class})
public class TcpRequesterConfig
{

    @Parameter
    @Optional
    TcpClientSocketProperties tcpClientSocketProperties = new DefaultTcpClientSocketProperties();

    @Parameter
    @Optional
    TcpProtocol protocol = new SafeProtocol();

    public TcpClientSocketProperties getTcpClientSocketProperties()
    {
        return tcpClientSocketProperties;
    }


    public TcpProtocol getProtocol()
    {
        return protocol;
    }

    public void setProtocol(TcpProtocol protocol)
    {
        this.protocol = protocol;
    }
}
