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
            new PatientCreated(aggregate: it, createdBy: UserUtil.user, name: name).save(failOnError: true)
            it
        }
    }

    static void changeName(PatientAggregate self, String name) {
        new PatientNameChanged(aggregate: self, createdBy: UserUtil.user, name: name).save()
    }

}
