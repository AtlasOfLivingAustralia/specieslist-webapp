package org.ala.dao;

public enum IndexedTypes {
	TAXON(1),
	REGION(2),
	LOCALITY(3),
	COLLECTION(4),
	INSTITUTION(5),
	DATAPROVIDER(6),
	DATASET(7),
    WORDPRESS(8),
    RANKING(9),
    LAYERS(10),
    SEARCH(11);
	
	private final int id;
    private IndexedTypes(int id) {
        this.id = id;
    }
}
