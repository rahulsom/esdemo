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
        def sePair = getSnapshotAndEventsSince(aggregate, lastEvent)
        def retval = applyEvents(sePair.aValue, applyReverts(sePair.bValue.reverse()).reverse())
        log.info "  --> Computed: $retval"

        retval
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
            PatientSnapshot snapshot, List<? extends PatientEvent> events) {
        if (!events) {
            return snapshot
        }
        def firstEvent = events.head()
        def remainingEvents = events.tail()
        snapshot.lastEvent = firstEvent.id
        log.debug "    --> Event: $firstEvent"
        switch (firstEvent) {
            case PatientCreated:
                snapshot.name = (firstEvent as PatientCreated).name
                return applyEvents(snapshot, remainingEvents)
            case PatientNameChanged:
                snapshot.name = (firstEvent as PatientNameChanged).name
                return applyEvents(snapshot, remainingEvents)
            default:
                throw new IllegalArgumentException("This kind of event is not supported - ${firstEvent.class}")
        }
    }

    /**
     * Given a last event, finds the latest snapshot older than that event
     * @param aggregate
     * @param lastEvent
     * @return
     */
    private static Pair<PatientSnapshot, List<PatientEvent>> getSnapshotAndEventsSince(
            PatientAggregate aggregate, long lastEvent) {
        def lastSnapshot = getLatestSnapshot(aggregate, lastEvent)

        List<? extends PatientEvent> uncomputedEvents = PatientEvent.
                findAllByAggregateAndIdBetween(aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)

        def uncomputedReverts = uncomputedEvents.
                findAll { it instanceof PatientEventReverted } as List<PatientEventReverted>

        def oldestRevertedEvent = uncomputedReverts*.event*.id.min()
        if (uncomputedReverts && oldestRevertedEvent < lastSnapshot.lastEvent) {
            getSnapshotAndEventsSince(aggregate, oldestRevertedEvent)
        } else {
            new Pair(lastSnapshot, uncomputedEvents)
        }
    }

    /**
     * Finds the latest snapshot that is older than event
     *
     * @param aggregate
     * @param startWithEvent
     * @return
     */
    private static PatientSnapshot getLatestSnapshot(PatientAggregate aggregate, Long startWithEvent) {

        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientSnapshot.findAllByAggregate(aggregate, LATEST) :
                PatientSnapshot.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        PatientSnapshot lastSnapshot = (snapshots ?: [new PatientSnapshot()])[0] as PatientSnapshot

        log.info "    --> Last Snapshot: ${lastSnapshot.id ? lastSnapshot : '<none>'}"

        if (lastSnapshot.isAttached()) {
            lastSnapshot.discard()
            lastSnapshot.id = null
        }

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

}
