package org.ala.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.log4j.Logger;

import com.sun.media.jai.codec.FileSeekableStream;

/**
 * Utlilty class to manipulate images: crop, square and scale (thumbnail).
 *
 * <p>Some code borrowed from:</p>
 * <ul>
 * <li>Perry Nguyen's article on
 * <a href="http://www.hanhuy.com/pfn/java-image-thumbnail-comparison">
 * A comparison of Java image thumbnailing techniques</a></li>
 * <li>Ricardo J. Méndez's <a href="http://github.com/ricardojmendez/grails-imagetools/blob/master/src/groovy/org/grails/plugins/imagetools/ImageTool.groovy">ImageTools
 * Grails plugin</a> on Github</li>
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class ImageUtils {
	
	static Logger logger = Logger.getLogger(ImageUtils.class);
	
    RenderedOp originalImage = null;
    RenderedOp modifiedImage = null;
    private boolean decreaseOnly = true;

    /* Remove the accelaration lib exception */
    static { System.setProperty("com.sun.media.jai.disableMediaLib", "true"); }

    /**
	 * Loads an image from a file.
	 *
     * @param file
     * @throws IOException
	 */
	public void load(String file) throws IOException {
		//originalImage = ImageIO.read(new File(file));
        FileSeekableStream fss = new FileSeekableStream(file);
		originalImage = JAI.create("stream", fss);
        // keep a copy
        modifiedImage = (RenderedOp) originalImage.createSnapshot();
	}

    /**
     * Crop image to a square by removing long edges and centering
     */
    public void square() {
        float border = modifiedImage.getWidth() - modifiedImage.getHeight();
		float cropX;
        float cropY;

		if (border > 0) {
			cropX = border;
			cropY = 0;
		} else {
			cropX = 0;
			cropY = -border;
		}

		crop(cropX, cropY);
    }

    /**
	 * Crop the image to the specified width & height lengths in pixels
	 *
	 * @param w
	 * @param h
	 */
	public void crop(float w, float h) {
        ParameterBlock params = new ParameterBlock();
		params.addSource(modifiedImage);
		params.add((float) Math.round(w / 2)); //x origin
		params.add((float) Math.round(h / 2)); //y origin
		params.add((float) (modifiedImage.getWidth() - w)); //width
		params.add((float) (modifiedImage.getHeight() - h)); //height
		modifiedImage = JAI.create("crop", params);
	}

    /**
	 * Create a thumbnail using JAI
	 *
	 * @param edgeLength Maximum length
	 */
	public void thumbnail(float edgeLength) {
		float height = modifiedImage.getHeight();
        float width = modifiedImage.getWidth();

        if (!(height < edgeLength && width < edgeLength && decreaseOnly)) {
			boolean tall = (height > width);
			float modifier = edgeLength / (float) (tall ? height : width);
			ParameterBlock params = new ParameterBlock();
			params.addSource(modifiedImage);
			params.add(modifier); //x scale factor
			params.add(modifier); //y scale factor
			params.add(0.0F); //x translate
			params.add(0.0F); //y translate
			params.add(new InterpolationNearest());//interpolation method
			modifiedImage = JAI.create("scale", params);
		}
	}

    /**
	 * Creates a smooth thumbnail using AWT
	 *
	 * @param edgeLength Maximum length
	 */
	public boolean smoothThumbnail(float edgeLength) {
		float height = modifiedImage.getHeight();
        float width = modifiedImage.getWidth();
        boolean ok = false;
        if(logger.isDebugEnabled()){
        	logger.debug("height: "+height+", edgeLength:"+edgeLength+", width: "+width+", decreaseOnly:"+decreaseOnly);
        }
         if (!(height < edgeLength && width < edgeLength && decreaseOnly)) {
			boolean tall = (height > width);
			float modifier = edgeLength / (float) (tall ? height : width);
            int w = (int)(width * modifier);
            int h = (int)(height * modifier);
            Image i = modifiedImage.getAsBufferedImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(i, null, null);
            g.dispose();
            i.flush();
            modifiedImage = JAI.create("awtImage", (Image)image);
            ok = true;
		}
        logger.debug("Returning :"+ok); 
        return ok;
	}
    
    public boolean imageIsBlank(File imageFile) throws Exception {
        //System.out.println("Using file path: " + args[0]);
        FileSeekableStream fss = new FileSeekableStream(imageFile);
        RenderedOp originalImage = JAI.create("stream", fss);
        //originalImage.get
        BufferedImage image = originalImage.getAsBufferedImage();
        int height = image.getHeight();
        int width = image.getWidth();
        Raster raster = image.getRaster();
        int[][] bins = new int[3][256];

        int firstSample = raster.getSample(0, 0, 0);
        
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if(raster.getSample(i, j, 0) != firstSample
                        || raster.getSample(i, j, 1) != firstSample
                        || raster.getSample(i, j, 2) != firstSample)
                    return false;
            }
        
        return true;
    }

    /**
	 * Creates a smooth thumbnail using AWT
	 *
	 * @param edgeLength Maximum length
	 */
	public boolean smoothRawThumbnail(float maxWidth) {
		float height = modifiedImage.getHeight();
        float width = modifiedImage.getWidth();
        boolean ok = false;

         if (!(width < maxWidth && decreaseOnly)) {
			float modifier = maxWidth / width;
            int w = (int)(width * modifier);
            int h = (int)(height * modifier);
            Image i = modifiedImage.getAsBufferedImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(i, null, null);
            g.dispose();
            i.flush();
            modifiedImage = JAI.create("awtImage", (Image)image);
            ok = true;
		}
        return ok;
	}
	
    /**
	 * Writes the resulting image to a file.
	 *
	 * @param file full path where the image should be saved
	 * @param type file type for the image
     * @throws IOException
     * @see <a href="http://java.sun.com/products/java-media/jai/iio.html">Possible JAI encodings</a>
	 */
	public void writeResult(String file, String type) throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		JAI.create("encode", modifiedImage, os, type, null);
		os.close();
	}

    /**
     * Output the image to the client as a BufferedImage
     *
     * @return
     */
    public BufferedImage getModifiedImage() {
        return modifiedImage.getAsBufferedImage();
    }

    /**
     * Output the original image (unaltered) to the client as a BufferedImage
     *
     * @return
     */
    public BufferedImage getOriginalImage() {
        return originalImage.getAsBufferedImage();
    }

}