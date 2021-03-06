/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.routing;

import org.mule.VoidMuleEvent;
import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleRuntimeException;
import org.mule.api.config.MuleProperties;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.construct.FlowConstructAware;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.registry.RegistrationException;
import org.mule.api.routing.Aggregator;
import org.mule.api.routing.MessageInfoMapping;
import org.mule.api.service.Service;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.store.ObjectStoreManager;
import org.mule.api.store.PartitionableObjectStore;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.routing.correlation.EventCorrelator;
import org.mule.routing.correlation.EventCorrelatorCallback;
import org.mule.util.concurrent.ThreadNameHelper;
import org.mule.util.store.DefaultObjectStoreFactoryBean;
import org.mule.util.store.ProvidedObjectStoreWrapper;
import org.mule.util.store.ProvidedPartitionableObjectStoreWrapper;

import org.apache.commons.collections.Factory;

/**
 * <code>AbstractEventAggregator</code> will aggregate a set of messages into a
 * single message. <b>EIP Reference:</b> <a
 * href="http://www.eaipatterns.com/Aggregator.html"
 * >http://www.eaipatterns.com/Aggregator.html</a>
 */

public abstract class AbstractAggregator extends AbstractInterceptingMessageProcessor
    implements Initialisable, MuleContextAware, FlowConstructAware, Aggregator, Startable, Stoppable, Disposable
{

    public static final int MAX_PROCESSED_GROUPS = 50000;
    public static final String EVENTS_STORE_REGISTRY_KEY_PREFIX = "aggregator.eventsObjectStore.";

    protected EventCorrelator eventCorrelator;
    protected MuleContext muleContext;
    protected FlowConstruct flowConstruct;
    protected MessageInfoMapping messageInfoMapping;

    private long timeout = 0;
    private boolean failOnTimeout = true;

    private ObjectStore<Long> processedGroupsObjectStore;
    private PartitionableObjectStore eventGroupsObjectStore;

    protected boolean persistentStores;
    protected String storePrefix = null;

    protected String eventsObjectStoreKey;

    @Override
    public void initialise() throws InitialisationException
    {
        if (messageInfoMapping == null)
        {
            messageInfoMapping = flowConstruct.getMessageInfoMapping();
        }
        if (storePrefix == null)
        {
            storePrefix = String.format("%s%s.%s.", ThreadNameHelper.getPrefix(muleContext),
                flowConstruct.getName(), this.getClass().getName());
        }

        initProcessedGroupsObjectStore();
        initEventGroupsObjectStore();

        eventCorrelator = new EventCorrelator(getCorrelatorCallback(muleContext), next, messageInfoMapping,
                muleContext, flowConstruct, eventGroupsObjectStore, storePrefix, processedGroupsObjectStore);

        // Inherit failOnTimeout from async-reply if this aggregator is being used
        // for async-reply
        if (flowConstruct instanceof Service)
        {
            Service service = (Service) flowConstruct;
            if (service.getAsyncReplyMessageSource().getMessageProcessors().contains(this))
            {
                failOnTimeout = service.getAsyncReplyMessageSource().isFailOnTimeout();
            }
        }

        eventCorrelator.setTimeout(timeout);
        eventCorrelator.setFailOnTimeout(isFailOnTimeout());
    }

    protected void initProcessedGroupsObjectStore()
    {
        if (processedGroupsObjectStore == null)
        {
            processedGroupsObjectStore = new ProvidedObjectStoreWrapper<>(null, internalProcessedGroupsObjectStoreFactory());
        }
    }

    protected Factory internalProcessedGroupsObjectStoreFactory()
    {
        return new Factory()
        {
            @Override
            public Object create()
            {
                ObjectStoreManager objectStoreManager = muleContext.getRegistry().get(MuleProperties.OBJECT_STORE_MANAGER);
                return objectStoreManager.getObjectStore(storePrefix + ".processedGroups", persistentStores, MAX_PROCESSED_GROUPS, -1, 1000);
            }
        };
    }

    protected void initEventGroupsObjectStore() throws InitialisationException
    {
        try
        {
            if (eventGroupsObjectStore == null)
            {
                eventGroupsObjectStore = new ProvidedPartitionableObjectStoreWrapper<>(null, internalEventsGroupsObjectStoreFactory());
            }
            eventGroupsObjectStore.open(storePrefix + ".expiredAndDispatchedGroups");
            eventGroupsObjectStore.open(storePrefix + ".eventGroups");
        }
        catch (MuleRuntimeException | ObjectStoreException e)
        {
            throw new InitialisationException(e, this);
        }
    }

    protected Factory internalEventsGroupsObjectStoreFactory()
    {
        return new Factory()
        {
            @Override
            public Object create()
            {
                try
                {
                    ObjectStore objectStore;
                    if (persistentStores)
                    {
                        objectStore = muleContext.getRegistry().lookupObject(DefaultObjectStoreFactoryBean.class).createDefaultPersistentObjectStore();
                    }
                    else
                    {
                        objectStore = muleContext.getRegistry().lookupObject(DefaultObjectStoreFactoryBean.class).createDefaultInMemoryObjectStore();
                    }
                    if (objectStore instanceof MuleContextAware)
                    {
                        ((MuleContextAware) objectStore).setMuleContext(muleContext);
                    }
                    return objectStore;
                }
                catch (RegistrationException e)
                {
                    throw new MuleRuntimeException(e);
                }
            }
        };
    }

    @Override
    public void start() throws MuleException
    {
        if (timeout != 0)
        {
            eventCorrelator.start();
        }
    }

    @Override
    public void stop() throws MuleException
    {
        eventCorrelator.stop();
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    protected abstract EventCorrelatorCallback getCorrelatorCallback(MuleContext muleContext);

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        MuleEvent result = eventCorrelator.process(event);
        if (result == null || VoidMuleEvent.getInstance().equals(result))
        {
            return result;
        }
        return processNext(result);
    }

    @Override
    public void expireAggregation(String groupId) throws MessagingException
    {
        eventCorrelator.forceGroupExpiry(groupId);
    }

    public long getTimeout()
    {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public boolean isFailOnTimeout()
    {
        return failOnTimeout;
    }

    @Override
    public void setFailOnTimeout(boolean failOnTimeout)
    {
        this.failOnTimeout = failOnTimeout;
    }

    @Override
    public void setFlowConstruct(FlowConstruct flowConstruct)
    {
        this.flowConstruct = flowConstruct;
    }

    public void setMessageInfoMapping(MessageInfoMapping messageInfoMapping)
    {
        this.messageInfoMapping = messageInfoMapping;
    }

    public void setProcessedGroupsObjectStore(ObjectStore<Long> processedGroupsObjectStore)
    {
        this.processedGroupsObjectStore = new ProvidedObjectStoreWrapper<>(processedGroupsObjectStore, internalProcessedGroupsObjectStoreFactory());
    }

    public void setEventGroupsObjectStore(PartitionableObjectStore<MuleEvent> eventGroupsObjectStore)
    {
        this.eventGroupsObjectStore = new ProvidedPartitionableObjectStoreWrapper<>(eventGroupsObjectStore, internalEventsGroupsObjectStoreFactory());
    }

    public boolean isPersistentStores()
    {
        return persistentStores;
    }

    public void setPersistentStores(boolean persistentStores)
    {
        this.persistentStores = persistentStores;
    }

    public String getStorePrefix()
    {
        return storePrefix;
    }

    public void setStorePrefix(String storePrefix)
    {
        this.storePrefix = storePrefix;
    }

    @Override
    public void dispose()
    {
        disposeIfDisposable(processedGroupsObjectStore);
        disposeIfDisposable(eventGroupsObjectStore);
        eventCorrelator.dispose();
    }


    private void disposeIfDisposable(ObjectStore objectStore)
    {
        if (objectStore != null && objectStore instanceof Disposable)
        {
            ((Disposable) objectStore).dispose();
        }
    }
}
