package dwas.main;

public class SimAnnealingSolution implements Comparable<SimAnnealingSolution> {
	float val=0;
	int[] sol=null;
	SimAnnealingSolution(float val,int[] sol){
		this.val=val;
		this.sol=sol;
	}
	@Override
	public int compareTo(SimAnnealingSolution o) {
		return (o.val-val)>0?-1:1;
	}
}
