package esdemo

import groovy.json.JsonBuilder

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

    /**
     * This says when the event was performed. It's a grails magic field and doesn't need to be populated manually
     */
    Date dateCreated

    /**
     * This says who performed the event. It goes along with `dateCreated`, but needs to be specified.
     */
    String createdBy

    static constraints = {
    }

    Long revertedBy
    static transients = ['revertedBy']

    abstract String getAudit()
}

/**
 * Represents the creation of a patient.
 *
 * @author Rahul Somasunderam
 */
class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} created $aggregate with name $name" }
    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
class PatientNameChanged extends PatientEvent {
    String name

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} changed name on $aggregate to ${name}" }
    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
class PatientProcedurePerformed extends PatientEvent {
    String code

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} performed ${code} on $aggregate" }
    @Override String getAudit() { new JsonBuilder([code: code]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
class PatientProcedurePlanned extends PatientEvent {
    String code

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} planned ${code} for $aggregate" }
    @Override String getAudit() { new JsonBuilder([code: code]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
class PatientDeleted extends PatientEvent {
    String reason

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} deleted $aggregate for reason '$reason'" }
    @Override String getAudit() { new JsonBuilder([reason: reason]).toString() }
}

/**
 * Indicates an event has been reverted
 */
class PatientEventReverted extends PatientEvent {
    PatientEvent event

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} reverted [$event]" }
    @Override String getAudit() { new JsonBuilder([eventId: event.id]).toString() }
}
