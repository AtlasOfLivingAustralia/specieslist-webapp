package org.ala.web.admin.controller;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import org.ala.dao.RankingDao;
import org.ala.report.GoogleSitemapGenerator;
import org.ala.util.ReadOnlyLock;
import org.ala.web.admin.dao.CollectionDao;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CasSolrAdminController {
	private static final String ADMIN_ROLE = "ROLE_SYSTEM_ADMIN";
	
	/** Logger initialisation */
    private final static Logger logger = Logger.getLogger(CasSolrAdminController.class);
    
	@Inject
	private RankingDao rankingDao;
	
	@Inject
	private CollectionDao collectionsDao;
	   
	@Inject
	private GoogleSitemapGenerator googleSitemapGenerator;
	
	/**
	 * Returns true when in service is in readonly mode.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/admin/isReadOnly", method = RequestMethod.GET)
	public @ResponseBody
	boolean isReadOnly() {
		return ReadOnlyLock.getInstance().isReadOnly();
	}

	/**
	 * Returns true when in service is in readonly mode.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/admin/forceUnlock/{password}", method = RequestMethod.GET)
	public @ResponseBody
	boolean forceUnlock(@PathVariable("password") String password, HttpServletRequest request) {
		boolean completed = false;
		String remoteuser = request.getRemoteUser();
		if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {	
			completed = ReadOnlyLock.getInstance().forceUnlock(password);
		}
		return completed;
	}

	/**
	 * Optimises the SOLR index. Use this API to optimise the index so that the
	 * bie-service can enter read only mode during this process.
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/admin/optimise", method = RequestMethod.GET)
	public void optimise(HttpServletRequest request,
			HttpServletResponse response) {
		String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {	
				completed = rankingDao.optimiseIndex();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
	}


	@RequestMapping(value = "/admin/loadCaab", method = RequestMethod.GET)
	public void loadCaab(HttpServletRequest request,
			HttpServletResponse response) {
		String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {				
				completed = rankingDao.loadCAAB();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
	}


    @RequestMapping(value = "/admin/reloadAllRanks", method = RequestMethod.GET)
    public void reloadAllRanks(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				completed = rankingDao.reloadAllRanks();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	
    
    @RequestMapping(value = "/admin/reloadCollections", method = RequestMethod.GET)
    public void reloadCollections(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				completed = collectionsDao.reloadCollections();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	

    @RequestMapping(value = "/admin/reloadInstitutions", method = RequestMethod.GET)
    public void reloadInstitutions(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				completed = collectionsDao.reloadInstitutions();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	

    @RequestMapping(value = "/admin/reloadDataProviders", method = RequestMethod.GET)
    public void reloadDataProviders(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				completed = collectionsDao.reloadDataProviders();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	

    @RequestMapping(value = "/admin/reloadDataResources", method = RequestMethod.GET)
    public void reloadDataResources(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				completed = collectionsDao.reloadDataResources();
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	
 
    @RequestMapping(value = "/admin/regenSitemap", method = RequestMethod.GET)
    public void regenSitemap(HttpServletRequest request, 
            HttpServletResponse response)throws Exception{
    	String remoteuser = request.getRemoteUser();
		boolean completed = false;
		PrintWriter writer = null;
		try{
			writer = response.getWriter();
			if (remoteuser != null && request.isUserInRole(ADMIN_ROLE)) {
				googleSitemapGenerator.doFullScan();
				completed = true;				
				response.setStatus(HttpServletResponse.SC_OK);
				writer.write("{task completed: " + completed + "}");
			}
			else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				writer.write("{You need to have the appropriate role (" + ADMIN_ROLE + ") to access this service. task completed:" + completed + "}");
			}
		}
		catch(Exception ex){
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			writer.write("{error: " + ex.getMessage() + "}");
			logger.error(ex);
		}		
    }	    
}
