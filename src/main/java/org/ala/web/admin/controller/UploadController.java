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
package org.ala.web.admin.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import org.ala.dao.DocumentDAO;
import org.ala.web.admin.dao.ImageUploadDao;
import org.ala.model.Document;
import org.ala.web.admin.model.UploadItem;
import org.ala.repository.Predicates;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 * Controller for upload single image
 * 
 * @author mok011
 *
 */
@Controller
//@RequestMapping(value = "/upload")
public class UploadController {

    @Inject
    protected ImageUploadDao imageUploadDao;
    @Inject
    protected DocumentDAO documentDao;
    
    private static final int INFOSOURCE_ID = 1061; // infosource id
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static Logger logger = Logger.getLogger(UploadController.class);

    /**
     * Display form via GET request
     * 
     * @param model
     * @param request
     * @return
     */
//    @RequestMapping(method = RequestMethod.GET)
    @RequestMapping(value = {"/admin/upload"}, method = RequestMethod.GET)
    public String getUploadForm(UploadItem uploadItem, BindingResult result, HttpServletRequest request) {
        String view = null;
        String remoteuser = request.getRemoteUser();
        
        if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
            AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
            
            if (uploadItem.getUserName() == null && uploadItem.getEmail() == null && principal != null) {
                // set the userName and email to logged-in user if not already set
                String userName = principal.getAttributes().get("firstname").toString() + " " + principal.getAttributes().get("lastname").toString();
                uploadItem.setUserName(userName);
                uploadItem.setEmail(remoteuser);
            }
            
            if (uploadItem.getAttribn() == null && uploadItem.getUserName() != null) {
                // set a default attribution
                DateFormat dateFormat = new SimpleDateFormat("yyyy");
                Date date = new java.util.Date();
                uploadItem.setAttribn(uploadItem.getUserName() + ", " + dateFormat.format(date));
            } 
            
            if (uploadItem.getRank() == null) {
                // default "rank" selected option
                uploadItem.setRank("Species"); 
            }
            
            if (uploadItem.getTitle() == null && uploadItem.getScientificName() != null) {
                // set default title to scientific name
                uploadItem.setTitle(uploadItem.getScientificName());
            }
            view = "admin/uploadForm";
        } 
        else {
            if (request.getUserPrincipal() != null) {
                logger.debug("user role = " + request.getUserPrincipal().toString());
            }
            view = "admin/error";
        }

        return view;
    }
    
    @RequestMapping(value = {"/admin/edit"}, method = RequestMethod.GET)
    public String getEditForm(UploadItem uploadItem, BindingResult result, HttpServletRequest request){
    	String view = "admin/editForm";   
    	
    	try {
            String remoteuser = request.getRemoteUser();
            
            if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
                AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();                 
		        String guid = request.getParameter("guid");
		        
		        String uri = request.getParameter("uri");
		        // mysql uri is not equal imageIdentifier..trim down begining part.
		        if(uri != null && uri.trim().startsWith("http://bie.ala.org.au/uploads/")){
		        	uri = uri.substring("http://bie.ala.org.au/uploads/".length(), uri.length());
		        }
		        
		        Document doc = documentDao.getByUri(uri);
		        if(doc == null){
		        	return "admin/docError";
		        }
		        
		        uploadItem.setDocumentId("" + doc.getId());
		        uploadItem.setGuid(guid);
		        
		        Map<String, String> dc = imageUploadDao.readDcFile(doc);
		        List<org.ala.repository.Triple<String, String, String>> rdf = imageUploadDao.readRdfFile(doc);		      
		        for(org.ala.repository.Triple<String, String, String> triple : rdf){
		        	if(Predicates.COMMON_NAME.toString().equalsIgnoreCase(triple.getPredicate())){
		        		uploadItem.setCommonName((String)triple.getObject());
		        	}
		        	if(Predicates.SCIENTIFIC_NAME.toString().equalsIgnoreCase(triple.getPredicate())){
		        		uploadItem.setScientificName((String)triple.getObject());
		        	}
		        }
		        
		        Set<String> keys = dc.keySet();
		    	Iterator<String> itr = keys.iterator();
		    	while(itr.hasNext()){
		    		String key = itr.next();
	                // expect 2 element String array (key, value)
	                if (Predicates.DC_CREATOR.toString().equalsIgnoreCase(key)) {
	                	uploadItem.setUserName(dc.get(key));
	                } 
	                else if (Predicates.DC_LICENSE.toString().equalsIgnoreCase(key)) {
	                	uploadItem.setLicence(dc.get(key));
	                } 
	                else if (Predicates.DC_RIGHTS.toString().equalsIgnoreCase(key)) {
	                	uploadItem.setAttribn(dc.get(key));
	                } 
	                else if (Predicates.DC_TITLE.toString().equalsIgnoreCase(key)) {
	                	uploadItem.setTitle(dc.get(key));
	                } 
	                else if (Predicates.DC_DESCRIPTION.toString().equalsIgnoreCase(key)) {
	                	uploadItem.setDescription(dc.get(key));
	                } 
	            }	            
            }
            else {
                if (request.getUserPrincipal() != null) {
                    logger.debug("user role = " + request.getUserPrincipal().toString());
                }
                view = "admin/error";
            }
        } 
    	catch (Exception ex) {
            logger.error(ex.toString());
        }
        return view;
    }
    
    @RequestMapping(value = {"/admin/edit"}, method = RequestMethod.POST)
    public ModelAndView handleEditForm(@Valid UploadItem uploadItem, BindingResult result) {
    	ModelAndView mav = null;
    	
        if (result.hasErrors()) {
        	List<ObjectError> errors = result.getAllErrors();
        	// empty email is allowed, invalid email address is not allowed.
        	if(errors.size() == 1 && ((FieldError)errors.get(0)).getField().equalsIgnoreCase("email") && "NotEmpty".equalsIgnoreCase(errors.get(0).getCode())){
        		mav = null;
        	}
        	else{
	            // validation failed - reload form with error messages
	            for (ObjectError error : result.getAllErrors()) {
	                System.err.println("Error: " + error.getCode() + " - " + error.getDefaultMessage());
	            }
	            // reload form with validation errors
	            mav = new ModelAndView("admin/editForm", "uploadItem", uploadItem);
        	}
        }

        if (mav == null && uploadItem.getDocumentId() != null){
        	Document doc = documentDao.getById(Integer.valueOf(uploadItem.getDocumentId()));
        	try {
				boolean ok = imageUploadDao.updateDocument(doc, uploadItem);
				if(ok){
					mav = new ModelAndView("admin/updateResult", "fileName", uploadItem.getDocumentId());
				}
				else{
					mav = new ModelAndView("admin/updateResult", "error", uploadItem.getDocumentId());
				}
			} 
        	catch (Exception e) {
				mav = new ModelAndView("admin/updateResult", "error", e);
				logger.error(e);
			}
        }        
        return mav;    	
    }
    
    /**
     * Process submitted form via POST request
     * 
     * @param uploadItem
     * @param result
     * @return
     */
