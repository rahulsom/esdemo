package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.validation.ValidationException
import spock.lang.Specification

@Integration
@Rollback
class PatientCommandUtilSpec extends Specification {

    def "createPatient for a new patient works"() {
        when: "I create a patient"
        def p1 = UserUtil.As('rahul') { PatientCommandUtil.createPatient('123', '1.2.3.4', 'john') }

        then: "It gets saved"
        p1
    }

    def "createPatient for a duplicate patient fails"() {
        when: "I create a patient"
        def p1 = UserUtil.As('rahul') { PatientCommandUtil.createPatient('123', '1.2.3.4', 'john') }
        def p2 = UserUtil.As('john') { PatientCommandUtil.createPatient('123', '1.2.3.4', 'rahul') }

        then: "It gets saved"
        p1
        thrown ValidationException
        !p2
    }

}
