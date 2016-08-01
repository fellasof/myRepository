package com.lchclearnet.cds.report.common.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * XML Generator class marshaling either a Simple/JAXB object or a {@link List} of JAXB instances which are returned by {@link #getData(Object)} method.
 * <p>
 * Generator can validate upon a {@link Schema} object defined by {@link #getSchemasLocations()}.
 * 
 * @param <T>
 *            JAXB class or element type to be marshalled.
 * 
 * @author Hichem BOURADA
 * @version 1.1, 23/09/2013
 */
public abstract class AbstractXmlReportGenerator<T> extends AbstractReportGenerator implements ResourceLoaderAware {

	private JAXBContext jContext;

	private ResourceLoader resourceLoader;

	private Schema schema;

	@Override
	protected void initialize() throws Exception {
		super.initialize();
		String[] locations = getSchemasLocations();
		if (locations != null && locations.length > 0) {
			Source[] sources = new Source[locations.length];
			for (int i = 0; i < locations.length; i++) {
				Resource resource = getResourceLoader().getResource(locations[i]);
				sources[i] = new StreamSource(resource.getInputStream(), resource.getURL().toString());
			}
			this.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(sources);
		}
	}

	@Override
	public final void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 
	 * @return {@link ResourceLoader} to load resources.
	 */
	protected final ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * 
	 * @return resource path array where xsd files are located otherwise null.
	 */
	protected abstract String[] getSchemasLocations();

	/**
	 * 
	 * @return
	 * @throws JAXBException
	 */
	protected JAXBContext getJAXBContext() throws JAXBException {
		if (jContext == null) {
			// if this is a proxy
			Class<?> targetClass = AopUtils.getTargetClass(this);
			Class<?> jaxbClass = GenericTypeResolver.resolveTypeArgument(targetClass, AbstractXmlReportGenerator.class);
			if (jaxbClass == null) {
				throw new IllegalArgumentException("You must define generic parameter correctly in parent of " + targetClass);
			}
			jContext = JAXBContext.newInstance(jaxbClass, Wrapper.class);
		}
		return jContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doGenerate(Object params) throws IOException {
		try {
			Marshaller marshaller = getJAXBContext().createMarshaller();
			if (isFormatted()) {
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			}
			if (getSchema() != null) {
				marshaller.setSchema(getSchema());
			}
			Object data = getData(params);
			try (Writer writer = getWriter(params)) {
				if (data == null) {
					return;
				}
				Object jaxbElement;
				final JAXBIntrospector introspector = getJAXBContext().createJAXBIntrospector();
				if (data instanceof Iterable) {
					List<T> items = toList(data);
					if (items.isEmpty()) {
						return;
					}
					T first = items.get(0);
					QName qName = introspector.getElementName(first);
					// if instance class is not JAXB class
					if (qName == null) {
						// qName = new QName(defaultElementName(first).concat("s"));
						throw new IllegalArgumentException(first.getClass() + " is not a JAXB class (no @XmlRootElement)");
					}
					String localPart = qName.getLocalPart().concat("s");
					qName = new QName(qName.getNamespaceURI(), localPart);
					Wrapper<T> wrapper = new Wrapper<>(items);
					jaxbElement = new JAXBElement<>(qName, Wrapper.class, wrapper);
				} else if (!introspector.isElement(data)) {
					jaxbElement = new JAXBElement<>(new QName(defaultElementName(data)), (Class<T>) data.getClass(), (T) data);
				} else {
					jaxbElement = data;
				}
				marshaller.marshal(jaxbElement, writer);
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<T> toList(Object data) {
		if (data instanceof List) {
			return (List<T>) data;
		}
		List<T> res = new ArrayList<T>();
		CollectionUtils.addAll(res, ((Iterable<T>) data).iterator());
		return res;
	}

	private String defaultElementName(Object obj) {
		return StringUtils.uncapitalize(obj.getClass().getSimpleName());
	}

	@Override
	protected String getFileExtension(Object params) {
		return ".xml";
	}

	/**
	 * 
	 * @return true iff generated xml is formatted, default is true.
	 */
	protected boolean isFormatted() {
		return true;
	}

	/**
	 * 
	 * @return schema to validate generated XML
	 */
	protected Schema getSchema() {
		return schema;
	}

	// ---------------------------------------------------------

	public static class Wrapper<T> {

		private final List<T> items;

		public Wrapper() {
			items = new ArrayList<>();
		}

		public Wrapper(List<T> items) {
			this.items = items;
		}

		@XmlAnyElement(lax = true)
		public List<T> getItems() {
			return items;
		}
	}

}
