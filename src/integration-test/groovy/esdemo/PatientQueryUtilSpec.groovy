package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class PatientQueryUtilSpec extends Specification {

    def "Patient is created"() {
        when: "I create a patient"
        UserUtil.As('rahul') {
            PatientCommandUtil.createPatient('123', '1.2.3.4', 'john')
        }
        def s1 = PatientQueryUtil.findPatient('123', '1.2.3.4')

        then: "I see 0 plans and 0 acts and the correct name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
    }

}
