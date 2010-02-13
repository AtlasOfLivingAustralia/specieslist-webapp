/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.TaxonConceptDao;
import org.apache.log4j.Logger;

/**
 * Serves all the details associated with a taxon concept.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class TaxonConceptServlet extends HttpServlet{

	private static final long serialVersionUID = 6560363824981541478L;
	protected Logger logger = Logger.getLogger(TaxonConceptServlet.class);
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String guid = req.getParameter("guid");
		String debug = req.getParameter("debug");
		
		logger.debug("Retrieving concept with guid: "+guid);
		try {
			if(debug!=null){
				TaxonConceptDao tcDao = new TaxonConceptDao();
				Map<String,String> properties = tcDao.getPropertiesFor(guid);
				req.setAttribute("properties", properties);
				//Servlet JSP communication
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/debug.jsp");
				reqDispatcher.forward(req,resp);
			} else {
				
				//TODO inject me
				TaxonConceptDao tcDao = new TaxonConceptDao();
				
				req.setAttribute("extendedTaxonConcept",tcDao.getExtendedTaxonConceptByGuid(guid));
				
				
//				req.setAttribute("taxonConcept", tcDao.getByGuid(guid));
//				req.setAttribute("taxonName", tcDao.getTaxonNameFor(guid));
//				req.setAttribute("synonyms", tcDao.getSynonymsFor(guid));
//				req.setAttribute("commonNames", tcDao.getCommonNamesFor(guid));
//				req.setAttribute("childConcepts", tcDao.getParentConceptsFor(guid));
//				req.setAttribute("parentConcepts", tcDao.getChildConceptsFor(guid));
//				req.setAttribute("images", tcDao.getImages(guid));
//				req.setAttribute("pestStatuses", tcDao.getPestStatuses(guid));
//				req.setAttribute("conservationStatuses", tcDao.getConservationStatuses(guid));
				
				//add literal properties 
				
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/taxonConcept.jsp");
				reqDispatcher.forward(req,resp);
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}
}
