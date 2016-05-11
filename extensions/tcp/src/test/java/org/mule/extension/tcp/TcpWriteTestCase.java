/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.tcp;

import org.junit.Test;

public class TcpWriteTestCase extends TcpConnectorTestCase
{
    @Override
    protected String getConfigFile()
    {
        return "tcp-write-config.xml";
    }

    @Test
    public void write() throws Exception
    {
        String payload = (String) flowRunner("write").run().getMessage().getPayload();
    }
}
