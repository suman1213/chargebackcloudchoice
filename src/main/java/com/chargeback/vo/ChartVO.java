package com.chargeback.vo;

import java.util.List;

public class ChartVO {

	final public List<String> label;
	
	final public List<String> data;

	public ChartVO(final List<String> label, final List<String> data) {
		this.label = label;
		this.data = data;
	}
}
