package com.lchclearnet.cds.common.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alain.primard
 * 
 *         code existant non modifié issu de XMLUtils
 */
@Deprecated
public class XmlUtils {

	public static final String ENCODING_UTF_8 = "UTF-8";

	// private static final SimpleDateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

	private XmlUtils() {
		// to prevent instanciation
	}

	public static String getDateAsString(Date date) {
		DateTimeFormatter xmlDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTime dateTime = new DateTime(date);
		String s = null;
		if (date != null) {
			s = dateTime.toString(xmlDateFormatter);
		}
		return s;
	}

	public static Document transform(Document document, InputStream is, Map<String, Object> parameters) {
		JDOMResult jdomResult = new JDOMResult();
		StreamSource xslt = new StreamSource(is);
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(xslt);
			if (parameters != null) {
				LOGGER.debug("Apply XSLT with parameters:");
				for (final Entry<String, Object> parameter : parameters.entrySet()) {
					transformer.setParameter(parameter.getKey(), parameter.getValue());
					LOGGER.debug("\t" + parameter.getKey() + "=" + parameter.getValue());
				}
			}
			transformer.transform(new JDOMSource(document), jdomResult);
		} catch (Exception e) {
			LOGGER.error(e.toString());
		}
		return jdomResult.getDocument();
	}

	private static boolean scanEmptyElement(Element element, Collection<String> exceptions) {
		ConcurrentLinkedQueue<Element> children = new ConcurrentLinkedQueue<Element>(element.getChildren());
		for (Element child : children) {
			if (scanEmptyElement(child, exceptions)) {
				element.removeContent(child);
			}
		}
		boolean emptyValue = "".equals(element.getValue().trim());
		int attributesCount = element.getAttributes().size();
		int chidrenCount = element.getChildren().size();
		boolean protectedTag = exceptions.contains(element.getName());

		return emptyValue && attributesCount == 0 && chidrenCount == 0 && !protectedTag;
	}

	/**
	 * Delete empties tags
	 * 
	 * @param document
	 *            : JDOM document to trim
	 * @param exceptions
	 *            : These tags won't be cleaned
	 */
	public static void trim(Document document, String[] exceptions) {
		scanEmptyElement(document.getRootElement(), new ArrayList<String>(Arrays.asList(exceptions)));
	}

	/**
	 * Return a String representation of a org.jdom.Document
	 * 
	 * @param document
	 * @return
	 */
	public static String getDocumentAsString(Document document) {
		Format format = Format.getPrettyFormat();
		format.setEncoding("UTF-8");
		return new XMLOutputter(format).outputString(document);
	}

	public static XMLGregorianCalendar getTimestampWithoutFractionalSecond(Date date) {
		XMLGregorianCalendar xmlgc = null;
		if (date != null) {
			// GregorianCalendar gCalendar = new GregorianCalendar(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
			TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
			Locale locale = Locale.getDefault();
			GregorianCalendar gCalendar = new GregorianCalendar(timeZone, locale);
			gCalendar.setTime(date);
			try {
				xmlgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
				xmlgc.setFractionalSecond(null);
			} catch (DatatypeConfigurationException e) {
				LOGGER.error("Error  : " + e.getMessage());
			}
		}
		return xmlgc;
	}

	public static void save(Document document, String encoding, OutputStream os, boolean prettyFormat) {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		try {
			if (prettyFormat) {
				new XMLOutputter(Format.getPrettyFormat().setEncoding(encoding)).output(document, osw);
			} else {
				new XMLOutputter(Format.getRawFormat().setEncoding(encoding)).output(document, osw);
			}
		} catch (IOException e) {
			LOGGER.error("Error  : " + e.getMessage());
		} finally {
			// try to close streams
			try {
				osw.close();
			} catch (IOException e) {
				LOGGER.error("Error  : " + e.getMessage());
			}
			try {
				os.close();
			} catch (IOException e) {
				LOGGER.error("Error  : " + e.getMessage());
			}
		}
	}

	public static String trimNamespace(String xmlContent) {
		xmlContent = xmlContent.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("<(\\s*[\\w:]+)?\\s+xmlns.*?>", "<$1>");
		xmlContent = xmlContent.replaceAll("<(\\s*[\\w:]+)?\\s+version.*?>", "<$1>");
		xmlContent = xmlContent.replaceAll("<\\s*\\w+?:", "<").replaceAll("</\\s*\\w+?:", "</");
		xmlContent = xmlContent.replaceAll("<(\\w+)\\s\\w+?:\\w+\\s*=\\s*\"\\w+\"", "<$1");
		return xmlContent.trim();
	}

}
