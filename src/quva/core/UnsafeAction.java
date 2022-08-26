package quva.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
@Documented
/**This annotation is used to mark unsafe operations. Unsafe actions usually use less qubits however automaticly take the lowest priority in the Qubo-Problem.*/
public @interface UnsafeAction {

}
