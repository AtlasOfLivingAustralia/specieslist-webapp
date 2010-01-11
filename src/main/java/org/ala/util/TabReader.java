package org.ala.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Written because CSVReader throws OutofMemory - not sure why.
 * 
 * @author Dave Martin
 */
public class TabReader {

	Pattern p = Pattern.compile("\t");
	BufferedReader br;
	
	public TabReader(String filePath) throws Exception{
    	FileReader fr = new FileReader(new File(filePath));
    	this.br = new BufferedReader(fr);
	}
	
	public String[] readNext() throws Exception{
		String line = br.readLine();
		if(line==null)
			return null;
		String[] fields = p.split(line);
		for(int i=0;i<fields.length;i++){
			if(fields[i].length()>=2){
				//remove quotes				
				fields[i] = fields[i].substring(1, fields[i].length()-1);
			}
			fields[i] = StringUtils.trimToNull(fields[i]);
		}
		return fields;
	}

	public void close() throws Exception {
		this.br.close();
	}
}
