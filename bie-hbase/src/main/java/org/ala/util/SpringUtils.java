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
package org.ala.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * Static utility methods for spring interaction.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SpringUtils {

	/**
	 * Retrieve a context, loading configuration from the classpath.
	 * @return ApplicationContext
	 */
	public static ApplicationContext getContext(){
		 return new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-profiler.xml", "classpath:spring.xml"});
	}
}
