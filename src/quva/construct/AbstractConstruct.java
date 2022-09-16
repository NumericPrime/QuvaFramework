package quva.construct;

import quva.core.QUBOMatrix;

/**A abstract implementation of a construct. It can be used as a basis to create constructs yourself. Though for most cases using SafeConstruct suffices.
 * @see quva.construct.QuvaConstruct
 * @see quva.construct.SafeConstruct*/
public abstract class AbstractConstruct implements QuvaConstruct {
	/**The QuvaConstructRegister used by the Construct and the one returned by getRegistry
	 * @see quva.construct.AbstractConstruct#getRegistry()*/
	public QuvaConstructRegister mainRegistry=new QuvaConstructRegister();
	/**Variable used by the construct*/
	public String var;
	/**QUBOMatrix the construct gets applied to*/
	public QUBOMatrix matrix;
	/**This constructor sets the var and the matrix field
	 * @see quva.construct.AbstractConstruct#var
	 * @see quva.construct.AbstractConstruct#matrix
	 * @param var name of the variable
	 * @param mt matrix to be used*/
	public AbstractConstruct(String var,QUBOMatrix mt) {
		this.var=var;
		matrix=mt;
	}
	/**This returns the main registry.
	 * @return the mainRegistry of the AbstractConstruct*/
	@Override
	public QuvaConstructRegister getRegistry() {
		// TODO Auto-generated method stub
		return mainRegistry;
	}
	/**{@inheritDoc}*/
	@Override
	public abstract void process() ;
	/**{@inheritDoc}*/
	@Override
	public abstract boolean authorizeRemoval(boolean[] b) ;
	/**Removes a element of the registry if authorized.
	 * @param b combination used*/
	public void authorizedRemoval(boolean[] b) {
		if(authorizeRemoval(b)) mainRegistry.remove(b);
	}
}
