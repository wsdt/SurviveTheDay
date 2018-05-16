package kevkevin.wsdt.tagueberstehen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation should be used to indicate that the method, class or other members should be
 * reviewed, because they contain a bug.*/

@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,
        ElementType.FIELD,ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,ElementType.TYPE,ElementType.ANNOTATION_TYPE,ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface Bug {
    enum Priority {
        NOT_DETERMINED, LOW, MEDIUM, HIGH
    }

    //Default params (as Arrays to allow multiple bugs)
    Priority[] priority() default Priority.NOT_DETERMINED;
    String[] problem() default "Problem/Bug not defined.";
    String[] possibleSolution() default "No solution recommendation set.";
    String[] message() default "No message provided"; //for setting additional information
    String[] byDeveloper() default "No developer specified.";
}
