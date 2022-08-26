package quva.transform;

import java.util.Iterator;

/**This iterator checks all possibilities to distribute p items on an array of g possibilities. This is used to make the {@code PowerSeries} work.
 * @see quva.transform.PowerSeries*/
public class ProductIterator implements Iterator<int[]>, Iterable<int[]> {
	  /** This iterator uses a {@code BruteForceIterator} as its basis*/
	  public BruteForceIterator mainIterator;
	  /**Number of items to be distributed*/
	  public int power;
	  /**Number of entries in the array*/
	  public int g;
	  /**This iterator checks all possibilities to distribute p items on an array of g possibilities. This is used to make the {@code PowerSeries} work.
	   * @param p number of items
	   * @param g number of slots
	   * @see quva.transform.PowerSeries*/
	  public ProductIterator(int p, int g) {
	    this.power=p;
	    this.g=g;
	    mainIterator=new BruteForceIterator(g, p);
	  }
	  /**{@inheritDoc}*/
	  @Override
	    public Iterator<int[]> iterator() {
	    return this;
	  }
	  /**{@inheritDoc}*/
	  @Override 
	    public boolean hasNext() {
	    return mainIterator.hasNext();
	  }
	  /**{@inheritDoc}*/
	  @Override
	    public int[] next(){
	      int template[]=mainIterator.next();
	      int[] ret=new int[g];
	      for(int i=0;i<ret.length;i++) ret[i]=0;
	      for(int i=0;i<template.length;i++) ret[template[i]]++;
	      return ret;
	    }
	}
