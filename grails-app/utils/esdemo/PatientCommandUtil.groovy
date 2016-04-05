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
class PatientCommandUtil {

    static PatientAggregate createPatient(String identifier, String authority, String name) {
        new PatientAggregate(identifier: identifier, authority: authority).save().with {
            new PatientCreated(aggregate: it, createdBy: Util.user, name: name, dateCreated: Util.time).save(failOnError: true)
            it
        }
    }

    static PatientNameChanged changeName(PatientAggregate self, String name) {
        new PatientNameChanged(aggregate: self, createdBy: Util.user, name: name, dateCreated: Util.time).save()
    }

    static PatientEventReverted revertEvent(PatientAggregate self, PatientEvent event) {
        new PatientEventReverted(aggregate: self, createdBy: Util.user, event: event).save()
    }

    static PatientProcedurePerformed performProcedure(PatientAggregate self, String code) {
        new PatientProcedurePerformed(aggregate: self, createdBy: Util.user, code: code).save()
    }

    static PatientProcedurePlanned planProcedure(PatientAggregate self, String code) {
        new PatientProcedurePlanned(aggregate: self, createdBy: Util.user, code: code).save()
    }

}
