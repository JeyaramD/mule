/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class EmailAttributes implements Serializable
{

    private int id;

    private List<String> fromAddresses;

    private List<String> toAddresses;

    private List<String> ccAddresses;

    private List<String> bccAddresses;

    private List<String> replyToAddresses;

    private Map<String, String> headers;

    private String subject;

    private LocalDateTime receivedDate;

    private EmailFlags flags;

    private List<EmailAttachment> attachments;

    public EmailAttributes(int id,
                           String subject,
                           List<String> fromAddresses,
                           List<String> toAddresses,
                           List<String> bccAddresses,
                           List<String> ccAddresses,
                           Map<String, String> headers,
                           List<EmailAttachment> attachments,
                           LocalDateTime receivedDate,
                           EmailFlags flags,
                           List<String> replyToAddresses)
    {
        this.id = id;
        this.attachments = attachments;
        this.bccAddresses = bccAddresses;
        this.ccAddresses = ccAddresses;
        this.fromAddresses = fromAddresses;
        this.headers = headers;
        this.receivedDate = receivedDate;
        this.replyToAddresses = replyToAddresses;
        this.subject = subject;
        this.toAddresses = toAddresses;
    }


    public int getId()
    {
        return id;
    }

    public List<String> getReplyToAddresses()
    {
        return replyToAddresses;
    }

    public String getSubject()
    {
        return subject;
    }

    public List<String> getToAddresses()
    {
        return toAddresses;
    }

    public List<String> getBccAddresses()
    {
        return bccAddresses;
    }

    public List<String> getCcAddresses()
    {
        return ccAddresses;
    }

    public List<String> getFromAddresses()
    {
        return fromAddresses;
    }

    public LocalDateTime getReceivedDate()
    {
        return receivedDate;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public List<EmailAttachment> getAttachments()
    {
        return attachments;
    }
}
