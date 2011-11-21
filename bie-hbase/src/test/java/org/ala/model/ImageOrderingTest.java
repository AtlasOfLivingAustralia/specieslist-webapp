package org.ala.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class ImageOrderingTest extends TestCase{

	public void testOrdering(){
		
		Image image1 = new Image("1", null, null,null, null, null,null, null, null,null, null, null,1, 1);
		Image image2 = new Image("2", null, null,null, null, null,null, null, null,null, null, null, null, null);
		Image image3 = new Image("3", null, null,null, null, null,null, null, null,null, null, null,1, -1);
		
		List<Image> images = new ArrayList<Image>();
		images.add(image2);
		images.add(image3);
		images.add(image1);
		Collections.sort(images);
		
		for( Image image: images){
			System.out.println(image.getGuid());
		}
	}
	
}
