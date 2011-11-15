package org.ala.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.ala.model.Triple;
import org.apache.commons.io.FileUtils;

public class ScientificNameListBuilder {
    
    private final static String BIE_PATH = "/data/bie";
    private final static String OUTPUT_PATH = "/data/bie/scientific_name_list";
    
    public static void main(String[] args) throws Exception {
        ScientificNameListBuilder s = new ScientificNameListBuilder();
        Writer writer = getWriter(OUTPUT_PATH);
        s.scanDirectory(writer, BIE_PATH);
        
        writer.flush();
        writer.close();
    }

    public void scanDirectory(Writer writer, String rootPath) throws Exception{
//        System.out.println(rootPath);
        File rootDir = new File(rootPath);
        
        if (rootDir.isDirectory()) {
            String[] subDirs = rootDir.list();
            
            for (String subDir : subDirs) {
                System.out.println(rootDir + File.separator + subDir);
                scanDirectory(writer, rootDir + File.separator + subDir);
            }
        } else if (rootDir.getName().equals(FileType.RDF.toString())) {
            FileReader reader = new FileReader(rootDir);
            List<Triple> triples = TurtleUtils.readTurtle(reader);
            //close the reader
            reader.close();

            //iterate through triple, splitting the triples by subject
            for(Triple triple: triples){
                String predicate = triple.predicate;
                
                if (predicate.endsWith("hasScientificName")) {
                    System.out.println(rootPath + "::" + triple.object);
                    writer.write(triple.object);
                    writer.write("\n");
                    writer.flush();
                } 
            }
        }

    }
    
    public static Writer getWriter(String outputPath) throws IOException {
        File outputFile = new File(outputPath);
        if(outputFile.exists())
            FileUtils.forceDelete(outputFile);
        return new FileWriter(outputFile);
    }
}
