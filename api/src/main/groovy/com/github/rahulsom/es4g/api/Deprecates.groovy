package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface Deprecates<A extends AggregateType> extends BaseEvent<A> {
    DeprecatedBy<A> getConverse()

    A getDeprecated()
}
