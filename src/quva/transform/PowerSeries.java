package quva.transform;

import java.util.*;

import quva.construct.CarryRule;
import quva.construct.QuvaConstruct;
import quva.construct.QuvaConstructRegister;
import quva.construct.SafeConstruct;
import quva.core.QUBOMatrix;
import quva.core.QuvaDebug;

/**Can create (and register) a power of a single variable.
 * It works with <b>vastly</b> less qubits than you would need with just chaining registerMultiply. This class is used by {@code QuvaPolynomial} to create a polynomial equation.
 * @see quva.transform.QuvaPolynomial*/
public class PowerSeries implements SingleVarTransformation,QuvaConstruct {
	/**CarryRule to be applied
	 * @see quva.construct.CarryRule*/
	public CarryRule appliedRule=SafeConstruct.standardRule;
	QuvaConstructRegister reg=new QuvaConstructRegister();
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
			int bit=0;
			if(counter==1) allocBit.put(keys,bit=m.find(var)[getter]); else
			allocBit.put(keys,bit=m.Qubits.next());
			reg.put(QuvaConstructRegister.convert(keys), bit);
		}
		QuvaDebug.log("PowerSeries.apply", "Used bits "+allocBit.size()+" "+reg.size());
		//relocAll();
		reprocess();
		/*for(List<Boolean> keys:allocWeight.keySet()) {
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
		//System.out.println(bitsmax);
		//System.out.println(bitsmin);
		float maxWeight=(1<<bitsmax);
		lowerPower(power,"pow<"+lastVarUsed+"><"+power+">");
		lastMatrixUsed.register(var,bitstotal,maxWeight);
		lastMatrixUsed.linearEquation("pow<"+lastVarUsed+"><"+power+">-"+var);
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
	/**Part of the algorithm to make the PowerSeries work*/
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
	/**Part of the algorithm to make the PowerSeries work
	 * @param map map to be used
	 * @param key key to be used
	 * @param value value to be used*/
	public void increment(Map<List<Boolean>,Float> map,List<Boolean> key,float value) {
		if(map.containsKey(key)) map.replace(key, value+map.get(key));
		else map.put(key, value);
	}
	/**Optimization algorithm to make the Hamiltonmatrix more sparse
	 * @deprecated This doesn't work yet!
	 * @param a first key
	 * @param b second key*/
	@Deprecated
	public void reloc(List<Boolean> a,List<Boolean> b) {
		QuvaDebug.log("PowerSeries.reloc", "\nRelocing:",QuvaDebug.BooleanOutput((Boolean[]) a.toArray(new Boolean[a.size()])), QuvaDebug.BooleanOutput((Boolean[]) b.toArray(new Boolean[b.size()])));
		boolean authorize=true;
		authorization: {
			Boolean[] and=(Boolean[]) b.toArray(new Boolean[b.size()]);
			for(int i=0;i<and.length;i++) if(a.get(i)) and[i]=true;
			if(!reg.containsKey(QuvaConstructRegister.convert(and))) authorize=false;
			//if(a.equals(b)) authorize=false;
			//List<Boolean> andAsList=Arrays.asList(and);
			//if(a.equals(andAsList)||b.equals(andAsList)) authorize=false;
			/*
		int found=0;
		int found2=0;
		if(a.equals(b)) {
			authorize=false;
			break authorization;
		}
		loop: {
			for(Boolean elm:a) if(elm) found++;
			if(found>=2) break loop;
			authorize=false;
			break authorization;
		}
		loop: {
			for(Boolean elm:b) if(elm) found2++;
			if(found2>=2) break loop;
			authorize=false;
			break authorization;
		}
		QuvaDebug.log("PowerSeries.reloc.checks", "\nChecking:",found,found2);
		
			if(found==found2+1) authorize=false;
			if(found==found2-1) authorize=false;*/
		}
		//authorize=true;
		authorized:{
			if(!authorize) break authorized;
			float val=lastMatrixUsed.get(allocBit.get(a), allocBit.get(b));
			lastMatrixUsed.set(0,allocBit.get(a), allocBit.get(b));
			Boolean[] and=(Boolean[]) b.toArray(new Boolean[b.size()]);
			for(int i=0;i<and.length;i++) if(a.get(i)) and[i]=true;
			QuvaDebug.log("PowerSeries.reloc","\nAuthorization Successful!",a+"\n"+b+"\n->"+Arrays.asList(and),allocBit.get(a)+" "+allocBit.get(b)+"->"+allocBit.get(Arrays.asList(and)),"Value "+val);
			QuvaDebug.log("PowerSeries.reloc","Recalling",lastMatrixUsed.get(allocBit.get(Arrays.asList(and)), allocBit.get(Arrays.asList(and))));

			lastMatrixUsed.addBasic(val, allocBit.get(Arrays.asList(and)), allocBit.get(Arrays.asList(and)));
			QuvaDebug.log("PowerSeries.reloc","New value",lastMatrixUsed.get(allocBit.get(Arrays.asList(and)), allocBit.get(Arrays.asList(and))));


		}
	}
	/**Optimization algorithm to make the Hamiltonmatrix more sparse
	 * @deprecated This doesn't work yet!*/
	@Deprecated
	public void relocAll() {
		QuvaDebug.log("PowerSeries.relocAll","Starting reloc");
		BruteForceIterator it=new BruteForceIterator(2,retw.length);
		BruteForceIterator it2=new BruteForceIterator(2,retw.length);
		it.next();
		it2.next();
		int i=0;
		for(int[] comb:it) {
			for(int[] comb2:it2) {
				Boolean[] bool1=conv(comb);
				Boolean[] bool2=conv(comb2);
				if(reg.containsKey(QuvaConstructRegister.convert(bool1)))
					if(reg.containsKey(QuvaConstructRegister.convert(bool2)))
						reloc(Arrays.asList(bool1),Arrays.asList(bool2));
			}
			it2=new BruteForceIterator(2,retw.length);
			i++;
			for(int j=0;j<=i;j++) it2.next();
		}
	}
	/**{@inheritDoc}*/
	@Override
	public QuvaConstructRegister getRegistry() {
		return reg;
	}
	/**{@inheritDoc}*/
	@Override
	public void process() {
	}
	/**Reapplies the Construct. You can e.g. change amplification or layer and reapply the construct.*/
	public void reprocess() {
		new SafeConstruct(var,appliedRule,this,lastMatrixUsed).process();
	}

}
