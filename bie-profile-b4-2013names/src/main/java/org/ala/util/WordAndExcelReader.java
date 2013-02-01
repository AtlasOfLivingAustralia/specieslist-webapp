package org.ala.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jacob.com.*;
import com.jacob.activeX.*;

public class WordAndExcelReader {

	private static final String DOC_FILE_APPENDIX = ".doc";
	private static final String XLS_FILE_APPENDIX = ".xls";
	private static final String TXT_FILE_APPENDIX = ".txt";
	private static final String CSV_FILE_APPENDIX = ".csv";
	private static String kingdom = null;
	private static String phylum = null;
	private static String klass = null;
	private static String order = null;
	private static String family = null;
	private static String genus = null;
	private static String species = null;
	
	private static final int KINGDOM_RANK = 1;
	private static final int PHYLUM_RANK = 2;
	private static final int KLASS_RANK = 3;
	private static final int ORDER_RANK = 4;
	private static final int FAMILY_RANK = 5;
	private static final int GENUS_RANK = 6;
	private static final int SPECIES_RANK = 7;
	
	private static Pattern kingdomPattern = Pattern.compile("[a-zA-Z]{1}");
	private static Pattern phylumPattern = Pattern.compile("[a-zA-Z]{2}");
	private static Pattern orderPattern = Pattern.compile("[a-zA-Z]{2}[0-9]{1}");
	private static Pattern familyPattern = Pattern.compile("[a-zA-Z]{2}[0-9]{2}");
	private static Pattern genusPattern = Pattern.compile("[a-zA-Z]{2}[0-9]{4}");
	private static Pattern speciesPattern = Pattern.compile("[a-zA-Z]{2}[a-zA-Z0-9]{6}");

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("USAGE: WordAndExcelReader PATH_TO_DOC_XLS_FILES");
		} else {
			String dir = args[0];
			
			File path = new File(dir);
			
			if (!path.isDirectory()) {
				System.out.println("Parameter error! " + dir + " is not a directory.") ;
			} else {
				
				String[] fileNames = path.list();
				
				for (String fileName : fileNames) {

					if (fileName.endsWith(DOC_FILE_APPENDIX)) {
						
						fileName = fileName.substring(0, fileName.length()-4);
						
						convertDocToHtm(dir, fileName);
						
						try {
							generateCsv(dir, fileName);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						
					} else if (fileName.endsWith(XLS_FILE_APPENDIX)) {
						fileName = fileName.substring(0, fileName.length()-4);
						
						convertXlsToHtm(dir, fileName);
						
						try {
							generateCsv(dir, fileName);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
				} 
			}
		}
	}

	public static void convertDocToHtm(String filePath, String fileName) {
		ActiveXComponent word = new ActiveXComponent("Word.Application");

		try {
			Dispatch wordObject = (Dispatch) word.getObject();
			//Create a Dispatch Parameter to show the document that is opened
			word.setProperty("Visible", new Variant(false));

			//Instantiate the Documents Property
			Dispatch documents = word.getProperty("Documents").toDispatch(); 

			//Add a new word document, Current Active Document
			Dispatch document = Dispatch.call(documents, "Open", filePath + File.separator + fileName + DOC_FILE_APPENDIX).toDispatch(); 

			Dispatch.invoke(document,"SaveAs",Dispatch.Method, new Object[]{filePath + File.separator + fileName + TXT_FILE_APPENDIX,new Variant(7)}, new int[1]);

			Variant f = new Variant(false); 

			Dispatch.call(document, "Close", f);    
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			word.invoke("Quit", new Variant[]{});
		}
	}
	
	public static void convertXlsToHtm(String filePath, String fileName) {
		ActiveXComponent excel = new ActiveXComponent("Excel.Application");

		try {
			Dispatch excelObject = (Dispatch) excel.getObject();
			//Create a Dispatch Parameter to show the document that is opened
			excel.setProperty("Visible", new Variant(false));

			//Instantiate the Documents Property
			Dispatch documents = excel.getProperty("Workbooks").toDispatch(); 

			//Add a new word document, Current Active Document
			Dispatch document = Dispatch.call(documents, "Open", filePath + File.separator + fileName + XLS_FILE_APPENDIX).toDispatch(); 

			Dispatch.invoke(document,"SaveAs",Dispatch.Method, new Object[]{filePath + File.separator + fileName + TXT_FILE_APPENDIX,new Variant(3)}, new int[1]);

			Variant f = new Variant(false); 

			Dispatch.call(document, "Close", f);    
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			excel.invoke("Quit", new Variant[]{});
		}
	}
	
	public static void convertXlsToCsv(String filePath, String fileName) {
        ActiveXComponent excel = new ActiveXComponent("Excel.Application");

        try {
            Dispatch excelObject = (Dispatch) excel.getObject();
            //Create a Dispatch Parameter to show the document that is opened
            excel.setProperty("Visible", new Variant(false));

            //Instantiate the Documents Property
            Dispatch documents = excel.getProperty("Workbooks").toDispatch(); 

            //Add a new word document, Current Active Document
            Dispatch document = Dispatch.call(documents, "Open", filePath + File.separator + fileName + XLS_FILE_APPENDIX).toDispatch(); 

            Dispatch.invoke(document,"SaveAs",Dispatch.Method, new Object[]{filePath + File.separator + fileName + CSV_FILE_APPENDIX,new Variant(3)}, new int[1]);

            Variant f = new Variant(false); 

            Dispatch.call(document, "Close", f);    
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            excel.invoke("Quit", new Variant[]{});
        }
    }

	//	private static String htmReader(String filePath, String fileName) throws IOException {
	//		File txtFile = new File(filePath + File.separator + fileName + TXT_FILE_APPENDIX);
	//		
	////		System.out.println(contentAsString);
	//		
	//		return contentAsString;
	//	}

	private static void generateCsv(String filePath, String fileName) throws IOException {
		File txtFile = new File(filePath + File.separator + fileName + TXT_FILE_APPENDIX);

		RandomAccessFile txtFileRaf = new RandomAccessFile(txtFile, "r");
		
		Writer dataFileWriter = getWriter(filePath, fileName + CSV_FILE_APPENDIX);
		
		String txtFileReadlineString = txtFileRaf.readLine();		
		while (txtFileReadlineString != null) {
			if (StringUtils.isNotBlank(txtFileReadlineString)) {
				String[] pairs = txtFileReadlineString.split("[\\t]{1,}");
				
				if (pairs.length == 2) {
					String[] tmpPair = pairs[1].split(",");
					int rank = 0;
					
					if (tmpPair.length == 1) {
						rank = getRank(pairs);
						
						if (rank != 0) {
//							System.out.println(rank);							
							setValue(rank, pairs[1]);
						}
					} else if (tmpPair.length == 2) {
						pairs[1] = tmpPair[0];
						rank = getRank(pairs);
						
						if (rank != 0) {
							setValue(rank, pairs[1]);
						}
						
						pairs[1] = tmpPair[1];
						rank = getRank(pairs);
						
						if (rank != 0) {
							setValue(rank, pairs[1]);
						}
					}
					
					if (rank == SPECIES_RANK) {
						dataFileWriter.write(pairs[0]);
						dataFileWriter.write("\t");
						dataFileWriter.write(kingdom != null ? kingdom : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(phylum != null ? phylum : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(klass != null ? klass : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(order != null ? order : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(family != null ? family : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(genus != null ? genus : "");
						dataFileWriter.write("\t");
						dataFileWriter.write(species != null ? species : "");
						dataFileWriter.write("\n");
					}
					
					dataFileWriter.flush();
					
				}
			}
			
			txtFileReadlineString = txtFileRaf.readLine();
		}
		
		dataFileWriter.close();

	}
	
	private static int getRank(String[] strPair) {
		int rank = 0;
		
		if (strPair.length == 2) {
			if (strPair[1].contains("KINGDOM") || strPair[1].contains("Kingdom")) {
				rank = KINGDOM_RANK;
			} else if (strPair[1].contains("PHYLUM") || strPair[1].contains("Phylum")) {
				rank = PHYLUM_RANK;
			} else if (strPair[1].contains("CLASS") || strPair[1].contains("Class")) {
				rank = KLASS_RANK;
			} else if (strPair[1].contains("ORDER") || strPair[1].contains("Order")) {
				rank = ORDER_RANK;
			} else if (strPair[1].contains("FAMILY") || strPair[1].contains("Family")) {
				rank = FAMILY_RANK;
			} else if (strPair[1].contains("GENUS") || strPair[1].contains("Genus")) {
				rank = GENUS_RANK;
			} else if (kingdomPattern.matcher(strPair[0]).matches()) {
				rank = KINGDOM_RANK;
			} else if (phylumPattern.matcher(strPair[0]).matches()) {
				rank = PHYLUM_RANK;
			} else if (orderPattern.matcher(strPair[0]).matches()) {
				rank = KINGDOM_RANK;
			} else if (familyPattern.matcher(strPair[0]).matches()) {
				rank = FAMILY_RANK;
			} else if (genusPattern.matcher(strPair[0]).matches()) {
				rank = GENUS_RANK;
			} else if (speciesPattern.matcher(strPair[0]).matches()) {
				rank = SPECIES_RANK;
			}
		}
		
		return rank;
	}
	
	private static void setValue(int rank, String value) {
		switch(rank) {
			case KINGDOM_RANK : kingdom = value.replaceAll("KINGDOM", "").trim(); 
								phylum = null;
								klass = null;
								order = null;
								family = null;
								genus = null;
								species = null;
								break;
			case PHYLUM_RANK : 	phylum = value.replaceAll("PHYLUM", "").trim();
								klass = null;
								order = null;
								family = null;
								genus = null;
								species = null;
								break;
			case KLASS_RANK : 	klass = value.replaceAll("CLASS", "").trim();
								order = null;
								family = null;
								genus = null;
								species = null;
								break;
			case ORDER_RANK : 	order = value.replaceAll("Order", "").trim();
								order = order.replaceAll("ORDER", "").trim();
								family = null;
								genus = null;
								species = null;
								break;
			case FAMILY_RANK : 	family = value.replaceAll("Family", "").trim();
								genus = null;
								species = null;
								break;
			case GENUS_RANK : 	genus = value.replaceAll("Genus", "").trim();
								species = null;
								break;
			case SPECIES_RANK : species = value.trim();
		}
	}
	
	private static Writer getWriter(String dataDir, String fileName) throws IOException {
		File outputFile = new File(dataDir + File.separator + fileName);
		if(outputFile.exists())
			org.apache.commons.io.FileUtils.forceDelete(outputFile);
		return new FileWriter(outputFile);
	}
}
