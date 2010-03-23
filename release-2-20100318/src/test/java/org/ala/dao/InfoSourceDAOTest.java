/* *************************************************************************
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

package org.ala.dao;

import java.util.List;
import javax.sql.DataSource;
import junit.framework.TestCase;
import org.ala.model.InfoSource;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class InfoSourceDAOTest extends TestCase {
    private InfoSourceDAO infoSourceDAO;
    private static ApplicationContext context;

    /**
     * @throws Exception 
     * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
		DataSource dataSource = (DataSource) context.getBean("dataSource");
		infoSourceDAO = new InfoSourceDAOImpl(dataSource);
	}

    @Test
	public void testLookups() {
        //
        List<InfoSource> ifs1 = infoSourceDAO.getAllByDatasetType();
        System.out.println("ordered by dataset:\n"+StringUtils.join(ifs1, "\n"));
    }

}
