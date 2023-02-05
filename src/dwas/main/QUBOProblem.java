package dwas.main;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.util.*;

import quva.core.QuvaException;

/**This class handles communication with the quantumannealer from java*/
public class QUBOProblem {
	/**Number of qubits used*/
	int qubitsnum;
	/**API-Token used for accessing the DWave-server*/
	public String API_TOKEN="";
	/**Solver to be used*/
	public String savedSolver="";
	/**Number of the solvers qubits*/
	public static int accessible_qubits=5730;
	/**Embeded QUBO-Problem (the keys tell what qubits are involved and the value is the value of the gate)*/
	Map<int[], Double> embeded;
	/**Used to make one of the constructors work
	 * @deprecated The constructor uses a bad implementation that is e.g. not thread-save*/
	@Deprecated
	public static String internalSavedSolver="";
	//protected static Map<Integer,List<Integer>> internalChains=null;
	/**Here the details of the embedding are saved*/
	public static Map<Integer,List<Integer>> chains=new HashMap<Integer,List<Integer>>();
	/**The DWave-Servers home*/
	public static final String SAPI_HOME="https://na-west-1.cloud.dwavesys.com/sapi/v2";
	/**This constructor takes an already embeded QUBO-Problem, the number of qubit to be freed and the API_Token
	 * @param qubitsnum Highest qubit-number used
	 * @param embeded embeded QUBO-Problem. The key is a array consisting of <b>two</b> elements. These elements tell the involved qubtis used in a gate
	 * @param API_TOKEN token used to access the DWave-Solvers*/
	public QUBOProblem(int qubitsnum,Map<int[], Double> embeded,String API_TOKEN) {
		this.qubitsnum=qubitsnum;
		this.embeded=embeded;
		this.API_TOKEN=API_TOKEN;
	}
	/**Saves the server responses*/
	public String[] recordedResults=new String[2];
	/**Creates the String to be sent as the QUBO-problem
	 * @return the problem data in a format that can be sent*/
	public String constructData() {
		String ret="";
		int gatecount=embeded.keySet().size();
		ret+=qubitsnum+" "+gatecount;
		for(int[] key:embeded.keySet())
			ret+="\\n"+key[0]+" "+key[1]+" "+embeded.get(key);
		System.out.println(ret.replaceAll("\\n", "\n"));
		return ret;
	}
	/**Constructor that takes the matrix,token and solver used. 
	 * @param matrix hamilton-matrix to be used
	 * @param API_TOKEN api-token to be used to access the server
	 * @param solver solver this will get run on.
	 * @deprecated this is a bad and non-thread-safe implementation use {@link #construct(float[][], String, String)} instead.*/
	@Deprecated
	public QUBOProblem(float[][] matrix,String API_TOKEN,String solver) {
		this(accessible_qubits,QUBOEmbedder.embedQubo(matrix, internalSavedSolver=solver, API_TOKEN,chains),API_TOKEN);
		savedSolver=internalSavedSolver;
		//chains=internalChains;
	}
	/**Factory-method used to create QUBOProblems
	 * @param matrix hamilton-matrix to be used
	 * @param API_TOKEN api-token to be used to access the server
	 * @param solver solver this will get run on.
	 * @return the created QUBO-Problem*/
	public static QUBOProblem construct(float[][] matrix,String API_TOKEN,String solver) {
		QUBOProblem ret=new QUBOProblem(accessible_qubits,QUBOEmbedder.embedQubo(matrix, solver, API_TOKEN,chains),API_TOKEN);
		ret.savedSolver=solver;
		return ret;
	}
	/**Gives an array mapping the physical Qubits to the matching logical ones
	 * @return array where the indices are the qubits on the chip and the values the according logical ones.*/
	int[] assignEmbeddedQubits() {
		PriorityQueue<EmbeddedQubit> pq=new PriorityQueue<EmbeddedQubit>();
		for(int i:chains.keySet()) for(int bit:chains.get(i)) pq.add(new EmbeddedQubit(bit,i));
		int[] ret=new int[pq.size()];
		for(int i=0;i<ret.length;i++) ret[i]=pq.remove().getValue();
		return ret;
	}
	/**The main execution-method this will return the values of the unembeded-qubits aka the logic qubits
	 * @param samples number of samples to be done
	 * @return the result of the Quantumannealer with reversed Embedding.*/
	public int[][] executeEmbedded(int samples) {
		send(savedSolver,"qubo",samples);
		waitForCompletion();
		int[] assigned=assignEmbeddedQubits();
		System.out.println(allSals);
		int[][] decoded=decodeAll(allSals, qubitsused, solutions);
		int[][] ret=new int[decoded.length][chains.size()];
		System.out.println("Result "+chains.size()+" "+decoded[0].length+" ||");
		for(int j=0;j<assigned.length;j++) System.out.print(" "+assigned[j]);
		for(int i=0;i<ret.length;i++) {
			System.out.println();
		for(int j=0;j<decoded[0].length;j++) System.out.print(decoded[i][j]+" ");
			int[] vote=new int[chains.size()];
			for(int j=0;j<assigned.length;j++) vote[assigned[j]]+=(decoded[i][j]==0?-1:1);
			//System.out.println("Original Sol:");
			//for(int j=0;j<assigned.length;j++) System.out.print(decoded[i][j]+" ");

			for(int j=0;j<ret[i].length;j++) ret[i][j]=(vote[j]>0?1:0);
		}
		return ret;
	}
	/**Sends the Problem to the DWave-Solvers
	 * @param solver solver the problem gets sent to
	 * @param type type of the Problem either qubo or ising
	 * @param samples number of samples*/
	public void send(String solver,String type,int samples) {
		try {
			HttpRequest postRequest=HttpRequest.newBuilder()
					.uri(new URI(SAPI_HOME+"/problems"))
					.header("X-Auth-Token", API_TOKEN)
					.POST(BodyPublishers.ofString("[{\"type\":\"qubo\""
							+ ",\"solver\":\""+solver+"\",\"data\": \""+constructData()+"\",\"params\":{\"answer_mode\":\"histogram\",\"num_reads\":"+samples+"}}]"
							)
							).build();//\"128 3\\n1 1 -1\\n2 2 1\\n1 5 -3\"
			HttpClient httpClient=HttpClient.newHttpClient();
			HttpResponse<String> postResponse=httpClient.send(postRequest, BodyHandlers.ofString());
			String reply="";
			reply=postResponse.body();
			recordedResults[0]=reply;
			//System.out.println(reply);
			reply=reply.replaceFirst(".+?\"id\": *\"","");
			reply=reply.replaceFirst("\".*","");
			id=reply;
			//System.out.println(reply);
		}
		catch(Exception e) {
			throw new QuvaException("A exception occured while connecting (Probable cause: no internet connection)",e);
		}
	}
	/**Saves the number of qubits used*/
	int qubitsused=0;
	/**Saves the number of solutions*/
	int solutions=0;
	/**Saves solutions returned by the DWave*/
	String allSals="";
	/**Fetches the result of the quantumannealer*/
	public void get() {
		try {
			HttpRequest postRequest=HttpRequest.newBuilder()
				.uri(new URI(SAPI_HOME+"/problems/"+id+"/answer"))
				//.uri(new URI(SAPI_HOME+"/solvers/remote"))
				.header("X-Auth-Token", API_TOKEN)
				.GET().build();
		String rep;
		HttpClient httpClient=HttpClient.newHttpClient();
		HttpResponse<String> postResponse=httpClient.send(postRequest, BodyHandlers.ofString());

		//System.out.println("DWave-Response");
		rep=postResponse.body();
		recordedResults[1]=rep;
		String copy=rep.replaceFirst(".+?\"solutions\" *: *\"", "");
		copy=copy.replaceFirst("\".*", "");
		allSals=(copy);
		String copy2=rep.replaceFirst(".+?\"energies\" *: *\\[", "");
		copy2=copy2.replaceFirst("\\].*", "");
		solutions=(copy2.split(",").length);
		String copy3=rep.replaceFirst(".+?\"active_variables\" *: *\\[", "");
		copy3=copy3.replaceFirst("\\].*", "");
		qubitsused=(copy3.split(",").length);
		System.out.println(rep);
		}
		catch(Exception e) {
			throw new QuvaException("A exception occured while connecting (Probable cause: no internet connection)",e);
		}
	}
	/**Checks if the request has been completed
	 * @return true if the solution can be fetched, false if not.*/
	public boolean checkStatus() {
		String info=getInfo();
		String edited=info.replaceFirst(".*?\"status\" *?: *?\"", "");
		edited=edited.replaceFirst("\".*", "");
		return edited.matches("(?i)COMPLETED");
	}
	/**Waits until the problem is completed (this method will check every 500ms if the problem has been solved)*/
	public void waitForCompletion() {
		while(!checkStatus()) {
			try {
				Thread.sleep(500);
				//System.out.println(getInfo());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		get();
		//System.out.println("execution done");
	}
	/**Gets info about the current status
	 * @return Info fetched from the problem status from the DWave-Server*/
	public String getInfo() {
		try {
			HttpRequest postRequest=HttpRequest.newBuilder()
				.uri(new URI(SAPI_HOME+"/problems/"+id+"/info"))
				//.uri(new URI(SAPI_HOME+"/solvers/remote"))
				.header("X-Auth-Token", API_TOKEN)
				.GET().build();
		String rep;
		HttpClient httpClient=HttpClient.newHttpClient();
		HttpResponse<String> postResponse=httpClient.send(postRequest, BodyHandlers.ofString());

		//System.out.println("Fetched Info");
		rep=postResponse.body();
		return rep;
		}
		catch(Exception e) {
			throw new QuvaException("A exception occured while connecting (Probable cause: no internet connection)",e);
		}
	}
	/**Id of the qubo-problem*/
	public String id="";
	/**Decodes the solutions of the qubo-problem from Base64
	 * @param message encoded string
	 * @param numqb number of bits per solution
	 * @param nums number of solutions
	 * @return All the solutions returned by the Quantumannealer*/
	public int[][] decodeAll(String message,int numqb,int nums) {
		//System.out.println(numqb+" "+nums);
		int[] ret1=fetchBits(message);
		int[][] ret=new int[nums][numqb];
		for(int i=0;i<nums;i++)
			for(int j=0;j<numqb;j++) {
				ret[i][j]=ret1[i*numqb+j];
			}
		
		return ret;
	}
	/**Decomposes a Base64-string into bits
	 * @param message String to be decoded
	 * @return the values of the Qubits*/
	public int[] fetchBits(String message) {
		int[] ret1=null;
		byte[] dec=Base64.getDecoder().decode(message);
		ret1=new int[dec.length*8];
		for(int j=0;j<dec.length;j++) 
			for(int i=0;i<8;i++) ret1[j*8-i+7]=((dec[j]>>i)&1);
		//System.out.println("Recieved bits "+ret1.length);
		//for(int i=0;i<ret1.length;i++)  System.out.print(ret1[i]+" ");
		return ret1;
	}
	/**Gets the solver info for a specific solver (this is a instance-method because the token is needed to execute this)
	 * @param solver solver to be analysed
	 * @return info by from the DWave-Server*/
	public String requestInfo(String solver) {
		try {
		HttpRequest postRequest=HttpRequest.newBuilder()
				.uri(new URI(SAPI_HOME+"/solvers/remote/"+solver))
				.header("X-Auth-Token", API_TOKEN)
				//.uri(new URI(SAPI_HOME+"/solvers/remote"))
				.GET().build();
		HttpClient httpClient=HttpClient.newHttpClient();
		HttpResponse<String> postResponse=httpClient.send(postRequest, BodyHandlers.ofString());
		return postResponse.body();
		}catch(Exception e) {
			throw new QuvaException("A exception occured while connecting (Probable cause: no internet connection)",e);}
	}
	/**Gets the list of the qubit-connections of the solver
	 * @param solver solver to be analysed
	 * @return a 2d containing the couplers of the quantumannealer. Each entry contains two entries containing the two Qubits that can be coupled.*/
	public int[][] getedges(String solver){
		String raw=requestInfo(solver);
		String processed=raw
				.replaceFirst(".*?\"couplers\" *: *\\[", "")
				.replaceFirst("\\]\\].*", "").substring(1);
		String[] splittedStr=processed.split("\\],\\[");
		//for(String n:splittedStr) System.out.println(n);
		int ret[][]=new int[splittedStr.length][2];
		for(int i=0;i<splittedStr.length;i++) {
			String splitted[]=splittedStr[i].split(",");
			ret[i][0]=Integer.parseInt(splitted[0]);
			ret[i][1]=Integer.parseInt(splitted[1]);
		}
		return ret;
	}
}
