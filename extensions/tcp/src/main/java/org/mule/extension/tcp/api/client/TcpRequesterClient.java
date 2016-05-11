/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.client;

import org.mule.extension.tcp.api.config.TcpRequesterConfig;
import org.mule.module.socket.api.TcpClientSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpRequesterClient
{

    private Socket socket;
    private TcpRequesterConfig config;

    public TcpRequesterClient(TcpRequesterConfig config) throws ConnectionException
    {
        this.socket = new Socket();
        this.config = config;
        configureSocket();
    }

    private void configureSocket() throws ConnectionException
    {
        TcpClientSocketProperties properties = config.getTcpClientSocketProperties();

        try
        {
            if (properties.getKeepAlive() != null)
            {
                socket.setKeepAlive(properties.getKeepAlive());
            }

            if (properties.getSendBufferSize() != null)
            {
                socket.setSendBufferSize(properties.getSendBufferSize());
            }

            if (properties.getReceiveBufferSize() != null)
            {
                socket.setReceiveBufferSize(properties.getReceiveBufferSize());
            }

            if (properties.getLinger() != null)
            {
                socket.setSoLinger(true, properties.getLinger());
            }


            socket.setTcpNoDelay(properties.getSendTcpNoDelay());
            socket.setSoTimeout(properties.getTimeout());
            socket.bind(null);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not create socket due to a SocketException");
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not create socket due to a IOException");
        }

    }

    public void connect() throws ConnectionException
    {
        // todo validate
        int port = config.getPort();
        String host = config.getHost();

        try
        {
            socket.connect(new InetSocketAddress(host, port));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException(String.format("Could not connect to host '%s' on port '%d'", host, port));
        }
    }

    public void write(Object data) throws ConnectionException
    {
        BufferedOutputStream outputStream = null;
        try
        {
            //outputStream = new BufferedOutputStream(socket.getOutputStream());
            //config.getProtocol().write(outputStream, data);
            config.getProtocol().write(socket.getOutputStream(), data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("An error ocured while trying to write into the socket");
        }
    }

    public void disconnect() throws ConnectionException
    {
        if (!socket.isConnected()){
            throw new ConnectionException("Trying to disconnect socket but it was not connected");
        }

        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("An error ocurred when trying to close the socket");
        }
    }

    public boolean isValid()
    {
        return socket.isConnected();
    }
}
