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
//tag::main[]
abstract class PatientEvent implements Event<PatientAggregate> {
    static belongsTo = [
            aggregate: PatientAggregate
    ]
//end::main[]
    /**
     * This says when the event was performed. It's a grails magic field and doesn't need to be populated manually
     */
//tag::date[]
    Date dateCreated
//end::date[]

    /**
     * This says who performed the event. It goes along with `dateCreated`, but needs to be specified.
     */
//tag::close[]
    String createdBy

    Long revertedBy
    static transients = ['revertedBy']
}
//end::close[]

/**
 * Represents the creation of a patient.
 *
 * @author Rahul Somasunderam
 */
//tag::created[]
class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} created $aggregate with name $name" }
    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}
//end::created[]

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
class PatientEventReverted extends PatientEvent implements RevertEvent<PatientAggregate> {
    PatientEvent event

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} reverted [$event]" }
    @Override String getAudit() { new JsonBuilder([eventId: event.id, id: id]).toString() }
}

/**
 * Indicates current patient deprecates another patient
 */
class PatientDeprecates extends PatientEvent implements Deprecates<PatientAggregate> {
    PatientAggregate deprecated

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} merged [$deprecated] into this." }
    @Override String getAudit() { new JsonBuilder([deprecated: deprecated.toString()]).toString() }

    static hasOne = [
            converse: PatientDeprecatedBy
    ]
}

/**
 * Indicates current patient is deprecated by new Patient
 */
class PatientDeprecatedBy extends PatientEvent implements DeprecatedBy<PatientAggregate> {
    PatientAggregate deprecator

    @Override String toString() { "<$id> ${dateCreated}: ${createdBy} merged into [$deprecator]" }
    @Override String getAudit() { new JsonBuilder([deprecator: deprecator.toString()]).toString() }

    static belongsTo = [
            converse: PatientDeprecates
    ]
}
