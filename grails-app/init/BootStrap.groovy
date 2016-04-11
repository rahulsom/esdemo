import grails.util.GrailsUtil

import java.time.Instant

import static esdemo.PatientCommandUtil.*
import static esdemo.Util.As

class BootStrap {

    def init = { servletContext ->

        if (GrailsUtil.developmentEnv) {
            def p1 = As('rahul') {
                at(Instant.parse('2016-02-24T16:17:49.00Z'))
                createPatient('123', '1.2.3.4', 'john')
            }
            As('donald') { changeName(p1, 'mike') }
            As('daffy') { changeName(p1, 'sean') }
            def nce = As('goofy') { changeName(p1, 'tim') }
            As('goofy') { revertEvent(nce)}
            As('micky') { planProcedure(p1, 'APPENDECTOMY')}
            As('micky') { planProcedure(p1, 'FLUSHOT')}
            As('stevie') { performProcedure(p1, 'FLUSHOT')}
            As('micky') { planProcedure(p1, 'FLUSHOT')}
        }

    }
    def destroy = {
    }
}
