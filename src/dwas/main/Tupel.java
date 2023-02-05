package dwas.main;

public class Tupel {
	int i,j;
	public Tupel(int i,int j) {
		this.i=i;
		this.j=j;
	}
	@Override
	public boolean equals(Object e) {
		if(!(e instanceof Tupel)) return false;
		Tupel t=(Tupel) e;
		if(t.i==i&&t.j==j) return true;
		if(t.i==j&&t.j==i) return true;
		return false;
	}
	@Override
	public int hashCode() {
		int getBinaryLength=Integer.bitCount(i+j);
		return i+j+(i*j)<<(getBinaryLength+1);
	}
	public int[] getVal() {
		if(i<j) return new int[] {i,j};
		else return new int[] {j,i};
	}
}
