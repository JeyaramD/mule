/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp.api.client;

import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.module.socket.api.TcpServerSocketProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TcpListenerClient implements TcpClient
{

    private ServerSocket socket;
    private TcpListenerConfig config;
    private String host;
    private int port;

    public TcpListenerClient(TcpListenerConfig config, String host, Integer port) throws IOException
    {
        this.socket = new ServerSocket();
        this.config = config;
        configureSocket();
    }

    public void configureSocket()
    {
        TcpServerSocketProperties properties = config.getTcpServerSocketProperties();
        try
        {
            socket.setSoTimeout(properties.getTimeout());
            socket.setReceiveBufferSize(properties.getReceiveBufferSize());
            socket.setReuseAddress(properties.getReuseAddress());
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        try
        {
            socket.bind(new InetSocketAddress(host, port), properties.getReceiveBacklog());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Socket connect()
    {
        try
        {
            // blocking
            Socket newConnection = socket.accept();
            return newConnection;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
