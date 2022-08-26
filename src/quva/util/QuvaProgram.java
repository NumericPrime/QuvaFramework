package quva.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import quva.core.QUBOMatrix;

/**This class allows using Quva from an anonymous inner class*/
public abstract class QuvaProgram extends QUBOMatrix implements QuvaTemplate{
	private static final long serialVersionUID = -4814876506086045704L;
	/**{@inheritDoc}*/
	public QuvaProgram() {
		super();
		run();
	}

	/**{@inheritDoc}*/
	public QuvaProgram(int size) {
		super(size);
		run();
	}

	/**{@inheritDoc}*/
	public QuvaProgram(float[][] matrix) {
		super(matrix);
		run();
	}
	@Override
	public QUBOMatrix run() {
		build();
		return this;
	}
	/**This method will be run after the constructor finishes.*/
	public abstract void build();
	
	/**Constructs a new {@code QUBOMatrix} from the class name and the constructor arguments
	 * @param className name of the class. The full name needs to be inputed here. So {@code QUBOMatrix} will not suffice while {@code quva.core.QUBOMatrix} would.
	 * @param params the params to be inserted into the constructor
	 * @return the done {@code QUBOMatrix} */
	public static QUBOMatrix launch(String className,Object... params) {
		try {
			Class<? extends QUBOMatrix> quboClass=Class.forName(className).asSubclass(QUBOMatrix.class);
			if(params.length==0)
				try {
					quboClass.getConstructor().trySetAccessible();
					return (QUBOMatrix) quboClass.getConstructor().newInstance();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			Class<?>[] classes=new Class[params.length];
			  for (int i=0; i<classes.length; i++) classes[i]=params[i].getClass();
			  boolean diditwork=false;

			  boolean iter[]=new boolean[classes.length+1];
			  for (int i=0;i<iter.length;i++) iter[i]=false;
			  while (!iter[iter.length-1]&&!diditwork) {
			    Class<?>[] current=new Class[classes.length];
			    for (int i=0; i<classes.length; i++)if (!iter[i]) {
			      if (classes[i]==(Integer.class)) current[i]=int.class;
			      else if (classes[i]==Float.class) current[i]=float.class;
			      else if (classes[i]==Long.class) current[i]=long.class;
			      else if (classes[i]==Short.class) current[i]=short.class;
			      else if (classes[i]==Double.class) current[i]=double.class;
			      else if (classes[i]==Boolean.class) current[i]=boolean.class;
			      else current[i]=classes[i];
			    } else current[i]=classes[i];
			    try {
			      Constructor<?> constr=quboClass.getConstructor(current);
			      constr.trySetAccessible();
			      return (QUBOMatrix) constr.newInstance(params);
			    }
			    catch(Exception e) {
			    }

			    boolean stack=iter[0];
			    iter[0]=!iter[0];
			    for (int i=1; i<iter.length&&stack; i++) {
			      stack=iter[i];
			      iter[i]=!iter[i];
			    }
			  }
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
