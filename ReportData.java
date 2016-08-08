package com.lchclearnet.cds.web.utils.export;

import java.util.List;

public class ReportData {

	private List<ColumnHeader> columnHeaders;

	private List<Row> rows;

	public List<ColumnHeader> getColumnHeaders() {
		return columnHeaders;
	}

	public void setColumnHeaders(List<ColumnHeader> columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

}
