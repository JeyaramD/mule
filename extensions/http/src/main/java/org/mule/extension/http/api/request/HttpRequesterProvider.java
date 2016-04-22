/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static org.mule.runtime.extension.api.introspection.parameter.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.module.http.api.HttpConstants.Protocols.HTTP;
import static org.mule.runtime.module.http.api.HttpConstants.Protocols.HTTPS;
import org.mule.extension.http.api.request.authentication.HttpAuthentication;
import org.mule.extension.http.api.request.client.HttpClient;
import org.mule.extension.http.api.request.proxy.ProxyConfig;
import org.mule.extension.http.internal.request.client.HttpClientConfiguration;
import org.mule.extension.http.internal.request.grizzly.GrizzlyHttpClient;
import org.mule.module.socket.api.TcpClientSocketProperties;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextFactoryBuilder;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.core.config.i18n.CoreMessages;
import org.mule.runtime.core.util.concurrent.ThreadNameHelper;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.module.http.api.HttpConstants;
import org.mule.runtime.module.tls.api.DefaultTlsContextFactoryBuilder;

import javax.inject.Inject;

@Alias("requester")
public class HttpRequesterProvider implements ConnectionProvider<HttpRequesterConfig, HttpClient>, MuleContextAware, Initialisable
{
    private static final int UNLIMITED_CONNECTIONS = -1;
    private static final String THREAD_NAME_PREFIX_PATTERN = "%shttp.requester.%s";

    /**
     * Host where the requests will be sent.
     */
    @Parameter
    @Expression(NOT_SUPPORTED)
    private String host;

    /**
     * Port where the requests will be sent. If the protocol attribute is HTTP (default) then the default value is 80, if the protocol
     * attribute is HTTPS then the default value is 443.
     */
    @Parameter
    @Optional
    @Expression(NOT_SUPPORTED)
    private Integer port;

    /**
     * Protocol to use for communication. Valid values are HTTP and HTTPS. Default value is HTTP. When using HTTPS the
     * HTTP communication is going to be secured using TLS / SSL. If HTTPS was configured as protocol then the
     * user can customize the tls/ssl configuration by defining the tls:context child element of this listener-config.
     * If not tls:context is defined then the default JVM certificates are going to be used to establish communication.
     */
    @Parameter
    @Expression(NOT_SUPPORTED)
    private HttpConstants.Protocols protocol;

    //TODO: document?
    @Parameter
    @Optional
    private TlsContextFactory tlsContextFactory;

    //TODO: document
    @Parameter
    @Optional
    private HttpAuthentication authentication;

    /**
     * Reusable configuration element for outbound connections through a proxy.
     * A proxy element must define a host name and a port attributes, and optionally can define a username
     * and a password.
     */
    @Parameter
    @Optional
    private ProxyConfig proxyConfig;

    /**
     * The maximum number of outbound connections that will be kept open at the same time.
     * By default the number of connections is unlimited.
     */
    @Parameter
    @Optional(defaultValue = "-1")
    @Expression(NOT_SUPPORTED)
    private int maxConnections;

    /**
     * The number of milliseconds that a connection can remain idle before it is closed.
     * The value of this attribute is only used when persistent connections are enabled.
     */
    @Parameter
    @Optional(defaultValue = "30000")
    @Expression(NOT_SUPPORTED)
    private int connectionIdleTimeout;

    /**
     * If false, each connection will be closed after the first request is completed.
     */
    @Parameter
    @Optional(defaultValue = "true")
    @Expression(NOT_SUPPORTED)
    private boolean usePersistentConnections;

    @Parameter
    @Optional
    @Expression(NOT_SUPPORTED)
    private TcpClientSocketProperties clientSocketProperties;

    @Inject
    @DefaultTlsContextFactoryBuilder
    private TlsContextFactoryBuilder defaultTlsContextFactoryBuilder;
    private MuleContext muleContext;

    @Override
    public HttpClient connect(HttpRequesterConfig httpRequesterConfig) throws ConnectionException
    {
        String threadNamePrefix = String.format(THREAD_NAME_PREFIX_PATTERN, ThreadNameHelper.getPrefix(muleContext), httpRequesterConfig.getName());

        HttpClientConfiguration configuration = new HttpClientConfiguration.Builder()
            .setTlsContextFactory(tlsContextFactory)
            .setProxyConfig(proxyConfig)
            .setClientSocketProperties(clientSocketProperties)
            .setMaxConnections(maxConnections)
            .setUsePersistentConnections(usePersistentConnections)
            .setConnectionIdleTimeout(connectionIdleTimeout)
            .setThreadNamePrefix(threadNamePrefix)
            .setOwnerName(httpRequesterConfig.getName())
            .build();

        //TODO: add DNS lookup changes
        HttpClient httpClient = new GrizzlyHttpClient(configuration);
        try
        {
            httpClient.start();
        }
        catch (MuleException e)
        {
            throw new ConnectionException("Unnable to create HTTP client", e);
        }
        return httpClient;
    }

    @Override
    public void disconnect(HttpClient httpClient)
    {
        //here we'll stop the client and any other stuff we need to get rid of
        try
        {
            httpClient.stop();
        }
        catch (MuleException e)
        {
            //
        }
    }

    @Override
    public ConnectionValidationResult validate(HttpClient httpClient)
    {
        return null;
    }

    @Override
    public ConnectionHandlingStrategy<HttpClient> getHandlingStrategy(ConnectionHandlingStrategyFactory<HttpRequesterConfig, HttpClient> connectionHandlingStrategyFactory)
    {
        return null;
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        LifecycleUtils.initialiseIfNeeded(authentication);
        verifyConnectionsParameters();

        if (port == null)
        {
            port = protocol.getDefaultPort();
        }

        if (protocol.equals(HTTP) && tlsContextFactory != null)
        {
            throw new InitialisationException(CoreMessages.createStaticMessage("TlsContext cannot be configured with protocol HTTP, " +
                                                                               "when using tls:context you must set attribute protocol=\"HTTPS\""), this);
        }

        if (protocol.equals(HTTPS) && tlsContextFactory == null)
        {
            tlsContextFactory = defaultTlsContextFactoryBuilder.buildDefault();
        }
        if (tlsContextFactory != null)
        {
            LifecycleUtils.initialiseIfNeeded(tlsContextFactory);
        }
    }

    private void verifyConnectionsParameters() throws InitialisationException
    {
        if (maxConnections < UNLIMITED_CONNECTIONS || maxConnections == 0)
        {
            throw new InitialisationException(CoreMessages.createStaticMessage("The maxConnections parameter only allows positive values or -1 for unlimited concurrent connections."), this);
        }

        if (!usePersistentConnections)
        {
            connectionIdleTimeout = 0;
        }
    }
}
