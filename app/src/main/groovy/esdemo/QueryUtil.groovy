package esdemo

import grails.util.Pair
import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
trait QueryUtil<A extends Aggregate, E extends Event<A>, S extends Snapshot<A>> {
    Logger log = LoggerFactory.getLogger(getClass())

    abstract S createEmptySnapshot()

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    abstract S maybeGetSnapshot(long startWithEvent, A aggregate)

    S getLatestSnapshot(A aggregate, long startWithEvent) {

        S lastSnapshot = maybeGetSnapshot(startWithEvent, aggregate) ?: createEmptySnapshot()

        log.info "    --> Last Snapshot: ${lastSnapshot.id ? lastSnapshot : '<none>'}"

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @return
     */
    List<E> applyReverts(List<E> events) {
        applyReverts(events, [])
    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    @TailRecursive
    List<E> applyReverts(List<E> events, List<E> accumulator) {
        if (!events) {
            return accumulator
        }
        def head = events.head()
        def tail = events.tail()

        if (head instanceof RevertEvent<A>) {
            def revert = head as RevertEvent<A>
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
     * Given a last event, finds the latest snapshot older than that event
     * @param aggregate
     * @param lastEventInSnapshot
     * @return
     */
    Pair<S, List<E>> getSnapshotAndEventsSince(A aggregate, long lastEventInSnapshot) {
        getSnapshotAndEventsSince(aggregate, lastEventInSnapshot, lastEventInSnapshot)
    }

    Pair<S, List<E>> getSnapshotAndEventsSince(A aggregate, long lastEventInSnapshot, long lastEvent) {
        def lastSnapshot = getLatestSnapshot(aggregate, lastEventInSnapshot)

        List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, lastEvent)

        def uncomputedReverts = uncomputedEvents.findAll { it instanceof RevertEvent<A> } as List<RevertEvent>

        def oldestRevertedEvent = ((uncomputedReverts*.event*.id) as List<Long>).min()
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

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, long lastEvent)

    abstract S applyEvents(S snapshot, List<? extends Event<A>> events, List<? extends Deprecates<A>> deprecatesList, List<A> aggregates)

    S computeSnapshot(A aggregate, long lastEvent) {
        Pair<S, List<E>> sePair = getSnapshotAndEventsSince(aggregate, lastEvent)

        List<E> forwardEventsSortedBackwards = applyReverts(sePair.bValue.reverse())
        def deprecator = forwardEventsSortedBackwards.find { it instanceof DeprecatedBy<A> } as DeprecatedBy<A>

        if (deprecator) {
            def snapshot = createEmptySnapshot()
            snapshot.deprecatedBy = deprecator.deprecator
            snapshot
        } else {
            S value = sePair.aValue as S
            def retval = applyEvents(value, forwardEventsSortedBackwards.reverse(), [], [aggregate])
            log.info "  --> Computed: $retval"
            retval
        }
    }

}
