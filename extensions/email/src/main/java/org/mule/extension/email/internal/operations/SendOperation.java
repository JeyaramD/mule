/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import org.mule.extension.email.internal.EmailAttachment;
import org.mule.extension.email.internal.builder.MessageBuilder;
import org.mule.extension.email.internal.exception.SenderException;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class SendOperation
{

    public void send(Session session,
                     String content,
                     String subject,
                     List<String> toAddresses,
                     String fromAddress,
                     List<String> ccAddresses,
                     List<String> bccAddresses,
                     Map<String, String> headers,
                     List<EmailAttachment> attachments)
    {
        try
        {
            Message message = MessageBuilder.newMessage(session)
                    .withSentDate(Calendar.getInstance().getTime())
                    .fromAddresses(fromAddress)
                    .to(toAddresses)
                    .cc(ccAddresses)
                    .bcc(bccAddresses)
                    .withSubject(subject)
                    .withAttachments(attachments)
                    .withContent(content)
                    .withHeaders(headers)
                    .build();

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            throw new SenderException(e.getMessage(), e);
        }
    }
}
