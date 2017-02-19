package com.github.rahulsom.es4g.api

/**
 * Revert a prior event. A reverted event's effects are not applied.
 *
 * @author Rahul Somasunderam
 */
interface RevertEvent<A extends AggregateType> extends BaseEvent<A> {
    BaseEvent<A> getRevertedEvent()
}