//    @RequestMapping(method = RequestMethod.POST)
    @RequestMapping(value = {"/admin/upload"}, method = RequestMethod.POST)
    public ModelAndView handleFormUpload(@Valid UploadItem uploadItem, BindingResult result) {
        if (result.hasErrors()) {
            // validation failed - reload form with error messages
            for (ObjectError error : result.getAllErrors()) {
                System.err.println("Error: " + error.getCode() + " - "  + error.getDefaultMessage());
            }
            // reload form with validation errors
            return new ModelAndView("admin/uploadForm", "uploadItem", uploadItem);
        }

        MultipartFile file = uploadItem.getFileData();
        String fileName = "";
        if (file != null) {
            try {
                fileName = file.getOriginalFilename();
                Document doc = imageUploadDao.storeDocument(INFOSOURCE_ID, uploadItem);

                if (doc == null) {
                    //store document error
                    String msg = "storeDocument failed for " + fileName;
                    logger.warn(msg);
                    return new ModelAndView("fileUploadResult", "error", msg);
                } else {
                    logger.info("Image document created: " + doc.toString());
                    }		
            } catch (Exception e) {
                logger.warn("handleForm error: " + e.getMessage(), e);
                return new ModelAndView("fileUploadResult", "error", e.getMessage());
            }
        }
        return new ModelAndView("admin/fileUploadResult", "fileName", fileName);
    }

    @RequestMapping(value = {"/admin"}, method = RequestMethod.GET)
    public String homeJsp(HttpServletRequest request) {
        String view = null;
        String remoteuser = request.getRemoteUser();
        
        if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
            view = "admin/home";
        } 
        else {
            if (request.getUserPrincipal() != null) {
                logger.debug("user role = " + request.getUserPrincipal().toString());
            }
            view = "admin/error";
        }

        return view;
    }

    @RequestMapping(value = {"/admin/multiUpload"}, method = RequestMethod.GET)
    public String multiUploadJsp(HttpServletRequest request) {
        String view = null;
        String remoteuser = request.getRemoteUser();
        
        if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
            view = "admin/multiUpload";
        } 
        else {
            if (request.getUserPrincipal() != null) {
                logger.debug("user role = " + request.getUserPrincipal().toString());
            }
            view = "admin/error";
        }

        return view;
    }

    @RequestMapping(value = {"/admin/multiUpload"}, method = RequestMethod.POST)
    public String multiUploadProcess(HttpServletRequest request) {
        String view = null;
        String remoteuser = request.getRemoteUser();
        
        if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
            view = "admin/processFileUpload";
        } 
        else {
            if (request.getUserPrincipal() != null) {
                logger.debug("user role = " + request.getUserPrincipal().toString());
            }
            view = "admin/error";
        }

        return view;
    }

    /**
     * Set the imageUploadDao
     * 
     * @param imageUploadDao
     */
    public void setImageUploadDao(ImageUploadDao imageUploadDao) {
        this.imageUploadDao = imageUploadDao;
    }
}
