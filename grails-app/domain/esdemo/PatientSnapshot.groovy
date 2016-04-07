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
class PatientSnapshot {

    static belongsTo = [
            aggregate: PatientAggregate
    ]

    Long lastEvent = 0L
    String name

    public static final ArrayList<String> HIDDEN_FIELDS = [
            'class', 'id', 'aggregate', 'aggregateId'
    ]

    static hasMany = [
            plannedProcedures  : PlannedProcedure,
            performedProcedures: PerformedProcedure,
    ]


    @Override
    public String toString() {
        return """\
            PatientSnapshot{
                id=$id,
                performedProcedures=$performedProcedures,
                plannedProcedures=$plannedProcedures,
                aggregate=$aggregate,
                lastEvent=$lastEvent,
                name='$name'
            }""".stripIndent()
    }
}

class PlannedProcedure {
    String code
    String datePlanned


    @Override
    public String toString() {
        return """\
            PlannedProcedure{
                id=$id,
                code='$code',
                datePlanned='$datePlanned'
            }""".stripIndent()
    }
}

class PerformedProcedure {
    String code
    String datePerformed


    @Override
    public String toString() {
        return """\
            PerformedProcedure{
                id=$id,
                datePerformed='$datePerformed',
                code='$code'
            }""".stripIndent()
    }
}