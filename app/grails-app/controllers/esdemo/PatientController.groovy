package esdemo

import grails.compiler.GrailsCompileStatic

import static esdemo.PatientCommandUtil.*
import static Util.As

@GrailsCompileStatic
class PatientController {

    PatientQueryUtil patientQueryUtil
    public static final LinkedHashMap<String, String> REVERSE_ORDER = [sort: 'id', order: 'desc']

    /**
     * <code>
     * http -v get http://localhost:8080/patient/index.json
     * </code>
     *
     * @return
     */
    def index() {
        respond PatientAggregate.list()
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/show.json identifier==42 authority==1.2.3.4
     * </code>
     *
     * @param identifier
     * @param authority
     * @return
     */
    def show(String identifier, String authority, Long version) {
        assert identifier
        assert authority


        def lastVersion = version?.longValue() ?: Long.MAX_VALUE
        def patientSnapshot = patientQueryUtil.findPatient(identifier, authority, lastVersion)
        def aggregate = patientSnapshot.aggregate


        def deprecates = patientSnapshot.deprecates as Set<DeprecatedPatient>
        def allAggregates = deprecates.collect {
            PatientAggregate.findByIdentifierAndAuthority(it.identifier, it.authority)
        } + [aggregate]

        def events = PatientEvent.findAllByAggregateInListAndIdLessThanEquals(
                allAggregates, lastVersion, REVERSE_ORDER) as List<? extends PatientEvent>

        log.error "AllAggregates: $allAggregates"
        log.error "LastVersion: $lastVersion"
        log.error "Events: $events"
        events.each { event ->
            if (event instanceof PatientEventReverted && event.revertedBy == null) {
                (event as PatientEventReverted).event.revertedBy = event.id
            }
        }
        def snapshots = PatientSnapshot.findAllByAggregateAndLastEventLessThanEquals(
                aggregate, lastVersion, REVERSE_ORDER) as List<PatientSnapshot>
        def snapshottedEvents = snapshots.collect { it.lastEvent }

        def deprecator = patientSnapshot.deprecatedBy
        if (deprecator) {
            redirect(action: 'show', params: [identifier: deprecator.identifier, authority: deprecator.authority])
        }
        respond patientSnapshot, model: [events: events, snapshotted: snapshottedEvents]
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/create.json identifier==46 authority==1.2.3.4 name==Rahul user:bugs
     * </code>
     *
     * @param authority
     * @param identifier
     * @param name
     * @return
     */
    def create(String authority, String identifier, String name) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert identifier
        assert authority
        assert name

        As(user) { createPatient(identifier, authority, name) }

        redirect([action: 'show', params: [identifier: identifier, authority: authority]])
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/changeName.json identifier==42 authority==1.2.3.4 name==tian user:rahul
     * </code>
     *
     * @param authority
     * @param identifier
     * @param name
     * @return
     */
    def changeName(String authority, String identifier, String name) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert name
        assert aggregate

        As(user) { changeName(aggregate, name) }
        redirect action: 'show', params: [authority: authority, identifier: identifier]
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/perform.json identifier==42 authority==1.2.3.4 code==FLUSHOT user:rahul
     * </code>
     *
     * @param authority
     * @param identifier
     * @param code
     * @return
     */
    def perform(String authority, String identifier, String code) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert code
        assert aggregate

        As(user) { performProcedure(aggregate, code) }
        redirect action: 'show', params: [authority: authority, identifier: identifier]
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/plan.json identifier==42 authority==1.2.3.4 code==FLUSHOT user:rahul
     * </code>
     *
     * @param authority
     * @param identifier
     * @param code
     * @return
     */
    def plan(String authority, String identifier, String code) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert code
        assert aggregate

        As(user) { planProcedure(aggregate, code) }
        redirect action: 'show', params: [authority: authority, identifier: identifier]
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/delete.json identifier==42 authority==1.2.3.4 reason=="My bad" user:rahul
     * </code>
     *
     * @param authority
     * @param identifier
     * @param reason
     * @return
     */
    def delete(String authority, String identifier, String reason) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert aggregate

        As(user) { delete(aggregate, reason) }
        redirect action: 'show', params: [authority: authority, identifier: identifier]
    }

    /**
     * <code>
     * http -v get http://localhost:8080/patient/merge.json identifier==001 authority==1.2.3.4 authority2==1.2.3.4 identifier2==002 user:rahul
     * </code>
     *
     * @param authority
     * @param identifier
     * @param authority2
     * @param identifier2
     * @return
     */
    def merge(String authority, String identifier, String authority2, String identifier2) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert aggregate

        assert authority2
        assert identifier2
        def aggregate2 = PatientAggregate.findByAuthorityAndIdentifier(authority2, identifier2)

        assert aggregate2

        As(user) { merge(aggregate, aggregate2) }
        redirect action: 'show', params: [authority: authority2, identifier: identifier2]
    }

    /**
     *
     * @param authority
     * @param identifier
     * @param id
     */
    def revertEvent(String authority, String identifier, Long eventId) {
        String user = request.getHeader('user') ?: session.getAttribute('user')
        assert user

        assert authority
        assert identifier
        def aggregate = PatientAggregate.findByAuthorityAndIdentifier(authority, identifier)

        assert eventId
        assert aggregate

        def event = PatientEvent.findByAggregateAndId(aggregate, eventId)

        assert event

        As(user) { revertEvent(event) }

        redirect action: 'show', params: [authority: authority, identifier: identifier]
    }

}
