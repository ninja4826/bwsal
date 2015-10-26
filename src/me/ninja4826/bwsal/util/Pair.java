package me.ninja4826.bwsal.util;

public class Pair<K, V> {
	
	public K first;
	public V second;
	
	public Pair() {
	}
	
	public Pair(K firstVal, V secondVal) {
		this.first = firstVal;
		this.second = secondVal;
	}
	
	public Pair(Pair<K, V> pairToCopy) {
		this.first = pairToCopy.first;
		this.second = pairToCopy.second;
	}
	
	public void replace(Pair<K, V> pairToCopy) {
		this.first = pairToCopy.first;
		this.second = pairToCopy.second;
	}
	
	@Override
	public String toString() {
		return "Pair{" +
				"first=" + this.first +
				", second=" + this.second +
				"}";
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		@SuppressWarnings("unchecked")
		Pair<K, V> pair = (Pair<K, V>) o;
		
		if (this.first != null ? !this.first.equals(pair.first) : pair.first != null) return false;
		if (this.second != null ? !this.second.equals(pair.second) : pair.second != null) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}
}
