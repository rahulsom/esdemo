package esdemo

import groovy.util.logging.Slf4j

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
class UserUtil {
    private static ThreadLocal<UserUtil> instance =
            ThreadLocal.withInitial({ return new UserUtil() } as Supplier<UserUtil>)

    private String _user

    static getUser() {
        instance.get()?._user
    }

    static <T> T As(String user, Closure<T> closure) {
        instance.get()._user = user
        def retval = closure.call()
        instance.get()._user = null
        retval
    }
}
