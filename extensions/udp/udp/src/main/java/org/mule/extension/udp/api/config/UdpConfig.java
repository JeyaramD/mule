/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.udp.api.config;

import org.mule.extension.udp.api.provider.UdpProvider;
import org.mule.extension.udp.api.source.UdpListener;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.DefaultUdpSocketProperties;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connector.Providers;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Configuration(name = "listener-config")
@Providers({UdpProvider.class})
@Sources({UdpListener.class})
public class UdpConfig
{

    @Parameter
    @Optional
    UdpSocketProperties udpSocketProperties = new DefaultUdpSocketProperties();

    public UdpSocketProperties getUdpSocketProperties()
    {
        return udpSocketProperties;
    }
}
