package me.ninja4826.bwsal.arbitrator;

import java.util.Set;

public interface Controller<_Tp, _Val> {
	
	public void onOffer(Set<_Tp> objects);
	public void onRevoke(_Tp tp, Integer second);
	public String getName();

}
