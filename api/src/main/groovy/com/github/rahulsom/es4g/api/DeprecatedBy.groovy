package com.github.rahulsom.es4g.api

/**
 * Created by rahul on 2/11/17.
 */
interface DeprecatedBy<A extends Aggregate> extends Event<A> {
    Deprecates<A> getConverse()

    A getDeprecator()
}
