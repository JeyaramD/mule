/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.util.Collections.unmodifiableMap;

import java.util.Map;

import javax.activation.DataHandler;

public class HttpResponseAttributes extends HttpAttributes
{
    private int statusCode;
    private String reasonPhrase;

    public HttpResponseAttributes(int statusCode, String reasonPhrase, Map<String, DataHandler> attachments, Map<String, String> headers)
    {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.parts = unmodifiableMap(attachments);
        this.headers = unmodifiableMap(headers);
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getReasonPhrase()
    {
        return reasonPhrase;
    }
}
