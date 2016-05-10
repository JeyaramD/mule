package org.mule.extension.email;/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.mule.extension.email.api.EmailConnector;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;

public abstract class EmailConnectorFunctionalTestCase extends ExtensionFunctionalTestCase
{
    protected static final int DEFAULT_TEST_PORT = 10222;

    protected static final String SUBJECT = "Email Subject";
    protected static final String CONTENT = "Email Content";
    protected static final String JSON_ATTACHMENT_CONTENT =  "{\r\n  \"key\": \"value\"\r\n}";

    protected static final String PABLON_EMAIL = "pablo.musumeci@mulesoft.com";
    protected static final String ESTEBAN_EMAIL = "esteban.wasinger@mulesoft.com";
    protected static final String JUANI_EMAIL = "juan.desimoni@mulesoft.com";
    protected static final String ALE_EMAIL = "ale.g.marra@mulesoft.com";
    protected static final String MG_EMAIL = "mariano.gonzalez@mulesoft.com";
    protected static final String[] EMAILS = {JUANI_EMAIL, ESTEBAN_EMAIL, PABLON_EMAIL, ALE_EMAIL};

    protected GreenMail server;

    @Override
    protected Class<?>[] getAnnotatedExtensionClasses()
    {
        return new Class<?>[] {EmailConnector.class};
    }

    @Before
    public void start() throws Exception
    {
        ServerSetup serverSetup = getServerSetup();
        server = new GreenMail(serverSetup);
        server.start();

        server.setUser(JUANI_EMAIL, JUANI_EMAIL, "password");
    }

    @After
    public void stop()
    {
        server.stop();
    }

    protected void assertMessageSubjectAndContentText(Message message) throws MessagingException
    {
        assertThat(message.getSubject(), is(SUBJECT));
        assertThat(getBody(message), is(CONTENT));
    }

    public abstract ServerSetup getServerSetup();
}
