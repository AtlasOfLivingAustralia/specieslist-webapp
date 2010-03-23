package org.ala.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

/**
 * Image handling utilities.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ImageUtils {

    /**
     * Thumbnail image generator, taken from
     * http://www.hanhuy.com/pfn/java-image-thumbnail-comparison;jsessionid=ED2CFDFF9B3A32CB89F1A15656902B44?page=2
     *
     * @param image
     * @param maxSize
     * @param interp
     * @return
     */
    protected BufferedImage jaiScaleImage(BufferedImage image, int maxSize, boolean square, Interpolation interp) {
        //System.out.println("JAI Scaling image to: " + maxSize);
        PlanarImage pi = new RenderedImageAdapter(image);
        int w = pi.getWidth();
        int h = pi.getHeight();
        float ratio = 0f;
        float scaleFactor = 1.0f;
        float cropBy = 0f; //maxSize / 2;
        float cropX = 0f;
        float cropY = 0f;

        if (w > h) {
            scaleFactor = ((float) maxSize / (float) w);
            int tmp = (int) (maxSize / ((float) w / (float) h));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (w * scaleFactor) - cropBy) / 2;
            cropX = (float) croptTmp;
            System.out.println("cropBy: "+cropBy+"|"+w+"|"+cropX);
        } else {
            scaleFactor = ((float) maxSize / (float) h);
            int tmp = (int) (maxSize / ((float) h / (float) w));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (h * scaleFactor) - cropBy) / 2;
            cropY = (float) croptTmp;
        }

        // scale image
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(pi);
        pb.add(scaleFactor);
        pb.add(scaleFactor);
        pb.add(0f);
        pb.add(0f);
        pb.add(interp);
        pi = JAI.create("scale", pb);
        
        if (square) {
            // crop image to a square
            int w1 = pi.getWidth();
            int h1 = pi.getHeight();
            ParameterBlockJAI params = new ParameterBlockJAI("crop");
            params.addSource(pi);
            params.setParameter("x", cropX);
            params.setParameter("y", cropY); // new Integer(pi.getMinY()).floatValue()
            params.setParameter("width", cropBy);
            params.setParameter("height", cropBy);
            pi = JAI.create("crop",params);
        }
        
        return pi.getAsBufferedImage();
    }

    /**
     * Thumbnail image generator, taken from
     * http://www.hanhuy.com/pfn/java-image-thumbnail-comparison;jsessionid=ED2CFDFF9B3A32CB89F1A15656902B44?page=2
     *
     * @param image
     * @param maxSize
     * @param hint
     * @return
     */
    public static BufferedImage awtScaleImage(BufferedImage image, int maxSize, int hint) {
        //System.out.println("AWT Scaling image to: " + maxSize);
        int w = image.getWidth();
        int h = image.getHeight();
        float scaleFactor = 1.0f;
        
        if (w > h) {
            scaleFactor = ((float) maxSize / (float) h);
        } else {
            scaleFactor = ((float) maxSize / (float) w);
        }
        
        w = (int)(w * scaleFactor);
        h = (int)(h * scaleFactor);
        // since this code can run both headless and in a graphics context
        // we will just create a standard rgb image here and take the
        // performance hit in a non-compatible image format if any
        Image i = image.getScaledInstance(w, h, hint);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(i, null, null);
        g.dispose();
        i.flush();
        return image;
    }

    /**
     * Crop image to a square (Flickr-style)
     *
     * @param scaled
     * @param scale
     * @return
     */
    public static BufferedImage cropImage(BufferedImage scaled, Integer scale) {
        // crop image to a square
        PlanarImage pi = new RenderedImageAdapter(scaled);
        int w = pi.getWidth();
        int h = pi.getHeight();
        float ratio = 0f;
        float scaleFactor = 1.0f;
        float cropBy = 0f; 
        float cropX = 0f;
        float cropY = 0f;
        if (w > h) {
            scaleFactor = ((float) scale / (float) w);
            int tmp = (int) (scale / ((float) w / (float) h));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (w * scaleFactor) - cropBy) / 2;
            cropX = (float) croptTmp;
            System.out.println("cropBy: " + cropBy + "|" + w + "|" + cropX);
        } else {
            scaleFactor = ((float) scale / (float) h);
            int tmp = (int) (scale / ((float) h / (float) w));
            cropBy = (float) tmp; // forces rounding down of float
            int croptTmp = (int) ((float) (h * scaleFactor) - cropBy) / 2;
            cropY = (float) croptTmp;
        }
        ParameterBlockJAI params = new ParameterBlockJAI("crop");
        params.addSource(pi);
        params.setParameter("x", cropX);
        params.setParameter("y", cropY); // new Integer(pi.getMinY()).floatValue()
        params.setParameter("width", (float) scale);
        params.setParameter("height", (float) scale);
        pi = JAI.create("crop", params);
        BufferedImage scaled2 = pi.getAsBufferedImage();
        return scaled2;
    }
}
