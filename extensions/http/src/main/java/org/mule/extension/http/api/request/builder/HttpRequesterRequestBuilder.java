/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request.builder;

import org.mule.runtime.extension.api.annotation.Parameter;

import java.util.Map;

public class HttpRequesterRequestBuilder
{
    @Parameter
    private Map<String, String> queryParams;

    @Parameter
    private Map<String, String> uriParams;

    @Parameter
    private Map<String, String> headers;

    //TODO: Analyse support of several definitions
    public String replaceUriParams(String path)
    {
        for (String uriParamName : uriParams.keySet())
        {
            String uriParamValue = uriParams.get(uriParamName);

            if (uriParamValue == null)
            {
                throw new NullPointerException(String.format("Expression {%s} evaluated to null.", uriParamName));
            }

            path = path.replaceAll(String.format("\\{%s\\}", uriParamName), uriParamValue);
        }
        return path;
    }

    public Map<String, String> getQueryParams()
    {
        return queryParams;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }
}
