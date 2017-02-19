package com.github.rahulsom.es4g.api

/**
 * Marks a class as a snapshot
 * @param <A> The Aggregate this snapshot works over
 *
 * @author Rahul Somasunderam
 */
interface Snapshot<A extends AggregateType> {
    A getAggregate()

    void setAggregate(A aggregate)

    A getDeprecatedBy()

    void setDeprecatedBy(A aggregate)

    Long getLastEvent()

    void setLastEvent(Long id)

    Set<A> getDeprecates()
}
