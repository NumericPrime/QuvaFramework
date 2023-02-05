package quva.core;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import dwas.main.QUBOEmbedder;
import dwas.main.QUBOProblem;
import dwas.main.SimulatedAnnealingSolver;

/**This class handles the execution of the Hamilton-Matrix using an own class with static methods allows multithreading the execution of multiple quantumannealer-programs*/
public class ExecuteProgram extends QuvaUtilities{
	/**Here the locations of the python scripts is saved*/
	  public static volatile File execScriptsRoot=null;
	  static {
			try {
				String pathString=QUBOMatrix.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				String regex="\\\\[^\\\\]*?\\.jar";
				if(pathString.contains(".jar"))execScriptsRoot=new File(pathString,"..\\pythonScriptsRoot");
				else
				execScriptsRoot=new File(pathString.replaceAll(regex, "\\\\"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	  /**Simulates the quantumannealer. When multithreading only one thread can run this method at once.
	   * @return the direct output of the python-script (unprocessed)
	   * @see quva.core.QUBOMatrix#simulate()*/
	  public synchronized static String simulateStatic() {
		  File outputFilePython=new File(execScriptsRoot,"input.txt");
		  try {
			Files.write(outputFilePython.toPath(), "".getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			try{
		//int orgLength=fileSize(new File(execScriptsRoot,"input.txt").getAbsolutePath());
		//Process p=Runtime.getRuntime().exec("cmd /c py QuboTestFromAPI.py",null,execScriptsRoot);
		ProcessBuilder pb=new ProcessBuilder("python",new File(execScriptsRoot,"QuboTestFromAPI.py").getAbsolutePath());
		pb.redirectErrorStream(true);
		
		Process p=pb.start();
		//PipedInputStream pis=new PipedInputStream();
		while(p.isAlive()) {
			/*if(p.getInputStream().available()>0) 
				{*/
				String ret=new String(p.getInputStream().readAllBytes());
				//System.out.println(ret);
				return ret;
				//p.getInputStream().skip(p.getInputStream().available());
				/*}*/
			/*if(pis.available()>0) {
			System.out.println(new String(pis.readAllBytes()));
			System.out.println("Test");
			}*/
		}
		p.destroy();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("Trying to retrieve");
		return readFile(new File(execScriptsRoot,"input.txt").getAbsolutePath());
	  }
	  /**Runs the matrix on the quantumannealer. When multithreading only one thread can run this method at once.
	   * @return the direct output of the python-script (unprocessed)
	   * @see quva.core.QUBOMatrix#runDWave()*/
	  public synchronized static String runDWaveStatic() {
		  File outputFilePython=new File(execScriptsRoot,"input.txt");
		  try {
			Files.write(outputFilePython.toPath(), "".getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  try {
			//int orgLength=fileSize(new File(execScriptsRoot,"input.txt").getAbsolutePath());
			ProcessBuilder builder=new ProcessBuilder("python",new File(execScriptsRoot,"QuboFromAPI.py").getAbsolutePath());
			builder.redirectErrorStream(true);
			
			Process p=builder.start();
			while(/*fileSize(new File(execScriptsRoot,"input.txt").getAbsolutePath())<=orgLength&&*/p.isAlive()) {
				//(((char)p.getInputStream().read()));
				//p.getInputStream().read();

				//System.out.println("creating output");
				//if(p.getInputStream().available()>0) {
				String ret=new String(p.getInputStream().readAllBytes());
				//System.out.println(ret);
				return ret;
				//}
				
				//if(p.getInputStream().available()>0) p.getInputStream().skip(p.getInputStream().available());
				//if(p.getOutputStream()) ;
				//System.out.println(p.getInputStream().available());
				//System.out.println(fileSize(new File(root,"console.txt").getAbsolutePath()));
			}
			p.destroy();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("Trying to retrieve");
		return readFile(new File(execScriptsRoot,"input.txt").getAbsolutePath());
	  }
	  /**Executes the written matrix and returns the values of the qubits. The results can be processed using readVar/readAllVars
   * @param mode way the hamilton matrix is processed<br>SIMULATE uses qubosolv to simulate the quantumannealer<br>ANNEAL sends the matrix to a real quantumannealer<br>You can add AUTOTRUNCATE to adjust the qubits of the variables.<br>This works like:<br>
   * SIMULATE+AUTOTRUNCATE
   * <br>
   * Adding +DELAYED to the mode will have no influence on this command.<br>
   * AUTOCHAINSTRENGTH will set the chain-strength to half the highest abs of the matrix.<br>
   * CUSTOMSETTINGS will make execution ignore the parameters specified in {@link quva.core.QuvaExecutionSettings#executionSettings(int[])}
   * @param push determines whether to refresh the saved matrix
   * @param m the matrix to be executed
   * @return the solution of the {@code QUBOMatrix} as an {@code int[]}
   * @see quva.core.QUBOMatrix#execute(int,boolean)
   * @see quva.core.QuvaUtilities#SIMULATE
   * @see quva.core.QuvaUtilities#ANNEAL
   * @see quva.core.QuvaUtilities#AUTOTRUNCATE
   * @see quva.core.QuvaUtilities#AUTOCHAINSTRENGTH
   * @see quva.core.QuvaUtilities#CUSTOMSETTINGS*/
	  public static int[] execute(int mode,boolean push,QUBOMatrix m) {
		  if(((mode>>4)&1)!=CUSTOMSETTINGS>>4) mode=mode|QuvaExecutionSettings.standardSetting;
		  float chainStr=m.chainStrength;
		  if(((mode>>3)&1)==((AUTOCHAINSTRENGTH>>3)&1)) {
			  chainStr=m.matrix[0][0]/2;
			  for(int i=0;i<m.matrix.length;i++) for(int j=i;j<m.matrix.length;j++) if(Math.abs(m.matrix[i][j])/2>chainStr) chainStr=(float) Math.abs(m.matrix[i][j])/2;
		  }
		  if((mode&1)==ANNEAL) {
			  m=m.objectClone();
			  for(int i=0;i<m.matrix.length;i++) for(int j=i;j<m.matrix.length;j++)
				  m.matrix[i][j]/=chainStr;
			  }
		  //System.out.println(m);
		  if(push) m.push();
		  if(((mode>>1)&1)==((AUTOTRUNCATE>>1)&1)) m.truncate();
		  if((mode&1)==SIMULATE&&((mode>>5)&1)!=INTERNAL>>5) return m.process(simulateStatic(),SIMULATE);
		  if((mode&1)==ANNEAL&&((mode>>5)&1)!=INTERNAL>>5) return m.process(runDWaveStatic(),ANNEAL);
		  QUBOEmbedder.chainStrength=m.chainStrength;
		  if((mode&1)==ANNEAL) return QUBOProblem.construct(m.matrix, 
				  QuvaExecutionSettings.settings.get("token"),
				  "Advantage_system4.1")
				  .executeEmbedded(Integer.parseInt(QuvaExecutionSettings.settings.get("samples")))[0];
		  if((mode&1)==SIMULATE) return SimulatedAnnealingSolver.solve(m.matrix);
		  return null;
	  }
}
