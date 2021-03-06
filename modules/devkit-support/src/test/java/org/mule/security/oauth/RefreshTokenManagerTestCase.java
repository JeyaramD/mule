/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.security.oauth;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.util.store.InMemoryObjectStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.Closure;
import org.junit.Test;

public class RefreshTokenManagerTestCase extends AbstractMuleContextTestCase
{

    private static final String NAME = "connector";

    private final int TIMEOUT = 1000;
    private final String ACCESS_TOKEN_ID = "accessTokenId";

    private RefreshTokenManager refreshTokenManager;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        this.refreshTokenManager = muleContext.getRegistry().lookupObject(RefreshTokenManager.class);
    }

    @Test
    public void dontRefreshConcurrently() throws Exception
    {
        doDontRefreshConcurrently(new Closure()
        {
            @Override
            public void execute(Object input)
            {
                ((RefreshTokenManager) input).setMinRefreshIntervalInMillis(TIMEOUT);
            }
        });
    }

    @Test
    public void dontRefreshConcurrentlyConfigureStore() throws Exception
    {
        doDontRefreshConcurrently(new Closure()
        {
            @Override
            public void execute(Object input)
            {
                ((RefreshTokenManager) input).setRefreshedTokensStore(new InMemoryObjectStore<Boolean>());
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void configureStoreTwice() throws Exception
    {
        this.refreshTokenManager.setRefreshedTokensStore(new InMemoryObjectStore<Boolean>());
        this.refreshTokenManager.setRefreshedTokensStore(new InMemoryObjectStore<Boolean>());
    }

    @Test(expected = IllegalStateException.class)
    public void configureStoreAfterInit() throws Exception
    {
        refreshTokenManager.setMinRefreshIntervalInMillis(TIMEOUT);
        final OAuth2Adapter adapter = this.getAdapter();
        refreshTokenManager.refreshToken(adapter, ACCESS_TOKEN_ID);
        refreshTokenManager.setRefreshedTokensStore(new InMemoryObjectStore<Boolean>());
    }

    private void doDontRefreshConcurrently(Closure configureRefreshStoreColsure) throws Exception
    {
        final int THREAD_COUNT = 10;
        final AtomicInteger exceptionCount = new AtomicInteger(0);

        final OAuth2Adapter adapter = this.getAdapter();

        configureRefreshStoreColsure.execute(refreshTokenManager);

        final Runnable task = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    refreshTokenManager.refreshToken(adapter, ACCESS_TOKEN_ID);
                }
                catch (Exception e)
                {
                    exceptionCount.addAndGet(1);
                }
            }
        };

        List<Thread> threads = new ArrayList<Thread>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++)
        {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads)
        {
            thread.join();
        }

        assertEquals(0, exceptionCount.get());
        verify(adapter, times(1)).refreshAccessToken(ACCESS_TOKEN_ID);

        // make sure you can refresh again after timeout
        Thread.sleep(TIMEOUT);
        reset(adapter);
        this.refreshTokenManager.refreshToken(adapter, ACCESS_TOKEN_ID);

        verify(adapter, times(1)).refreshAccessToken(ACCESS_TOKEN_ID);
    }

    private OAuth2Adapter getAdapter()
    {
        OAuth2Adapter adapter = mock(OAuth2Adapter.class);
        when(adapter.getName()).thenReturn(NAME);
        return adapter;
    }

}
