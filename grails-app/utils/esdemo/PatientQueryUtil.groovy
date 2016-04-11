package esdemo

import grails.compiler.GrailsCompileStatic
import grails.util.Pair
import groovy.transform.TailRecursive
import groovy.util.logging.Slf4j

/**
 * Provides capability to obtain a snapshot of a Patient
 *
 * @author Rahul Somasunderam
 */
@GrailsCompileStatic
@Slf4j
class PatientQueryUtil {

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
    static PatientSnapshot findPatient(String identifier, String authority, long lastEvent) {
        log.info "Identifier: $identifier, Authority: $authority"
        PatientAggregate aggregate = PatientAggregate.findByIdentifierAndAuthority(identifier, authority)
        log.info "  --> Aggregate: $aggregate"
        computeSnapshot(aggregate, lastEvent)
    }

    private static PatientSnapshot computeSnapshot(PatientAggregate aggregate, long lastEvent) {
        def sePair = getSnapshotAndEventsSince(aggregate, lastEvent)

        def forwardEventsSortedBackwards = applyReverts(sePair.bValue.reverse())
        def deprecator = forwardEventsSortedBackwards.find { it instanceof PatientDeprecatedBy } as PatientDeprecatedBy

        if (deprecator) {
            return createEmptySnapshot().with {
                it.deprecatedBy = deprecator.newPatient
                it
            }
        } else {
            def retval = applyEvents(sePair.aValue, forwardEventsSortedBackwards.reverse(), [], [aggregate])
            log.info "  --> Computed: $retval"
            retval
        }

    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events. Can be skipped when called from outside code.
     * @return
     */
    @TailRecursive
    private static List<? extends PatientEvent> applyReverts(
            List<? extends PatientEvent> events, List<? extends PatientEvent> accumulator = []) {
        if (!events) {
            return accumulator
        }
        def head = events.head()
        def tail = events.tail()

        if (head instanceof PatientEventReverted) {
            def revert = head as PatientEventReverted
            if (!tail.contains(revert.event)) {
                throw new Exception("Cannot revert event that does not exist in unapplied list - ${revert.event.id}")
            }
            log.debug "    --> Revert: $revert"
            revert.event.revertedBy = revert.id
            return applyReverts(tail.findAll { it != revert.event }, accumulator)
        } else {
            return applyReverts(tail, accumulator + head)
        }
    }

    /**
     * Applies forward events on a snapshot and returns snapshot
     *
     * @param snapshot
     * @param events
     * @return
     */
    @TailRecursive
    private static PatientSnapshot applyEvents(
            PatientSnapshot snapshot, List<? extends PatientEvent> events, List<PatientDeprecates> deprecatesList,
            List<PatientAggregate> aggregates
    ) {
        if (events.empty || snapshot.deleted) {
            return snapshot
        }
        def firstEvent = events.head()
        def remainingEvents = events.tail()
        // snapshot.lastEvent = firstEvent.id
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

    /**
     * Given a last event, finds the latest snapshot older than that event
     * @param aggregate
     * @param lastEventInSnapshot
     * @return
     */
    private static Pair<PatientSnapshot, List<PatientEvent>> getSnapshotAndEventsSince(
            PatientAggregate aggregate, long lastEventInSnapshot, long lastEvent = lastEventInSnapshot) {
        def lastSnapshot = getLatestSnapshot(aggregate, lastEventInSnapshot)

        List<? extends PatientEvent> uncomputedEvents = PatientEvent.
                findAllByAggregateAndIdGreaterThanAndIdLessThanEquals(
                        aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)

        def uncomputedReverts = uncomputedEvents.
                findAll { it instanceof PatientEventReverted } as List<PatientEventReverted>

        def oldestRevertedEvent = uncomputedReverts*.event*.id.min()
        log.info "Oldest reverted event: $oldestRevertedEvent"
        if (uncomputedReverts && oldestRevertedEvent <= lastSnapshot.lastEvent) {
            log.info "Uncomputed reverts exist: $uncomputedEvents"
            getSnapshotAndEventsSince(aggregate, oldestRevertedEvent, lastEvent)
        } else {
            log.info "Event Ids in pair: ${uncomputedEvents*.id}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents*.id.max()
            }
            new Pair(lastSnapshot, uncomputedEvents)
        }
    }

    /**
     * Finds the latest snapshot that is older than event. The snapshot returned is detached.
     *
     * @param aggregate
     * @param startWithEvent
     * @return
     */
    private static PatientSnapshot getLatestSnapshot(PatientAggregate aggregate, Long startWithEvent) {

        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientSnapshot.findAllByAggregate(aggregate, LATEST) :
                PatientSnapshot.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        PatientSnapshot lastSnapshot = (snapshots ?: [createEmptySnapshot()])[0] as PatientSnapshot

        log.info "    --> Last Snapshot: ${lastSnapshot.id ? lastSnapshot : '<none>'}"

        if (lastSnapshot.isAttached()) {
            lastSnapshot.discard()
            lastSnapshot.id = null
        }

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    private static PatientSnapshot createEmptySnapshot() {
        new PatientSnapshot().with {
            it.performedProcedures = [] as Set
            it.plannedProcedures = [] as Set
            it.deprecates = [] as Set
            it
        }
    }

}
