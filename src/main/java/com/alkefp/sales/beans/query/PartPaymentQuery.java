package com.alkefp.sales.beans.query;

public class PartPaymentQuery {

	private String invoiceId;
	private String fyYearDesc;
	private Integer fyYear;
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public String getFyYearDesc() {
		return fyYearDesc;
	}
	public void setFyYearDesc(String fyYearDesc) {
		this.fyYearDesc = fyYearDesc;
	}
	public Integer getFyYear() {
		return fyYear;
	}
	public void setFyYear(Integer fyYear) {
		this.fyYear = fyYear;
	}

}
