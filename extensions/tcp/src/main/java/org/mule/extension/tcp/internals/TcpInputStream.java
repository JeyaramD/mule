/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.internals;

import java.io.InputStream;

public class TcpInputStream extends DelegatingInputStream
{
    private boolean streaming;

    public TcpInputStream(InputStream delegate)
    {
        super(delegate);
    }

    public boolean isStreaming()
    {
        return streaming;
    }

    public void setStreaming(boolean streaming)
    {
        this.streaming = streaming;
    }

}
