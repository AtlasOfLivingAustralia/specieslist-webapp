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
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.TaxonConceptDao;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.TaxonConcept;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * Debug query servlet that uses the lucene indicies.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class QueryServlet extends HttpServlet{

	private static final long serialVersionUID = 6560363824981541478L;
	protected Logger logger = Logger.getLogger(QueryServlet.class);

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String query = StringUtils.trimToNull(req.getParameter("q"));
		String guid = StringUtils.trimToNull(req.getParameter("guid"));
		
		logger.debug("Query: "+query);
		logger.debug("Guid: "+guid);
		
		if(query!=null){
			try {
				TaxonConceptDao tcDao = new TaxonConceptDao();
				List<SearchTaxonConceptDTO> tcs = tcDao.findByScientificName(query, 100);
				req.setAttribute("taxonConcepts", tcs);
				
				//Servlet JSP communication
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/ajaxTree.jsp");
				reqDispatcher.forward(req,resp);
			} catch (Exception e) {
				throw new ServletException(e.getMessage(), e);
			}
			
		} else if(guid!=null){
			try {
				TaxonConceptDao tcDao = new TaxonConceptDao();
				TaxonConcept tc = tcDao.getByGuid(guid);
				
				//add core concept
				req.setAttribute("taxonConcept", tc);
				
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/ajaxTree.jsp");
				reqDispatcher.forward(req,resp);
			} catch (Exception e) {
				throw new ServletException(e.getMessage(), e);
			}
		}
	}
}
