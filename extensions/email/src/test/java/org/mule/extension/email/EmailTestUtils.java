/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email;

import static java.util.Collections.singletonList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.mule.extension.email.EmailConnectorFunctionalTestCase.ALE_EMAIL;
import static org.mule.extension.email.EmailConnectorFunctionalTestCase.ESTEBAN_EMAIL;
import static org.mule.extension.email.internal.util.EmailUtils.toAddressArray;

import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailTestUtils
{

    public static MimeMessage defaultMimeMessage(String to) throws MessagingException
    {
        return createMimeMessage(singletonList(to),
                                 singletonList(ESTEBAN_EMAIL),
                                 singletonList(ALE_EMAIL),
                                 "Email Subject",
                                 "Email Content",
                                 null);
    }

    public static MimeMessage createMimeMessage(List<String> to,
                                          List<String> cc,
                                          List<String> bcc,
                                          String subject,
                                          String text,
                                          List<Object> attachments) throws MessagingException
    {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        mimeMessage.addRecipients(TO, toAddressArray(to));
        mimeMessage.addRecipients(CC, toAddressArray(cc));
        mimeMessage.addRecipients(BCC, toAddressArray(bcc));
        mimeMessage.setSubject(subject);

        if(attachments == null)
        {
            mimeMessage.setText(text);
        }
        else
        {
            MimeMultipart mimeMultipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setText(text);
            mimeMultipart.addBodyPart(mimeBodyPart);

            for (Object attachment : attachments)
            {
                mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(attachment, "type?");
                mimeMultipart.addBodyPart(mimeBodyPart);
            }

            mimeMessage.setContent(mimeMultipart);
        }

        return mimeMessage;
    }

}
