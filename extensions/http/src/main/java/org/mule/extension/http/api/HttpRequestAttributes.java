/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api;

import static java.util.Collections.emptyMap;
import static org.mule.runtime.module.http.internal.HttpParser.decodeQueryString;
import static org.mule.runtime.module.http.internal.HttpParser.decodeUriParams;
import static org.mule.runtime.module.http.internal.HttpParser.extractPath;
import static org.mule.runtime.module.http.internal.HttpParser.extractQueryParams;
import org.mule.runtime.module.http.internal.ParameterMap;
import org.mule.runtime.module.http.internal.domain.request.ClientConnection;
import org.mule.runtime.module.http.internal.domain.request.HttpRequest;
import org.mule.runtime.module.http.internal.domain.request.HttpRequestContext;
import org.mule.runtime.module.http.internal.listener.ListenerPath;

import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

public class HttpRequestAttributes extends HttpAttributes
{
    private String listenerPath;
    private String relativePath;
    private String version;
    private String scheme;
    private String method;
    private String requestPath;
    private String requestUri;
    private String queryString;
    private Map<String, String> queryParams;
    private Map<String, String> uriParams;
    private String remoteHostAddress;
    private Certificate clientCertificate;

    public HttpRequestAttributes(HttpRequestContext requestContext, ListenerPath listenerPath, Map<String, DataHandler> attachments)
    {
        final String resolvedListenerPath = listenerPath.getResolvedPath();
        HttpRequest request = requestContext.getRequest();
        this.version = request.getProtocol().asString();
        this.scheme = requestContext.getScheme();
        this.method = request.getMethod();
        String uri = request.getUri();
        this.requestUri = uri;
        final String path = extractPath(uri);
        this.requestPath = path;
        String queryString = extractQueryParams(uri);
        this.queryString = queryString;
        ParameterMap queryParams = decodeQueryString(queryString);
        this.queryParams = queryParams == null ? emptyMap() : queryParams.toImmutableParameterMap();
        this.uriParams = decodeUriParams(resolvedListenerPath, path);
        ClientConnection clientConnection = requestContext.getClientConnection();
        this.remoteHostAddress = clientConnection.getRemoteHostAddress().toString();
        this.clientCertificate = clientConnection.getClientCertificate();
        this.relativePath = listenerPath.getRelativePath(path);
        this.listenerPath = resolvedListenerPath;
        final Collection<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        for (String headerName : headerNames)
        {
            final Collection<String> values = request.getHeaderValues(headerName);
            if (values.size() == 1)
            {
                headers.put(headerName, values.iterator().next());
            }
            else
            {
                headers.put(headerName, values);
            }
        }
        this.headers = Collections.unmodifiableMap(headers);
        this.parts = Collections.unmodifiableMap(attachments);
    }

    public String getListenerPath()
    {
        return listenerPath;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public String getVersion()
    {
        return version;
    }

    public String getScheme()
    {
        return scheme;
    }

    public String getMethod()
    {
        return method;
    }

    public String getRequestPath()
    {
        return requestPath;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public Map<String, String> getQueryParams()
    {
        return queryParams;
    }

    public Map<String, String> getUriParams()
    {
        return uriParams;
    }

    public String getRemoteHostAddress()
    {
        return remoteHostAddress;
    }

    public Certificate getClientCertificate()
    {
        return clientCertificate;
    }
}
