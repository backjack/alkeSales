package com.alkefp.sales.beans;

import java.util.Date;

public class PartPayment {

	private String invoiceId;
	
	private Date partPayDate;
	
	private String comments;
	
	private String fyYear;
	private Integer fyYearId;
	
	public Integer getFyYearId() {
		return fyYearId;
	}

	public void setFyYearId(Integer fyYearId) {
		this.fyYearId = fyYearId;
	}

	private Double partPayment;

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Date getPartPayDate() {
		return partPayDate;
	}

	public void setPartPayDate(Date partPayDate) {
		this.partPayDate = partPayDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFyYear() {
		return fyYear;
	}

	public void setFyYear(String fyYear) {
		this.fyYear = fyYear;
	}

	public Double getPartPayment() {
		return partPayment;
	}

	public void setPartPayment(Double partPayment) {
		this.partPayment = partPayment;
	}

}
