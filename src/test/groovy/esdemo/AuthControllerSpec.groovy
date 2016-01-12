package esdemo

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AuthController)
class AuthControllerSpec extends Specification {

    void "login sets the user in session"() {
        given:"a controller"
        def controller = new AuthController()

        when: "I login"
        request.addHeader 'referer', 'http://localhost:8080/patient'
        controller.params << [user: 'rahul']
        controller.login()

        then: "It gets persisted to the session"
        session.user == 'rahul'
    }
    void "logout invalidates the session"() {
        given:"a controller"
        def controller = new AuthController()

        when: "I login"
        session.user = 'rahul'
        request.addHeader 'referer', 'http://localhost:8080/patient'
        controller.logout()

        then: "It gets persisted to the session"
        session.user == null
    }
}
