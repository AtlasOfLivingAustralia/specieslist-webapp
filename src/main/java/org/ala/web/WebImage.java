package org.ala.web;

import org.ala.model.Image;
import org.springframework.beans.BeanUtils;

/**
 * An extended DTO for image that adds the additional URL paths
 * to make it more consumable by front ends.
 */
public class WebImage extends Image {

    String largeImageUrl;
    String smallImageUrl;

    public WebImage(Image image){
        BeanUtils.copyProperties(image, this);
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }
}
