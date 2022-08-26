package quva.util;

import java.util.*;
import java.lang.reflect.*;

import quva.core.QUBOMatrix;
import quva.core.Registers;
import quva.postprocessing.PostProcessingHandler;
/**This allows using add commands to an already done matrix. This is used in the {@link quva.core.QUBOMatrix#sample(float[][], QuvaModifiers, int[])} method.
 * You can register variables using it. An example would be:<pre>
 * QUBOMatrix.sample(matrix,
 * 	new QuvaModifiers()
 * 		.put("registerNat","x",4)
 * 		.put("registerNat","y",4)
 * 		.attached(handler),
 * 	SIMULATE+AUTOTRUNCATE);
 * </pre>
 * @see quva.core.QUBOMatrix#sample(float[][], QuvaModifiers, int[])*/
public class QuvaModifiers implements QuvaTemplate{
	//public Map<String, int[]> varQubits=new WeakHashMap<String,int[]>();
	//public Map<int[], float[]> varWeights=new WeakHashMap<int[],float[]>();
	public List<String> methods=new ArrayList<String>();
	public List<Object[]> parameters=new ArrayList<Object[]>();
	public boolean allowNonRegistersMethods=false;
	public PostProcessingHandler handler;
	public boolean writeWarnings=true;
	/**This QuvaModifiers object stores and executes commands that are annoted with the {@code &#64;Registers} annotation. When trying to execute an command without the annotaion the command will be ignored and a message will be written into the console.*/
	public QuvaModifiers() {
		this(false,true);
	}
	/**This QuvaModifiers object stores and executes commands that are annoted with the {@code &#64;Registers} annotation. When trying to execute an command without the annotaion the command will be ignored and a message will be written into the console. The check for the annotation can be disabled however.
	 * @param nonRegisters setting this to true will allow any command even if it doesn't, have the Registers annotation.*/
	public QuvaModifiers(boolean nonRegisters) {
		this(true,false);
	}
	/**This QuvaModifiers object stores and executes commands that are annoted with the {@code &#64;Registers} annotation. When trying to execute an command without the annotaion the command will be ignored and a message will be written into the console. This can be disabled. The check for the annotation can also be disabled.
	 * @param nonRegisters setting this to true will allow any command even if it doesn't, have the Registers annotation.
	 * @param writeWarnings setting this to falso will disable the warnings being disabled*/
	public QuvaModifiers(boolean nonRegisters,boolean writeWarnings) {
		allowNonRegistersMethods=nonRegisters;
		this.writeWarnings=writeWarnings;
	}
	/**Puts a command into the queue.
	 * @param str the name of the command
	 * @param params parameters of the commands. Please note that when the parameter says float you can't use e.g. 1 as it will be considered an int. Use 1f instead.
	 * @return the current {@code QuvaModifiers} object.*/
	public QuvaModifiers put(String str,Object... params){
		methods.add(str);
		parameters.add(params);
		return this;
	}
	/**Applies the commands to the {@code QUBOMatrix}
	 * @param m the matrix where the modifiers are to be applied
	 * @return the current {@code QuvaModifiers} object.*/
	public QuvaModifiers apply(QUBOMatrix m) {
		for(int k=0;k<methods.size();k++) {
			String met=methods.get(k);
			Object[] parameter=parameters.get(k);
			Class<?>[] classes=new Class<?>[parameter.length];
			for(int i=0;i<classes.length;i++) switch(parameter[i].getClass().getSimpleName()) {
				case "Integer":
					classes[i]=int.class;
					break;
				case "Float":
					classes[i]=float.class;
					break;
				case "Double":
					classes[i]=double.class;
					break;
				default:
					classes[i]=parameter[i].getClass();
					break;
			}
			try {
				Method reflectMethod=QUBOMatrix.class.getDeclaredMethod(met, classes);
				if(allowNonRegistersMethods||reflectMethod.isAnnotationPresent(Registers.class))
				reflectMethod.invoke(m, parameter);
				else if(writeWarnings) 
					System.err.println("Command "+met+" has been ignored as allowNonRegistersMethods is set to false");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	/**Attaches a PostProcessingHandler. This will only work with {@link quva.core.QUBOMatrix#sample(float[][], QuvaModifiers, int[])}.
	 * @param handler the handler to be added
	 * @return the current {@code QuvaModifiers} object.*/
	public QuvaModifiers attach(PostProcessingHandler... handler) {
		this.handler=new quva.postprocessing.BundledHandler(handler);
		return this;
	}
	/**This will create a new {@code QUBOMatrix} defined by the QuvaModifiers.
	 * @return new {@code QUBOMatrix}*/
	@Override
	public QUBOMatrix run() {
		QUBOMatrix m=new QUBOMatrix();
		apply(m);
		return m;
	}
}
