package org.ala.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtils {

	public static ApplicationContext getContext(){
		 return new ClassPathXmlApplicationContext(new String[]{"classpath*:spring-profiler.xml", "classpath:spring.xml"});
	}
}
