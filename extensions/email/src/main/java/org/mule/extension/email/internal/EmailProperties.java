/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal;

import java.util.Map;
import java.util.Properties;

public class EmailProperties
{

    public static final String PROTOCOL_SMTP = "smtp";
    public static final String PROTOCOL_SMTPS = "smtps";
    public static final String PROTOCOL_POP3 = "pop3";
    public static final String PROTOCOL_POP3S = "pop3s";
    public static final String PROTOCOL_IMAP = "imap";
    public static final String PROTOCOL_IMAPS = "imaps";

    private static final String HOST_PROPERTY = "mail.%s.host";
    private static final String PORT_PROPERTY = "mail.%s.port";
    private static final String CONNECTION_TIMEOUT_PROPERTY = "mail.%s.connectiontimeout";
    private static final String TIMEOUT_PROPERTY = "mail.%s.timeout";
    private static final String WRITE_TIMEOUT_PROPERTY = "mail.%s.writetimeout";
    private static final String LOCAL_ADDRESS_PROPERTY = "mail.%s.localaddress";
    private static final String START_TLS_PROPERTY = "mail.%s.starttls.enable";
    private static final String SOCKET_FACTORY_CLASS_PROPERTY = "mail.%s.socketFactory.class";
    private static final String SOCKET_FACTORY_FALLBACK_PROPERTY = "mail.%s.socketFactory.fallback";

    private static final String TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String TRANSPORT_PROTOCOL_RFC822 = "mail.transport.protocol.rfc822";


    /**
     * Default socket read timeout. See JavaMail session properties.
     */
    private static final long READ_TIMEOUT = 15000L;
    /**
     * Default socket connection timeout. See JavaMail session properties.
     */
    private static final long CONNECTION_TIMEOUT = 15000L;

    private final Properties props;
    private final String protocol;

    private EmailProperties(String protocol, String host, String port, long connectionTimeout, long readTimeout, long writeTimeout)
    {
        this.props = new Properties();
        this.protocol = protocol;

        //if (debug) {
        //    props.setProperty("mail.debug", "true");
        //}

        //// Set local host address (makes tests much faster. If this is not set java mail always looks for the address)
        //set(LOCAL_ADDRESS_PROPERTY, host);
        set(PORT_PROPERTY, port);
        set(HOST_PROPERTY, host);

        if (isSecure())
        {
            set(START_TLS_PROPERTY, "true");
            set(SOCKET_FACTORY_FALLBACK_PROPERTY, "false");

            //props.setProperty("mail." + protocol + ".socketFactory.class", DummySSLSocketFactory.class.getName());
            //set(SOCKET_FACTORY_CLASS_PROPERTY, "className");
        }

        set(CONNECTION_TIMEOUT_PROPERTY, Long.toString(connectionTimeout < 0L ? CONNECTION_TIMEOUT : readTimeout));
        set(TIMEOUT_PROPERTY, Long.toString(readTimeout < 0L ? READ_TIMEOUT : readTimeout));

        // Note: "mail." + protocol + ".writetimeout" breaks TLS/SSL Dummy Socket and makes tests run 6x slower!!!
        if (writeTimeout > 0L)
        {
            set(WRITE_TIMEOUT_PROPERTY, Long.toString(writeTimeout));
        }

        if (isSMTP())
        {
            set(TRANSPORT_PROTOCOL, protocol);
            set(TRANSPORT_PROTOCOL_RFC822, protocol);
        }
    }

    private boolean isSMTP()
    {
        return protocol.startsWith(PROTOCOL_SMTP);
    }

    private boolean isSecure()
    {
        return protocol.endsWith("s");
    }

    private void set(String prop, String value)
    {
        props.setProperty(String.format(prop, protocol), value);
    }

    public static Properties get(String protocol, String host, String port, Map<String, String> properties)
    {
        return get(protocol, host, port, CONNECTION_TIMEOUT, READ_TIMEOUT, 0L, properties);
    }

    public static Properties get(String protocol, String host, String port, long connectionTimeout, long readTimeout, long writeTimeout, Map<String, String> properties)
    {
        Properties emailProps = new EmailProperties(protocol, host, port, connectionTimeout, readTimeout, writeTimeout).props;
        if (properties != null && !properties.isEmpty())
        {
            emailProps.putAll(properties);
        }
        return emailProps;
    }
}
