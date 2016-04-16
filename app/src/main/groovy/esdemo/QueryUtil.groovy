package esdemo

import groovy.transform.CompileStatic

@CompileStatic
trait QueryUtil<T, A extends Aggregate, E extends Event<A, T>, S extends Snapshot<A>> {

}
