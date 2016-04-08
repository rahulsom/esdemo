package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.util.logging.Slf4j
import spock.lang.Specification

import static esdemo.PatientCommandUtil.changeName
import static esdemo.PatientCommandUtil.createPatient
import static esdemo.PatientCommandUtil.performProcedure
import static esdemo.PatientCommandUtil.planProcedure
import static esdemo.PatientCommandUtil.revertEvent
import static esdemo.PatientQueryUtil.findPatient
import static Util.As

@Integration
@Rollback
@Slf4j
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

    def "Plans can be seen"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            planProcedure p1, 'APPENDECTOMY'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 0
        s1.plannedProcedures.size() == 2
    }

    def "Redundant plans can NOT be seen"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            planProcedure p1, 'APPENDECTOMY'
            planProcedure p1, 'FLUSHOT'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 0
        s1.plannedProcedures.size() == 2
    }

    def "Plan is performed"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            performProcedure p1, 'FLUSHOT'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 1
        s1.plannedProcedures.size() == 0
    }

    def "Snapshots can be reused"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            performProcedure p1, 'FLUSHOT'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s1.save()
        As('mickey') {
            planProcedure p1, 'FLUSHOT'
        }
        s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 1
        s1.plannedProcedures.size() == 1
    }

    def "Snapshots work with reverts"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        def e1 = As('rahul') {
            planProcedure p1, 'FLUSHOT'
            performProcedure p1, 'FLUSHOT'
        }
        def s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s1.save()
        log.info "Snapshot 1 saved"
        As('mickey') {
            planProcedure p1, 'FLUSHOT'
        }
        def lastEvent = As('goofy') {
            revertEvent p1, e1
        }
        s1 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 0
        s1.plannedProcedures.size() == 1

        when: "I snapshot again"
        log.info "Computing Snapshot 2"
        def s2 = findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s2.save()
        log.info "Snapshot 2 saved"

        s2 = findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "The snapshot should have the correct last event"
        s2.lastEvent == lastEvent.id
    }

}
