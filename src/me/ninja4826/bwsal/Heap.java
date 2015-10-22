package me.ninja4826.bwsal;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

import me.ninja4826.bwsal.util.Pair;

public class Heap<_Tp, _Val> {
	
	private ArrayList<Pair<_Tp, Integer>> data;
//	private HashMap<_Tp, Integer> mapping;
	private ConcurrentSkipListMap<_Tp, Integer> mapping;
	private boolean minHeap;
	
	public Heap() {
		this(false);
	}
	
	public Heap(boolean isMinHeap) {
		minHeap = isMinHeap;
	}
	
	private int percolate_up(int index) {
		if (index < 0 || index >= (int)data.size()) { return -1; }
		
		int parent = (index - 1) / 2;
		int m = 1;
		if (this.minHeap) { m = -1; }
		
		while (index > 0 && m * (int)data.get(parent).second < m * (int)data.get(index).second) {
			Pair<_Tp, Integer> temp = data.get(parent);
			data.get(parent).replace(data.get(index));
			data.get(index).replace(temp);
			
			mapping.put(data.get(index).first, index);
			index = parent;
			parent = (index - 1) / 2;
		}
		mapping.put(data.get(index).first, index);
		return index;
	}
	
	private int percolate_down(int index) {
		if (index < 0 || index >= data.size()) { return -1; }
		int lchild = (index*2) + 1;
		int rchild = (index*2) + 2;
		int mchild;
		int m = 1;
		
		if (this.minHeap) { m = -1; }
		
		while ((data.size() > lchild && m * (int)data.get(index).second < m * (int)data.get(lchild).second) ||
				(data.size() > rchild && m * (int)data.get(index).second < m * (int)data.get(rchild).second)) {
			mchild = lchild;
			if (data.size() > rchild && m * (int)data.get(rchild).second > m * (int)data.get(lchild).second) { mchild = lchild; }
			Pair<_Tp, Integer> temp = data.get(mchild);
			data.get(mchild).replace(data.get(index));
			data.get(index).replace(temp);
			mapping.put(data.get(index).first, index);
			index = mchild;
			lchild = (index * 2) + 1;
			rchild = (index * 2) + 2;
		}
		mapping.put(data.get(index).first, index);
		return index;
	}
	
	public void push(Pair<_Tp, Integer> pair) {
		int index = data.size();
		mapping.put(pair.first, index);
		if (mapping.get(pair.first) != null) {
			data.add(pair);
			percolate_up(index);
		}
	}
	
	public void pop() {
		if (data.isEmpty()) { return; }
		mapping.remove(data.get(0).first);
		data.get(0).replace(data.get(data.size() - 1));
		data.remove(data.size() - 1);
		if (data.isEmpty()) { return; }
		Integer iter = mapping.get(data.get(0).first);
		if (iter != null) {
			mapping.put(data.get(0).first, 0);
			percolate_down(0);
		}
	}
	
	public Pair<_Tp, Integer> top() { return data.get(0); }
	
	public boolean empty() { return data.isEmpty(); }
	
	public boolean set(_Tp x, Integer v) {
		Integer iter = mapping.get(x);
		
		if (iter == null) {
			push(new Pair<_Tp, Integer>(x, v));
			return true;
		}
		
		data.get(iter).second = v;
		int index = percolate_up(iter);
		
		if (index >= 0 && index < (int)data.size()) {
			percolate_down(index);
			return true;
		}
		return false;
	}
	
	public Integer get(_Tp x) { return data.get(mapping.get(x)).second; }
	
	public boolean contains(_Tp x) { return mapping.get(x) != null; }
	
	public int size() { return data.size(); }
	
	public void clear() {
		data.clear();
		mapping.clear();
	}
	
	public boolean erase(_Tp x) {
		Integer iter = mapping.get(x);
		
		if (iter == null) { return false; }
		
		if (data.size() == 1) {
			this.clear();
		} else {
			int index = mapping.get(x);
			data.get(index).replace(data.get(data.size() - 1));
			data.remove(data.size() - 1);
			mapping.remove(x);
			percolate_down(index);
		}
		return true;
	}
}
