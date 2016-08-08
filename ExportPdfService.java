package com.lchclearnet.cds.web.utils.export;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import com.lchclearnet.tcm.dto.TcmGuiDto;

public interface ExportPdfService {

	void parseFOP(InputStream templateFile, String dataFile, String filename, ExternalContext response) throws FileNotFoundException, MalformedURLException;

	Transformer getTransformer(StreamSource streamSource);

	void exportPdf(List<String> listPreference, Collection<?> exportCSsResults, Collection<?> filterCSsResults, Map<String, String> mapNameHeader, FacesContext facesContext);
}
