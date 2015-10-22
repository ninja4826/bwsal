package me.ninja4826.bwsal.arbitrator;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import me.ninja4826.bwsal.Heap;
import me.ninja4826.bwsal.util.Pair;

public class Arbitrator<_Tp, _Val> {
	
	private ConcurrentSkipListMap<_Tp, Heap<Controller<_Tp, _Val>, Integer>> bids;
	private ConcurrentSkipListMap<_Tp, Controller<_Tp, _Val>> owner;
	private ConcurrentSkipListMap<Controller<_Tp, _Val>, Set<_Tp>> objectsOwned;
	private ConcurrentSkipListMap<Controller<_Tp, _Val>, Set<_Tp>> objectsBidOn;
	private Set<_Tp> updatedObjects;
	private Set<_Tp> objectsCanIncreaseBid;
	private Set<_Tp> unansweredObjects;
	private Set<_Tp> emptySet;
//	private Heap<Controller<_Tp, _Val>, _Val> emptyHeap;
	private Pair<Controller<_Tp, _Val>, Integer> defaultBidder;
	@SuppressWarnings("unused")
	private boolean inUpdate;
	private boolean inOnOffer;
	private boolean inOnRevoke;
	
	public Arbitrator() {
		inUpdate = false;
		inOnOffer = false;
		inOnRevoke = false;
	}
	
	public boolean setBid(Controller<_Tp, _Val> c, _Tp obj, Integer bid) {
		if (c == null || obj == null) return false;
		
		if (bids.containsKey(obj) && bids.get(obj).contains(c) && bid > bids.get(obj).get(c)) {
			if (inOnRevoke || (inOnOffer && !(objectsCanIncreaseBid.contains(obj)))) return false;
		}
		
		bids.get(obj).set(c, bid);
		objectsBidOn.get(c).add(obj);
		updatedObjects.add(obj);
		return true;
	}
	
	public boolean setBid(Controller<_Tp, _Val> c, Set<_Tp> objs, Integer bid) {
		boolean result = false;
		for (_Tp o : objs) {
			result |= setBid(c, o, bid);
		}
		return result;
	}
	
	public boolean removeBid(Controller<_Tp, _Val> c, _Tp obj) {
		if (c == null || obj == null) return false;
		if (bids.containsKey(obj) && bids.get(obj).contains(c)) {
			bids.get(obj).erase(c);
			if (bids.get(obj).empty()) { bids.remove(obj); }
			updatedObjects.add(obj);
		}
		
		if (objectsBidOn.containsKey(c)) {
			objectsBidOn.get(c).remove(obj);
			if (objectsBidOn.get(c).isEmpty()) { objectsBidOn.remove(c); }
		}
		return true;
	}
	
	public boolean removeBid(Controller<_Tp, _Val> c, Set<_Tp> objs) {
		boolean result = false;
		for (_Tp o : objs) { result |= removeBid(c, o); }
		return result;
	}
	
	public boolean removeAllBids(Controller<_Tp, _Val> c) {
		if (!(objectsBidOn.containsKey(c))) return false;
		return removeBid(c, objectsBidOn.get(c));
	}
	
	public boolean removeController(Controller<_Tp, _Val> c) {
		if (c == null) return false;
		
		removeAllBids(c);
		
		if (objectsOwned.containsKey(c)) {
			for (_Tp o : objectsOwned.get(c)) {
				if (owner.containsKey(o) && owner.get(o) == c) owner.remove(o);
			}
			objectsOwned.remove(c);
		}
		return true;
	}
	
	public boolean accept(Controller<_Tp, _Val> c, _Tp obj, Integer bid) {
		if (c == null || obj == null) return false;
		if (!inOnOffer) return false;
		if (hasBid(obj) == false || bids.get(obj).top().first != c) return false;
		unansweredObjects.remove(obj);
		inOnOffer = false;
		revokeOwnership(obj, bids.get(obj).top().second, c);
		inOnOffer = true;
		owner.put(obj, c);
		objectsOwned.get(c).add(obj);
		updatedObjects.add(obj);
		
		if (bids.containsKey(obj) && bids.get(obj).contains(c) && bid < bids.get(obj).get(c)) return true;
		bids.get(obj).set(c, bid);
		return true;
	}
	
	public boolean accept(Controller<_Tp, _Val> c, Set<_Tp> objs, Integer bid) {
		boolean result = false;
		for (_Tp o : objs) {
			result |= accept(c, o, bid);
		}
		return result;
	}
	
	public boolean accept(Controller<_Tp, _Val> c, _Tp obj) {
		if (c == null || obj == null) return false;
		if (!inOnOffer) return false;
		if (hasBid(obj) == false || bids.get(obj).top().first != c) return false;
		
		unansweredObjects.remove(obj);
		inOnOffer = false;
		revokeOwnership(obj, bids.get(obj).top().second, c);
		inOnOffer = true;
		owner.put(obj, c);
		objectsOwned.get(c).add(obj);
		return true;
	}
	
	public boolean accept(Controller<_Tp, _Val> c, Set<_Tp> objs) {
		boolean result = false;
		for (_Tp o : objs) { result |= accept(c, o); }
		return result;
	}
	
