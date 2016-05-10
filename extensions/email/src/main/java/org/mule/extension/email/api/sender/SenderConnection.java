/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.sender;

import org.mule.extension.email.api.EmailConnection;
import org.mule.extension.email.internal.EmailProperties;
import org.mule.runtime.api.connection.ConnectionValidationResult;

import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

public class SenderConnection implements EmailConnection
{
    private final Session session;

    public SenderConnection(String protocol,
                            String host,
                            String port,
                            Map<String, String> properties)
    {
        Properties senderProps = EmailProperties.get(protocol, host, port, properties);
        session = Session.getInstance(senderProps, null);
    }

    public ConnectionValidationResult isConnected()
    {
        return ConnectionValidationResult.success();
    }

    public void disconnect()
    {
        // No implementation
    }

    public Session getSession()
    {
        return session;
    }

}
