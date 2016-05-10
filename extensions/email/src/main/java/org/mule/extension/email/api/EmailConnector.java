/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api;


import org.mule.extension.email.api.retriever.RetrieverConfiguration;
import org.mule.extension.email.api.sender.SenderConfiguration;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;

@Configurations({SenderConfiguration.class, RetrieverConfiguration.class})
@Extension(name = "email")
public class EmailConnector
{

}
