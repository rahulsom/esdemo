package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.validation.ValidationException
import spock.lang.Specification

import static esdemo.PatientCommandUtil.changeName
import static esdemo.PatientCommandUtil.createPatient
import static esdemo.UserUtil.As

@Integration
@Rollback
class PatientCommandUtilSpec extends Specification {

    def "createPatient for a new patient works"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }

        then: "It gets saved"
        p1
        PatientEvent.count() == 1 + old(PatientEvent.count())
    }

    def "createPatient for a duplicate patient fails"() {
        when: "I create 2 patients with same aggregate"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        def p2 = As('john') { createPatient '123', '1.2.3.4', 'rahul' }

        then: "First one gets saved"
        p1

        and: "The other one throws a validation exception"
        thrown ValidationException
        PatientEvent.count() == 1 + old(PatientEvent.count())
        !p2
    }

    def "change name of patient works"() {
        when: "I create a patient and change its name"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('john') { changeName p1, 'mike' }

        then: "It gets saved"
        p1
        PatientEvent.count() == 2 + old(PatientEvent.count())

    }

}
