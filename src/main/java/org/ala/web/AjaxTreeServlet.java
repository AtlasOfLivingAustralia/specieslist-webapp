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

public class AjaxTreeServlet extends HttpServlet{

	private static final long serialVersionUID = 6560363824981541478L;

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String guid = req.getParameter("root");
		if("source".equalsIgnoreCase(guid)){
//			guid = "NULL"; //chordata
			guid = "urn:lsid:biodiversity.org.au:afd.taxon:065f1da4-53cd-40b8-a396-80fa5c74dedd"; //chordata
		}
		
		try {
			TaxonConceptDao tcDao = new TaxonConceptDao();
			List<TaxonConcept> tcs = tcDao.getByParentGuid(guid, 1000);
			req.setAttribute("taxonConcepts", tcs);
			
			//Servlet JSP communication
			RequestDispatcher reqDispatcher = getServletConfig().getServletContext().getRequestDispatcher("/ajaxTree.jsp");
			reqDispatcher.forward(req,resp);
		} catch (Exception e) {
			throw new ServletException(e.getMessage(), e);
		}
	}
}
