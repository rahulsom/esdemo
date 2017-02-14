package com.github.rahulsom.es4g.api

/**
 * Base class for Events
 *
 * @param <A> Aggregate this event applies to
 *
 * @author Rahul Somasunderam
 */
interface BaseEvent<A extends AggregateType> {
    A getAggregate()

    abstract String getAudit()

    Date getDateCreated()

    String getCreatedBy()

    Long getRevertedBy()

    void setRevertedBy(Long revertEventId)

    Long getId()
}



