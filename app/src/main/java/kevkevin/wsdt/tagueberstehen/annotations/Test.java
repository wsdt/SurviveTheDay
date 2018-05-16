package kevkevin.wsdt.tagueberstehen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation should be used to indicate that the method, variable, class etc. should
 * be tested carefully. */

@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,
        ElementType.FIELD,ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Test {
    enum Priority {
        NOT_DETERMINED, LOW, MEDIUM, HIGH
    }

    //Default params (as array to allow multiple)
    Priority[] priority() default Priority.NOT_DETERMINED;
    String[] message() default "No message provided";
    String[] byDeveloper() default "No developer specified.";
}
