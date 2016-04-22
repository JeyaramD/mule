/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.listener.builder;

import static org.mule.runtime.module.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.module.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.module.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.runtime.module.http.api.requester.HttpStreamingType.ALWAYS;
import static org.mule.runtime.module.http.api.requester.HttpStreamingType.AUTO;
import org.mule.extension.http.api.HttpMessageBuilder;
import org.mule.runtime.api.message.NullPayload;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.api.MessagingException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleMessage;
import org.mule.runtime.core.api.config.MuleProperties;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.config.i18n.MessageFactory;
import org.mule.runtime.core.transformer.types.DataTypeFactory;
import org.mule.runtime.core.transformer.types.MimeTypes;
import org.mule.runtime.core.util.DataTypeUtils;
import org.mule.runtime.core.util.IOUtils;
import org.mule.runtime.core.util.UUID;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.module.http.api.HttpHeaders;
import org.mule.runtime.module.http.api.requester.HttpStreamingType;
import org.mule.runtime.module.http.internal.HttpParser;
import org.mule.runtime.module.http.internal.ParameterMap;
import org.mule.runtime.module.http.internal.domain.ByteArrayHttpEntity;
import org.mule.runtime.module.http.internal.domain.EmptyHttpEntity;
import org.mule.runtime.module.http.internal.domain.HttpEntity;
import org.mule.runtime.module.http.internal.domain.InputStreamHttpEntity;
import org.mule.runtime.module.http.internal.domain.MultipartHttpEntity;
import org.mule.runtime.module.http.internal.domain.response.HttpResponse;
import org.mule.runtime.module.http.internal.listener.HttpResponseHeaderBuilder;
import org.mule.runtime.module.http.internal.multipart.HttpMultipartEncoder;
import org.mule.runtime.module.http.internal.multipart.HttpPartDataSource;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseBuilder extends HttpMessageBuilder implements MuleContextAware
{
    public static final String MULTIPART = "multipart";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Parameter
    @Optional
    private Integer statusCode;
    @Parameter
    @Optional
    private String reasonPhrase;
    @Parameter
    @Optional(defaultValue = "AUTO")
    private HttpStreamingType responseStreaming;

    private boolean multipartEntityWithNoMultipartContentyTypeWarned;
    private boolean mapPayloadButNoUrlEncodedContentyTypeWarned;
    private String httpVersion;
    private MuleContext muleContext;

    public HttpResponse build(org.mule.runtime.module.http.internal.domain.response.HttpResponseBuilder httpResponseBuilder, MuleMessage muleMessage, boolean supportsTransferEncoding) throws MessagingException
    {
        final HttpResponseHeaderBuilder httpResponseHeaderBuilder = new HttpResponseHeaderBuilder();


        if (httpResponseHeaderBuilder.getHeader(MuleProperties.CONTENT_TYPE_PROPERTY) != null)
        {
            DataType<?> dataType = muleMessage.getDataType();
            if (!MimeTypes.ANY.equals(dataType.getMimeType()))
            {
                httpResponseHeaderBuilder.addHeader(MuleProperties.CONTENT_TYPE_PROPERTY, DataTypeUtils.getContentType(dataType));
            }
        }

        for (String name : headers.keySet())
        {
            //TODO: Support multiple values like ParamMap does
            //final Collection<String> paramValues = resolvedHeaders.getAll(name);
            //for (String value : paramValues)
            //{
                if (TRANSFER_ENCODING.equals(name) && !supportsTransferEncoding)
                {
                    logger.debug("Client HTTP version is lower than 1.1 so the unsupported 'Transfer-Encoding' header has been removed and 'Content-Length' will be sent instead.");
                }
                else
                {
                    httpResponseHeaderBuilder.addHeader(name, headers.get(name));
                }
            //}
        }

        final String configuredContentType = httpResponseHeaderBuilder.getContentType();
        final String existingTransferEncoding = httpResponseHeaderBuilder.getTransferEncoding();
        final String existingContentLength = httpResponseHeaderBuilder.getContentLength();

        HttpEntity httpEntity;

        if (!attachments.isEmpty())
        {
            if (configuredContentType == null)
            {
                httpResponseHeaderBuilder.addContentType(createMultipartFormDataContentType());
            }
            else if (!configuredContentType.startsWith(MULTIPART))
            {
                warnNoMultipartContentTypeButMultipartEntity(httpResponseHeaderBuilder.getContentType());
            }
            httpEntity = createMultipartEntity(muleMessage, httpResponseHeaderBuilder.getContentType());
            resolveEncoding(httpResponseHeaderBuilder, existingTransferEncoding, existingContentLength, supportsTransferEncoding, (ByteArrayHttpEntity) httpEntity);
        }
        else
        {
            final Object payload = muleMessage.getPayload();
            if (payload == NullPayload.getInstance())
            {
                setupContentLengthEncoding(httpResponseHeaderBuilder, 0);
                httpEntity = new EmptyHttpEntity();
            }
            else if (payload instanceof Map)
            {
                if (configuredContentType == null)
                {
                    httpResponseHeaderBuilder.addContentType(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
                }
                else if (!configuredContentType.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED))
                {
                    warnMapPayloadButNoUrlEncodedContentType(httpResponseHeaderBuilder.getContentType());
                }
                httpEntity = createUrlEncodedEntity((Map) payload);
            }
            else if (payload instanceof InputStream)
            {
                if (responseStreaming == ALWAYS || (responseStreaming == AUTO && existingContentLength == null))
                {
                    if (supportsTransferEncoding)
                    {
                        setupChunkedEncoding(httpResponseHeaderBuilder);
                    }
                    httpEntity = new InputStreamHttpEntity((InputStream) payload);
                }
                else
                {
                    ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity(IOUtils.toByteArray(((InputStream) payload)));
                    setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getContent().length);
                    httpEntity = byteArrayHttpEntity;
                }
            }
            else
            {
                try
                {
                    //TODO: Find a better way to do this
                    ByteArrayHttpEntity byteArrayHttpEntity = new ByteArrayHttpEntity((byte[]) muleContext.getTransformationService().transform((org.mule.runtime.core.api.MuleMessage) muleMessage, DataTypeFactory.BYTE_ARRAY).getPayload());
                    resolveEncoding(httpResponseHeaderBuilder, existingTransferEncoding, existingContentLength, supportsTransferEncoding, byteArrayHttpEntity);
                    httpEntity = byteArrayHttpEntity;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        Collection<String> headerNames = httpResponseHeaderBuilder.getHeaderNames();
        for (String headerName : headerNames)
        {
            Collection<String> values = httpResponseHeaderBuilder.getHeader(headerName);
            for (String value : values)
            {
                httpResponseBuilder.addHeader(headerName, value);
            }
        }

        if (statusCode != null)
        {
            httpResponseBuilder.setStatusCode(statusCode);
        }
        if (reasonPhrase != null)
        {
            httpResponseBuilder.setReasonPhrase(reasonPhrase);
        }
        httpResponseBuilder.setEntity(httpEntity);
        return httpResponseBuilder.build();
    }

    private void resolveEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder,
                                 String existingTransferEncoding,
                                 String existingContentLength,
                                 boolean supportsTranferEncoding,
                                 ByteArrayHttpEntity byteArrayHttpEntity)
    {
        if (responseStreaming == ALWAYS || (responseStreaming == AUTO && existingContentLength == null && CHUNKED.equals(existingTransferEncoding)))
        {
            if (supportsTranferEncoding)
            {
                setupChunkedEncoding(httpResponseHeaderBuilder);
            }
        }
        else
        {
            setupContentLengthEncoding(httpResponseHeaderBuilder, byteArrayHttpEntity.getContent().length);
        }
    }

    private void setupContentLengthEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder, int contentLength)
    {
        if (httpResponseHeaderBuilder.getTransferEncoding() != null)
        {
            logger.debug("Content-Length encoding is being used so the 'Transfer-Encoding' header has been removed");
            httpResponseHeaderBuilder.removeHeader(TRANSFER_ENCODING);
        }
        httpResponseHeaderBuilder.addContentLenght(String.valueOf(contentLength));
    }

    private void setupChunkedEncoding(HttpResponseHeaderBuilder httpResponseHeaderBuilder)
    {
        if (httpResponseHeaderBuilder.getContentLength() != null)
        {
            logger.debug("Chunked encoding is being used so the 'Content-Length' header has been removed");
            httpResponseHeaderBuilder.removeHeader(CONTENT_LENGTH);
        }
        httpResponseHeaderBuilder.addHeader(TRANSFER_ENCODING, CHUNKED);
    }

    private String createMultipartFormDataContentType()
    {
        return String.format("%s; boundary=%s", HttpHeaders.Values.MULTIPART_FORM_DATA, UUID.getUUID());
    }

    private HttpEntity createUrlEncodedEntity(Map payload)
    {
        final Map mapPayload = payload;
        HttpEntity entity = new EmptyHttpEntity();
        if (!mapPayload.isEmpty())
        {
            //TODO: Figure out a proper way to get the encoding
            String encodedBody;
            if (mapPayload instanceof ParameterMap)
            {
                encodedBody = HttpParser.encodeString("UTF-8", ((ParameterMap) mapPayload).toListValuesMap());
            }
            else
            {
                encodedBody = HttpParser.encodeString("UTF-8", mapPayload);
            }
            entity = new ByteArrayHttpEntity(encodedBody.getBytes());
        }
        return entity;
    }

    private void warnMapPayloadButNoUrlEncodedContentType(String contentType)
    {
        if (!mapPayloadButNoUrlEncodedContentyTypeWarned)
        {
            logger.warn(String.format("Payload is a Map which will be used to generate an url encoded http body but Contenty-Type specified is %s and not %s", contentType, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED));
            mapPayloadButNoUrlEncodedContentyTypeWarned = true;
        }
    }

    private void warnNoMultipartContentTypeButMultipartEntity(String contentType)
    {
        if (!multipartEntityWithNoMultipartContentyTypeWarned)
        {
            logger.warn(String.format("Sending http response with Content-Type %s but the message has attachment and a multipart entity is generated", contentType));
            multipartEntityWithNoMultipartContentyTypeWarned = true;
        }
    }

    private HttpEntity createMultipartEntity(MuleMessage muleMessage, String contentType) throws MessagingException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Message contains outbound attachments. Ignoring payload and trying to generate multipart response");
        }
        final HashMap<String, DataHandler> parts = new HashMap<>();
        for (String attachmentName : attachments.keySet())
        {
            parts.put(attachmentName, attachments.get(attachmentName));
        }
        final MultipartHttpEntity multipartEntity;
        try
        {
            multipartEntity = new MultipartHttpEntity(HttpPartDataSource.createFrom(parts));
            return new ByteArrayHttpEntity(HttpMultipartEncoder.createMultipartContent(multipartEntity, contentType));
        }
        catch (Exception e)
        {
            throw new MessagingException(MessageFactory.createStaticMessage("Error creating multipart HTTP entity."), (org.mule.runtime.core.api.MuleMessage) muleMessage, e);
        }
    }

    public static HttpResponseBuilder emptyInstance(MuleContext muleContext) throws InitialisationException
    {
        final HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
        httpResponseBuilder.setMuleContext(muleContext);
        return httpResponseBuilder;
    }

    public HttpStreamingType getResponseStreaming()
    {
        return responseStreaming;
    }

    public void setHttpVersion(String httpVersion)
    {
        this.httpVersion = httpVersion;
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}