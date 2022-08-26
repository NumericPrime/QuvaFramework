package quva.core;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ METHOD, CONSTRUCTOR,FIELD })
@Documented
/**This annotation marks methods that register new variables. 
 * This makes reading code and documentation easier.*/
public @interface Registers {

}
