/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.tcp.api.protocol;

import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Abstract class has been introduced so as to have the byte protocols (i.e. the
 * protocols that had only a single write method taking just an array of bytes as a
 * parameter) to inherit from since they will all behave the same, i.e. if the object
 * is serializable, serialize it into an array of bytes and send it.
 */
public abstract class AbstractByteProtocol implements TcpProtocol
{

    private static final Logger logger = LoggerFactory.getLogger(DirectProtocol.class);
    private static final long PAUSE_PERIOD = 100;
    public static final int EOF = -1;

    public static final boolean STREAM_OK = true;
    public static final boolean NO_STREAM = false;
    private boolean streamOk;

    protected static int bufferSize = 8 * 4 * 1024;

    /**
     * Rethrow the exception if read fails
     */
    @Parameter
    @Optional(defaultValue = "false")
    protected boolean rethrowExceptionOnRead = false;

    public boolean getRethrowExceptionOnRead()
    {
        return rethrowExceptionOnRead;
    }

    public AbstractByteProtocol(boolean streamOk)
    {
        this.streamOk = streamOk;
    }

    public void write(OutputStream os, Object data, boolean payloadOnly) throws IOException
    {
        if (data instanceof InputStream)
        {
            if (streamOk)
            {
                InputStream is = (InputStream) data;
                //copyLarge(is, os);
                //os.flush();
                //os.close();
                //is.close();
                writeByteArray(os, IOUtils.toByteArray(is));
            }
            else
            {
                throw new IOException(String.format("TCP protocol '%s' cannot handle streaming", getClass().getSimpleName()));
            }
        }
        else if (data instanceof MuleMessage)
        {
            if (payloadOnly)
            {
                write(os, ((MuleMessage) data).getPayload(), payloadOnly);
            }
            else
            {
                // TODO write mule message write(os, data.by);
            }
        }
        else if (data instanceof byte[])
        {
            writeByteArray(os, (byte[]) data);
        }
        else if (data instanceof String)
        {
            writeByteArray(os, ((String) data).getBytes());
        }
        else
        {
            throw new IllegalArgumentException(String.format("Cannot serialize data: '%s'", data));
        }
    }

    protected void writeByteArray(OutputStream os, byte[] data) throws IOException
    {
        os.write(data);
    }


    /**
     * Manage non-blocking reads and handle errors
     *
     * @param is     The input stream to read from
     * @param buffer The buffer to read into
     * @param size   The amount of data (upper bound) to read
     * @return The amount of data read (always non-zero, -1 on EOF or socket exception)
     * @throws IOException other than socket exceptions
     */
    protected int safeRead(InputStream is, byte[] buffer, int size, boolean rethrowExceptionOnRead) throws IOException
    {
        int len;
        do
        {
            len = is.read(buffer, 0, size);
            if (0 == len)
            {
                // wait for non-blocking input stream
                // use new lock since not expecting notification
                try
                {
                    Thread.sleep(PAUSE_PERIOD);
                }
                catch (InterruptedException e)
                {
                    // no-op
                }
            }
        }
        while (0 == len);
        return len;
    }

    /**
     * Make a single transfer from source to dest via a byte array buffer
     *
     * @param source Source of data
     * @param buffer Buffer array for transfer
     * @param dest   Destination of data
     * @return Amount of data transferred, or -1 on eof or socket error
     * @throws IOException On non-socket error
     */
    protected int copy(InputStream source, byte[] buffer, OutputStream dest, boolean rethrowExceptionOnRead) throws IOException
    {
        return copy(source, buffer, dest, buffer.length, rethrowExceptionOnRead);
    }

    /**
     * Make a single transfer from source to dest via a byte array buffer
     *
     * @param source Source of data
     * @param buffer Buffer array for transfer
     * @param dest   Destination of data
     * @param size   The amount of data (upper bound) to read
     * @return Amount of data transferred, or -1 on eof or socket error
     * @throws IOException On non-socket error
     */
    protected int copy(InputStream source, byte[] buffer, OutputStream dest, int size, boolean rethrowExceptionOnRead) throws IOException
    {
        int len = safeRead(source, buffer, size, rethrowExceptionOnRead);
        if (len > 0)
        {
            dest.write(buffer, 0, len);
        }
        return len;
    }

    protected byte[] nullEmptyArray(byte[] data)
    {
        if (0 == data.length)
        {
            return null;
        }
        else
        {
            return data;
        }
    }


    private long copyLarge(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer)))
        {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
