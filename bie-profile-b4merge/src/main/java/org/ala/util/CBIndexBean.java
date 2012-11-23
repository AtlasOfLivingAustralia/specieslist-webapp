package org.ala.util;

import java.io.IOException;

import org.springframework.stereotype.Component;

import au.org.ala.checklist.lucene.CBIndexSearch;

@Component("CBIndexSearch")
public class CBIndexBean extends CBIndexSearch {
	public CBIndexBean(){}
	public CBIndexBean(String indexDirectory) throws IOException {
		super(indexDirectory);
	}	
}
