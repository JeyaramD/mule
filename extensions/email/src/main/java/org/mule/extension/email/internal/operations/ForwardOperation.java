/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.builder.MessageBuilder;
import org.mule.runtime.api.message.MuleMessage;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class ForwardOperation
{

    public void forward(Session session,
                        MuleMessage muleMessage,
                        String content,
                        String subject,
                        String from,
                        List<String> toAddresses)
    {


        EmailAttributes attributes = muleMessage.getAttributes() instanceof EmailAttributes ? ((EmailAttributes) muleMessage.getAttributes())
                                                                                            : null ;
        if (subject == null)
        {
            subject = attributes != null ? attributes.getSubject() : "No Subject";
        }

        try
        {
            Message forward = MessageBuilder.newMessage(session)
                    .withSubject("Fwd: " + subject)
                    .fromAddresses(from)
                    .withRecipients(toAddresses)
                    .withAttachments(attributes != null ? attributes.getAttachments() : new ArrayList<>())
                    .withContent(content)
                    .build();

            Transport.send(forward);
        }
        catch (MessagingException me)
        {
            throw new RuntimeException("asdasd", me);
        }
    }
}
