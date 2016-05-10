/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.email;

import static org.junit.runners.Parameterized.*;
import org.mule.runtime.core.api.MuleEvent;

import com.icegreen.greenmail.util.ServerSetup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EmailRetrieverTestCase extends EmailConnectorFunctionalTestCase
{

    public static final String RETRIEVE = "retrieve";

    @Parameter(value = 0)
    public String protocol;
    @Parameter(value = 1)
    public String configFile;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] {
                {"imap", "imap-receiver.xml"},
                {"pop3", "pop3-receiver.xml"}
        });
    }

    @Override
    protected String getConfigFile()
    {
        return configFile;
    }

    @Test
    public void testFetch() throws Exception
    {
        MuleEvent fetch = runFlow(RETRIEVE);
        List payload = (List) fetch.getMessage().getPayload();
        //assertThat(payload, instanceOf(ArrayList.class));
        //assertThat(payload.size(), is(4));

    }

    @Override
    public ServerSetup getServerSetup()
    {
        return new ServerSetup(DEFAULT_TEST_PORT, null, protocol);
    }


}
