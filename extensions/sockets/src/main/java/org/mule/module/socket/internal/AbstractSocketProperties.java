/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.socket.internal;

import org.mule.module.socket.api.SocketProperties;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.ConfigName;
import org.mule.runtime.extension.api.annotation.param.Optional;

public abstract class AbstractSocketProperties implements SocketProperties
{
    /**
     * The name of this config object, so that it can be referenced by config elements.
     */
    @ConfigName
    private String name;

    /**
     * The size of the buffer (in bytes) used when sending data, set on the socket itself.
     */
    @Parameter
    @Optional
    private Integer sendBufferSize;

    /**
     * The size of the buffer (in bytes) used when receiving data, set on the socket itself.
     */
    @Parameter
    @Optional
    private Integer receiveBufferSize;

    /**
     * This sets the SO_TIMEOUT value on client sockets. Reading from the socket will block for up to this long
     * (in milliseconds) before the read fails.
     * <p>
     * A value of 0 (the default) causes the read to wait indefinitely (if no data arrives).
     */
    @Parameter
    @Optional(defaultValue = "0")
    private Integer timeout = 0;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getSendBufferSize()
    {
        return sendBufferSize;
    }

    public void setSendBufferSize(Integer sendBufferSize)
    {
        this.sendBufferSize = sendBufferSize;
    }

    public Integer getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(Integer receiveBufferSize)
    {
        this.receiveBufferSize = receiveBufferSize;
    }

    public Integer getTimeout()
    {
        return timeout;
    }

    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }
}
