package com.alkefp.sales.beans;

import java.util.List;

public class SaleSummaryInput {

	public List<Integer> years;
	public List<Integer> clientids;
	public List<Integer> getYears() {
		return years;
	}
	public void setYears(List<Integer> years) {
		this.years = years;
	}
	public List<Integer> getClientids() {
		return clientids;
	}
	public void setClientids(List<Integer> clientids) {
		this.clientids = clientids;
	}

}
