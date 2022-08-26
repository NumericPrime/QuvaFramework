package quva.transform;

import java.util.*;

import quva.core.QUBOMatrix;

/**Can create (and register) a power of a single variable.
 * It works with <b>vastly</b> less qubits than you would need with just chaining registerMultiply. This class is used by {@code QuvaPolynomial} to create a polynomial equation.
 * @see quva.transform.QuvaPolynomial*/
public class PowerSeries implements SingleVarTransformation {
	/**Name of the variable in wich the highest power gets saved*/
	public String var;
	/**<b>highest</b> power used in the series*/
	public int power=0;
	/**This constructor takes the maximum power in the series and the name of the variable in wich it gets saved.
	 * @param power_ the <b>highest</b> power to be used in the series
	 * @param vr the name of the variable in which the result gets saved*/
	public PowerSeries(int power_,String vr){
		var=vr;
		power=power_;
	}
	/**Cloning constructor
	 * @param s the {@code PowerSeries} to be cloned*/
	public PowerSeries(PowerSeries s){
		var=s.var;
		power=s.power;
		base=s.base;
		allocBit=s.allocBit;
		retw=s.retw;
		lastMatrixUsed=s.lastMatrixUsed;
	}
	/**Name of the variable to which the power series gets applied*/
	String lastVarUsed;
	/**The {@code QUBOMatrix} to be manipulated*/
	QUBOMatrix lastMatrixUsed;
	/**{@inheritDoc}*/
	@Override
	@quva.core.Registers
	public void apply(QUBOMatrix m, String var) {
		lastVarUsed=var;
		lastMatrixUsed=m;
		retw=m.findWeight(var);
		for(int[] r:new ProductIterator(1,retw.length)) incre(r);
		for(int i=1;i<power;i++) {
			savedPowers.add(new WeakHashMap<List<Boolean>, Float>(allocWeight));
			advancePower();
		}
		savedPowers.add(new WeakHashMap<List<Boolean>, Float>(allocWeight));
		for(List<Boolean> keys:allocWeight.keySet()) if(!allocBit.containsKey(keys)){
			int counter=0;
			int getter=0;
			for(Boolean cur:keys) if(cur) counter++;
			base.put(keys, counter==1);
			if(counter==1) for(int i=0;i<keys.size();i++) if(keys.get(i)) getter=i;
			if(counter==1) allocBit.put(keys,m.find(var)[getter]); else
			allocBit.put(keys,m.Qubits.next());
			}
		for(List<Boolean> keys:allocWeight.keySet()) {
			Boolean[] el=keys.toArray(new Boolean[] {});
			//int j=0;
			if(!base.get(keys)) {
				for(int i=0;i<el.length;i++) {
					if(el[i]) {
						//j=i;
						el[i]=false;
						m.link(allocBit.get(keys), allocBit.get(Arrays.asList(el)), m.find(var)[i]);
						break;
					}
				}
			}
		}/*
		List<Float> weights=new LinkedList<Float>();
		List<Integer> bits=new LinkedList<Integer>();
		for(List<Boolean> keys:allocWeight.keySet()) {
			weights.add(allocWeight.get(keys));
			bits.add(allocBit.get(keys));
		}
		Integer[] bitsArray1=bits.toArray(new Integer[1]);
		Float[] weightsArray1=weights.toArray(new Float[1]);
		int[] bitsArray2=new int[bitsArray1.length];
		float[] weightsArray2=new float[bitsArray1.length];
		for(int i=0;i<bitsArray1.length;i++) {
			bitsArray2[i]=bitsArray1[i];
			weightsArray2[i]=weightsArray1[i];
		}
		m.register(this.var, bitsArray2.clone(), weightsArray2.clone());*/
		lowerPower(power,this.var);
		//System.out.println("apply call");
	}
	/**Here you can use the carries created by apply to create a new variable containing a power <b>lower or equal</b> to the one made by apply. This method has to be used <b>after</b> apply.
	 * @param power the power which gets calculated
	 * @param varName name of the new variable*/
	@quva.core.Registers
	public void lowerPower(int power,String varName) {
		//System.out.println(power+" "+varName);
		QUBOMatrix m=lastMatrixUsed;
		//String var=lastVarUsed;
		/*ProductIterator it=new ProductIterator(power,retw.length);
		for(int[] r:it) incre(r);*/
		/*for(int[] r:new ProductIterator(1,retw.length)) incre(r);
		
		for(int i=1;i<power;i++)advancePower();/**/
		if(power<=savedPowers.size()) allocWeight=savedPowers.get(power-1);
		List<Float> weights=new LinkedList<Float>();
		List<Integer> bits=new LinkedList<Integer>();
		//System.out.println();
		for(List<Boolean> keys:allocWeight.keySet()) {
			weights.add(allocWeight.get(keys));
			bits.add(allocBit.get(keys));
			/*System.out.print(allocBit.get(keys));
			System.out.print(" ");
			System.out.print(keys);
			System.out.print(" ");
			System.out.print(allocWeight.get(keys));
			System.out.println();*/
		}
		Integer[] bitsArray1=bits.toArray(new Integer[bits.size()]);
		Float[] weightsArray1=new Float[weights.size()];
		weights.toArray(weightsArray1);
		int[] bitsArray2=new int[bitsArray1.length];
		float[] weightsArray2=new float[bitsArray1.length];
		for(int i=0;i<bitsArray1.length;i++) {
			//System.out.println(bitsArray1[i]+"  "+i+"/"+bitsArray1.length+"  "+power);
			bitsArray2[i]=bitsArray1[i];
			weightsArray2[i]=weightsArray1[i];
			//System.out.println(bitsArray1[i]+" "+weightsArray1[i]+" "+power+"    "+bitsArray1.length);
		}
		weights.clear();
		m.register(varName, bitsArray2.clone(), weightsArray2.clone());		
	}
	/**This saves a power in a new variable with less qubits
	 * @param power power of the variable
	 * @param var name of the new Variable*/
	@quva.core.Registers
	public void push(int power,String var) {
		float highest=retw[0];
		float lowest=retw[retw.length-1];
		if(highest>0) highest=2*highest-lowest; else highest*=-1;
		int bitsmax=(int)(quva.core.QuvaUtilities.log2((float) Math.pow(highest,power)));
		int bitsmin=(int)(quva.core.QuvaUtilities.log2((float) Math.pow(lowest,power)));
		int bitstotal=bitsmax-bitsmin;
		System.out.println(bitsmax);
		System.out.println(bitsmin);
		float maxWeight=(1<<bitsmax);
		System.out.println(bitstotal);
		System.out.println(maxWeight);
		lowerPower(power,"pow<"+lastVarUsed+"><"+power+">");
		lastMatrixUsed.register(var,bitstotal,maxWeight);
	}
	public List<Map<List<Boolean>,Float>> savedPowers=new ArrayList<Map<List<Boolean>,Float>>();
	/**Buffer for the weights of the variable to be transformed*/
	public float retw[];
	/**Buffer for the weights of the new variable*/
	public Map<List<Boolean>,Float> allocWeight=new WeakHashMap<List<Boolean>,Float>();
	/**Here the carries needed are saved.*/
	public Map<List<Boolean>,Integer> allocBit=new WeakHashMap<List<Boolean>,Integer>();
	/**Here the info is saved if a combination is part of the original variable*/
	public Map<List<Boolean>,Boolean> base=new WeakHashMap<List<Boolean>,Boolean>();
	/**This method checks if individual entries of an array are &gt;0
	 * @param inp array to be entered
	 * @return array that contains info about the sign of the arrays*/
	Boolean[] conv(int[] inp){
		Boolean[] ret=new Boolean[inp.length];
		for(int i=0;i<ret.length;i++) ret[i]=inp[i]>0;
		return ret;
	}
	/**Adds the weights corresponding to a certain element of the ProductIterator
	 * @param inp input to be used*/
	public void incre(int[] inp) {
		Boolean[] r=conv(inp);
		float w=1;
		List<Boolean> imp=Arrays.asList(r);
		//for(Boolean l:imp) System.out.println(l);
		//System.out.println("");
		for(int i=0;i<inp.length;i++) w*=Math.pow(retw[i],inp[i]);
		float old=0;
		if(allocWeight.containsKey(imp)) old=allocWeight.get(imp);
		else allocWeight.put(imp, old);
		
		allocWeight.replace(imp, old+w);
	}
	public void advancePower() {
		Map<List<Boolean>,Float> newAllocWeight=new WeakHashMap<List<Boolean>,Float>();
		Map<List<Boolean>,Float> oldAllocWeight=allocWeight;
		for(int i=0;i<retw.length;i++) {
			for(List<Boolean> l:oldAllocWeight.keySet()) {
				List<Boolean> dupe=Arrays.asList(l.toArray(new Boolean[1]));
				dupe.set(i, true);
				//System.out.println(dupe);
				increment(newAllocWeight,dupe,retw[i]*oldAllocWeight.get(l));
			}
		}
		//System.out.println("");
		allocWeight=newAllocWeight;
	}
	public void increment(Map<List<Boolean>,Float> map,List<Boolean> key,float value) {
		if(map.containsKey(key)) map.replace(key, value+map.get(key));
		else map.put(key, value);
	}
}
