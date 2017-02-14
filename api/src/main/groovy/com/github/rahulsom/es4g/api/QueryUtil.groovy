package com.github.rahulsom.es4g.api

import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import groovy.transform.TypeCheckingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
trait QueryUtil<A extends AggregateType, E extends BaseEvent<A>, S extends Snapshot<A>> {
    private Logger log = LoggerFactory.getLogger(getClass())

    abstract S createEmptySnapshot()

    /**
     * Gets the last snapshot before said event. Is responsible for discarding attached entity
     *
     * @param startWithEvent
     * @param aggregate
     * @return
     */
    abstract Optional<S> getSnapshot(long startWithEvent, A aggregate)

    abstract void detachSnapshot(S retval)

    private S getLatestSnapshot(A aggregate, long startWithEvent) {
        S lastSnapshot = getSnapshot(startWithEvent, aggregate).orElse(createEmptySnapshot()) as S

        log.info "    --> Last Snapshot: ${lastSnapshot.id ? lastSnapshot : '<none>'}"
        detachSnapshot(lastSnapshot)

        lastSnapshot.aggregate = aggregate
        lastSnapshot
    }

    /**
     * Applies all revert events from a list and returns the list with only valid forward events
     *
     * @param events list of events
     * @param accumulator accumulator of events.
     * @return
     */
    @TailRecursive
    private List<E> applyReverts(List<E> events, List<E> accumulator) {
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
    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, long lastEventInSnapshot) {
        getSnapshotAndEventsSince(aggregate, lastEventInSnapshot, lastEventInSnapshot)
    }

    private Tuple2<S, List<E>> getSnapshotAndEventsSince(A aggregate, long lastEventInSnapshot, long lastEvent) {
        if (lastEventInSnapshot) {
            def lastSnapshot = getLatestSnapshot(aggregate, lastEventInSnapshot)

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, lastEvent)
            def uncomputedReverts = uncomputedEvents.findAll { it instanceof RevertEvent<A> } as List<RevertEvent>

            if (uncomputedReverts) {
                log.info "Uncomputed reverts exist: ${uncomputedEvents}"
                getSnapshotAndEventsSince(aggregate, 0, lastEvent)
            } else {
                log.info "Events in pair: ${uncomputedEvents*.id}"
                if (uncomputedEvents) {
                    lastSnapshot.lastEvent = uncomputedEvents*.id.max()
                }
                new Tuple2(lastSnapshot, uncomputedEvents)
            }
        } else {
            def lastSnapshot = createEmptySnapshot()

            List<E> uncomputedEvents = getUncomputedEvents(aggregate, lastSnapshot, lastEvent)

            log.info "Events in pair: ${uncomputedEvents*.id}"
            if (uncomputedEvents) {
                lastSnapshot.lastEvent = uncomputedEvents*.id.max()
            }
            new Tuple2(lastSnapshot, uncomputedEvents)
        }

    }

    abstract List<E> getUncomputedEvents(A aggregate, S lastSnapshot, long lastEvent)

    abstract boolean shouldEventsBeApplied(S snapshot)

    abstract List<E> findEventsForAggregates(List<A> aggregates)

    @TailRecursive
    private S applyEvents(S snapshot, List<E> events, List deprecatesList, List<A> aggregates) {
        if (events.empty || !shouldEventsBeApplied(snapshot)) {
            return snapshot
        }
        def event = events.head()
        def remainingEvents = events.tail()

        log.debug "    --> Event: $event"

        if (event instanceof Deprecates<A>) {
            def deprecatesEvent = event as Deprecates<A>
            def newSnapshot = createEmptySnapshot()
            newSnapshot.aggregate = deprecatesEvent.aggregate

            def otherAggregate = deprecatesEvent.deprecated
            addToDeprecates(newSnapshot, otherAggregate)

            def allEvents = findEventsForAggregates(aggregates + deprecatesEvent.deprecated)

            def sortedEvents = allEvents.
                    findAll { it.id != deprecatesEvent.id && it.id != deprecatesEvent.converse.id }.
                    toSorted { a, b -> (a.dateCreated.time - b.dateCreated.time) as int }

            log.info "Sorted Events: [\n    ${sortedEvents.join(',\n    ')}\n]"

            def forwardEventsSortedBackwards = applyReverts(sortedEvents.reverse(), [] as List<E>)
            applyEvents(newSnapshot, forwardEventsSortedBackwards.reverse(), deprecatesList + deprecatesEvent, aggregates)
        } else if (event instanceof DeprecatedBy<A>) {
            def deprecatedByEvent = event as DeprecatedBy<A>
            def newAggregate = deprecatedByEvent.deprecator
            snapshot.deprecatedBy = newAggregate
            snapshot
        } else {
            def methodName = "apply${event.class.simpleName}".toString()
            def retval = callMethod(methodName, snapshot, event)
            if (retval == EventApplyOutcome.CONTINUE) {
                applyEvents(snapshot as S, remainingEvents as List<E>, deprecatesList, aggregates as List<A>)
            } else if (retval == EventApplyOutcome.RETURN) {
                snapshot
            } else {
                throw new Exception("Unexpected value from calling '$methodName'")
            }
        }
    }

    abstract void addToDeprecates(S snapshot, A otherAggregate)

    @CompileStatic(TypeCheckingMode.SKIP)
    private EventApplyOutcome callMethod(String methodName, S snapshot, E event) {
        this."${methodName}"(unwrapIfProxy(event), snapshot) as EventApplyOutcome
    }

    abstract E unwrapIfProxy(E event)

    Optional<S> computeSnapshot(A aggregate, long lastEvent) {

        Tuple2<S, List<E>> seTuple2 = getSnapshotAndEventsSince(aggregate, lastEvent)
        def events = seTuple2.second as List<E>
        def snapshot = seTuple2.first as S

        if (events.any { it instanceof RevertEvent<A> } && snapshot.aggregate) {
            return Optional.empty()
        }
        snapshot.aggregate = aggregate

        List<E> forwardEventsSortedBackwards = applyReverts(events.reverse(), [] as List<E>)
        assert !forwardEventsSortedBackwards.find { it instanceof RevertEvent<A> }

        def retval = applyEvents(snapshot, forwardEventsSortedBackwards.reverse(), [], [aggregate])
        log.info "  --> Computed: $retval"
        Optional.of(retval)
    }

}
