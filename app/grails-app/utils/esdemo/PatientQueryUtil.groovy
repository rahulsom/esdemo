package esdemo

import grails.compiler.GrailsCompileStatic
import groovy.transform.TailRecursive
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

/**
 * Provides capability to obtain a snapshot of a Patient
 *
 * @author Rahul Somasunderam
 */
@GrailsCompileStatic
@Slf4j
@Component
// tag::begin[]
class PatientQueryUtil implements QueryUtil<PatientAggregate, PatientEvent, PatientSnapshot> {
// end::begin[]

    public static final Map LATEST = [sort: 'id', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'id', order: 'asc']

    /**
     * Obtains the snapshot of a patient based on the lastEvent. If lastEvent is MAX_LONG, you get the current state
     * of the Patient.
     *
     * @param identifier
     * @param authority
     * @param lastEvent
     * @return
     */
    //tag::method[]
    PatientSnapshot findPatient(String identifier, String authority, long lastEvent) {
        //end::method[]
        log.info "Identifier: $identifier, Authority: $authority"
        PatientAggregate aggregate = PatientAggregate.findByIdentifierAndAuthority(identifier, authority)
        log.info "  --> Aggregate: $aggregate"
        computeSnapshot(aggregate, lastEvent)
        //tag::endMethod[]
    }
    //end::endMethod[]

    @Override
    List<PatientEvent> getUncomputedEvents(PatientAggregate aggregate, PatientSnapshot lastSnapshot, long lastEvent) {
        PatientEvent.
                findAllByAggregateAndIdGreaterThanAndIdLessThanEquals(
                        aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)
    }

    @Override
    @TailRecursive
    PatientSnapshot applyEvents(
            PatientSnapshot snapshot, List<? extends Event<PatientAggregate>> events,
            List<? extends Deprecates<PatientAggregate>> deprecatesList, List<PatientAggregate> aggregates) {
        if (events.empty || snapshot.deleted) {
            return snapshot
        }
        def firstEvent = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $firstEvent"
        switch (firstEvent) {
            case PatientCreated:
                snapshot.name = (firstEvent as PatientCreated).name
                return applyEvents(snapshot, remainingEvents, deprecatesList, aggregates)
            case PatientNameChanged:
                snapshot.name = (firstEvent as PatientNameChanged).name
                return applyEvents(snapshot, remainingEvents, deprecatesList, aggregates)
            case PatientProcedurePlanned:
                def planned = firstEvent as PatientProcedurePlanned
                def match = snapshot.plannedProcedures?.find { it.code == planned.code }
                if (!match) {
                    snapshot.addToPlannedProcedures(code: planned.code, datePlanned: planned.dateCreated)
                }
                return applyEvents(snapshot, remainingEvents, deprecatesList, aggregates)
            case PatientProcedurePerformed:
                def performed = firstEvent as PatientProcedurePerformed
                def match = snapshot.plannedProcedures?.find { it.code == performed.code }
                if (match) {
                    snapshot.removeFromPlannedProcedures(match)
                }
                snapshot.addToPerformedProcedures(code: performed.code, datePerformed: performed.dateCreated)
                return applyEvents(snapshot, remainingEvents, deprecatesList, aggregates)
            case PatientDeleted:
                snapshot.deleted = true
                return snapshot
            case PatientDeprecates:
                def deprecated = firstEvent as PatientDeprecates
                def newSnapshot = createEmptySnapshot()
                newSnapshot.aggregate = deprecated.aggregate

                def otherPatient = deprecated.deprecated
                newSnapshot.addToDeprecates(identifier: otherPatient.identifier, authority: otherPatient.authority)

                def allEvents = PatientEvent.findAllByAggregateInList(
                        aggregates + [deprecated.deprecated], INCREMENTAL) as List<? extends PatientEvent>

                def sortedEvents = allEvents.
                        findAll { it.id != deprecated.id && it.id != deprecated.converse.id }.
                        toSorted { a, b -> (a.dateCreated.time - b.dateCreated.time) as int }

                log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"

                def forwardEventsSortedBackwards = applyReverts(sortedEvents.reverse())
                return applyEvents(newSnapshot, forwardEventsSortedBackwards.reverse(), deprecatesList + [deprecated], aggregates)
            default:
                throw new IllegalArgumentException("This kind of event is not supported - ${firstEvent.class}")
        }
    }

    @Override
    PatientSnapshot maybeGetSnapshot(long startWithEvent, PatientAggregate aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientSnapshot.findAllByAggregate(aggregate, LATEST) :
                PatientSnapshot.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        if (!snapshots) {
            return null
        }

        def lastSnapshot = snapshots[0] as PatientSnapshot

        if (lastSnapshot.isAttached()) {
            lastSnapshot.discard()
            lastSnapshot.id = null
        }

        lastSnapshot
    }

    @Override
    PatientSnapshot createEmptySnapshot() {
        new PatientSnapshot().with {
            it.performedProcedures = [] as Set
            it.plannedProcedures = [] as Set
            it.deprecates = [] as Set
            it
        }
    }
//tag::end[]
}
//end::end[]