/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.client;

import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.module.socket.api.TcpServerSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;

import com.oracle.tools.packager.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class TcpListenerClient implements TcpClient
{

    private ServerSocket socket;
    private TcpListenerConfig config;
    private String host;
    private int port;

    public TcpListenerClient(TcpListenerConfig config, String host, Integer port) throws ConnectionException
    {
        try
        {
            this.socket = new ServerSocket();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not create socket");
        }

        this.config = config;
        this.host = host;
        this.port = port;
        configureSocket();
    }

    public void configureSocket() throws ConnectionException
    {
        TcpServerSocketProperties properties = config.getTcpServerSocketProperties();
        try
        {
            if (properties.getTimeout() != null)
            {
                socket.setSoTimeout(properties.getTimeout());
            }

            if (properties.getReceiveBufferSize() != null)
            {

                socket.setReceiveBufferSize(properties.getReceiveBufferSize());
            }

            if (properties.getReuseAddress() != null)
            {

                socket.setReuseAddress(properties.getReuseAddress());
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not configure socket");
        }

        try
        {
            socket.bind(new InetSocketAddress(host, port), properties.getReceiveBacklog());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException(String.format("Could not bind socket to host '%s' and port '%d'", host, port));
        }
    }

    public Socket connect() throws ConnectionException
    {
        try
        {
            // blocking
            Socket newConnection = socket.accept();
            configureIncomingConnection(newConnection);
            return newConnection;
        }
        catch (SocketTimeoutException e)
        {
            // do nothing because timeout is configurable
            Log.debug("Socked timed out");
            return null;
        }
        catch (IOException e)
        {
            // this is the expected behaviour while closing the socket
            return null;
        }
        catch (ConnectionException e)
        {
            throw e;
        }
    }

    private void configureIncomingConnection(Socket newConnection) throws ConnectionException
    {
        TcpServerSocketProperties properties = config.getTcpServerSocketProperties();
        try
        {
            if (properties.getTimeout() != null)
            {
                newConnection.setSoTimeout(properties.getTimeout());
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            throw new ConnectionException("Could not configure incoming connection");
        }
    }

    @Override
    public void disconnect() throws ConnectionException
    {
        try
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("An error ocurred when closing the listener socket");
        }
    }

    @Override
    public boolean isValid()
    {
        return !socket.isClosed() && socket.isBound();
    }
}
