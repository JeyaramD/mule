/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.tcp.TcpSocketProperties;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

/**
 * Mutable base class for implementations of {@link TcpSocketProperties}
 *
 * @since 4.0
 */
public abstract class AbstractUdpSocketProperties extends AbstractSocketProperties implements UdpSocketProperties
{

    /**
     */
    @Parameter
    @Optional(defaultValue = "false")
    private Boolean broadcast = false;

    /**
     */
    @Parameter
    @Optional(defaultValue = "false")
    private Boolean keepSendSocketOpen = false;

    public Boolean getBroadcast()
    {
        return broadcast;
    }

    public void setBroadcast(Boolean broadcast)
    {
        this.broadcast = broadcast;
    }

    public Boolean getKeepSendSocketOpen()
    {
        return keepSendSocketOpen;
    }

    public void setKeepSendSocketOpen(Boolean keepSendSocketOpen)
    {
        this.keepSendSocketOpen = keepSendSocketOpen;
    }

}
