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
package au.org.ala.cas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PatternMatchingUtils {

	private final static Logger logger = Logger.getLogger(PatternMatchingUtils.class);

	public static List<Pattern> getPatternList(String context, String regexPatterns) {
		List<Pattern> patternList = new ArrayList<Pattern>();

		if (regexPatterns != null && !regexPatterns.equals("")) {
			for (String regex : regexPatterns.split(",")) {
				patternList.add(Pattern.compile(context + regex.trim(), Pattern.CASE_INSENSITIVE));
			}
		}

		return patternList;
	}

	public static boolean matches(String str, List<Pattern> patterns) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(str);
			logger.trace("Matching string '" + str + "' against Pattern '" + pattern + "'");
			if (matcher.matches()) {
				logger.trace("Matches!");
				return true;
			} else {
				logger.trace("No match");
			}
		}

		return false;
	}
}
