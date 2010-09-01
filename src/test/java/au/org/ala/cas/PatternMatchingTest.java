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

		List<Pattern> noPatterns = PatternMatchingUtils.getPatternList(null, null);
		assertFalse(PatternMatchingUtils.matches("", noPatterns));

		List<Pattern> emptyPatterns = PatternMatchingUtils.getPatternList("", "");
		assertFalse(PatternMatchingUtils.matches("", emptyPatterns));
		
		try {
			List<Pattern> badPatterns = PatternMatchingUtils.getPatternList("", "\\k");
			PatternMatchingUtils.matches("", badPatterns);
			fail("Bad pattern exception not thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
}
