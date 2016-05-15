/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.mule.extension.tcp.api.client.TcpListenerClient;
import org.mule.extension.tcp.api.client.TcpRequesterClient;
import org.mule.extension.tcp.api.config.TcpListenerConfig;
import org.mule.extension.tcp.api.config.TcpRequesterConfig;
import org.mule.extension.tcp.api.source.TcpAttributes;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.message.MuleMessage;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.core.DefaultMuleMessage;
import org.mule.runtime.core.el.context.MessageContext;
import org.mule.runtime.core.util.IOUtils;
import org.mule.runtime.core.util.ValueHolder;
import org.mule.tck.probe.JUnitLambdaProbe;
import org.mule.tck.probe.PollingProber;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

public class TcpWriteTestCase extends TcpConnectorTestCase
{

    private static final int TIMEOUT_MILLIS = 5000;
    private static final int POLL_DELAY_MILLIS = 100;
    public static final String TEST_STRING = "test string";
    private static String HOST = "localhost";
    private static int SOURCE_PORT = 8005;
    private static int LISTENING_PORT = 8006;
    private static List<MuleMessage<?, TcpAttributes>> receivedMessages;

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception
    {
        super.doSetUpBeforeMuleContextCreation();
        receivedMessages = new CopyOnWriteArrayList<>();
    }

    @Override
    protected String getConfigFile()
    {
        return "tcp-write-config.xml";
    }


    @Test
    public void write() throws Exception
    {
        TcpListenerConfig config = new TcpListenerConfig();
        TcpListenerClient client = new TcpListenerClient(config, HOST, LISTENING_PORT);

        Thread thread = new Thread(){
            public void run(){
                try
                {
                    Socket incomingConnection = client.connect();
                    String message = IOUtils.toString(incomingConnection.getInputStream());
                    assertEquals(message, TEST_STRING);
                    incomingConnection.close();
                    client.disconnect();
                }
                catch (ConnectionException e)
                {
                    fail(e.getMessage());
                }
                catch (IOException e)
                {
                    fail(e.getMessage());
                }
            }
        };

        thread.start();

        flowRunner("write").withPayload(TEST_STRING).run();
    }

    @Test
    public void listen() throws Exception
    {
        TcpRequesterConfig config = new TcpRequesterConfig();
        TcpRequesterClient client = new TcpRequesterClient(config, HOST, SOURCE_PORT);
        client.connect();
        client.write(TEST_STRING);
        client.disconnect();
        assertEvent(receiveConnection(), TEST_STRING);
    }

    public static void onIncomingConnection(MessageContext messageContext)
    {
        MuleMessage message = new DefaultMuleMessage(messageContext.getPayload(), (DataType<Object>) messageContext.getDataType(), messageContext.getAttributes());
        receivedMessages.add(message);
    }

    private void assertEvent(MuleMessage<?, TcpAttributes> message, Object expectedContent) throws Exception
    {
        //Object payload = message.getPayload();
        //if (payload instanceof InputStream)
        //{
        //    payload = IOUtils.toString((InputStream) payload);
        //}
        String payload = new String((byte[]) message.getPayload());
        //assertThat(payload, equalTo(expectedContent));
        assertEquals(payload, expectedContent);
    }

    private MuleMessage<?, TcpAttributes> receiveConnection()
    {
        PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
        ValueHolder<MuleMessage<?, TcpAttributes>> messageHolder = new ValueHolder<>();
        prober.check(new JUnitLambdaProbe(() -> {
            for (MuleMessage<?, TcpAttributes> message : receivedMessages)
            {
                TcpAttributes attributes = message.getAttributes();
                messageHolder.set(message);
                return true;
            }

            return false;
        }));

        return messageHolder.get();
    }
}
