/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.retriever;

import static org.mule.extension.email.internal.EmailProperties.PROTOCOL_IMAP;
import org.mule.extension.email.api.EmailConnectionSetting;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Alias("imap")
public class IMAPConnectionProvider implements ConnectionProvider<RetrieverConfiguration, RetrieverConnection>
{

    @ParameterGroup
    private EmailConnectionSetting setting;

    @Parameter
    @Optional
    private String user;

    @Parameter
    @Optional
    private String password;

    @Parameter
    @Optional(defaultValue = "995")
    private String port;

    @Parameter
    @Optional(defaultValue = "INBOX")
    private String folder;

    @Override
    public RetrieverConnection connect(RetrieverConfiguration config) throws ConnectionException
    {
        return new RetrieverConnection(PROTOCOL_IMAP, user, password, setting.getHost(), port, setting.getProperties(), null, folder);
    }

    @Override
    public void disconnect(RetrieverConnection connection)
    {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(RetrieverConnection connection)
    {
        return ConnectionValidationResult.success();
    }

    @Override
    public ConnectionHandlingStrategy<RetrieverConnection> getHandlingStrategy(ConnectionHandlingStrategyFactory<RetrieverConfiguration, RetrieverConnection> connectionHandlingStrategyFactory)
    {
        return connectionHandlingStrategyFactory.supportsPooling();
    }
}
