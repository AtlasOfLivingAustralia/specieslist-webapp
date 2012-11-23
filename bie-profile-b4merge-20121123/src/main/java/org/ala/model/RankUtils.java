package org.ala.model;

public class RankUtils {

	/**
	 * Compare two objects.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static int compareTo(Rankable o1, Rankable o2) {
	    
	    if(o1.isPreferred() && !o2.isPreferred())
	        return -1;
	    if(o2.isPreferred() && !o1.isPreferred())
	        return 1;
	
		if(o1.getRanking()!=null && o2.getRanking()==null){
			return o1.getRanking() *-1;
		}
	
		if(o2.getRanking()!=null && o1.getRanking()==null){
			return o2.getRanking();
		}
	
		//compare on rankings
		if(o1.getRanking()!=null && !o1.getRanking().equals(o2.getRanking())){
			return o2.getRanking().compareTo(o1.getRanking());
		}
	
		//compare on number of rankings
		if(o2.getNoOfRankings()!=null && o1.getNoOfRankings()!=null && !o1.getNoOfRankings().equals(o2.getNoOfRankings())){
			return o2.getNoOfRankings().compareTo(o1.getNoOfRankings());
		}
		return -1;
	}
	
	/**
	 * Util for preserving rankings.
	 * @param source
	 * @param target
	 */
	public static void copyAcrossRankings(Rankable source, Rankable target){
		target.setNoOfRankings(source.getNoOfRankings());
		target.setRanking(source.getRanking());
		target.setIsBlackListed(source.getIsBlackListed());
	}
}
