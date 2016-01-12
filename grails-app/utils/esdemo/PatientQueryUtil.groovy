package esdemo

import grails.compiler.GrailsCompileStatic
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

    static PatientSnapshot findPatient(String identifier, String authority, long lastEvent) {
        log.info "Identifier: $identifier, Authority: $authority"

        PatientAggregate aggregate = PatientAggregate.findByIdentifierAndAuthority(identifier, authority)
        log.info "  --> Aggregate: $aggregate"

        def lastSnapshot = getLatestSnapshot(aggregate, lastEvent)

        List<? extends PatientEvent> uncomputedEvents = PatientEvent.
                findAllByAggregateAndIdBetween(aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)

        def retval = uncomputedEvents.
                inject(lastSnapshot) { PatientSnapshot snapshot, PatientEvent event ->
                    log.debug "    --> Event: $event"
                    mutateSnapshot(snapshot, event)
                } as PatientSnapshot


        log.info "  --> Computed: $retval"

        retval
    }

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

    private static PatientSnapshot mutateSnapshot(PatientSnapshot snapshot, PatientEvent event) {
        if (event instanceof PatientCreated) {
            snapshot.name = event.name
        } else if (event instanceof PatientNameChanged) {
            snapshot.name = event.name
        } else {
            throw new IllegalArgumentException("This kind of event is not supported - ${event.class}")
        }
        snapshot.lastEvent = event.id
        snapshot
    }

}
