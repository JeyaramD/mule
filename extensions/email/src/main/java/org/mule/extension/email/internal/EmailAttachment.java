/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal;

import static org.mule.runtime.extension.api.introspection.parameter.ExpressionSupport.REQUIRED;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

public class EmailAttachment
{

    @Parameter
    private String id;

    @Parameter
    @Expression(REQUIRED)
    private Object data;

    @Parameter
    @Optional
    private String contentType;

    public EmailAttachment(String key, Object data, String contentType)
    {

    }

    public EmailAttachment()
    {

    }

    public String getId()
    {
        return id;
    }

    public Object getData()
    {
        return data;
    }

    public String getContentType()
    {
        return contentType;
    }


    public static List<EmailAttachment> fromMessage(Part message) throws IOException, MessagingException
    {
        List<EmailAttachment> attachments = new ArrayList<>();
        Object content = message.getContent();
        if (message.isMimeType("multipart/*"))
        {
            Multipart mp = (Multipart) content;
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
            {
                BodyPart bodyPart = mp.getBodyPart(i);
                if (i == 0 && bodyPart.isMimeType("text/plain"))
                {
                    continue;
                }
                attachments.add(new EmailAttachment("attachment-" + i, content, message.getContentType()));
            }
        }
        else if (message.isMimeType("message/rfc822"))
        {
            return fromMessage((Part) message.getContent());
        }
        //check if the content is an inline image
        else if (message.isMimeType("image/jpeg"))
        {
            new EmailAttachment("image-attachment", content, message.getContentType());
        }

        return attachments;
    }
}
