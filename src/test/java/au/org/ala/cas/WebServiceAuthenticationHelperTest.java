package au.org.ala.cas;

import au.org.ala.cas.client.WebServiceAuthenticationHelper;
import junit.framework.TestCase;

public class WebServiceAuthenticationHelperTest extends TestCase {

	public void testWebServiceInvocation() {
		WebServiceAuthenticationHelper wsw = new WebServiceAuthenticationHelper("https://alatstdb1-cbr.vm.csiro.au", "hit.user@ala.org.au", "hit-password");
		String response = wsw.invoke("http://collections.ala.org.au/co/co13.json");
		System.out.println(response);
	}
}
