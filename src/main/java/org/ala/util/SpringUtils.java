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
