/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal.exception;

public class RetrieverException extends RuntimeException
{

    public static final String ERROR = "Error while retrieving emails: ";

    public RetrieverException(String message)
    {
        super(ERROR + message);
    }

    public RetrieverException(String msg, Exception e)
    {
        super(ERROR + msg, e);
    }
}
