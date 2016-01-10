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
        new PatientAggregate(identifier: identifier, authority: authority).save(flush: true, failOnError: true).with {
            new PatientCreated(aggregate: it, name: name, createdBy: UserUtil.user).save(flush: true, failOnError: true)
            it
        }
    }

}
