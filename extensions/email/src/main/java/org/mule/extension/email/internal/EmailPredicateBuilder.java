/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.internal;

import org.mule.runtime.extension.api.annotation.Alias;


@Alias("matcher")
public class EmailPredicateBuilder
{

    ///**
    // *
    // */
    //@Parameter
    //private String subjectPattern;
    //
    ///**
    // *
    // */
    //@Parameter
    //private List<EmailAddress> toAddresses;
    //
    ///**
    // *
    // */
    //@Parameter
    //private EmailAddress fromAddress;
    //
    ///**
    // *
    // */
    //@Parameter
    //private LocalDateTime receivedSince;
    //
    ///**
    // *
    // */
    //@Parameter
    //private LocalDateTime receivedUntil;
    //
    ///**
    // *
    // */
    //@Parameter
    //private Boolean read;
    //
    ///**
    // *
    // */
    //@Parameter
    //private Boolean deleted;
    //
    ///**
    // *
    // */
    //@Parameter
    //private Boolean recent;
    //
    ///**
    // *
    // */
    //@Parameter
    //private Boolean answered;
    //
    ///**
    // * Builds a {@link Predicate} from the criterias in {@code this} builder's state.
    // *
    // * @return a {@link Predicate}
    // */
    //public Predicate<EmailAttributes> build()
    //{
    //    Predicate<EmailAttributes> predicate = attributes -> true;
    //    if (subjectPattern != null)
    //    {
    //        predicate = predicate.and(attributes -> Pattern.compile(subjectPattern).asPredicate().test(attributes.getSubject()));
    //    }
    //
    //    if (toAddresses != null)
    //    {
    //        predicate = predicate.and(attributes -> attributes.getToAddresses().stream().allMatch(c -> toAddresses.contains(c)));
    //    }
    //
    //    if (fromAddress != null)
    //    {
    //        predicate = predicate.and(attributes -> attributes.getFromAddresses().equals(fromAddress));
    //    }
    //
    //    if (receivedSince != null)
    //    {
    //        predicate = predicate.and(attributes -> receivedSince.compareTo(attributes.getReceivedDate()) >= 0);
    //    }
    //
    //    if (receivedUntil != null)
    //    {
    //        predicate = predicate.and(attributes -> receivedUntil.compareTo(attributes.getReceivedDate()) <= 0);
    //    }
    //
    //    //if (read != null)
    //    //{
    //    //    predicate = predicate.and(EmailAttributes::isSeen);
    //    //}
    //    //
    //    //if (deleted != null)
    //    //{
    //    //    predicate = predicate.and(EmailAttributes::isDeleted);
    //    //}
    //    //
    //    //if (recent != null)
    //    //{
    //    //    predicate = predicate.and(EmailAttributes::isRecent);
    //    //}
    //    //
    //    //if (answered != null)
    //    //{
    //    //    predicate = predicate.and(EmailAttributes::isAnswered);
    //    //}
    //
    //    return predicate;
    //}
}
