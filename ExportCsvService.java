package com.lchclearnet.cds.web.utils.export;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import com.lchclearnet.tcm.dto.TcmGuiDto;

public interface ExportCsvService {

	void exportCsv(List<String> listPreference, Collection<?> searchCSsResults, Collection<?> filterCSsResults, Map<String, String> mapNameHeader, FacesContext facesContext);

	void parseXSLT(String text, InputStream templateFile, String filename, ExternalContext response) throws Exception;

	Transformer getTransformer(StreamSource streamSource);
}
