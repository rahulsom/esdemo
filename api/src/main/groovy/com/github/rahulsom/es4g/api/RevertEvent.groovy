package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface RevertEvent<A extends Aggregate> extends Event<A> {
    Event<A> getEvent()
}
