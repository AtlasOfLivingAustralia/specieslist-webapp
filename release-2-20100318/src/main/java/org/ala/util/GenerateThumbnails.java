package org.ala.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.ala.repository.Predicates;
import org.apache.commons.io.FileUtils;
/**
 * Generates thumbnails for the repository by recursively scanning the
 * filesystem from the root. 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class GenerateThumbnails {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		
		if(args.length==0){
			System.err.println("Please supply the root directory to recursively search. e.g. /data/bie");
			System.exit(1);
		}
		String rootDir = args[0];
		
		GenerateThumbnails r = new GenerateThumbnails();
//		r.generate("/data/bie/");
		
		boolean overwrite = System.getProperty("overwrite") !=null;
		
//		r.generate("/data/bie/1011/0/288", overwrite);
		int filesRead = r.generate(rootDir, overwrite);
		System.out.println("Scanning complete. "+filesRead+" files read.");
		
	}

	/**
	 * Recursively scan the directory creating thumbnails.
	 * 
	 * @param rootDirectory
	 * @param overwriteIfExists
	 * @return
	 * @throws Exception
	 */
	private int generate(String rootDirectory, boolean overwriteIfExists) throws Exception {
		
		System.out.println("Scanning directory: "+rootDirectory);
		
		int filesRead = 0;
		
		List<String> mimeTypes = MimeType.getImageMimeTypes();
		
		//start scan
		File file = new File(rootDirectory);
		Iterator<File> fileIterator = FileUtils.iterateFiles(file, null, true);
		while(fileIterator.hasNext()){
			File currentFile = fileIterator.next();
			try {
				if (currentFile.getName().equals(FileType.DC.toString())) {
					filesRead++;

					//read the dublin core in the same directory - determine if its an image
					Map<String, String> properties = RepositoryFileUtils.readDcFileAsMap(currentFile);
					String mimeType = properties.get(Predicates.DC_FORMAT.toString());

					//get the identifier
					if (mimeTypes.contains(mimeType)) {

						String directory = currentFile.getParent();
						String fileExtension = MimeType.getForMimeType(mimeType).getFileExtension();
						String fileName = directory + File.separator + FileType.RAW.toString() + fileExtension;
						String thumbnailFilePath = directory + File.separator + "thumbnail" + fileExtension;
						File thumbnailFile = new File(thumbnailFilePath);

						if (!thumbnailFile.exists() || overwriteIfExists) {
							
							System.out.println("Generating thumbnail for : "+fileName);
							//delete if it exists
							if (thumbnailFile.exists()) {
								FileUtils.forceDelete(thumbnailFile);
							}
							BufferedImage original = ImageIO.read(new File(fileName));
							BufferedImage scaled = ImageUtils.awtScaleImage(original, 100, Image.SCALE_SMOOTH);
							scaled = ImageUtils.cropImage(scaled, 100);
							FileOutputStream fOut = new FileOutputStream(thumbnailFile);
							ImageIO.write(scaled, "jpg", fOut);
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Problem generating thumbnails for: "+currentFile.getName());
				e.printStackTrace();
			}
		}
		return filesRead;
	}
}
