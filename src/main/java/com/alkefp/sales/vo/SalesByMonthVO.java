package com.alkefp.sales.vo;

import java.util.List;

public class SalesByMonthVO {

	private List<SalesByMonthDataSetVO> dataSets;
	
	private List<String> labels;

	public List<SalesByMonthDataSetVO> getDataSets() {
		return dataSets;
	}

	public void setDataSets(List<SalesByMonthDataSetVO> dataSets) {
		this.dataSets = dataSets;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

}
