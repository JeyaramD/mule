/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static java.lang.Integer.MAX_VALUE;
import org.mule.extension.http.api.HttpConnector;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.HttpSendBodyMode;
import org.mule.extension.http.api.HttpStreamingType;
import org.mule.extension.http.api.request.authentication.HttpAuthentication;
import org.mule.extension.http.api.request.authentication.UsernamePasswordAuthentication;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.request.HttpRequestBuilder;
import org.mule.extension.http.internal.request.HttpResponseToMuleMessage;
import org.mule.extension.http.internal.request.MuleEventToHttpRequest;
import org.mule.extension.http.internal.request.grizzly.GrizzlyHttpClient;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.core.DefaultMuleEvent;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.api.MessagingException;
import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.core.config.i18n.CoreMessages;
import org.mule.runtime.core.util.StringUtils;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.module.http.api.HttpConstants;
import org.mule.runtime.module.http.internal.HttpParser;
import org.mule.runtime.module.http.internal.domain.request.HttpRequest;
import org.mule.runtime.module.http.internal.domain.request.HttpRequestAuthentication;
import org.mule.runtime.module.http.internal.domain.response.HttpResponse;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequesterOperations
{
    private static final Logger logger = LoggerFactory.getLogger(HttpRequesterOperations.class);

    static final String REMOTELY_CLOSED = "Remotely closed";
    private static final int WAIT_FOR_EVER = MAX_VALUE;

    /**
     * Consumes an HTTP service.
     *
     * @param path Path where the request will be sent.
     * @param method The HTTP method for the request.
     * @param host Host where the requests will be sent.
     * @param port Port where the requests will be sent.
     * @param source The expression used to obtain the body that will be sent in the request. Default is empty, so the payload will be used as the body.
     * @param followRedirects Specifies whether to follow redirects or not.
     * @param parseResponse Defines if the HTTP response should be parsed or it's raw contents should be propagated instead.
     * @param requestStreamingMode Defines if the request should be sent using streaming or not.
     * @param sendBodyMode Defines if the request should contain a body or not.
     * @param responseTimeout Maximum time that the request element will block the execution of the flow waiting for the HTTP response.
     * @param responseValidator Configures error handling of the response.
     * @param config the {@link HttpConnector} configuration for this operation. All parameters not configured will be taken from it.
     * @param muleEvent the current {@link MuleEvent}
     * @return a {@link MuleMessage} with {@link HttpResponseAttributes}
     */
    @MetadataScope(keysResolver = MetadataResolver.class, outputResolver = MetadataResolver.class)
    public MuleMessage<Object, HttpResponseAttributes> request(String path,
                                                          @Optional(defaultValue = "GET") String method,
                                                          @Optional String host,
                                                          @Optional Integer port,
                                                          @Optional String source,
                                                          @Optional Boolean followRedirects,
                                                          @Optional Boolean parseResponse,
                                                          @Optional HttpStreamingType requestStreamingMode,
                                                          @Optional HttpSendBodyMode sendBodyMode,
                                                          @Optional Integer responseTimeout,
                                                          @Optional ResponseValidator responseValidator,
                                                          @Optional HttpRequesterRequestBuilder requestBuilder,
                                                          @MetadataKeyId String key,
                                                          @Connection GrizzlyHttpClient client,
                                                          @UseConfig HttpRequesterConfig config,
                                                          MuleEvent muleEvent) throws MuleException
    {
        //TODO: Add request builder
        if (host == null)
        {
            host = config.getHost().apply(muleEvent);
        }

        if (port == null)
        {
            port = config.getPort().apply(muleEvent);
        }

        if (followRedirects == null)
        {
            followRedirects = config.getFollowRedirects().apply(muleEvent);
        }

        if (parseResponse == null)
        {
            parseResponse = config.getParseResponse().apply(muleEvent);
        }

        if (requestStreamingMode == null)
        {
            requestStreamingMode = config.getRequestStreamingMode().apply(muleEvent);
        }

        if (sendBodyMode == null)
        {
            sendBodyMode = config.getSendBodyMode().apply(muleEvent);
        }

        responseTimeout = resolveResponseTimeout(muleEvent, config, responseTimeout);

        if (responseValidator == null)
        {
            responseValidator = new SuccessStatusCodeValidator("0..399");
        }

        if (requestBuilder == null)
        {
            requestBuilder = new HttpRequesterRequestBuilder();
        }

        HttpAuthentication authentication = config.getAuthentication();
        String resolvedPath = requestBuilder.replaceUriParams(buildPath(config.getBasePath().apply(muleEvent), path));
        String uri = resolveUri(config.getScheme(), host, port, resolvedPath);
        MuleEventToHttpRequest eventToHttpRequest = new MuleEventToHttpRequest(config, uri, method, requestStreamingMode, sendBodyMode, source);
        MuleMessage<Object, HttpResponseAttributes> muleMessage = doRequest(followRedirects, parseResponse, responseTimeout, requestBuilder, client, config, muleEvent, authentication, eventToHttpRequest, true);

        //notificationHelper.fireNotification(this, muleEvent, httpRequest.getUri(), muleEvent.getFlowConstruct(), MESSAGE_REQUEST_END);
        responseValidator.validate(muleMessage);
        return muleMessage;
    }

    //TODO: This is awful. Refactor.
    private MuleMessage<Object, HttpResponseAttributes> doRequest(Boolean followRedirects, Boolean parseResponse, Integer responseTimeout, HttpRequesterRequestBuilder requestBuilder, GrizzlyHttpClient client, HttpRequesterConfig config, MuleEvent muleEvent, HttpAuthentication authentication, MuleEventToHttpRequest eventToHttpRequest, boolean checkRetry) throws MuleException
    {
        HttpRequest httpRequest = createHttpRequest(muleEvent, eventToHttpRequest, requestBuilder, authentication);

        HttpResponse response;
        try
        {
            //notificationHelper.fireNotification(this, muleEvent, httpRequest.getUri(), muleEvent.getFlowConstruct(), MESSAGE_REQUEST_BEGIN);
            response = client.send(httpRequest, responseTimeout, followRedirects, resolveAuthentication(config.getAuthentication()));
        }
        catch (Exception e)
        {
            checkIfRemotelyClosed(e, config);
            throw new MessagingException(CoreMessages.createStaticMessage("Error sending HTTP request"), muleEvent, e);
        }

        HttpResponseToMuleMessage httpResponseToMuleMessage = new HttpResponseToMuleMessage(config, parseResponse);
        MuleMessage<Object, HttpResponseAttributes> responseMessage = httpResponseToMuleMessage.convert(muleEvent, response, httpRequest.getUri());

        //Create a new event based on the old and the response so that the auth can use it
        MuleEvent responseEvent = new DefaultMuleEvent(new DefaultMuleMessage(responseMessage.getPayload(), null, responseMessage.getAttributes(), muleEvent.getMuleContext()), muleEvent, muleEvent.isSynchronous());
        if (resendRequest(responseEvent, checkRetry, authentication))
        {
            consumePayload(responseEvent);
            responseMessage = doRequest(followRedirects, parseResponse, responseTimeout, requestBuilder, client, config, responseEvent, authentication, eventToHttpRequest, false);
        }
        return responseMessage;
    }

    private boolean resendRequest(MuleEvent muleEvent, boolean retry, HttpAuthentication authentication) throws MuleException
    {
        return retry && authentication != null && authentication.shouldRetry(muleEvent);
    }

    private void consumePayload(final MuleEvent event)
    {
        if (event.getMessage().getPayload() instanceof InputStream)
        {
            try
            {
                event.getMessageAsBytes();
            }
            catch (Exception e)
            {
                throw new MuleRuntimeException(e);
            }
        }
    }

    private String resolveUri(HttpConstants.Protocols scheme, String host, Integer port, String path)
    {
        // Encode spaces to generate a valid HTTP request.
        String resolvedPath = HttpParser.encodeSpaces(path);

        return String.format("%s://%s:%s%s", scheme, host, port, resolvedPath);
    }

    private int resolveResponseTimeout(MuleEvent muleEvent, HttpRequesterConfig config, Integer responseTimeout)
    {
        if (responseTimeout == null && config.getResponseTimeout() != null)
        {
            responseTimeout = config.getResponseTimeout().apply(muleEvent);
        }

        if (muleEvent.getMuleContext().getConfiguration().isDisableTimeouts())
        {
            return WAIT_FOR_EVER;
        }
        else if (responseTimeout == null)
        {
            return muleEvent.getTimeout();
        }
        else
        {
            return responseTimeout;
        }
    }

    private HttpRequestAuthentication resolveAuthentication(HttpAuthentication authentication)
    {
        HttpRequestAuthentication requestAuthentication = null;
        if (authentication instanceof UsernamePasswordAuthentication)
        {
            requestAuthentication = ((UsernamePasswordAuthentication) authentication).buildRequestAuthentication();
        }
        return requestAuthentication;
    }

    private HttpRequest createHttpRequest(MuleEvent muleEvent, MuleEventToHttpRequest muleEventToHttpRequest, HttpRequesterRequestBuilder requestBuilder, HttpAuthentication authentication) throws MuleException
    {
        HttpRequestBuilder builder = muleEventToHttpRequest.create(muleEvent, requestBuilder);

        if (authentication != null)
        {
            authentication.authenticate(muleEvent, builder);
        }
        return builder.build();
    }

    private void checkIfRemotelyClosed(Exception exception, HttpRequesterConfig config)
    {
        if (config.getTlsContextFactory() != null && StringUtils.containsIgnoreCase(exception.getMessage(), REMOTELY_CLOSED))
        {
            logger.error("Remote host closed connection. Possible SSL/TLS handshake issue. Check protocols, cipher suites and certificate set up. Use -Djavax.net.debug=handshake for further debugging.");
        }
    }

    protected String buildPath(String basePath, String path)
    {
        String resolvedBasePath = basePath;
        String resolvedRequestPath = path;

        if (!resolvedBasePath.startsWith("/"))
        {
            resolvedBasePath = "/" + resolvedBasePath;
        }

        if (resolvedBasePath.endsWith("/") && resolvedRequestPath.startsWith("/"))
        {
            resolvedBasePath = resolvedBasePath.substring(0, resolvedBasePath.length() - 1);
        }

        if (!resolvedBasePath.endsWith("/") && !resolvedRequestPath.startsWith("/") && !resolvedRequestPath.isEmpty())
        {
            resolvedBasePath += "/";
        }


        return resolvedBasePath + resolvedRequestPath;

    }

}
