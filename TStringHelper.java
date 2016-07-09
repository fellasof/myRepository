package com.lchclearnet.cds.common.utils;

public class TStringHelper {

	private String result = null;
	private String classname = null;

	public TStringHelper(String className) {
		this.classname = className;
	}

	@SuppressWarnings("rawtypes")
	public TStringHelper(Class clazz) {
		this.classname = clazz.getName();
	}

	public TStringHelper add(String name, Object value) {
		if (result == null) {
			result = "[ " + classname + " ";
		} else {
			result += " | ";
		}
		if (value == null) {
			result += name + "=" + "null";
		} else {
			result += name + "=" + value.toString();
		}

		return this;
	}

	@Override
	public String toString() {
		return result;
	}
}