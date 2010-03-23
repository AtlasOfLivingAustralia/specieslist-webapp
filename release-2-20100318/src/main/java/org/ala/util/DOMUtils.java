package org.ala.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DOMUtils {

	public static String domToString(Document domToConvert) throws Exception {
		try {

			Source source = new DOMSource(domToConvert);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);

			return stringWriter.getBuffer().toString();

		} catch (Exception convertErr) {
			throw new Exception("Failed to convert DOM to String.", convertErr);
		}
	} // End of `domToString` method.

	public static Document stringToDom(String stringToConvert) throws Exception {

		// Turns String content of `speciesDetailPageXml` into XML DOM.
		// Uses the original encoding that was used to create the
		// `speciesDetailPageXml` variable.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document tempDomDoc = builder.parse(new InputSource(
					new StringReader(stringToConvert)));

			return tempDomDoc;

		} catch (Exception convertErr) {
			throw new Exception("Failed to convert String to DOM", convertErr);
		}

	} // End of `stringToDom` method.

} // End of class.
