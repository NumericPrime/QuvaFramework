package quva.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ TYPE, CONSTRUCTOR,METHOD })
@Documented
/**Allows an easier setup for a custom QUBOMatrix*/
public @interface QuvaSettings {
	/**Allows setting m
	 * @return m of the new QUBOMatrix*/
	public float m() default 10;
	/**Allows setting if the layering begins with one
	 * @return whether the baseline is layer 1 or 0*/
	public boolean layering_with_one() default false;
	/**Sets the maximum size of the matrix
	 * @return maximum size of the matrix*/
	public int size() default -1;
	/**Allows using ShortQuva to set up the matrix
	 * @return ShortQuva setup to be loaded
	 * @see quva.util.ShortQuva*/
	public String shortQuva() default "";
}
