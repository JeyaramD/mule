/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.operations;

import static java.lang.String.format;
import static javax.mail.Flags.Flag.SEEN;
import static org.mule.extension.email.internal.util.EmailUtils.getAttributesFromMessage;
import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.exception.EmailException;
import org.mule.runtime.api.message.MuleMessage;

import java.io.IOException;
import java.util.Optional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

public class ReadOperation
{
    public Object read(Store store, String folder, MuleMessage muleMessage, Integer emailId)
    {
        Optional<EmailAttributes> attributes = getAttributesFromMessage(muleMessage);

        if (attributes.isPresent())
        {
            emailId = attributes.get().getId();
        }
        else
        {
            if (emailId == null)
            {
                throw new EmailException("No emailId specified for the read operation. Expecting EmailAttributes mule message or an explicit emailId value");
            }
        }

        try
        {
            Message message = store.getFolder(folder).getMessage(emailId);
            message.setFlag(SEEN, true);
            return message.getContent();
        }
        catch (MessagingException e)
        {
            throw new EmailException(format("Error while fetching email id [%s] ", emailId), e);
        }
        catch (IOException e)
        {
            throw new EmailException(format("Error while getting email id [%s] content", emailId), e);
        }
    }
}
