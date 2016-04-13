package esdemo

/**
 * An aggregate is somewhat of a primary key in an Event Sourcing System
 *
 * Patient Aggregate represents a complete or partial patient.
 *
 * @author Rahul Somasunderam
 */
//tag::main[]
class PatientAggregate {
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