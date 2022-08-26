package quva.transform;

import java.util.Iterator;

/**This iterator will iterate upon every possibility for a int array with the individual values being between 0 and a cap specified in the constructor.*/
public class BruteForceIterator implements Iterator<int[]>, Iterable<int[]> {
  /**The caps are saved here*/
  public int[] maxl;
  /**the current iteration*/
  public int currmod[];
  /**buffer*/
  public int currmod2[];
  /**Is true when the iterator is complete*/
  public boolean done=false;
  /**The caps are entered here. If you e.g. enter 3,5 it will iterate upon all arrays with the {@code array[0]} being between 0 and <b>2</b> and {@code array[1]} begin between 0 and <b>5</b>
   * @param ml the caps used*/
  public BruteForceIterator(int... ml) {
    maxl=ml;
    currmod=new int[maxl.length];
    currmod2=new int[maxl.length];
    for (int i=0; i<currmod.length; i++) currmod[i]=0;
  }
  /**A simplified form of the first constructor this iterator will iterate upon all arrays of length l with entries between 0 and m-1.
   * @param m the cap used
   * @param l the length of the array*/
  public BruteForceIterator(int m, int l) {
    maxl=new int[l];
    for (int i=0; i<maxl.length; i++) maxl[i]=m;
    currmod=new int[maxl.length];
    currmod2=new int[maxl.length];
    for (int i=0; i<currmod.length; i++) currmod[i]=0;
  }
  @Override
    public boolean hasNext() {
    return !done;
  }

  @Override
    public int[] next() {
    for (int i=0; i<currmod.length; i++) currmod2[i]=currmod[i];
    currmod[0]++;
    int i=0;
    int end=currmod.length-1;
    while (currmod[i]>=maxl[i]&&i<currmod.length-1) {
      currmod[i+1]++;
      currmod[i]-=maxl[i];
      i++;
    }
    if (currmod[end]>=maxl[end]) done=true;
    return currmod2;
  }
  @Override
    public Iterator<int[]> iterator() {
    return this;
  }
}
