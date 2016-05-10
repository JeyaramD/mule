/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.sender;

import static org.mule.extension.email.internal.EmailProperties.PROTOCOL_SMTP;
import org.mule.extension.email.api.EmailConnectionSetting;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class SMTPConnectionProvider implements ConnectionProvider<SenderConfiguration, SenderConnection>
{

    @ParameterGroup
    private EmailConnectionSetting setting;

    @Parameter
    @Optional(defaultValue = "600")
    private String port;

    @Override
    public SenderConnection connect(SenderConfiguration config) throws ConnectionException
    {
        return new SenderConnection(PROTOCOL_SMTP, setting.getHost(), port, setting.getProperties());
    }

    @Override
    public void disconnect(SenderConnection connection)
    {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(SenderConnection connection)
    {
        return connection.isConnected();
    }

    @Override
    public ConnectionHandlingStrategy<SenderConnection> getHandlingStrategy(ConnectionHandlingStrategyFactory<SenderConfiguration, SenderConnection> handlingStrategyFactory)
    {
        return handlingStrategyFactory.supportsPooling();
    }
}
