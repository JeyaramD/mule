/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.builder;

import static javax.mail.Flags.Flag.ANSWERED;
import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Flags.Flag.DRAFT;
import static javax.mail.Flags.Flag.RECENT;
import static javax.mail.Flags.Flag.SEEN;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import org.mule.extension.email.internal.EmailAttachment;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.EmailFlags;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

public class EmailAttributesBuilder
{

    private int id;
    private String subject;
    private List<String> fromAddresses = new ArrayList<>();
    private List<String> toAddresses = new ArrayList<>();
    private List<String> bccAddresses = new ArrayList<>();
    private List<String> ccAddresses = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private List<EmailAttachment> attachments;
    private LocalDateTime receivedDate;
    private List<String> replyToAddresses = new ArrayList<>();
    private boolean answered;
    private boolean deleted;
    private boolean draft;
    private boolean recent;
    private boolean seen;

    private EmailAttributesBuilder()
    {
    }

    public EmailAttributesBuilder withId(int id)
    {
        this.id = id;
        return this;
    }

    public EmailAttributesBuilder withSubject(String subject)
    {
        this.subject = subject;
        return this;
    }

    public EmailAttributesBuilder fromAddresses(Address[] fromAddresses)
    {
        Arrays.stream(fromAddresses).map(Object::toString).forEach(this.fromAddresses::add);
        return this;
    }

    public EmailAttributesBuilder toAddresses(Address[] toAddresses)
    {
        Arrays.stream(toAddresses).map(Object::toString).forEach(this::toAddress);
        return this;
    }

    public EmailAttributesBuilder toAddress(String toAddress)
    {
        this.toAddresses.add(toAddress);
        return this;
    }

    public EmailAttributesBuilder bccAddress(String bccAddress)
    {
        this.bccAddresses.add(bccAddress);
        return this;
    }

    public EmailAttributesBuilder bccAddresses(Address[] bccAddresses)
    {
        Arrays.stream(bccAddresses).map(Object::toString).forEach(this::bccAddress);
        return this;
    }

    public EmailAttributesBuilder ccAddress(String ccAddress)
    {
        this.ccAddresses.add(ccAddress);
        return this;
    }

    public EmailAttributesBuilder ccAddresses(Address[] ccAddresses)
    {
        Arrays.stream(ccAddresses).map(Object::toString).forEach(this::ccAddress);
        return this;
    }

    public EmailAttributesBuilder setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
        return this;
    }

    public EmailAttributesBuilder withAttachments(List<EmailAttachment> attachments)
    {
        this.attachments = attachments;
        return this;
    }

    public EmailAttributesBuilder receivedDate(LocalDateTime receivedDate)
    {
        this.receivedDate = receivedDate;
        return this;
    }

    public EmailAttributesBuilder replyToAddress(String replyToAddresses)
    {
        this.replyToAddresses.add(replyToAddresses);
        return this;
    }

    public EmailAttributesBuilder answered(boolean answered)
    {
        this.answered = answered;
        return this;
    }

    public EmailAttributesBuilder deleted(boolean deleted)
    {
        this.deleted = deleted;
        return this;
    }

    public EmailAttributesBuilder draft(boolean draft)
    {
        this.draft = draft;
        return this;
    }

    public EmailAttributesBuilder recent(boolean recent)
    {
        this.recent = recent;
        return this;
    }

    public EmailAttributesBuilder seen(boolean seen)
    {
        this.seen = seen;
        return this;
    }

    public EmailAttributes build()
    {
        return new EmailAttributes(id,
                                   subject,
                                   fromAddresses,
                                   toAddresses,
                                   bccAddresses,
                                   ccAddresses,
                                   headers,
                                   attachments,
                                   receivedDate,
                                   new EmailFlags(answered, deleted, draft, recent, seen),
                                   replyToAddresses);
    }

    public static EmailAttributes fromMessage(Message msg) throws MessagingException, IOException
    {
        return EmailAttributesBuilder.getInstance()
                .withId(msg.getMessageNumber())
                .withSubject(msg.getSubject())
                .fromAddresses(msg.getFrom())
                .toAddresses(msg.getRecipients(TO))
                .bccAddresses(msg.getRecipients(BCC))
                .ccAddresses(msg.getRecipients(CC))
                .withAttachments(EmailAttachment.fromMessage(msg))
                .receivedDate(LocalDateTime.now())
                .seen(msg.getFlags().contains(SEEN))
                .recent(msg.getFlags().contains(RECENT))
                .draft(msg.getFlags().contains(DRAFT))
                .answered(msg.getFlags().contains(ANSWERED))
                .deleted(msg.getFlags().contains(DELETED))
                .build();
    }

    private static EmailAttributesBuilder getInstance()
    {
        return new EmailAttributesBuilder();
    }
}

