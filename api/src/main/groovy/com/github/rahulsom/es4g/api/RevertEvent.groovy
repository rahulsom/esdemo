package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface RevertEvent<A extends AggregateType> extends BaseEvent<A> {
    BaseEvent<A> getEvent()
}
