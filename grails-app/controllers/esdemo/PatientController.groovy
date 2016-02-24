package esdemo

import grails.compiler.GrailsCompileStatic

import static esdemo.PatientCommandUtil.changeName
import static esdemo.PatientCommandUtil.createPatient
import static esdemo.PatientQueryUtil.findPatient
import static esdemo.UserUtil.As

@GrailsCompileStatic
class PatientController {

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

        def patientSnapshot = findPatient(identifier, authority, version?.longValue() ?: Long.MAX_VALUE)
        def events = PatientEvent.findAllByAggregate(patientSnapshot.aggregate, [sort: 'id', order: 'desc'])
        respond patientSnapshot, model: [events: events]
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

}
