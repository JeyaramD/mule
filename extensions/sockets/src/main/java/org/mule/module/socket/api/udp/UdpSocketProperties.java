/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.api.udp;

import org.mule.module.socket.api.SocketProperties;

/**
 * Interface for objects that provide common TCP configuration that applies for both
 * client and server sockets.
 * <p>
 * {@code null} values can be returned by any of the methods, meaning that there is no value defined for the property.
 *
 * @since 4.0
 */
public interface UdpSocketProperties extends SocketProperties
{

    /**
     * Whether to enable the socket to send broadcast data
     */
    Boolean getBroadcast();

    /**
     * Whether to keep the Sending socket open
     */
    Boolean getKeepSendSocketOpen();

}
