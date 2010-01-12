package org.ala.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.TaxonConcept;
import org.apache.commons.lang.StringUtils;

public class QueryServlet extends HttpServlet{

	private static final long serialVersionUID = 6560363824981541478L;

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String query = StringUtils.trimToNull(req.getParameter("q"));
		String guid = StringUtils.trimToNull(req.getParameter("guid"));
		
		System.out.println("Query: "+query);
		System.out.println("Guid: "+guid);
		
		if(query!=null){
			try {
				TaxonConceptDao tcDao = new TaxonConceptDao();
				List<TaxonConcept> tcs = tcDao.getByScientificName(query, 100);
				if(tcs==null || tcs.isEmpty()){
					TaxonConcept tc = tcDao.getByGuid(query);
					if(tc!=null){
						tcs.add(tc);
					}
				} 
					
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
				
				//add synonyms
				
				//add parent
				
				//add child links
				
				//add vernacular names
				
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/ajaxTree.jsp");
				reqDispatcher.forward(req,resp);
			} catch (Exception e) {
				throw new ServletException(e.getMessage(), e);
			}			
		}
	}
}
