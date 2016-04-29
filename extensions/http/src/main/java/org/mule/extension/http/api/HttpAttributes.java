/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import java.io.Serializable;
import java.util.Map;

import javax.activation.DataHandler;

public class HttpAttributes implements Serializable
{
    protected Map<String, String> headers;
    protected Map<String, DataHandler> parts;

    public Map<String, String> getHeaders()
    {
        return headers;
    }
    public Map<String, DataHandler> getParts()
    {
        return parts;
    }
}
