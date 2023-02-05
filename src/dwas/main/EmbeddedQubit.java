package dwas.main;

public class EmbeddedQubit implements Comparable<EmbeddedQubit>{
	int compareValue=0;
	int carriedValue=0;
	public EmbeddedQubit(int comparedValue,int carriedValue) {
		this.compareValue=comparedValue;
		this.carriedValue=carriedValue;
	}
	@Override
	public int compareTo(EmbeddedQubit o) {
		return compareValue-o.compareValue;
	}
	public int getValue() {
		return carriedValue;
	}
}
