package com.lchclearnet.cds.web.utils.export;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lchclearnet.cds.web.utils.TcmGuiUtils;
import com.lchclearnet.tcm.utils.TcmDateUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@Service
public class ExportPdfServiceImpl implements ExportPdfService {

	/** Attribute for logger */
	private static final Logger logger = LoggerFactory.getLogger(TcmGuiUtils.class);

	@Override
	public void exportPdf(List<String> listPreference, Collection<?> searchCSsResults, Collection<?> filterCSsResults, Map<String, String> mapNameHeader, FacesContext facesContext) {

		String valueHeader = null;
		InputStream tmp = null;
		Collection<?> exportCSsResults;
		List<ColumnHeader> listEntet = new ArrayList<ColumnHeader>();
		List<ColumnHeader> columnList = new ArrayList<ColumnHeader>();

		XStream xstream = new XStream(new DomDriver());
		ReportData reportData = new ReportData();

		// Parcourir les clés et afficher les entrées de chaque clé;
		for (String preference : listPreference) {
			for (Map.Entry<String, String> entry : mapNameHeader.entrySet()) {
				String key = entry.getKey();
				if (key.equals(preference)) {
					valueHeader = entry.getValue();
					listEntet.add(new ColumnHeader(valueHeader));
				}
			}
		}

		for (String columnsName : listPreference) {
			columnList.add(new ColumnHeader(columnsName));
		}

		Row row = null;
		List<Column> values = null;
		List<Row> rowList = new ArrayList<Row>();

		exportCSsResults = filterCSsResults == null ? searchCSsResults : filterCSsResults;
		Iterator<?> exportCSsResultsIterator =  exportCSsResults.iterator();
		while(exportCSsResultsIterator.hasNext()){
			row = new Row();
			values = new ArrayList<Column>();
			Object exportCSsRst = exportCSsResultsIterator.next();
			for (ColumnHeader columnName : columnList) {
				Object value = null;
				String valueString = null;
				try {

					value = PropertyUtils.getProperty(exportCSsRst, columnName.getName());

					if (value instanceof Date) {

						valueString = TcmDateUtils.getStringFromDateByPattern((Date) value, TcmDateUtils.DATE_DATE_PDF);

					} else if (value instanceof BigDecimal) {

						DecimalFormat df = new DecimalFormat("#,##0.00");
						valueString = df.format(value);
						valueString = valueString.replace(",", ".");
						valueString = valueString.replaceAll(" ", ",");

					} else {
						valueString = (value == null) ? "" : value.toString();
					}

				} catch (IllegalAccessException e) {
					logger.error(e.getMessage());
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage());
				} catch (NoSuchMethodException e) {
					logger.error(e.getMessage());
				}

				Column column = new Column();
				column.setValue(valueString);
				values.add(column);

			}
			row.setColumns(values);
			rowList.add(row);

		}
		xstream.alias("reportData", ReportData.class);
		xstream.alias("ColumnHeader", ColumnHeader.class);
		xstream.alias("Row", Row.class);
		xstream.alias("Column", Column.class);

		reportData.setColumnHeaders(listEntet);

		reportData.setRows(rowList);

		String xml = xstream.toXML(reportData);

		try {
			tmp = FileUtils.openInputStream(FileUtils.toFile(this.getClass().getResource("/templates/templateExportPdf.xsl")));
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		try {
			parseFOP(tmp, xml, "CS", facesContext.getExternalContext());
			facesContext.responseComplete();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());

		}

	}

	@Override
	public void parseFOP(InputStream templateFile, String dataFile, String filename, ExternalContext response) throws FileNotFoundException, MalformedURLException {

		StreamSource source = new StreamSource(new StringReader(dataFile));
		// creation of transform source
		StreamSource transformSource = new StreamSource(templateFile);
		// create an instance of fop factory
		FopFactory fopFactory = FopFactory.newInstance();
		String basePath = URI.create(getClass().getClassLoader().getResource("templatePdf").getPath()).getPath();
		fopFactory.setBaseURL(basePath);
		// a user agent is needed for transformation
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.setBaseURL(fopFactory.getBaseURL());
		// to store output
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		Transformer xslfoTransformer;
		try {
			xslfoTransformer = getTransformer(transformSource);

			// Construct fop with desired output format
			Fop fop;
			try {
				fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, foUserAgent, outStream);
				Result res = new SAXResult(fop.getDefaultHandler());

				try {
					xslfoTransformer.transform(source, res);

					// to write the content to out put stream
					byte[] pdfBytes = outStream.toByteArray();
					response.setResponseContentLength(pdfBytes.length);
					response.setResponseContentType("application/pdf");
					response.addResponseHeader("Content-Disposition", "attachment;filename=" + filename + ".pdf");
					response.getResponseOutputStream().write(pdfBytes);
					response.getResponseOutputStream().flush();
				} catch (Exception e) {
					logger.error(e.getMessage());

				}
			} catch (FOPException e) {
				logger.error(e.getMessage());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public Transformer getTransformer(StreamSource streamSource) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			return transformerFactory.newTransformer(streamSource);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

}
