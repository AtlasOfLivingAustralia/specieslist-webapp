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
package au.org.ala.cas.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

/**
 * Helper class that authenticates user credentials prior to invoking a web service that requires authentication.
 * Authentication is carried out during construction and the CAS generated Ticket Granting Ticket is saved for
 * subsequent web service invocations.
 * <p/>
 * Invoking a web service using the invoke() method involves obtaining a CAS service ticket which is then passed to
 * the web service provider as a URI parameter.  The web service provider application then obtains the user attributes
 * via its CAS validation filter.
 * 
 * @author peterflemming
 *
 */
public class WebServiceAuthenticationHelper {
	
	private final static Logger logger = Logger.getLogger(UriFilter.class);
	private final static String CAS_CONTEXT = "/cas/v1/tickets/";
	
	private final String casServer;
	private final String ticketGrantingTicket;
	
	/**
	 * Constructor that authenticates the user credentials and obtains a CAS Ticket Granting ticket.
	 * 
	 * @param casServer The CAS server URI
	 * @param userName	User name
	 * @param password  Password
	 */
	public WebServiceAuthenticationHelper(final String casServer, final String userName, final String password) {
		super();
		this.casServer = casServer;
		ticketGrantingTicket = getTicketGrantingTicket(casServer, userName, password);
	}

	/**
	 * Invokes a web service.  Firstly a CAS Service ticket is obtained and passed with the web service request.
	 * 
	 * @param serviceUrl Web service URI
	 * @return Web service response as a string
	 */
	public String invoke(final String serviceUrl) {
		String serviceTicket = getServiceTicket(this.casServer, this.ticketGrantingTicket, serviceUrl);
		if (serviceTicket != null) {
			return getServiceResponse(serviceUrl, serviceTicket);
		} else {
			return null;
		}
	}
	
	/**
	 * Authenticates user credentials with CAS server and obtains a Ticket Granting ticket.
	 * 
	 * @param server   CAS server URI
	 * @param username User name
	 * @param password Password
	 * @return The Ticket Granting Ticket id
	 */
	private String getTicketGrantingTicket(final String server, final String username, final String password) {
		final HttpClient client = new HttpClient();

		final PostMethod post = new PostMethod(server + CAS_CONTEXT);

		post.setRequestBody(new NameValuePair[] {
				new NameValuePair("username", username),
				new NameValuePair("password", password) });

		try {
			client.executeMethod(post);

			final String response = post.getResponseBodyAsString();

			switch (post.getStatusCode()) {
			case 201: {
				final Matcher matcher = Pattern.compile(
						".*action=\".*/(.*?)\".*").matcher(response);

				if (matcher.matches())
					return matcher.group(1);

				logger.warn("Successful ticket granting request, but no ticket found!");
				logger.info("Response (1k): " + getMaxString(response));
				break;
			}

			default:
				logger.warn("Invalid response code (" + post.getStatusCode() + ") from CAS server!");
				logger.info("Response (1k): " + getMaxString(response));
				break;
			}
		}

		catch (final IOException e) {
			logger.warn(e.getMessage(), e);
		}

		finally {
			post.releaseConnection();
		}

		return null;
	}

	/**
	 * Obtains a Service ticket for a web service invocation.
	 * 
	 * @param server CAS server URI
	 * @param ticketGrantingTicket TGT id
	 * @param service Web service URI
	 * @return Service ticket id
	 */
	private String getServiceTicket(final String server, final String ticketGrantingTicket, final String service) {
		if (ticketGrantingTicket == null)
			return null;

		final HttpClient client = new HttpClient();

		final PostMethod post = new PostMethod(server + CAS_CONTEXT + ticketGrantingTicket);

		post.setRequestBody(new NameValuePair[] { new NameValuePair("service", service) });

		try {
			client.executeMethod(post);

			final String response = post.getResponseBodyAsString();

			switch (post.getStatusCode()) {
			case 200:
				return response;

			default:
				logger.warn("Invalid response code (" + post.getStatusCode() + ") from CAS server!");
				logger.info("Response (1k): " + getMaxString(response));
				break;
			}
		}

		catch (final IOException e) {
			logger.warn(e.getMessage(), e);
		}

		finally {
			post.releaseConnection();
		}

		return null;
	}

	/**
	 * Invokes a web service request via an HTTP Get method.
	 * 
	 * @param url Web service URI
	 * @param serviceTicket CAS service ticket for web service
	 * @return Web service response as a string
	 */
	private String getServiceResponse(final String url, final String serviceTicket) {
		final HttpClient client = new HttpClient();

		final GetMethod get = new GetMethod(url + "?ticket=" + serviceTicket);

		try {
			client.executeMethod(get);

			final InputStream response = get.getResponseBodyAsStream();

			switch (get.getStatusCode()) {
			case 200:
				return getStringFromInputStream(response);

			default:
				logger.warn("Invalid response code (" + get.getStatusCode() + ") from web service!");
				logger.info("Response (1k): " +  getMaxString(getStringFromInputStream(response)));
				break;
			}
		}

		catch (final IOException e) {
			logger.warn(e.getMessage(), e);
		}

		finally {
			get.releaseConnection();
		}

		return null;
	}

	/**
	 * Builds a string from an InputStream.
	 * 
	 * @param is InputStream
	 * @return resulting string
	 * @throws IOException
	 */
	private String getStringFromInputStream(InputStream is) throws IOException {
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader( new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * Truncates a string to a maximum length.
	 * @param string Input string
	 * @return Truncated string
	 */
	private String getMaxString(String string) {
		return string.substring(0, Math.min(1024, string.length()));
	}

}
