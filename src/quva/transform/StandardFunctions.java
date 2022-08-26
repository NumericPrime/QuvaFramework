package quva.transform;

import quva.core.QUBOMatrix;

public enum StandardFunctions implements FunctionsList,FunctionTransformation{
	RELU(FunctionsList.RELU),SQRT(FunctionsList.SQRT),SQ(FunctionsList.SQ);
	public FunctionTransformation transformation;
	StandardFunctions(FunctionTransformation f){
		transformation=f;
	}
	@Override
	public void apply(QUBOMatrix m, String var, String target) {
		transformation.apply(m, var,target);
	}
	
}
