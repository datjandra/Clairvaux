package org.clairvaux.utils;

public class Pair<A,B> {

	private A first;
	private B second;
	
	Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public A getFirst() {
		return first;
	}
	
	public void setFirst(A first) {
		this.first = first;
	}
	
	public B getSecond() {
		return second;
	}
	
	public void setSecond(B second) {
		this.second = second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<A,B> other = (Pair<A,B>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
}
