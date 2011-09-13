package org.ala.web.admin.controller;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import org.ala.dao.RankingDao;
import org.ala.util.ReadOnlyLock;
import org.springframework.stereotype.Controller;
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
}
