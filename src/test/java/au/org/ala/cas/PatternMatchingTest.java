package au.org.ala.cas;

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import au.org.ala.cas.util.PatternMatchingUtils;

public class PatternMatchingTest extends TestCase {

	public void testMatches() {
		String contextPath = "/biocache-webapp";
		String uriPattern = "/, /occurrences/\\d+";

		List<Pattern> patterns = PatternMatchingUtils.getPatternList(contextPath, uriPattern);
		assertFalse(PatternMatchingUtils.matches("", patterns));
		assertFalse(PatternMatchingUtils.matches("/", patterns));
		assertTrue(PatternMatchingUtils.matches("/biocache-webapp/", patterns));
		assertTrue(PatternMatchingUtils.matches("/biocache-webapp/occurrences/35661424", patterns));
		assertFalse(PatternMatchingUtils.matches("/favicon.ico", patterns));

		List<Pattern> noContextPatterns = PatternMatchingUtils.getPatternList(uriPattern);
		assertTrue(PatternMatchingUtils.matches("/", noContextPatterns));
		assertFalse(PatternMatchingUtils.matches("/biocache-webapp/", noContextPatterns));
		assertTrue(PatternMatchingUtils.matches("/occurrences/35661424", noContextPatterns));
		
		List<Pattern> noPatterns = PatternMatchingUtils.getPatternList(null, null);
		assertFalse(PatternMatchingUtils.matches("", noPatterns));

		List<Pattern> emptyPatterns = PatternMatchingUtils.getPatternList("", "");
		assertFalse(PatternMatchingUtils.matches("", emptyPatterns));
		
		List<Pattern> userAgentPatterns = PatternMatchingUtils.getPatternList(".*Googlebot.*");
		assertTrue(PatternMatchingUtils.matches("Googlebot/2.1 (+http://www.google.com/bot.html)", userAgentPatterns));

		try {
			List<Pattern> badPatterns = PatternMatchingUtils.getPatternList("", "\\k");
			PatternMatchingUtils.matches("", badPatterns);
			fail("Bad pattern exception not thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
}
