package quva.core;
import java.util.*;

import quva.postprocessing.BundledHandler;
import quva.postprocessing.PostProcessingHandler;
import quva.util.*;
import quva.transform.*;
/**This class is intended to be used with anonymous inner classes to create an application like this:<br>
 * <pre> 
 * new QuvaApplication(SIMULATE+AUTOTRUNCATE){
 * 	&#64;Override
 * 	public void build(){
 * 	//Do some stuff
 * 	}
 * 	&#64;Override
 * 	public void postprocessing(){
 * 	//Do some custom postprocessing
 * 	}
 * };
 * </pre>
 * This code will build and execute the matrix with the specified method and also do the postprocessing. This will also truncate the matrix before execution. If you prefer not to execute the matrix right away you can also choose NONE as mode of execution. You also don't have to override the postprocessing method.<br> An example without execution would look like this:
 * <pre>
 * new QuvaApplication(NONE){
 * 	&#64;Override
 * 	public void postprocessing(){
 * 		//do some stuff
 * 	}
 * };
 * </pre><br>However if you only want to execute the application later you can also write that into the mode:<br>
 * <pre> 
 * new QuvaApplication(ANNEAL+AUTOTRUNCATE+DELAYED){
 * 	&#64;Override
 * 	public void build(){
 * 	//Do some stuff
 * 	}
 * 	&#64;Override
 * 	public void postprocessing(){
 * 	//Do some custom postprocessing
 * 	}
 * };
 * </pre>
 * @see quva.core.QUBOMatrix
 * @see quva.core.QuvaUtilities*/
public abstract class QuvaApplication extends QuvaProgram implements quva.util.QuvaTemplate,ArithmeticOperationsList,FunctionsList{
	private static final long serialVersionUID = 8920358000428113126L;
	public static volatile Map<Thread,PostProcessingHandler> buffer=new HashMap<Thread,PostProcessingHandler>();
	public PostProcessingHandler h=null;
	public boolean delayed=false;
	public int mode=0;
	/**This constructor takes only the mode used for execution of the application.
	 * @param mode the mode used for execution. It accepts the same parameters as {@link quva.core.QUBOMatrix#execute(int)} plus {@code NONE} (=-1) wich will cause the applet not to be executed at all.*/
	public QuvaApplication(int mode) {
		super();
		this.mode=mode;
		delayed=((mode>>2&1)==1);
		initialize(mode);
	}
	/**This constructor takes the size of the matrix and the mode used for execution of the application.
	 * @param size size of the matrix
	 * @param mode the mode used for execution. It accepts the same parameters as {@link quva.core.QUBOMatrix#execute(int)} plus {@code NONE} (=-1) wich will cause the applet not to be executed at all.*/
	public QuvaApplication(int size,int mode) {
		super(size);
		initialize(mode);
	}
	/**This constructor takes a done matrix and the mode used for execution of the application.
	 * @param mat the matrix to be loaded
	 * @param mode the mode used for execution. It accepts the same parameters as {@link quva.core.QUBOMatrix#execute(int)} plus {@code NONE} (=-1) wich will cause the applet not to be executed at all.*/
	public QuvaApplication(float[][] mat,int mode) {
		super(mat);
		initialize(mode);
	}
	/**Creates a QuvaApplication based on a template, an execution mode and a PostProcessingHandler.
	 * @param template template to be used
	 * @param mode execution mode to be used
	 * @param handler PostProcessingHandler to be used
	 * @see quva.util.QuvaTemplate
	 * @see quva.postprocessing.PostProcessingHandler
	 * @return a new QuvaApplication*/
	public static QuvaApplication construct(QuvaTemplate template,int mode,PostProcessingHandler handler) {
		QuvaApplication ret= new QuvaApplication(mode) {
			private static final long serialVersionUID = 13423675896534L;
			@Override
			public void build() {
				load(QuvaTemplate.load(template));
				setPostProcessingHandler(handler);
			}
		};
		return ret;
	}
	/**Creates a QuvaApplication based on a QUBOMatrix, an execution mode and a PostProcessingHandler.
	 * @param matrix matrix to be used
	 * @param mode execution mode to be used
	 * @param handler PostProcessingHandler to be used
	 * @see quva.core.QUBOMatrix
	 * @see quva.postprocessing.PostProcessingHandler
	 * @return the new QuvaApplication*/
	public static QuvaApplication construct(QUBOMatrix matrix,int mode,PostProcessingHandler... handler) {
		QuvaApplication ret= new QuvaApplication(mode) {
			private static final long serialVersionUID = 13423675896534L;
			@Override
			public void build() {
				load(matrix);
				setPostProcessingHandler(new BundledHandler(handler));
			}
		};
		return ret;
	}
	/**This method is called by the constructor
	 * @param mode of the execution chosen in the constructor*/
	public void initialize(int mode) {
		h=buffer.get(Thread.currentThread());
		if(h==null) h=(in1,in2,in3)->{};
		if(mode>=0&&!delayed) execute();
	}
	/**{@inheritDoc}*/
	@Override
	public QUBOMatrix run() {
		build();
		return this;
	}
	/**Applies a shortQuva command
	 * @param in shortQuva command to be applied*/
	public void shortQuva(String in) {
		new quva.util.ShortQuva(this).put(in);
	}
	/**{@inheritDoc}*/
	public abstract void build();
	/**This method is intended to be overridden by the implementation. Alternatively one may set a {@code PostProcessingHandler}
	 * @param results the values of the qubits returned by execute
	 * @see quva.postprocessing.PostProcessingHandler
	 * @see #setPostProcessingHandler(PostProcessingHandler[])*/
	public void postprocessing(int[] results) {
		h.postprocessing(this,results);
	}
	/**This overrides the {@code PostProcessingHandler}. By standard a empty handler is used.
	 * @param handler handler to be used
	 * @see quva.postprocessing.PostProcessingHandler*/
	public void setPostProcessingHandler(PostProcessingHandler... handler) {
		h=new BundledHandler(handler);
		buffer.put(Thread.currentThread(),new BundledHandler(handler));
	}
	/**Runs and postprocesses the matrix with the current mode. This command will also be used in automatic execution. When using DELAYED as part of the mode. This command should be used for execution.
	 * <br><b>WARNING: </b>Using NONE as the mode will cause this method to glitch out and run it with ANNEAL instead.<br>If you want to disable automatic execution and ANNEAL it use<br>ANNEAL+DELAYED as mode instead.
	 * @see quva.core.QuvaUtilities#DELAYED
	 * @see quva.core.QUBOMatrix#execute(int)*/
	public void execute() {
		postprocessing(execute(mode));
	}
}
