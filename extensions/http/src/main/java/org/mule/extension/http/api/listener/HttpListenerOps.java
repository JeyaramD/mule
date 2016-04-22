/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.module.http.internal.listener.Server;

public class HttpListenerOps
{

    public MuleMessage<String, HttpRequestAttributes> dummyOp(@Connection Server pepe)
    {
        MuleMessage message = new DefaultMuleMessage("Oops");
        return message;
    }

}
