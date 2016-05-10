/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import org.mule.extension.email.api.EmailConnection;
import org.mule.extension.email.internal.EmailProperties;
import org.mule.extension.email.internal.exception.EmailConnectionException;

import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class RetrieverConnection implements EmailConnection
{
    private final Session session;
    private final Store store;
    private final String folder;

    public RetrieverConnection(String protocol,
                               String username,
                               String password,
                               String host,
                               String port,
                               Map<String, String> properties,
                               Authenticator authenticator,
                               String folder)
    {

        Properties sessionProperties = EmailProperties.get(protocol, host, port, properties);
        this.session = Session.getInstance(sessionProperties, authenticator);
        this.folder = folder;
        try
        {
            this.store = session.getStore(protocol);
            this.store.connect(username, password);
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException("Error while stablishing connection with the " + protocol + "store", e);
        }
    }

    @Override
    public void disconnect()
    {
        try
        {
            store.close();
        }
        catch (MessagingException e)
        {
            throw new EmailConnectionException("Error while disconnecting", e);
        }
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    public Store getStore()
    {
        return store;
    }

    public String getFolder()
    {
        return folder;
    }
}
