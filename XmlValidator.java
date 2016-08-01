package com.lchclearnet.cds.common.xml;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lchclearnet.cds.common.utils.ResourcesHelper;

public class XmlValidator {

	private static final Logger LOG = LoggerFactory.getLogger(XmlValidator.class);

	private static final String XSD_PATH = "xsd/marketdata/";
	private static final String[] XSD_LIST = new String[] {
			// @formatter:off
			"clearml-message.xsd",
			"clearml-trade-message.xsd",
			"clearml-cashandcollateral-message.xsd",
			"clearml-position-message.xsd",
			"clearml-product-message.xsd",
			"clearml-marketdata-message.xsd",
			"clearml-riskmeasurement-message.xsd",
			//"clearml-marketdata-message_20130806.xsd"
			// @formatter:on
	};

	private static Source[] schemas = null;
	static {
		schemas = new Source[XSD_LIST.length];
		int index = 0;
		for (final String fileName : XSD_LIST) {
			final String systemId = XSD_PATH + fileName;
			final Source source = new StreamSource(new StringReader(ResourcesHelper.getResourceAsString(systemId)), systemId);
			schemas[index++] = source;
		}
	}

	private static Validator validator = null;

	private XmlValidator() {
	}

	private static void initValidator() throws Exception {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new ClearmlResourceResolver());
		schemaFactory.setErrorHandler(new XmlErrorHandler());

		Schema schema = schemaFactory.newSchema(schemas);

		validator = schema.newValidator();
		validator.setResourceResolver(new ClearmlResourceResolver());

		LOG.info("XML validator creation success.");
	}

	private static void validate(InputStream xmlInputStream) throws Exception {
		if (validator == null) {
			initValidator();
		}
		validator.validate(new StreamSource(xmlInputStream));
		LOG.info("XML file validated");
	}

	public static boolean isValid(InputStream xmlFile) {
		try {
			validate(xmlFile);
			return true;
		} catch (Exception e) {
			LOG.error("Exception in XML validation ", e);
			return false;
		}
	}

}
