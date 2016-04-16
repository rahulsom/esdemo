package esdemo

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.util.logging.Slf4j
import spock.lang.IgnoreRest
import spock.lang.Specification

import static Util.As
import static esdemo.PatientCommandUtil.*

@Integration
@Rollback
@Slf4j
class PatientQueryUtilSpec extends Specification {

    def patientQueryUtil

    def "Patient is created"() {
        when: "I create a patient"
        As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'mike'
    }

    def "Name change is reverted"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        def e = As('rahul') { changeName p1, 'mike' }
        def e1 = As('rahul') { revertEvent e }
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s1.save()
        As('mickey') { planProcedure p1, 'FLUSHOT' }
        s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

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
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s1.save()
        As('mickey') { planProcedure p1, 'FLUSHOT' }
        def lastEvent = As('goofy') { revertEvent e1 }
        s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "I see the correct new name"
        s1 != null
        s1 instanceof PatientSnapshot
        s1.name == 'john'
        s1.performedProcedures.size() == 0
        s1.plannedProcedures.size() == 1

        when: "I snapshot again"
        def s2 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE
        s2.save()

        s2 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "The snapshot should have the correct last event"
        s2.lastEvent == lastEvent.id
    }

    def "Merge can be read correctly"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            performProcedure p1, 'FLUSHOT'
        }

        and: "Create another patient"
        def p2 = As('rahul') { createPatient '42', '1.2.3.4', 'John' }
        As('rahul') {
            planProcedure p2, 'APPENDECTOMY'
            performProcedure p2, 'FLUSHOT'
        }

        and: "I merge"
        def m1 = As('rahul') { merge(p1, p2) }

        then: "Merge should be valid"
        m1 != null
        m1.converse != null

        when: "I find the deprecated patient"
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "There should be one planned procedure and one performed procedure"
        s1.plannedProcedures.size() == 0
        s1.performedProcedures.size() == 0
        s1.deprecatedBy == p2

        when: "I find the deprecating patient"
        def s2 = patientQueryUtil.findPatient '42', '1.2.3.4', Long.MAX_VALUE

        then: "There should be one planned procedure and one performed procedure"
        s2.aggregate.identifier == '42'
        s2.plannedProcedures.size() == 1
        s2.performedProcedures.size() == 2
        s2.deprecatedBy == null
        s2.deprecates.size() == 1
        s2.deprecates[0].identifier == '123'
        s2.deprecates[0].authority == '1.2.3.4'
    }

    def "Merge can be reverted"() {
        when: "I create a patient"
        def p1 = As('rahul') { createPatient '123', '1.2.3.4', 'john' }
        As('rahul') {
            planProcedure p1, 'FLUSHOT'
            performProcedure p1, 'FLUSHOT'
        }

        and: "Create another patient"
        def p2 = As('rahul') { createPatient '42', '1.2.3.4', 'John' }
        As('rahul') {
            planProcedure p2, 'APPENDECTOMY'
            performProcedure p2, 'FLUSHOT'
        }

        then:"There should be 6 events"
        PatientEvent.count() == 6

        when: "I merge"
        def m1 = As('rahul') { merge(p1, p2) }

        then: "Merge should be valid"
        m1 != null
        m1.converse != null
        PatientEvent.count() == 8

        when: "I revert the merge"
        def m2 = As('rahul') { revertEvent m1 }

        then: "The merge should be valid"
        m2 != null
        PatientEvent.count() == 10

        when: "I find the deprecated patient"
        log.info "Loading deprecated patient"
        def s1 = patientQueryUtil.findPatient '123', '1.2.3.4', Long.MAX_VALUE

        then: "There should be one planned procedure and one performed procedure"
        s1.plannedProcedures.size() == 0
        s1.performedProcedures.size() == 1
        s1.deprecatedBy == null

        when: "I find the deprecating patient"
        def s2 = patientQueryUtil.findPatient '42', '1.2.3.4', Long.MAX_VALUE

        then: "There should be one planned procedure and one performed procedure"
        s2.plannedProcedures.size() == 1
        s2.performedProcedures.size() == 1
        s2.deprecatedBy == null
        s2.deprecates.size() == 0
    }
}
