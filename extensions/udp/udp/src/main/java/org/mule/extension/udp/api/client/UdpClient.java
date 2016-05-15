/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.udp.api.client;

import org.mule.extension.udp.api.config.UdpConfig;
import org.mule.module.socket.api.udp.UdpSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UdpClient
{

    private DatagramSocket socket;
    private UdpConfig config;
    private String host;
    private int port;

    public UdpRequesterClient(UdpConfig config, String host, Integer port) throws ConnectionException
    {
        this.socket = new DatagramSocket();
        this.config = config;
        this.host = host;
        this.port = port;
        configureSocket();
    }

    private void configureSocket() throws ConnectionException
    {
        UdpSocketProperties properties = config.getUdpSocketProperties();

        try
        {
            if (properties.getSendBufferSize() != null)
            {
                socket.setSendBufferSize(properties.getSendBufferSize());
            }

            if (properties.getReceiveBufferSize() != null)
            {
                socket.setReceiveBufferSize(properties.getReceiveBufferSize());
            }

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

        try
        {
            InetSocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException(String.format("Could not connect to host '%s' on port '%d'", host, port));
        }
    }

    public void write(Object data) throws ConnectionException
    {
        try
        {
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, host, port);
            socket.send(sendPacket);
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
        return socket.isConnected() && socket.isBound();
    }
}
