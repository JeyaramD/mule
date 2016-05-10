/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.builder;

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static javax.mail.Part.ATTACHMENT;
import static javax.mail.Part.INLINE;
import static org.mule.extension.email.internal.util.EmailUtils.toAddressArray;
import org.mule.extension.email.internal.EmailAttachment;
import org.mule.extension.email.internal.exception.EmailException;
import org.mule.extension.email.internal.util.EmailUtils;
import org.mule.runtime.core.util.IOUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MessageBuilder
{
    private final Message message;

    private String content = "";
    private List<EmailAttachment> attachments;

    private MessageBuilder(Session s) throws MessagingException
    {
        this.message = new MimeMessage(s);
    }

    private MessageBuilder(Message message) throws MessagingException
    {
        this.message = message;
    }

    public static MessageBuilder newMessage(Session session)
    {
        try
        {
            return new MessageBuilder(session);
        }
        catch (MessagingException e)
        {
            throw new EmailException("Error while creating Message", e);
        }
    }

    public static MessageBuilder newReplyMessage(Session session, boolean replyToAll)
    {
        try
        {
            Message reply = new MimeMessage(session).reply(replyToAll);
            return new MessageBuilder(reply);
        }
        catch (MessagingException e)
        {
            throw new EmailException("Error while creating Message", e);
        }
    }

    public MessageBuilder withSubject(String subject) throws MessagingException
    {
        this.message.setSubject(subject);
        return this;
    }

    public MessageBuilder fromAddresses(List<String> fromAddresses) throws MessagingException
    {
        this.message.addFrom(toAddressArray(fromAddresses));
        return this;
    }

    public MessageBuilder fromAddresses(String from) throws MessagingException
    {
        if (from != null)
        {
            this.message.setFrom(EmailUtils.toAddress(from));
        }
        else
        {
            this.message.setFrom();
        }
        return this;
    }

    public MessageBuilder to(List<String> toAddresses) throws MessagingException
    {
        this.message.setRecipients(TO, toAddressArray(toAddresses));
        return this;
    }

    public MessageBuilder bcc(List<String> bccAddresses) throws MessagingException
    {
        this.message.setRecipients(BCC, toAddressArray(bccAddresses));
        return this;
    }

    public MessageBuilder cc(List<String> ccAddresses) throws MessagingException
    {
        this.message.setRecipients(CC, toAddressArray(ccAddresses));
        return this;
    }

    public MessageBuilder withHeaders(Map<String, String> headers) throws MessagingException
    {
        for (String h : headers.keySet())
        {
            this.message.addHeader(h, headers.get(h));
        }
        return this;
    }

    public MessageBuilder withAttachments(List<EmailAttachment> attachments)
    {
        this.attachments = attachments;
        return this;
    }

    public MessageBuilder withSentDate(Date date) throws MessagingException
    {
        this.message.setSentDate(date);
        return this;
    }

    public MessageBuilder withContent(String content) throws MessagingException
    {
        this.content = content;
        return this;
    }

    public MessageBuilder withRecipients(List<String> recipients) throws MessagingException
    {
        this.message.setRecipients(TO, toAddressArray(recipients));
        return this;
    }

    public MessageBuilder replyTo(List<String> replyAddresses) throws MessagingException
    {
        this.message.setReplyTo(toAddressArray(replyAddresses));
        return this;
    }

    public Message build() throws MessagingException
    {
        if (attachments != null && !attachments.isEmpty())
        {
            // first part of the message is the content
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDisposition(INLINE);
            mimeBodyPart.setContent(content, "text/plain");
            multipart.addBodyPart(mimeBodyPart);
            this.message.setContent(multipart);

            for (EmailAttachment attachment : attachments)
            {
                try
                {
                    mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDisposition(ATTACHMENT);
                    DataHandler dataHandler = IOUtils.toDataHandler(attachment.getId(), attachment.getData(), attachment.getContentType());
                    mimeBodyPart.setDataHandler(dataHandler);
                    multipart.addBodyPart(mimeBodyPart);
                }
                catch (Exception e)
                {
                    throw new EmailException("Error while adding attachment: " + attachment.getId(), e);
                }
            }
        }
        else
        {
            this.message.setText(content);
        }

        return message;
    }

}
