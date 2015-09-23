/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.context.notification;

import org.mule.api.context.notification.PipelineMessageNotificationListener;
import org.mule.context.notification.PipelineMessageNotification;

/**
 * Listener for PipelineMessageNotification that delegates notifications to NotificationTextDebugger
 */
public class FlowNotificationTextDebugger implements PipelineMessageNotificationListener<PipelineMessageNotification>
{

    private final MessageProcessingStackManager messageProcessingStackManager;

    public FlowNotificationTextDebugger(MessageProcessingStackManager messageProcessingStackManager)
    {
        this.messageProcessingStackManager = messageProcessingStackManager;
    }


    @Override
    public void onNotification(PipelineMessageNotification notification)
    {
        if (notification.getAction() == PipelineMessageNotification.PROCESS_COMPLETE)
        {
            messageProcessingStackManager.onPipelineNotificationComplete(notification);
        }
        else if (notification.getAction() == PipelineMessageNotification.PROCESS_START)
        {
            messageProcessingStackManager.onPipelineNotificationStart(notification);
        }
    }


}
