package esdemo

import groovy.util.logging.Slf4j

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.util.function.Supplier

/**
 * Poor mans authentication.
 *
 * This class allows one to specify who the user was to the PatientCommandUtil. In a practical system, this would be
 * replaced by something like Spring Security.
 *
 * @author Rahul Somasunderam
 */
@Slf4j
class Util {
    private static ThreadLocal<Util> instance =
            ThreadLocal.withInitial({ return new Util() } as Supplier<Util>)

    private String _user

    static getUser() {
        instance.get()?._user
    }

    static class TimeKeeper {
        Instant time = null

        void at(Instant theTime) {
            time = theTime
        }
    }

    private timeKeeper = new TimeKeeper()

    static Date getTime() {
        def ts = instance.get()?.timeKeeper?.time
        ts ? Date.from(ts) : new Date()
    }

    static <T> T As(String user, @DelegatesTo(TimeKeeper) Closure<T> closure) {
        def instance = instance.get()
        instance._user = user
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = instance.timeKeeper
        def retval = closure.call()
        instance._user = null
        retval
    }


}
