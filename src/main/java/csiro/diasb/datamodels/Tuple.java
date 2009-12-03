package csiro.diasb.datamodels;

/**
 * A tupel is a vector with 2 elements. The elements can be of different Types.
 *
 * @author fri096
 * @param <A> Type of the first item in the Tuple
 * @param <B> Type of the second item in the Tuple
 */

public class Tuple<A, B> {

	/**
	 * Create a new Tuple
	 * @param first the first value.
	 * @param second the second value.
	 */
	public Tuple(A first, B second) {
		this.first=first;
		this.second=second;
	}
	/**
	 * The first element.
	 */

	private A first;

	/**
	 * The second element.
	 */
	private B second;

	/**
	 * @return the first
	 */
	public A getFirst() {
		return first;
	}

	/**
	 * @param first the first to set
	 */
	public void setFirst(A first) {
		this.first = first;
	}
	/**
	 * @return the second
	 */
	public B getSecond() {
		return second;
	}
  
	/**
	 * @param second the second to set
	 */
	public void setSecond(B second) {
		this.second = second;
	}

	/**
	 * @return The hash code of the object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;

	} // End of `Tuple.hashCode` method.

	/**
	 * @param obj the object to compare to.
	 * @return whether the objects are identical.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tuple))
			return false;
		Tuple other = (Tuple) obj;
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

	} // End of `Tuple.equals` method.

  @Override
  public String toString() {

    return ("< " + this.getFirst().toString() + " , " +
      this.getSecond().toString() + " >");
    
  } // End of `Tuple.toString` method.

} // End of `Tuple` class.
