/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.sender;

import org.mule.extension.email.internal.EmailAttachment;
import org.mule.extension.email.internal.operations.ForwardOperation;
import org.mule.extension.email.internal.operations.ReplyOperation;
import org.mule.extension.email.internal.operations.SendOperation;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.annotation.param.display.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SenderOperations
{

    private static final String PAYLOAD = "#[payload]";

    public void send(@Connection SenderConnection connection,
                     @UseConfig SenderConfiguration configuration,
                     @Optional(defaultValue = PAYLOAD) @Text String content,
                     @Optional(defaultValue = "[No Subject]") String subject,
                     List<String> toAddresses,
                     @Optional List<String> ccAddresses,
                     @Optional List<String> bccAddresses,
                     @Optional Map<String, String> headers,
                     @Optional List<EmailAttachment> attachments)
    {
        new SendOperation().send(connection.getSession(),
                                 content,
                                 subject,
                                 toAddresses,
                                 configuration.getFrom(),
                                 ccAddresses != null ? ccAddresses : new ArrayList<>(),
                                 bccAddresses != null ? bccAddresses : new ArrayList<>(),
                                 headers != null ? headers : new HashMap<>(),
                                 attachments);
    }

    public void forward(@Connection SenderConnection connection,
                        @UseConfig SenderConfiguration configuration,
                        MuleMessage muleMessage,
                        @Optional(defaultValue = PAYLOAD) String content,
                        @Optional String subject,
                        List<String> toAddresses)
    {
        new ForwardOperation().forward(connection.getSession(),
                                       muleMessage,
                                       content,
                                       subject,
                                       configuration.getFrom(),
                                       toAddresses);
    }

    public void reply(@Connection SenderConnection connection,
                      @UseConfig SenderConfiguration configuration,
                      MuleMessage muleMessage,
                      String content,
                      @Optional List<String> toAddresses,
                      @Optional(defaultValue = "false") Boolean replyToAll)
    {
        new ReplyOperation().reply(connection.getSession(),
                                   muleMessage,
                                   content,
                                   configuration.getFrom(),
                                   toAddresses,
                                   replyToAll);
    }

}
