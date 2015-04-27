/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.context.notification;

import org.mule.api.context.notification.MessageProcessorNotificationListener;
import org.mule.context.notification.MessageProcessorNotification;

/**
 * Listener for MessageProcessorNotification that delegates notifications to NotificationTextDebugger
 */
public class MessageProcessorTextDebugger implements MessageProcessorNotificationListener<MessageProcessorNotification>
{

    private final MessageProcessingStackManager messageProcessingStackManager;

    public MessageProcessorTextDebugger(MessageProcessingStackManager messageProcessingStackManager)
    {
        this.messageProcessingStackManager = messageProcessingStackManager;
    }


    @Override
    public void onNotification(MessageProcessorNotification notification)
    {
        messageProcessingStackManager.onMessageProcessorNotification(notification);
    }

}
