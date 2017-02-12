package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface Snapshot<A extends Aggregate> {
    Long getId()

    A getAggregate()

    void setAggregate(A aggregate)

    A getDeprecatedBy()

    void setDeprecatedBy(A aggregate)

    Long getLastEvent()

    void setLastEvent(Long id)

    Set<A> getDeprecates()
}
