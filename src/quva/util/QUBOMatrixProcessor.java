package quva.util;
import quva.core.QUBOMatrix;

import java.lang.reflect.*;

/**This class does some preprocessing most notibly it processes the {@code QuvaSettings} annotation*/
public class QUBOMatrixProcessor {
	/**Processes the new {@code QUBOMatrix}
	 * @param m matrix to be processed*/
	public static void process(QUBOMatrix m) {
		QuvaSettings sett=null;
		Constructor<?>[] constructors=m.getClass().getConstructors();
		for(Constructor<?> c:constructors) 
			if(c.isAnnotationPresent(QuvaSettings.class))
				sett=c.getAnnotation(QuvaSettings.class);
		if(m.getClass().isAnnotationPresent(QuvaSettings.class))
			sett=m.getClass().getAnnotation(QuvaSettings.class);
		if(m instanceof QuvaProgram) {
			try {
				Method method=m.getClass().getMethod("build",new Class[] {});
				if(method.isAnnotationPresent(QuvaSettings.class))
					sett=method.getAnnotation(QuvaSettings.class);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		if(sett!=null) {
			if(sett.size()!=-1) m.load(emptyMatrix(sett.size()));
			m.init(sett.m(),sett.layering_with_one());
			if(!sett.shortQuva().equals("")) new ShortQuva(m).put(sett.shortQuva());
		}
		}
	}
	/**Creates an empty matrix
	 * @param size size of the new matrix
	 * @return matrix with the given size only consisting of zeroes*/
	public static float[][] emptyMatrix(int size) {
		float[][] ret=new float[size][size];
		for(int i=0;i<size;i++)
			for(int j=0;j<size;j++)
				ret[i][j]=0;
		return ret;
	}
}
