package esdemo

import com.github.rahulsom.es4g.annotations.Event
import com.github.rahulsom.es4g.api.DeprecatedBy
import com.github.rahulsom.es4g.api.Deprecates
import com.github.rahulsom.es4g.api.BaseEvent
import com.github.rahulsom.es4g.api.RevertEvent
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
abstract class PatientEvent implements BaseEvent<PatientAggregate> {
    static belongsTo = [
            aggregate: PatientAggregate
    ]
//end::main[]
    /**
     * This says when the event was performed. It's a grails magic field and doesn't need to be populated manually
     */
//tag::date[]
    Date date
//end::date[]

    /**
     * This says who performed the event. It goes along with `dateCreated`, but needs to be specified.
     */
//tag::close[]
    String createdBy

    BaseEvent<PatientAggregate> revertedBy
    static transients = ['revertedBy']
}
//end::close[]

/**
 * Represents the creation of a patient.
 *
 * @author Rahul Somasunderam
 */
//tag::created[]
@Event(PatientAggregate)
class PatientCreated extends PatientEvent {
    String name

    @Override String toString() { "<$id> ${date}: ${createdBy} created $aggregate with name $name" }
    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}
//end::created[]

/**
 * Indicates the name changed for a patient
 */
@Event(PatientAggregate)
class PatientNameChanged extends PatientEvent {
    String name

    @Override String toString() { "<$id> ${date}: ${createdBy} changed name on $aggregate to ${name}" }
    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
@Event(PatientAggregate)
class PatientProcedurePerformed extends PatientEvent {
    String code

    @Override String toString() { "<$id> ${date}: ${createdBy} performed ${code} on $aggregate" }
    @Override String getAudit() { new JsonBuilder([code: code]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
@Event(PatientAggregate)
class PatientProcedurePlanned extends PatientEvent {
    String code

    @Override String toString() { "<$id> ${date}: ${createdBy} planned ${code} for $aggregate" }
    @Override String getAudit() { new JsonBuilder([code: code]).toString() }
}

/**
 * Indicates the name changed for a patient
 */
@Event(PatientAggregate)
class PatientDeleted extends PatientEvent {
    String reason

    @Override String toString() { "<$id> ${date}: ${createdBy} deleted $aggregate for reason '$reason'" }
    @Override String getAudit() { new JsonBuilder([reason: reason]).toString() }
}

/**
 * Indicates an event has been reverted
 */
// @Event(PatientAggregate)
class PatientEventReverted extends PatientEvent implements RevertEvent<PatientAggregate> {
    PatientEvent revertedEvent

    @Override String toString() { "<$id> ${date}: ${createdBy} reverted [$revertedEvent]" }
    @Override String getAudit() { new JsonBuilder([eventId: revertedEvent.id, id: id]).toString() }
}

/**
 * Indicates current patient deprecates another patient
 */
// @Event(PatientAggregate)
class PatientDeprecates extends PatientEvent implements Deprecates<PatientAggregate> {
    PatientAggregate deprecated

    @Override String toString() { "<$id> ${date}: ${createdBy} merged [$deprecated] into this." }
    @Override String getAudit() { new JsonBuilder([deprecated: deprecated.toString()]).toString() }

    static hasOne = [
            converse: PatientDeprecatedBy
    ]
}

/**
 * Indicates current patient is deprecated by new Patient
 */
// @Event(PatientAggregate)
class PatientDeprecatedBy extends PatientEvent implements DeprecatedBy<PatientAggregate> {
    PatientAggregate deprecator

    @Override String toString() { "<$id> ${date}: ${createdBy} merged into [$deprecator]" }
    @Override String getAudit() { new JsonBuilder([deprecator: deprecator.toString()]).toString() }

    static belongsTo = [
            converse: PatientDeprecates
    ]
}
