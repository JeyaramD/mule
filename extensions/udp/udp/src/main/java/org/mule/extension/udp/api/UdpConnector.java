/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.udp.api;

import org.mule.extension.udp.api.config.UdpConfig;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.module.socket.internal.DefaultUdpSocketProperties;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.capability.Xml;

@Extension(name = "UDP Connector", description = "Connector to establish UDP connections")
@Configurations({UdpConfig.class})
@SubTypeMapping(baseType = UdpSocketProperties.class, subTypes = {DefaultUdpSocketProperties.class})


//@Import(type = TcpServerSocketProperties.class, from = SocketsExtension.class)
@Xml(namespace = "udp")
public class UdpConnector{

}
