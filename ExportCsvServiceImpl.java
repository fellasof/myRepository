package com.lchclearnet.cds.web.utils.export;

import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import com.lchclearnet.cds.web.utils.TcmGuiUtils;
import com.lchclearnet.tcm.utils.TcmDateUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@Service
public class ExportCsvServiceImpl implements ExportCsvService {

	/** Attribute for logger */
	private static final Logger logger = LoggerFactory.getLogger(TcmGuiUtils.class);

	@Override
	public void exportCsv(List<String> listPreference, Collection<?> searchCSsResults, Collection<?> filterCSsResults, Map<String, String> mapNameHeader, FacesContext facesContext) {

		String nameHeader = null;

		Collection<?> exportCSsResults;
		ReportData reportData = new ReportData();
		XStream xstream = new XStream(new DomDriver());
		List<Row> rowList = new ArrayList<Row>();
		List<Column> columnValuedList = null;
		List<ColumnHeader> headerList = new ArrayList<ColumnHeader>();
		List<ColumnHeader> columnList = new ArrayList<ColumnHeader>();
		Row row = null;
		InputStream template = null;
		String xml = "";

		exportCSsResults = filterCSsResults == null ? searchCSsResults : filterCSsResults;

		for (String preference : listPreference) {
			for (Entry<String, String> entry : mapNameHeader.entrySet()) {
				if (entry.getKey().equals(preference)) {
					nameHeader = entry.getValue();
					headerList.add(new ColumnHeader(nameHeader));
				}
			}
		}

		for (String columnsName : listPreference) {
			columnList.add(new ColumnHeader(columnsName));
		}
		Iterator<?> exportCSsResultsIterator =  exportCSsResults.iterator();
		while(exportCSsResultsIterator.hasNext()){
			row = new Row();
			columnValuedList = new ArrayList<Column>();
			Object exportCSsRslt = exportCSsResultsIterator.next();
			for (ColumnHeader columnName : columnList) {
				Object value = null;
				String valueString = null;
				try {
					value = PropertyUtils.getProperty(exportCSsRslt, columnName.getName());
					if (value instanceof Date) {

						valueString = TcmDateUtils.getStringFromDateByPattern((Date) value, TcmDateUtils.DATE_DATE_PDF);

					} else if (value instanceof BigDecimal) {
						DecimalFormat df = new DecimalFormat("##0.00");
						valueString = df.format(value);
						valueString = valueString.replace(",", ".");
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
				columnValuedList.add(column);
			}
			row.setColumns(columnValuedList);
			rowList.add(row);
		}
		xstream.alias("reportData", ReportData.class);
		xstream.alias("ColumnHeader", ColumnHeader.class);
		xstream.alias("Row", Row.class);
		xstream.alias("Column", Column.class);
		reportData.setColumnHeaders(headerList);
		reportData.setRows(rowList);
		xml = xstream.toXML(reportData);
		try {
			template = FileUtils.openInputStream(FileUtils.toFile(this.getClass().getResource("/templates/templateExportCSV.xsl")));
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		try {
			parseXSLT(xml, template, "cs", facesContext.getExternalContext());
			facesContext.responseComplete();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void parseXSLT(String text, InputStream templateFile, String filename, ExternalContext response) throws Exception {

		Source xslt = new StreamSource(templateFile);
		TransformerFactory fac = TransformerFactory.newInstance();
		Templates templates = fac.newTemplates(xslt);
		Transformer transformer = templates.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		CharArrayWriter result = new CharArrayWriter();
		SAXSource source = new SAXSource(new InputSource(new StringReader(text)));
		transformer.transform(source, new StreamResult(result));
		byte[] csvBytes = result.toString().getBytes();

		response.setResponseContentLength(csvBytes.length);
		response.setResponseContentType("text/csv");
		response.addResponseHeader("Content-Disposition", "attachment;filename=" + filename + ".csv");
		response.getResponseOutputStream().write(csvBytes);
		response.getResponseOutputStream().flush();

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
