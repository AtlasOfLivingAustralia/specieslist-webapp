/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.util;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.time.DateUtils;

/**
 * A utility to cleanup the filesystem based on last modified date of 
 * files. We can then use the following unix command to remove empty directories.
 * 
 * find . -depth -empty -type d -exec rmdir {} \;
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class CleanupRepository {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String filePath = null;
		Date thresholdDate = null;
		try {
			filePath = args[0];
			thresholdDate = DateUtils.parseDate(args[1], new String[]{"yyyyMMdd"});
		} catch (Exception e){
			System.out.println("Usage: <absolute-file-path> <delete-before date (yyyyMMdd)>");
			System.exit(0);
		}
		
		//iterate through directory
		
		File file = new File(filePath);
		File[] dirs = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
 
		int deleting = 0;
		int keeping = 0;
			
		for (File currentDir : dirs) {
			System.out.println("Reading directory: " + currentDir.getAbsolutePath());
			Iterator<File> fileIterator = FileUtils.iterateFiles(currentDir, null, true);
			while (fileIterator.hasNext()) {
				File currentFile = fileIterator.next();
				Date lastModified = new Date(currentFile.lastModified());
				if(lastModified.before(thresholdDate)){
					SimpleDateFormat sf = new SimpleDateFormat("dd MMM yyyy"); 
					System.out.println("Candidate for deletion: "+currentFile.getAbsolutePath()+" last modified: "+sf.format(lastModified));
					FileUtils.forceDelete(currentFile);
					deleting++;
				} else {
					keeping++;
				}
			}
		}
		
		System.out.println("Keeping :"+keeping+", deleting :"+deleting);
		
		System.out.println("Use the following command to remove empty directories.");
		
		System.out.println("find . -depth -empty -type d -exec rmdir {} \\;");
	}
}
