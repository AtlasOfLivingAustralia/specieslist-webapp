/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.harvester;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import org.ala.dao.InfoSourceDAO;
import org.ala.documentmapper.DocumentMapper;
import org.ala.model.InfoSource;
import org.ala.repository.Repository;
import org.ala.repository.Validator;

/**
 * Class with main method to trigger harvesting of infoSource resources.
 * 
 * Note: Code is probably not thread safe as some beans with state are created
 * as singletons by Spring.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("harvestRunner")
public class HarvestRunner {

	private static final Logger logger = Logger.getLogger(HarvestRunner.class);
	@Inject
	private InfoSourceDAO infoSourceDAO;
	@Inject
	private Validator validator;
	private static ApplicationContext context;

	/**
	 * Constructor to set the infoSourceDAO
	 * 
	 * @param infoSourceDAORO
	 */
	public HarvestRunner(InfoSourceDAO infoSourceDAORO) {
		this.infoSourceDAO = infoSourceDAORO;
	}

	/**
	 * Default no-args contructor
	 */
	public HarvestRunner() {}


	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// Get Spring context
		context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
		//InfoSourceDAO infoSourceDAORO = (InfoSourceDAO) context.getBean("infoSourceDao");
		HarvestRunner hr = (HarvestRunner) context.getBean("harvestRunner");

		if (args.length < 1) {
			System.out.println("Please enter an info source ID to harvest OR enter \"all\" " +
			"to harvest all info sources");
			System.exit(-1);
		} else if (args.length < 2 && args[0].equalsIgnoreCase("all")) {
			logger.info("Harvesting all info sources...");
			List<Integer> infoSourceIds = hr.getAllIds();
			hr.harvest(infoSourceIds);
		} else if (args.length < 2){
			try {
				Integer infoSourceId = Integer.parseInt(args[0]);
				List<Integer> infoSourceIds = new ArrayList<Integer>();
				infoSourceIds.add(infoSourceId);
				hr.harvest(infoSourceIds);
			} catch (NumberFormatException ex) {
				logger.error("info source id was not recognised as a number: "+ex.getMessage());
			}
		} else if (args.length < 3 && args[0].equalsIgnoreCase("all")) {
			try {
				Integer timeGap = Integer.parseInt(args[1]);
				logger.info("Harvesting all info sources...");
				List<Integer> infoSourceIds = hr.getAllIds();
				hr.harvest(infoSourceIds, timeGap);
			} catch (NumberFormatException ex) {
				logger.error("time gap was not recognised as a number: "+ex.getMessage());
			} 
		} else if (args.length < 3){
			try {
				Integer infoSourceId = Integer.parseInt(args[0]);
				Integer timeGap = Integer.parseInt(args[1]);
				List<Integer> infoSourceIds = new ArrayList<Integer>();
				infoSourceIds.add(infoSourceId);
				hr.harvest(infoSourceIds, timeGap);
			} catch (NumberFormatException ex) {
				logger.error("info source id was not recognised as a number: "+ex.getMessage());
			}
		} 

	}

	/**
	 * Get alist of all info sources
	 *
	 * @return infoSourceIds 
	 */
	private List<Integer> getAllIds() {
		List<Integer> infoSourceIds = infoSourceDAO.getIdsforAll();
		return infoSourceIds;
	}
	
	/**
	 * Harvest info sources in list
	 *
	 * @param infoSourceIds, timeGap
	 */	
	private void harvest(List<Integer> infoSourceIds, int timeGap) {
		for (Integer id : infoSourceIds) {
			logger.info("Harvesting infosource id: "+id);
			try {
				this.harvest(id, timeGap);
			} catch (Exception ex) {
				logger.error("Error - Exception: " + ex.getMessage(), ex);
			}
		}
	}
	/**
	 * Harvest info sources in list
	 *
	 * @param infoSourceIds
	 */
	private void harvest(List<Integer> infoSourceIds) {
		for (Integer id : infoSourceIds) {
			logger.info("Harvesting infosource id: "+id);
			try {
				this.harvest(id);
			} catch (Exception ex) {
				logger.error("Error - Exception: " + ex.getMessage(), ex);
			}
		}
	}
	
	/**
	 * Do the harvesting
	 * 
	 * @param infoSourceId, timeGap
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void harvest(Integer infoSourceId, int timeGap) throws ClassNotFoundException, InstantiationException,
	Exception {
		InfoSource is = infoSourceDAO.getById(infoSourceId);
		logger.debug("Harvesting info source: " + is.toString());
		// instantiate harvester class
		Harvester harvester = createHarvester(is.getHarvester());
		// pass connection properties to harvester class from infosource
		harvester.setConnectionParams(is.getConnectionParams());
		// instantiate document mapper
		DocumentMapper dm = createDocumentMapper(is.getDocumentMapper());
		// pass document mapper
		harvester.setDocumentMapper(dm);
		// start harvest
		harvester.start(infoSourceId, timeGap);
		// validate the resulting files
		validator.setInfoSourceId(infoSourceId);
		validator.findAndValidateFiles();
	}
	
	/**
	 * Do the harvesting
	 * 
	 * @param infoSourceId
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void harvest(Integer infoSourceId) throws ClassNotFoundException, InstantiationException,
	Exception {
		InfoSource is = infoSourceDAO.getById(infoSourceId);
		logger.debug("Harvesting info source: " + is.toString());
		// instantiate harvester class
		Harvester harvester = createHarvester(is.getHarvester());
		// pass connection properties to harvester class from infosource
		harvester.setConnectionParams(is.getConnectionParams());
		// instantiate document mapper
		DocumentMapper dm = createDocumentMapper(is.getDocumentMapper());
		// pass document mapper
		harvester.setDocumentMapper(dm);
		// start harvest
		harvester.start(infoSourceId);
		// validate the resulting files
		validator.setInfoSourceId(infoSourceId);
		validator.findAndValidateFiles();
	}

	/**
	 * Instantiate a Harvester object from a class name string,
	 * passing in a ref to a Spring-managed repository object
	 * 
	 * @param harvesterName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Harvester createHarvester(String harvesterName) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException {
		Harvester harvester = null;

		if (harvesterName != null) {
			logger.debug("Instantiating Harvester class: "+ harvesterName);
			Class cls = Class.forName(harvesterName);
			harvester = (Harvester) cls.newInstance();
			harvester.setRepository((Repository) context.getBean("repository")); // manually inject repository dependency
		}

		return harvester;
	}

	/**
	 * Instantiate a DocumentLogger object from a class name string
	 *
	 * @param documentMapperName
	 * @return dm the DocumentLogger
	 */
	private DocumentMapper createDocumentMapper(String documentMapperName) throws ClassNotFoundException, 
	InstantiationException, IllegalAccessException {
		DocumentMapper dm = null;

		if (documentMapperName != null) {
			logger.debug("Instantiating DocumentMapper class: "+ documentMapperName);
			Class cls = Class.forName(documentMapperName);
			dm = (DocumentMapper) cls.newInstance();
		}

		return dm;
	}

	/**
	 * @return the infoSourceDAO
	 */
	public InfoSourceDAO getInfoSourceDAO() {
		return infoSourceDAO;
	}

	/**
	 * @param infoSourceDAO the infoSourceDAO to set
	 */
	public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
		this.infoSourceDAO = infoSourceDAO;
	}
}
