package esdemo

import groovy.json.JsonBuilder

/**
 * A snapshot in Event Sourcing is the effective value of an aggregate at a point in time. This serves as a computed
 * value in most cases. However it can also serve as an optimization that reduce the number of reads from a database
 * if it gets persisted.
 *
 * @author Rahul Somasunderam
 */
class PatientSnapshot {

    static belongsTo = [
            aggregate: PatientAggregate
    ]

    Long lastEvent = 0L
    String name

    public static final ArrayList<String> HIDDEN_FIELDS = [
            'class', 'id', 'aggregate', 'aggregateId'
    ]

    @Override
    String toString() {
        new JsonBuilder(this.properties.findAll { !HIDDEN_FIELDS.contains(it.key) })
    }
}