	public boolean decline(Controller<_Tp, _Val> c, _Tp obj, Integer bid) {
		if (c == null || obj == null) return false;
		if (!inOnOffer) return false;
		if (hasBid(obj) == false || bids.get(obj).top().first != c) return false;
		updatedObjects.add(obj);
		unansweredObjects.remove(obj);
		objectsCanIncreaseBid.remove(obj);
		if (bids.containsKey(obj) && bids.get(obj).contains(c) && bid >= bids.get(obj).get(c)) {
			bid = 0;
		}
		
		if (bid <= 0) {
			if (bids.containsKey(obj) && bids.get(obj).contains(c)) {
				bids.get(obj).erase(c);
				if (bids.get(obj).empty()) bids.remove(obj);
			}
			return true;
		}
		bids.get(obj).set(c, bid);
		return true;
	}
	
	public boolean decline(Controller<_Tp, _Val> c, Set<_Tp> objs, Integer bid) {
		boolean result = false;
		for (_Tp o : objs) {
			result |= decline(c, o, bid);
		}
		return result;
	}
	
	public boolean hasBid(_Tp obj) {
		return (bids.containsKey(obj) && !bids.get(obj).empty());
	}
	
	public Pair<Controller<_Tp, _Val>, Integer> getHighestBidder(_Tp obj) {
		if (!bids.containsKey(obj)) return defaultBidder;
		return bids.get(obj).top();
	}
	
	public ArrayList<Pair<Controller<_Tp, _Val>, Integer>> getAllBidders(_Tp obj) {
		ArrayList<Pair<Controller<_Tp, _Val>, Integer>> bidders = new ArrayList<>();
		if (!bids.containsKey(obj)) return bidders;
		
		Heap<Controller<_Tp, _Val>, Integer> bid_heap = bids.get(obj);
		
		while (!bid_heap.empty()) {
			bidders.add(bid_heap.top());
			bid_heap.pop();
		}
		return bidders;
	}
	
	public Set<_Tp> getObjects(Controller<_Tp, _Val> c) {
		if (!objectsOwned.containsKey(c)) return emptySet;
		return objectsOwned.get(c);
	}
	
	public Set<_Tp> getObjectsBidOn(Controller<_Tp, _Val> c) {
		if (!objectsBidOn.containsKey(c)) return emptySet;
		return objectsBidOn.get(c);
	}
	
	public void onRemoveObject(_Tp obj) {
		if (bids.containsKey(obj)) {
			Heap<Controller<_Tp, _Val>, Integer> bid_heap = bids.get(obj);
			
			while (!bid_heap.empty()) {
				Controller<_Tp, _Val> bidder = bid_heap.top().first;
				bid_heap.pop();
				if (objectsBidOn.containsKey(bidder)) {
					objectsBidOn.get(bidder).remove(obj);
					if (objectsBidOn.get(bidder).isEmpty()) objectsBidOn.remove(bidder);
				}
			}
			bids.remove(bids.get(obj));
			Integer temp = 0;
			revokeOwnership(obj, temp);
			if (updatedObjects.contains(obj)) updatedObjects.remove(obj);
		}
	}
	
	public Integer getBid(Controller<_Tp, _Val> c, _Tp obj) {
		if (bids.containsKey(obj) && bids.get(obj).contains(c)) return bids.get(obj).get(c);
		return 0;
	}
	
	public void update() {
		inUpdate = true;
		boolean first = true;
		
		ConcurrentSkipListMap<Controller<_Tp, _Val>, Set<_Tp>> objectsToOffer = new ConcurrentSkipListMap<>();
		
		while (first || !objectsToOffer.isEmpty()) {
			first = false;
			objectsToOffer.clear();
			
			objectsCanIncreaseBid.clear();
			
			for (_Tp i : updatedObjects) {
				if (bids.containsKey(i) && !bids.get(i).empty()) {
					if (!owner.containsKey(i) || bids.get(i).top().first != owner.get(i)) objectsToOffer.get(bids.get(i).top().first).add(i);
				} else {
					if (owner.containsKey(i)) revokeOwnership(i, 0);
				}
			}
			updatedObjects.clear();
			
			for (Entry<Controller<_Tp, _Val>, Set<_Tp>> i : objectsToOffer.entrySet()) {
				objectsCanIncreaseBid = i.getValue();
				unansweredObjects = i.getValue();
				
				inOnOffer = true;
				i.getKey().onOffer(i.getValue());
				inOnOffer = false;
			}
		}
		
		inUpdate = false;
	}
	
	private boolean revokeOwnership(_Tp obj, Integer bid) {
		return revokeOwnership(obj, bid, null);
	}
	
	private boolean revokeOwnership(_Tp obj, Integer second, Controller<_Tp, _Val> c) {
		
		if (!(owner.containsKey(obj))) return false;
		
		Controller<_Tp, _Val> theOwner = owner.get(obj);
		
		if (theOwner == c) return false;
		
		inOnRevoke = true;
		theOwner.onRevoke(obj, second);
		inOnRevoke = false;
		if (objectsOwned.containsKey(theOwner)) {
			objectsOwned.get(theOwner).remove(obj);
			if (objectsOwned.get(theOwner).isEmpty()) {
				objectsOwned.remove(theOwner);
			}
		}
		owner.remove(obj);
		return true;
	}
}
