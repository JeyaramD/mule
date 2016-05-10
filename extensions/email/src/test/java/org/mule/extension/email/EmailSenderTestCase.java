package org.mule.extension.email;/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import org.mule.runtime.core.util.IOUtils;

import com.icegreen.greenmail.util.ServerSetup;

import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Multipart;

import org.junit.Test;

public class EmailSenderTestCase extends EmailConnectorFunctionalTestCase
{

    private static final String SEND_EMAIL = "sendEmail";
    private static final String SEND_EMAIL_WITH_ATTACHMENT = "sendEmailWithAttachment";

    @Override
    protected String getConfigFile()
    {
        return "sender.xml";
    }

    @Override
    public ServerSetup getServerSetup()
    {
        return  new ServerSetup(DEFAULT_TEST_PORT, null, "smtp");
    }

    @Test
    public void sendEmail() throws Exception
    {
        runFlow(SEND_EMAIL);
        assertThat(server.waitForIncomingEmail(5000, 1), is(true));
        Message[] messages = server.getReceivedMessages();
        assertThat(messages.length, is(1));
        assertMessageSubjectAndContentText(messages[0]);
    }

    @Test
    public void sendEmailWithAttachment() throws Exception
    {
        runFlow(SEND_EMAIL_WITH_ATTACHMENT);
        assertThat(server.waitForIncomingEmail(5000, 4), is(true));
        Message[] messages = server.getReceivedMessages();
        assertThat(messages.length, is(4));

        for (Message message : messages)
        {
            Multipart content = (Multipart)message.getContent();
            assertThat(content.getCount(), is(2));

            DataHandler dataHandler = content.getBodyPart(0).getDataHandler();
            assertThat(dataHandler.getContent(), is(CONTENT));
            assertThat(dataHandler.getContentType(), is("text/plain; charset=us-ascii"));

            dataHandler = content.getBodyPart(1).getDataHandler();
            assertThat(dataHandler.getContent(), instanceOf(InputStream.class));
            assertThat(JSON_ATTACHMENT_CONTENT, is(IOUtils.toString((InputStream) dataHandler.getContent())));
        }
    }

}
