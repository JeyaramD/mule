/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api;

import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.extension.tcp.api.config.TcpRequesterConfig;
import org.mule.extension.tcp.api.protocol.DirectProtocol;
import org.mule.extension.tcp.api.protocol.LengthProtocol;
import org.mule.extension.tcp.api.protocol.SafeProtocol;
import org.mule.extension.tcp.api.protocol.TcpProtocol;
import org.mule.module.socket.api.TcpClientSocketProperties;
import org.mule.module.socket.api.TcpServerSocketProperties;
import org.mule.module.socket.internal.DefaultTcpClientSocketProperties;
import org.mule.module.socket.internal.DefaultTcpServerSocketProperties;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.capability.Xml;

@Extension(name = "TCP Connector", description = "Connector to establish TCP connections")
@Configurations({TcpListenerConfig.class, TcpRequesterConfig.class})

//@Import(type = TcpClientSocketProperties.class, from = SocketsExtension.class)
@SubTypeMapping(baseType = TcpClientSocketProperties.class, subTypes = {DefaultTcpClientSocketProperties.class})
@SubTypeMapping(baseType = TcpServerSocketProperties.class, subTypes = {DefaultTcpServerSocketProperties.class})


//@Import(type = TcpServerSocketProperties.class, from = SocketsExtension.class)
@SubTypeMapping(baseType = TcpProtocol.class, subTypes = {SafeProtocol.class, DirectProtocol.class, LengthProtocol.class})
@Xml(namespace = "tcp")
public class TcpConnector{

}
