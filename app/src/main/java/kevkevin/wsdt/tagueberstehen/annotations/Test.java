package kevkevin.wsdt.tagueberstehen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kevkevin.wsdt.tagueberstehen.interfaces.IConstants_Global;

@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,
        ElementType.FIELD,ElementType.PARAMETER,
        ElementType.LOCAL_VARIABLE,ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Test {
    enum Priority {
        NOT_DETERMINED, LOW, MEDIUM, HIGH
    }

    //Default params
    Priority priority() default Priority.NOT_DETERMINED;
    String message() default "No message provided";
    String developer() default IConstants_Global.DEVELOPERS.WSDT;
}
