package esdemo

import groovy.transform.ToString

/**
 * A snapshot in Event Sourcing is the effective value of an aggregate at a point in time. This serves as a computed
 * value in most cases. However it can also serve as an optimization that reduce the number of reads from a database
 * if it gets persisted.
 *
 * @author Rahul Somasunderam
 */
@ToString
//tag::main[]
class PatientSnapshot implements Snapshot<PatientAggregate> {

    static belongsTo = [
            aggregate: PatientAggregate
    ]

    Long lastEvent = 0L
    String name
    Boolean deleted = Boolean.FALSE
    PatientAggregate deprecatedBy

    static hasMany = [
            plannedProcedures  : PlannedProcedure,
            performedProcedures: PerformedProcedure,
            deprecates         : DeprecatedPatient
    ]
//end::main[]

    static constraints = {
        deprecatedBy nullable: true
    }

    static mapping = {
        plannedProcedures lazy: false
        performedProcedures lazy: false
        deprecates lazy: false
    }

    @Override
    public String toString() {
        return """\
            PatientSnapshot{
                id=$id,
                performedProcedures=$performedProcedures,
                plannedProcedures=$plannedProcedures,
                otherIds=$deprecates,
                aggregate=$aggregate,
                lastEvent=$lastEvent,
                name='$name',
                deprecatedBy=$deprecatedBy
            }""".stripIndent()
    }
//tag::close[]
}
//end::close[]

class PlannedProcedure {

    static belongsTo = [
            patientSnapshot: PatientSnapshot
    ]
    String code
    String datePlanned

    @Override
    public String toString() { "PlannedProcedure{id=$id, code='$code', datePlanned='$datePlanned'}" }
}

class PerformedProcedure {

    static belongsTo = [
            patientSnapshot: PatientSnapshot
    ]
    String code
    String datePerformed

    @Override
    public String toString() { return "PerformedProcedure{id=$id, datePerformed='$datePerformed', code='$code'}" }
}

class DeprecatedPatient {

    static belongsTo = [
            patientSnapshot: PatientSnapshot
    ]
    String identifier
    String authority

    @Override
    String toString() { "DeprecatedPatient#${id} - $identifier@$authority" }
}
