/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.tcp.TcpSocketProperties;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

/**
 * Mutable base class for implementations of {@link TcpSocketProperties}
 *
 * @since 4.0
 */
public abstract class AbstractTcpSocketProperties extends AbstractSocketProperties implements TcpSocketProperties
{

    /**
     * If set, transmitted data is not collected together for greater efficiency but sent immediately.
     * <p>
     * Defaults to {@code true} even though Socket default is false because optimizing to reduce amount of network
     * traffic over latency is hardly ever a concern today.
     */
    @Parameter
    @Optional(defaultValue = "true")
    private Boolean sendTcpNoDelay = true;


    /**
     * This sets the SO_LINGER value. This is related to how long (in milliseconds) the socket will take to close so
     * that any remaining data is transmitted correctly.
     */
    @Parameter
    @Optional
    private Integer linger;

    /**
     * Enables SO_KEEPALIVE behavior on open sockets. This automatically checks socket connections that are open but
     * unused for long periods and closes them if the connection becomes unavailable.
     * <p>
     * This is a property on the socket itself and is used by a server socket to control whether connections to the
     * server are kept alive before they are recycled.
     */
    @Parameter
    @Optional
    private Boolean keepAlive;

    public Boolean getSendTcpNoDelay()
    {
        return sendTcpNoDelay;
    }

    public void setSendTcpNoDelay(Boolean sendTcpNoDelay)
    {
        this.sendTcpNoDelay = sendTcpNoDelay;
    }

    public Integer getLinger()
    {
        return linger;
    }

    public void setLinger(Integer linger)
    {
        this.linger = linger;
    }

    public Boolean getKeepAlive()
    {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }
}
