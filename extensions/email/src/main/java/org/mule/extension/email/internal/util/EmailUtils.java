/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.util;

import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.exception.EmailException;
import org.mule.runtime.api.message.MuleMessage;

import java.util.List;
import java.util.Optional;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailUtils
{

    public static Address toAddress(String address)
    {
        try
        {
            return new InternetAddress(address);
        }
        catch (AddressException e)
        {
            throw new EmailException(String.format("Error while creating %s InternetAddress", address));
        }
    }

    public static Address[] toAddressArray(List<String> addresses)
    {
        return addresses.stream().map(EmailUtils::toAddress).toArray(Address[]::new);
    }

    public static Optional<EmailAttributes> getAttributesFromMessage(MuleMessage muleMessage)
    {
        if (muleMessage.getAttributes() instanceof EmailAttributes)
        {
            return Optional.ofNullable((EmailAttributes) muleMessage.getAttributes());
        }
        return Optional.empty();
    }

}
