/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.tcp.api.client;

import org.mule.runtime.api.connection.ConnectionException;

public interface TcpClient
{

    void disconnect() throws ConnectionException;
    //void connect() throws ConnectionException;
    boolean isValid();
}
