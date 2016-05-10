/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.email;

import com.icegreen.greenmail.util.ServerSetup;

public class POP3RetrieverTestCase extends EmailConnectorFunctionalTestCase
{

    @Override
    protected String getConfigFile()
    {
        return "pop3-receiver.xml";
    }

    @Override
    public ServerSetup getServerSetup()
    {
        return new ServerSetup(DEFAULT_TEST_PORT, null, "pop3");
    }


}
