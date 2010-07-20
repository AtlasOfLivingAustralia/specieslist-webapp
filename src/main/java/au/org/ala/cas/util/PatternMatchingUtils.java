package au.org.ala.cas.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PatternMatchingUtils {

	private final static Logger logger = Logger.getLogger(PatternMatchingUtils.class);

	public static List<Pattern> getPatternList(String uriPatterns) {
		List<Pattern> patternList = new ArrayList<Pattern>();

		if (uriPatterns != null && !uriPatterns.equals("")) {
			for (String pattern : uriPatterns.split(",")) {
				patternList.add(Pattern.compile(pattern.trim(), Pattern.CASE_INSENSITIVE));
			}
		}
		
		return patternList;
	}
	
	public static boolean matches(String uri, List<Pattern> patterns) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(uri);
			logger.trace("Matching Uri '" + uri + "' against Pattern '" + pattern + "'");
			if (matcher.matches()) {
				logger.trace("Uri matches!");
				return true;
			} else {
				logger.trace("No match");
			}
		}
		
		return false;
	}
}
