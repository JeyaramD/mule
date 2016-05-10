/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import org.mule.extension.email.internal.EmailAttributes;
import org.mule.extension.email.internal.operations.FetchOperation;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.extension.api.annotation.param.Connection;

import java.util.List;

public class RetrieverOperations
{
    public List<MuleMessage<Object, EmailAttributes>> fetch(@Connection RetrieverConnection connection)
    {
        return new FetchOperation().fetch(connection.getStore(), connection.getFolder(), false, false);
    }
}
