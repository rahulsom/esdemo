package esdemo

/**
 * An aggregate is somewhat of a primary key in an Event Sourcing System
 *
 * Patient Aggregate represents a complete or partial patient.
 *
 * @author Rahul Somasunderam
 */
class PatientAggregate {
    String identifier
    String authority

    static constraints = {
        identifier unique: ['authority']
    }

    @Override
    String toString() { "PatientAggregate#${id} - $identifier@$authority" }
}
