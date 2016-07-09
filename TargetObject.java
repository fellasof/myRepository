package com.lchclearnet.cds.common.utils;

public class TargetObject {

	// Data type
	public static final String TYPE_STRING = "String";
	public static final String TYPE_INTEGER = "Integer";
	public static final String TYPE_LONG = "Long";
	public static final String TYPE_DATE_DTCC = "Date";
	public static final String TYPE_DATE_NLS = "DateNls";
	public static final String TYPE_BIGDECIMAL = "BigDecimal";
	public static final String TYPE_TIMESTAMP = "TimeStamp";
	public static final String TYPE_DATE_DBUS = "DateDBus";

	private String type = null;
	private String value = null;

	public TargetObject(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
