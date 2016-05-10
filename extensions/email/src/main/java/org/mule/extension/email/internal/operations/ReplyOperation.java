/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.builder.MessageBuilder;
import org.mule.extension.email.internal.exception.SenderException;
import org.mule.runtime.api.message.MuleMessage;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class ReplyOperation
{
    public void reply(Session session,
                      MuleMessage muleMessage,
                      String content,
                      String from,
                      List<String> toAddresses,
                      Boolean replyToAll)
    {


        EmailAttributes attributes = muleMessage.getAttributes() instanceof EmailAttributes ? (EmailAttributes) muleMessage.getAttributes() : null;
        if (toAddresses == null)
        {
            if (attributes != null)
            {
                toAddresses = attributes.getToAddresses();
            }
            else
            {
                throw new SenderException("No reply address found. Specify an address to reply.");
            }
        }

        try
        {
            Message reply = MessageBuilder.newReplyMessage(session, replyToAll)
                    .fromAddresses(from)
                    .withContent(content)
                    .replyTo(toAddresses)
                    .withAttachments(attributes != null ? attributes.getAttachments() : new ArrayList<>())
                    .build();

            Transport.send(reply);
        }
        catch (MessagingException e)
        {
            throw new SenderException(e.getMessage(), e);
        }
    }
}
