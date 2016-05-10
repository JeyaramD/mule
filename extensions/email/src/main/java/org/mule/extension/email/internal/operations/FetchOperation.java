/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static org.mule.extension.email.internal.builder.EmailAttributesBuilder.fromMessage;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.exception.RetrieverException;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;

public class FetchOperation
{

    public List<MuleMessage<Object, EmailAttributes>> fetch(Store store, String folder, Boolean markAsRead, Boolean delete)
    {
        List<MuleMessage<Object, EmailAttributes>> emails = new ArrayList<>();
        try
        {
            Folder emailFolder = store.getFolder(folder);
            emailFolder.open(Folder.READ_ONLY);
            Message[] messages = emailFolder.getMessages();
            for (Message message : messages)
            {
                MuleMessage muleMessage = new DefaultMuleMessage(getMessageText(message), fromMessage(message));
                emails.add(muleMessage);
            }
            return emails;
        }
        catch (MessagingException | IOException e)
        {
            throw new RetrieverException("could not fetch messages " + e.getMessage(), e);
        }
    }

    // Asuming the text always come in the first elemnt of the multipart message
    public Optional<String> getMessageText(Part message) throws IOException, MessagingException
    {
        Object content = message.getContent();
        if (message.isMimeType("text/plain"))
        {
            return Optional.ofNullable(content.toString());
        }
        else if (message.isMimeType("multipart/*"))
        {
            Multipart mp = (Multipart) content;
            if (mp.getCount() > 0)
            {
                return getMessageText(mp.getBodyPart(0));
            }

        }
        return Optional.empty();
    }

}
