package com.alkefp.sales.beans;

import java.util.Date;

public class SaleSummary {

	private String invoiceId;
	private String fyYear;
	private Date invoiceDate;
	private String clientName;
	private Double totalAmt;
	private Double payedAmt;
	private Integer fyYearId;
	private boolean payReceived;
	private int payRecoveryDays;
	private String details;
	private Date payDateFinal;
	private String ragStatus;
	private Integer clientId;
	
	public Date getPayDateFinal() {
		return payDateFinal;
	}
	public void setPayDateFinal(Date payDateFinal) {
		this.payDateFinal = payDateFinal;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public int getPayRecoveryDays() {
		return payRecoveryDays;
	}
	public void setPayRecoveryDays(int payRecoveryDays) {
		this.payRecoveryDays = payRecoveryDays;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public Double getTotalAmt() {
		return totalAmt;
	}
	public void setTotalAmt(Double totalAmt) {
		this.totalAmt = totalAmt;
	}

	public boolean isPayReceived() {
		return payReceived;
	}
	public void setPayReceived(boolean payReceived) {
		this.payReceived = payReceived;
	}
	public Double getPayedAmt() {
		return payedAmt;
	}
	public void setPayedAmt(Double payedAmt) {
		this.payedAmt = payedAmt;
	}
	public String getRagStatus() {
		return ragStatus;
	}
	public void setRagStatus(String ragStatus) {
		this.ragStatus = ragStatus;
	}
	public String getFyYear() {
		return fyYear;
	}
	public void setFyYear(String fyYear) {
		this.fyYear = fyYear;
	}
	public Integer getFyYearId() {
		return fyYearId;
	}
	public void setFyYearId(Integer fyYearId) {
		this.fyYearId = fyYearId;
	}
	public Integer getClientId() {
		return clientId;
	}
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}
}
