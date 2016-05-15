/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.protocol;

import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The DirectProtocol class is an application level tcp protocol that does nothing.
 * The socket is read until no more bytes are (momentarily) available
 * (previously the transfer buffer also had to be full on the previous read, which made
 * stronger requirements on the underlying network).  On slow networks
 * {@link org.mule.transport.tcp.protocols.EOFProtocol} and
 * {@link org.mule.transport.tcp.protocols.LengthProtocol} may be more reliable.
 * <p>
 * <p>Writing simply writes the data to the socket.</p>
 */
public class DirectProtocol extends AbstractByteProtocol
{

    protected static final int UNLIMITED = -1;

    private static final Log logger = LogFactory.getLog(DirectProtocol.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Parameter
    @Optional(defaultValue = "true")
    private boolean payloadOnly = true;

    protected int bufferSize;

    public DirectProtocol()
    {
        this(STREAM_OK, DEFAULT_BUFFER_SIZE);
    }

    public DirectProtocol(boolean streamOk, int bufferSize)
    {
        super(streamOk);
        this.bufferSize = bufferSize;
    }

    @Override
    public Object read(InputStream is) throws IOException
    {
        return read(is, UNLIMITED);
    }

    public Object read(InputStream is, int limit) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);

        try
        {
            byte[] buffer = new byte[bufferSize];
            int len;
            int remain = remaining(limit, limit, 0);
            boolean repeat;
            do
            {

                len = copy(is, buffer, baos, remain, rethrowExceptionOnRead);
                remain = remaining(limit, remain, len);
                repeat = EOF != len && remain > 0 && isRepeat(len, is.available());
            }
            while (repeat);
        }
        finally
        {
            baos.flush();
            baos.close();
        }
        return nullEmptyArray(baos.toByteArray());
    }

    protected int remaining(int limit, int remain, int len)
    {
        if (UNLIMITED == limit)
        {
            return bufferSize;
        }
        else if (EOF != len)
        {
            return remain - len;
        }
        else
        {
            return remain;
        }
    }

    /**
     * Decide whether to repeat transfer.  This implementation does so if
     * more data are available.  Note that previously, while documented as such,
     * there was also the additional requirement that the previous transfer
     * completely used the transfer buffer.
     *
     * @param len       Amount transferred last call (-1 on EOF or socket error)
     * @param available Amount available
     * @return true if the transfer should continue
     */
    protected boolean isRepeat(int len, int available)
    {
        return available > 0;
    }

    @Override
    public void write(OutputStream os, Object data) throws IOException
    {
        this.write(os, data, payloadOnly);
    }

}
