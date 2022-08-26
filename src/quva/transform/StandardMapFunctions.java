package quva.transform;

import quva.core.QUBOMatrix;

public enum StandardMapFunctions implements MapTransformation,ArithmeticOperationsList{
	ADDITION(ArithmeticOperationsList.ADDITION),
	SUBTRACTION(ArithmeticOperationsList.SUBTRACTION),
	MULTIPLICATION(ArithmeticOperationsList.MULTIPLICATION),
	DIVISION(ArithmeticOperationsList.DIVISION);
	public MapTransformation transformation;
	StandardMapFunctions(MapTransformation f){
		transformation=f;
	}
	@Override
	public void apply(QUBOMatrix m, String var1, String var2, String target) {
		transformation.apply(m, var1,var2,target);
	}
}
