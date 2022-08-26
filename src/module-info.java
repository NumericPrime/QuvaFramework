/**This API allows using quantum annealing using java. It specializes in creating applications for QuantumAnnealers. Quva is composed of three API's.<br>
 * The most important is the Core API. That handles most of the operations such as constructing the hamilton-matrix and handles execution.<br>
 * The Transformation API handles transformations involving variables. This API also contatins the means of creating extreamly efficient algorithms for the solution of polynomial equations.<br>
 * The Post-processing API handles post-processing of the results and offers the means of printing and saving the results.
 * @see quva.core.QuvaApplication
 * @see quva.transform.AbstractTransform
 * @see quva.postprocessing.PostProcessingHandler*/
open module QuvaAPI {
	exports quva.core;
	exports quva.util;
	exports quva.transform;
	exports quva.postprocessing;
	requires java.desktop;
}