package esdemo

/**
 * An event in Event sourcing, is well, an event. It represents an atomic action that was performed on an aggregate.
 * Our version of events cares about when it was performed, and by who.
 *
 * This is a base class that will be extended by all events.
 *
 * @author Rahul Somasunderam
 */
abstract class PatientEvent {
    static belongsTo = [
            aggregate: PatientAggregate
    ]
    Date dateCreated
    String createdBy
    static constraints = {
    }
}

/**
 * Represents the creation of a patient.
 *
 * @author Rahul Somasunderam
 */
class PatientCreated extends PatientEvent {
    String name

    @Override
    String toString() { "${dateCreated}: ${createdBy} created $aggregate with name $name" }
}