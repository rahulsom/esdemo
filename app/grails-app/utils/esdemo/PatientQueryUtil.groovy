package esdemo

import grails.compiler.GrailsCompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import static esdemo.EventApplyOutcome.CONTINUE
import static esdemo.EventApplyOutcome.RETURN

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

    EventApplyOutcome applyEvent(PatientCreated event, PatientSnapshot snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyEvent(PatientNameChanged event, PatientSnapshot snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyEvent(PatientProcedurePlanned planned, PatientSnapshot snapshot) {
        def match = snapshot.plannedProcedures?.find { it.code == planned.code }
        if (!match) {
            snapshot.addToPlannedProcedures(code: planned.code, datePlanned: planned.dateCreated)
        }
        CONTINUE
    }

    EventApplyOutcome applyEvent(PatientProcedurePerformed performed, PatientSnapshot snapshot) {
        def match = snapshot.plannedProcedures?.find { it.code == performed.code }
        if (match) {
            snapshot.removeFromPlannedProcedures(match)
        }
        snapshot.addToPerformedProcedures(code: performed.code, datePerformed: performed.dateCreated)
        CONTINUE
    }

    EventApplyOutcome applyEvent(PatientDeleted event, PatientSnapshot snapshot) {
        snapshot.deleted = true
        RETURN
    }

    @Override
    List<PatientEvent> findEventsForAggregates(List<PatientAggregate> aggregates) {
        PatientEvent.findAllByAggregateInList(aggregates, INCREMENTAL) as List<? extends PatientEvent>
    }

    @Override
    boolean shouldEventsBeApplied(PatientSnapshot snapshot) {
        !snapshot.deleted
    }

    @Override
    PatientSnapshot maybeGetSnapshot(long startWithEvent, PatientAggregate aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientSnapshot.findAllByAggregate(aggregate, LATEST) :
                PatientSnapshot.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        if (!snapshots) {
            return null
        }

        snapshots[0] as PatientSnapshot
    }

    void detachSnapshot(PatientSnapshot retval) {
        if (retval.isAttached()) {
            retval.discard()
            retval.id = null
        }
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

    @Override
    void addToDeprecates(PatientSnapshot snapshot, PatientAggregate otherAggregate) {
        snapshot.addToDeprecates(identifier: otherAggregate.identifier, authority: otherAggregate.authority)
    }
//tag::end[]
}
//end::end[]