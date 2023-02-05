package dwas.main;

import java.util.PriorityQueue;

import quva.core.QuvaDebug;
import quva.core.QuvaExecutionSettings;

public class SimulatedAnnealingSolver {
	/**Runs simulated annealine
	 * @param matrix matrix to be processed
	 * @return solution
	*/
	public static int[] solve(float[][] matrix) {
		return bundleExecution(matrix,
				Integer.parseInt(QuvaExecutionSettings.settings.get("simAnnealingIterations")),
				Integer.parseInt(QuvaExecutionSettings.settings.get("simAnnealingSamples"))
				);
	}
	/**Runs simulated annealing with the nmber of iterations and number of samples
	 * @param matrix matrix to be processed
	 * @param maxiterations maximum number of iterations per simulated annealing
	 * @param number maximum simulated annealing
	 * @return solution given
	*/
	public static int[] bundleExecution(float[][] matrix, int maxiterations,int number) {
		PriorityQueue<SimAnnealingSolution> pq=new PriorityQueue<SimAnnealingSolution>();
		for(int i=0;i<number;i++)
			pq.add(runSimulatedAnnealing(matrix,maxiterations));
		return pq.remove().sol;
	}
	/**Runs simulated annealing once
	 * @param matrix matrix to be processed
	 * @param maxiterations number of iterations
	 * @return solution found
	 */
	public static SimAnnealingSolution runSimulatedAnnealing(float[][] matrix, int maxiterations) {
		  int n=matrix.length;
		  int[] solution = new int[n];
		  float temp=-1;
		  String settingTemp=QuvaExecutionSettings.settings.get("simAnnealingStartingTemp");
		  if(!settingTemp.equals("auto")) temp=Float.parseFloat(settingTemp);
		  float temp_delt=Float.parseFloat(QuvaExecutionSettings.settings.get("simAnnealingTempChange"));
		  float cost2=0;
		  for (int iter=0; iter<maxiterations; iter++) {
		    float cost1 = cal(solution, matrix);
		    int x = (int) (Math.random()*n);
		    solution[x]=solution[x]==1?0:1;
		    cost2 = cal(solution, matrix);
		    float dif=cost1-cost2;
		    if(dif<0&&temp<0) temp=dif/(float)Math.log(0.99d);
		    QuvaDebug.log("simann.iteration","Iteration start "+(dif<0)+" "+cost1+" "+cost2+"  "+x+"  "+Math.exp(dif/temp)+"  "+temp);
		    if ((dif<0 && (Math.random()>=Math.exp(dif/temp)))) {
		      solution[x]=solution[x]==1?0:1;
		    }

		    temp*=temp_delt; //0.99
		  }
		  return new SimAnnealingSolution(cost2,solution);
		}
		/**Calculates the costs
		 * @param sol vector that are put in
		 * @param matrix matrix to be processed
		 * @return cost of the solution
		 */
		public static float cal(int[] sol, float[][] matrix) {
		  int ret=0;
		  for (int i=0; i<sol.length; i++) {
		    for (int j=i; j<sol.length; j++) {
		      ret+=matrix[i][j]*sol[i]*sol[j];
		    }
		  }

		  return ret;
		}
}
