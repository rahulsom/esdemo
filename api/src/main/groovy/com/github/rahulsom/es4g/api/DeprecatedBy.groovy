package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface DeprecatedBy<A extends AggregateType> extends BaseEvent<A> {
    Deprecates<A> getConverse()

    A getDeprecator()
}
