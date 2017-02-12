package esdemo

import com.github.rahulsom.es4g.annotations.Aggregate

/**
 * An aggregate is somewhat of a primary key in an Event Sourcing System
 *
 * Patient Aggregate represents a complete or partial patient.
 *
 * @author Rahul Somasunderam
 */
//tag::main[]
@Aggregate
class PatientAggregate implements com.github.rahulsom.es4g.api.Aggregate {
    String identifier
    String authority

    static constraints = {
        identifier unique: ['authority']
    }
//end::main[]
    @Override
    String toString() { "PatientAggregate#${id} - $identifier@$authority" }
//tag::close[]
}
//end::close[]