package quva.postprocessing;

import java.io.PrintStream;
import java.util.Map;

import quva.core.QUBOMatrix;
/**This {@code PostProcessingHandler} creates a grid out of the qubit values. This will generate outputs like:
 * <pre>
 * 0010
 * 1000
 * 0001
 * 0100
 * </pre> but using a custom style can also generate outputs like:
 * <pre>
 * OOOXO
 * OXOOO
 * OOXOO
 * YYYYY
 * </pre>
 * @see SingleBitOutput*/
public class OutputGrid implements PostProcessingHandler{
	public int w;
	public static PrintStream output=null;
	public SingleBitOutput style=(value,index,x,y)->value+"";
	/**This constructor only takes the number of columns and uses the a custom style.
	 * @param w number of columns (if &lt;0 then abs(w) is the number of rows, if =0 then a square grid will be used)
	 * @param style style used*/
	public OutputGrid(int w,SingleBitOutput style) {
		this.w=w;
		this.style=style;
	}
	/**This constructor only takes the number of columns and uses the default style wich means that the values are chained without any spaces.
	 * @param w number of columns (if &lt;1 then a square grid will be used)*/
	public OutputGrid(int w) {
		this(w,(value,index,x,y)->value+"");
	}
	/**{@inheritDoc}*/
	@Override
	public void postprocessing(QUBOMatrix m, Map<String, Float> mp, int[] res) {
		if(w==0) w=(int) Math.sqrt(res.length);
		if(w<0) {
			w=-w;
			w=(int) (res.length/w);
		}
		for(int i=0;i<res.length;i++) {
			int x=i%w;
			int y=i/w;
			PrintStream newout=output;
			if(newout==null) newout=System.out;
			newout.print(style.genString(res[i],i,x,y));
			if(x==w-1) newout.println("");
		}
	}

}
