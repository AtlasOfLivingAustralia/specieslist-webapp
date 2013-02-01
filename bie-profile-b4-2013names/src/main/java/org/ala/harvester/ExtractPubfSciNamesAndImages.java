/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ala.harvester;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.exceptions.InvalidPasswordException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripperByArea;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is an example on how to extract text from a specific area on the PDF document.
 *
 * Usage: java org.apache.pdfbox.examples.util.ExtractTextByArea &lt;input-pdf&gt;
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class ExtractPubfSciNamesAndImages
{

    private static int imageCounter = 1;
    private ExtractPubfSciNamesAndImages()
    {
        //utility class and should not be constructed.
    }


    /**
     * This will print the documents text in a certain area.
     *
     * @param args The command line arguments.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load( args[0] );
                if( document.isEncrypted() )
                {
                    try
                    {
                        document.decrypt( "" );
                    }
                    catch( InvalidPasswordException e )
                    {
                        System.err.println( "Error: Document is encrypted with a password." );
                        System.exit( 1 );
                    }
                }
                
                extractSciNameAndImages(document);
                
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }
    
    private static void extractSciNameAndImages(PDDocument document) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition( true );
        Rectangle rect = new Rectangle( 10, 60, 275, 20 );
        stripper.addRegion( "class1", rect );
        List allPages = document.getDocumentCatalog().getAllPages();
        
        Writer writer = getSiteMapWriter("anic");
        
        writeColumnHeaders(writer);

        for (int pageNum = 37; pageNum <= 249; pageNum++) {
//        for (int pageNum = 156; pageNum <= 156; pageNum++) {
            PDPage page = (PDPage)allPages.get( pageNum );
            PDResources resources = page.getResources();
            Map images = resources.getImages();
            stripper.extractRegions( page );

            String sciName = stripper.getTextForRegion( "class1" ).trim();
            System.out.println("Scientific Name: " + sciName);

            if( images != null )
            {
                Iterator imageIter = images.keySet().iterator();
                while( imageIter.hasNext() )
                {
                    String key = (String)imageIter.next();
                    PDXObjectImage image = (PDXObjectImage)images.get( key );
                    String name = null;

                    if ("jpg".equals(image.getSuffix())) {
                        name = getUniqueFileName(sciName + "_" + key, image.getSuffix() );
                        System.out.println( "Writing image:" + name );
                        image.write2file("/data/tmp/" + name );
                        
                        writer.write(sciName);
                        writer.write(",");
                        writer.write(name+"."+image.getSuffix());
                        writer.write("\n");
                    }
                }
            }
        }
    }
    
    private static void writeColumnHeaders(Writer writer) throws IOException {
        writer.write("SPECIES");
        writer.write(",");
        writer.write("FILE_NAME");
        writer.write("\n");
    }

    private static Writer getSiteMapWriter(String infosourceSimpleName) throws IOException {
        String dataDir = "/data/mapping/";
        FileUtils.forceMkdir(new File(dataDir));
        File siteMapFile = new File(dataDir+"perth_urban_bushland_fungi.csv");
        if(siteMapFile.exists())
            FileUtils.forceDelete(siteMapFile);
        return new FileWriter(siteMapFile);
    }
    
    private static String getUniqueFileName( String prefix, String suffix )
    {
        String uniqueName = null;
        File f = null;
        while( f == null || f.exists() )
        {
            uniqueName = prefix + "-" + imageCounter;
            f = new File( uniqueName + "." + suffix );
            imageCounter++;
        }
        return uniqueName;
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.apache.pdfbox.examples.util.ExtractTextByArea <input-pdf>" );
    }

}
