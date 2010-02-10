package org.ala.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.TaxonConceptDao;

public class TaxonConceptServlet extends HttpServlet{

	private static final long serialVersionUID = 6560363824981541478L;

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String guid = req.getParameter("guid");
		
		String debug = req.getParameter("debug");
		
		System.out.println("Guid: "+guid);
		try {
			if(debug!=null){
				TaxonConceptDao tcDao = new TaxonConceptDao();
				Map<String,String> properties = tcDao.getPropertiesFor(guid);
				req.setAttribute("properties", properties);
				//Servlet JSP communication
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/debug.jsp");
				reqDispatcher.forward(req,resp);
			} else {
				TaxonConceptDao tcDao = new TaxonConceptDao();
				req.setAttribute("taxonConcept", tcDao.getByGuid(guid));
				req.setAttribute("taxonName", tcDao.getTaxonNameFor(guid));
				req.setAttribute("synonyms", tcDao.getSynonymsFor(guid));
				req.setAttribute("commonNames", tcDao.getCommonNamesFor(guid));
				req.setAttribute("childConcepts", tcDao.getParentConceptsFor(guid));
				req.setAttribute("parentConcepts", tcDao.getChildConceptsFor(guid));
				req.setAttribute("images", tcDao.getImages(guid));
				req.setAttribute("pestStatuses", tcDao.getPestStatuses(guid));
				req.setAttribute("conservationStatuses", tcDao.getConservationStatuses(guid));
				
				//add literal properties 
				
				RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/taxonConcept.jsp");
				reqDispatcher.forward(req,resp);
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}
}
