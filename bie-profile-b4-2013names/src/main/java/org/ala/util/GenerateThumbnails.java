/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.util;

import org.ala.repository.Predicates;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Generates thumbnails for the repository by recursively scanning the
 * filesystem from the root.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class GenerateThumbnails {

    private static Logger logger = Logger.getLogger(GenerateThumbnails.class);

    static final float THUMB_SIZE = 100.0F;
    static final float SMALL_RAW_THUMB_SIZE = 314.0F;
    static final float LARGE_RAW_THUMB_SIZE = 650.0F;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        boolean overwriteThumbnail = false;
        boolean overwriteRawThumbnail = false;
        boolean overwriteLargeRawIfExists = false;

        if (!(args.length == 4 || args.length == 1)) {
            System.err.println("Please supply the root directory to recursively search and overwriteThumbnail and overwriteRawThumbnail");
            System.err.println("e.g. /data/bie OR /data/bie false true false");
            System.exit(1);
        }

        if (args.length == 4) {
            overwriteThumbnail = Boolean.parseBoolean(args[1]);
            overwriteRawThumbnail = Boolean.parseBoolean(args[2]);
            overwriteLargeRawIfExists = Boolean.parseBoolean(args[3]);
        }
        String rootDir = args[0];

        GenerateThumbnails r = new GenerateThumbnails();
        r.generate(rootDir, FileType.RAW, overwriteThumbnail, overwriteRawThumbnail, overwriteLargeRawIfExists);
        logger.debug("Scanning complete. ");
    }

    /**
     * Recursively scan the directory creating thumbnails.
     *
     * @param rootDirectory
     * @param overwriteIfExists
     * @return
     * @throws Exception
     */
    private void generate(String rootDirectory, FileType fileType, boolean overwriteIfExists, boolean overwriteRawThumbnailIfExists, boolean overwriteLargeRawIfExists) throws Exception {
        logger.debug("Scanning directory: " + rootDirectory);
        //start scan
        File file = new File(rootDirectory);
        processAllDirFiles(file, overwriteIfExists, overwriteRawThumbnailIfExists, overwriteLargeRawIfExists);
    }

    public static void generateThumbnail(File currentFile, String mimeType, boolean overwriteIfExists) throws IOException, FileNotFoundException {
        generateThumbnail(currentFile, FileType.RAW, mimeType, overwriteIfExists, false, false);
    }

    /**
     * Generate a thumbnail for this file.
     *
     * @param overwriteIfExists
     * @param currentFile
     * @param mimeType
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void generateThumbnail(File currentFile, FileType fileType, String mimeType,
                                         boolean overwriteIfExists,
                                         boolean overwriteRawThumbnailIfExists,
                                         boolean overwriteLargeRawIfExists)
            throws IOException, FileNotFoundException {
        String directory = currentFile.getParent();
        String fileExtension = MimeType.getForMimeType(mimeType).getFileExtension();

        //the original file
        String fileName = directory + File.separator + fileType.toString() + fileExtension;

        //generate the square thumbnail
        String thumbnailFilePath = directory + File.separator + "thumbnail";
        File thumbnailFile = new File(thumbnailFilePath + fileExtension);
        generateThumbnail(fileName, thumbnailFile, THUMB_SIZE, overwriteIfExists);

        //generate the smallRaw
        String rawThumbnailFilePath = directory + File.separator + "smallRaw";
        File rawThumbnailFile = new File(rawThumbnailFilePath + fileExtension);
        generateRawThumbnail(fileName, rawThumbnailFile, SMALL_RAW_THUMB_SIZE, overwriteRawThumbnailIfExists);

        //generate the largeRaw
        String largeRawFilePath = directory + File.separator + "largeRaw";
        File largeRawFile = new File(largeRawFilePath + fileExtension);
        generateRawThumbnail(fileName, largeRawFile, LARGE_RAW_THUMB_SIZE, overwriteLargeRawIfExists);
    }

    private static void generateThumbnail(String originalFile, File thumbnailFile, float size, boolean overwriteIfExists) throws IOException, FileNotFoundException {
        logger.debug("Generating thumbnail for : " + originalFile + ", size: " + size);
        if (!thumbnailFile.exists() || overwriteIfExists) {
            //delete if it exists
            if (thumbnailFile.exists()) {
                FileUtils.forceDelete(thumbnailFile);
            }
            // generate the square thumbnail images
            createThumbnail(originalFile, thumbnailFile, size);
        }
    }

    /**
     * Create the thumbnail and write to file.
     *
     * @param fileName
     * @param thumbnailFile
     * @param thumbnailSize
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void createThumbnail(String fileName, File thumbnailFile, Float thumbnailSize)
            throws IOException, FileNotFoundException {
        ImageUtils iu = new ImageUtils();
        iu.load(fileName);
        iu.square();
        if (iu.smoothThumbnail(thumbnailSize)) {
            FileOutputStream fOut = new FileOutputStream(thumbnailFile);
            ImageIO.write(iu.getModifiedImage(), "jpg", fOut);
            fOut.flush();
            fOut.close();
        } else {
            File originalFile = new File(fileName);
            FileUtils.copyFile(originalFile, thumbnailFile);
        }
    }

    /**
     * Create the thumbnail and write to file.
     *
     * @param fileName
     * @param thumbnailFile
     * @param thumbnailSize
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void createRawThumbnail(String fileName, File thumbnailFile, Float thumbnailSize)
            throws IOException, FileNotFoundException {
        ImageUtils iu = new ImageUtils();
        iu.load(fileName);
        if (iu.smoothRawThumbnail(thumbnailSize)) {
            FileOutputStream fOut = new FileOutputStream(thumbnailFile);
            ImageIO.write(iu.getModifiedImage(), "jpg", fOut);
            fOut.flush();
            fOut.close();
        } else {
            File originalFile = new File(fileName);
            FileUtils.copyFile(originalFile, thumbnailFile);
        }
    }

    private static void generateRawThumbnail(String originalFile, File thumbnailFile, float size, boolean overwriteIfExists) throws IOException, FileNotFoundException {
        logger.debug("Generating thumbnail for : " + originalFile + ", size: " + size);

        if (!thumbnailFile.exists() || overwriteIfExists) {
            //delete if it exists
            if (thumbnailFile.exists()) {
                FileUtils.forceDelete(thumbnailFile);
            }

            // generate the square thumbnail images
            createRawThumbnail(originalFile, thumbnailFile, size);
        }
    }

    // Process only files under dir
    public static void processAllDirFiles(File dir, boolean overwriteIfExists, boolean overwriteRawThumbnailIfExists, boolean overwriteLargeRawIfExists) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                children = new String[]{};
            }
            logger.debug("Recursive Dir: " + dir.getName() + ", size of subDirs: " + children.length);
            for (int i = 0; i < children.length; i++) {
                processAllDirFiles(new File(dir, children[i]), overwriteIfExists, overwriteRawThumbnailIfExists, overwriteLargeRawIfExists);
            }
        } else {
            try {
                if (dir.getName().equals(FileType.DC.toString())) {

                    //read the dublin core in the same directory - determine if its an image
                    Map<String, String> properties = RepositoryFileUtils.readDcFileAsMap(dir);
                    String mimeType = properties.get(Predicates.DC_FORMAT.toString());

                    //get the identifier
                    //if (mimeTypes.contains(mimeType)) {
                    if (MimeType.PNG.getMimeType().equals(mimeType) || MimeType.JPEG.getMimeType().equals(mimeType) || MimeType.GIF.getMimeType().equals(mimeType)) {
                        generateThumbnail(dir, FileType.RAW, mimeType, overwriteIfExists, overwriteRawThumbnailIfExists, overwriteLargeRawIfExists);
                    }
                }
            } catch (Exception e) {
                System.err.println("Problem generating thumbnails for: " + dir.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }
}
