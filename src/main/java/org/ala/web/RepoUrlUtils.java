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
package org.ala.web;

import java.io.File;
import java.util.List;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.Image;
import org.ala.util.FileType;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * A utility that maps directory paths in the repository to externally accessible URLs.
 * Doing this in the controllers saves a lot of common string manipulation in JSPs 
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("repoUrlUtils")
public class RepoUrlUtils {
	
	/** Logger initialisation */
	private final static Logger logger = Logger.getLogger(RepoUrlUtils.class);
	/** The path to the repository */
	protected String repositoryPath = "/data/bie/";
	/** The URL to the repository */
	protected String repositoryUrl = "http://bie.ala.org.au/repo/";

	/**
	 * Fix the repository URLs
	 * 
	 * @param searchConceptDTO
	 * @return
	 */
	public SearchResultsDTO<SearchDTO> fixRepoUrls(SearchResultsDTO<SearchDTO> searchResults){
		List<SearchDTO> dtos = searchResults.getResults();
		for(SearchDTO dto : dtos){
			if(dto instanceof SearchTaxonConceptDTO){
				fixRepoUrls((SearchTaxonConceptDTO) dto);
			}
		}
		return searchResults;
	}
	
	/**
	 * Fix the repository URLs
	 * 
	 * @param searchConceptDTO
	 * @return
	 */
	public List<Image> fixRepoUrls(List<Image> images){
		for(Image image : images){
			image = fixRepoUrls(image);
			logger.debug(image);
		}
		return images;
	}
	
	/**
	 * Fix the repository URLs
	 * 
	 * @param searchConceptDTO
	 * @return
	 */
	public SearchTaxonConceptDTO fixRepoUrls(SearchTaxonConceptDTO searchConceptDTO){
		
		String thumbnail = searchConceptDTO.getThumbnail();
		if(thumbnail!=null && thumbnail.contains(repositoryPath)){
			searchConceptDTO.setThumbnail(fixSingleUrl(thumbnail));
		}
		String image = searchConceptDTO.getImage();
		if(image!=null && image.contains(repositoryPath)){
			searchConceptDTO.setImage(fixSingleUrl(image));
		}
		return searchConceptDTO;
	}

	/**
	 * Fix the supplied URL
	 * 
	 * @param thumbnail
	 * @return
	 */
	public String fixSingleUrl(String thumbnail) {
		logger.debug("Converting filepath to URL");
		String url = thumbnail.replace(repositoryPath, repositoryUrl);
		logger.debug("Returning URL: "+url);
		return url;
	}
	
	/**
	 * Fix the repository URLs
	 * 
	 * @param searchConceptDTO
	 * @return
	 */
	public ExtendedTaxonConceptDTO fixRepoUrls(ExtendedTaxonConceptDTO taxonConceptDTO){
		List<Image> images = taxonConceptDTO.getImages();
		if(images!=null){
			for(Image image: images){
				fixRepoUrls(image);
			}
		}
		images = taxonConceptDTO.getDistributionImages();
		if(images!=null){
			for(Image image: images){
				fixRepoUrls(image);
			}
		}
		images = taxonConceptDTO.getScreenshotImages();
        if(images!=null){
            for(Image image: images){
                fixRepoUrls(image);
            }
        }
		return taxonConceptDTO;
	}

	/**
	 * Fix URLS for images.
	 * 
	 * @param image
	 */
	public Image fixRepoUrls(Image image) {
		String imageLocation = image.getRepoLocation();
		
		if(imageLocation!=null && imageLocation.contains(repositoryPath)){
			imageLocation = fixSingleUrl(imageLocation);
			image.setRepoLocation(imageLocation);
		}
		
		int lastFileSep = imageLocation.lastIndexOf(File.separatorChar);
		String baseUrl = imageLocation.substring(0, lastFileSep+1);
		String fileName = imageLocation.substring(lastFileSep+1);
		String extension = FilenameUtils.getExtension(fileName);
		String thumbnail = baseUrl + "thumbnail"+ "." + extension;
		
		//set the thumbnail location and DC path
		image.setDcLocation(baseUrl + FileType.DC.getFilename());
		image.setThumbnail(thumbnail);
		return image;
	}

	/**
	 * @param repositoryPath the repositoryPath to set
	 */
	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	/**
	 * @param repositoryUrl the repositoryUrl to set
	 */
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
}
