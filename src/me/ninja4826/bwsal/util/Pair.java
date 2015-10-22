package me.ninja4826.bwsal.util;

public class Pair<T1, T2> {
	
	public T1 first;
	public T2 second;
	
	public Pair() {
		first = null;
		second = null;
	}
	
	public Pair(T1 firstVal, T2 secondVal) {
		this.first = firstVal;
		this.second = secondVal;
	}
	
	public Pair(Pair<T1, T2> pairToCopy) {
		this.first = pairToCopy.first;
		this.second = pairToCopy.second;
	}
	
	public void replace(Pair<T1, T2> pairToCopy) {
		this.first = pairToCopy.first;
		this.second = pairToCopy.second;
	}
}
