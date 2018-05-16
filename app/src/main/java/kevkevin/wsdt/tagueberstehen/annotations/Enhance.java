package kevkevin.wsdt.tagueberstehen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation should be used to indicate that the method, class or other members should be
 * improved e.g. by:
 *      - Performance
 *      - Concise/Legible Code
 *      - Additional features
 *      - Solving multiple bugs (single bugs might be just marked with an TO´DO comment. */

@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,
        ElementType.FIELD,ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,ElementType.TYPE,ElementType.ANNOTATION_TYPE,ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface Enhance {
    enum Priority {
        NOT_DETERMINED, LOW, MEDIUM, HIGH
    }

    //Default params
    Priority[] priority() default Priority.NOT_DETERMINED;
    String[] message() default "No message provided";
    String[] byDeveloper() default "No developer specified.";
}
