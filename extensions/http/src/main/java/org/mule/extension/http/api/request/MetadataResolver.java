/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.impl.DefaultUnionType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.message.NullPayload;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeyBuilder;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.MetadataKeysResolver;
import org.mule.runtime.api.metadata.resolving.MetadataOutputResolver;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.introspection.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.module.http.internal.ParameterMap;

import com.google.common.collect.Lists;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetadataResolver implements Initialisable, MetadataKeysResolver, MetadataOutputResolver<String>
{
    //TODO: Refactor this whole mess.

    private static final ClassTypeLoader  TYPE_LOADER = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
    private static final String ANY = "ANY";
    private Map<MetadataKey, MetadataType> types = new HashMap<>();
    private Class[] classes = new Class[]{InputStream.class, NullPayload.class, ParameterMap.class};

    @Override
    public List<MetadataKey> getMetadataKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException
    {
        List<MetadataKey> keyList = new LinkedList<>();
        keyList.addAll(types.keySet());
        keyList.add(MetadataKeyBuilder.newKey(ANY).build());
        return keyList;
    }

    @Override
    public MetadataType getOutputMetadata(MetadataContext context, String key) throws MetadataResolvingException, ConnectionException
    {
        if (ANY.equals(key))
        {
            return new DefaultUnionType(Lists.newLinkedList(types.values()), JAVA, Collections.EMPTY_LIST);
        }
        return types.get(key);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        //UnionTypeBuilder builder = new BaseTypeBuilder<>(JAVA).unionType().of(TYPE_LOADER.load(InputStream.class));
        //builder.of().nullType();
        //builder.of(TYPE_LOADER.load(InputStream.class))
        Arrays.stream(classes).map(aClass -> types.put(MetadataKeyBuilder.newKey(aClass.getSimpleName()).build(), TYPE_LOADER.load(aClass)));
    }

}
