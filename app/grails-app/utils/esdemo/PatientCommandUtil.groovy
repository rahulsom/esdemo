package esdemo

import grails.compiler.GrailsCompileStatic
import groovy.util.logging.Slf4j

/**
 * Provides commands that can be executed
 *
 * @author Rahul Somasunderam
 */
@GrailsCompileStatic
@Slf4j
//tag::begin[]
class PatientCommandUtil {

    static PatientAggregate createPatient(String identifier, String authority, String name) {
        def aggregate = new PatientAggregate(identifier: identifier, authority: authority).save(failOnError: true)
        new PatientCreated(aggregate: aggregate, createdBy: Util.user, name: name,
                date: Util.time, position: 1).save(failOnError: true)
        aggregate
    }

    static PatientNameChanged changeName(PatientAggregate self, String name, Long position = null) {
        if (position == null) {
            position = nextPosition(self)
        }
        new PatientNameChanged(aggregate: self, createdBy: Util.user, name: name, date: Util.time, position: position).save(failOnError: true)
    }

    //end::begin[]

    private static long nextPosition(PatientAggregate self) {
        def events = PatientEvent.findAllByAggregate(self, [sort: 'position', order: 'desc', offset: 0, max: 1]) as List<PatientEvent>
        events[0].position + 1
    }

    static PatientEventReverted revertEvent(PatientEvent event) {
        /*
         * PatientDeprecatedBy and PatientDeprecates always work as a pair.
         */
        if (event instanceof PatientDeprecatedBy) {
            new PatientEventReverted(aggregate: event.converse.aggregate, createdBy: Util.user, revertedEvent: event.converse,
                    date: Util.time, position: nextPosition(event.converse.aggregate)).save(failOnError: true)
        }
        if (event instanceof PatientDeprecates) {
            new PatientEventReverted(aggregate: event.converse.aggregate, createdBy: Util.user, revertedEvent: event.converse,
                    date: Util.time, position: nextPosition(event.converse.aggregate)).save(failOnError: true)
        }
        new PatientEventReverted(aggregate: event.aggregate, createdBy: Util.user, revertedEvent: event, date: Util.time, position: nextPosition(event.aggregate)).
                save(failOnError: true)
    }

    static PatientProcedurePerformed performProcedure(PatientAggregate self, String code, Long position = null) {
        if (position == null) {
            position = nextPosition(self)
        }
        new PatientProcedurePerformed(aggregate: self, createdBy: Util.user, code: code, date: Util.time, position: position).save(failOnError: true)
    }

    static PatientProcedurePlanned planProcedure(PatientAggregate self, String code, Long position = null) {
        if (position == null) {
            position = nextPosition(self)
        }
        new PatientProcedurePlanned(aggregate: self, createdBy: Util.user, code: code, date: Util.time, position: position).save(failOnError: true)
    }

    static PatientDeleted delete(PatientAggregate self, String reason, Long position = null) {
        if (position == null) {
            position = nextPosition(self)
        }
        new PatientDeleted(aggregate: self, createdBy: Util.user, reason: reason, date: Util.time, position: position).save(failOnError: true)
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    static PatientDeprecatedBy merge(PatientAggregate self, PatientAggregate into) {
        def e1 = new PatientDeprecatedBy(aggregate: self, createdBy: Util.user, deprecator: into, date: Util.time, position: nextPosition(self))
        def e2 = new PatientDeprecates(aggregate: into, createdBy: Util.user, deprecated: self, date: Util.time, converse: e1, position: nextPosition(into))
        e1.converse = e2
        e2.save(flush: true, failOnError: true)
        e2.converse
    }
//tag::close[]
}
//end::close[]
