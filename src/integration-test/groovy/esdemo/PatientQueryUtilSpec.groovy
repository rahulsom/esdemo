package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

import static esdemo.PatientCommandUtil.changeName
import static esdemo.PatientCommandUtil.createPatient
import static esdemo.PatientCommandUtil.revertEvent
import static esdemo.PatientQueryUtil.findPatient
import static Util.As

@Integration
@Rollback
class PatientQueryUtilSpec extends Specification {

    def "Patient is created"() {
        when: "I create a patient"
        As('rahul') {
            createPatient '123', '1.2.3.4', 'john'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
    }

    def "Name is changed"() {
        when: "I create a patient"
        As('rahul') {
            def p1 = createPatient '123', '1.2.3.4', 'john'
            changeName p1, 'mike'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'mike'
    }

    def "Name change is reverted"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        def e = As('rahul') { changeName p1, 'mike' }
        def e1 = As('rahul') { revertEvent p1, e }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
    }

}
