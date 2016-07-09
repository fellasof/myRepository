package com.lchclearnet.cds.common.utils;

public enum MsgOutType {

	CLEARED_TRADE_NOVATED(1), TRADE_EXIT(2), CLEARED_TRADE_TERMINATED(3), CLEARED_TRADE_CANCELLED(4), TRADE_NOVATION(5);

	private final int value;

	private MsgOutType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static String getMessageName(int msgType) {
		String value;
		if (msgType == 1) {
			value = "Novation";
		} else if (msgType == 2) {
			value = "Exit";
		} else if (msgType == 3) {
			value = "Termination";
		} else if (msgType == 4) {
			value = "Cancellation";
		} else {
			value = "Valuation";
		}
		return value;
	}

}
