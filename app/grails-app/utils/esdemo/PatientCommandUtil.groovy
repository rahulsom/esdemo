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
                dateCreated: Util.time).save(failOnError: true)
        aggregate
    }

    static PatientNameChanged changeName(PatientAggregate self, String name) {
        new PatientNameChanged(aggregate: self, createdBy: Util.user, name: name, dateCreated: Util.time).save(failOnError: true)
    }
    //end::begin[]

    static PatientEventReverted revertEvent(PatientEvent event) {
        /*
         * PatientDeprecatedBy and PatientDeprecates always work as a pair.
         */
        if (event instanceof PatientDeprecatedBy) {
            new PatientEventReverted(aggregate: event.converse.aggregate, createdBy: Util.user, revertedEvent: event.converse,
                    dateCreated: Util.time).save(failOnError: true)
        }
        if (event instanceof PatientDeprecates) {
            new PatientEventReverted(aggregate: event.converse.aggregate, createdBy: Util.user, revertedEvent: event.converse,
                    dateCreated: Util.time).save(failOnError: true)
        }
        new PatientEventReverted(aggregate: event.aggregate, createdBy: Util.user, revertedEvent: event, dateCreated: Util.time).
                save(failOnError: true)
    }

    static PatientProcedurePerformed performProcedure(PatientAggregate self, String code) {
        new PatientProcedurePerformed(aggregate: self, createdBy: Util.user, code: code, dateCreated: Util.time).save(failOnError: true)
    }

    static PatientProcedurePlanned planProcedure(PatientAggregate self, String code) {
        new PatientProcedurePlanned(aggregate: self, createdBy: Util.user, code: code, dateCreated: Util.time).save(failOnError: true)
    }

    static PatientDeleted delete(PatientAggregate self, String reason) {
        new PatientDeleted(aggregate: self, createdBy: Util.user, reason: reason, dateCreated: Util.time).save(failOnError: true)
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    static PatientDeprecatedBy merge(PatientAggregate self, PatientAggregate into) {
        def e1 = new PatientDeprecatedBy(aggregate: self, createdBy: Util.user, deprecator: into, dateCreated: Util.time)
        def e2 = new PatientDeprecates(aggregate: into, createdBy: Util.user, deprecated: self, dateCreated: Util.time, converse: e1)
        e1.converse = e2
        e2.save(flush: true, failOnError: true)
        e2.converse
    }
//tag::close[]
}
//end::close[]
