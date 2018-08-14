package com.alkefp.sales.vo;

import java.util.Date;

public class MonthlySales {

	@Override
	public String toString() {
		return "MonthlySales [invoiceId=" + invoiceId + ", clientName="
				+ clientName + ", address=" + address + ", gstNo=" + gstNo
				+ ", totalBill=" + totalBill + ", billAmount=" + billAmount
				+ ", invoiceDate=" + invoiceDate + ", cgst=" + cgst + ", sgst="
				+ sgst + ", igst=" + igst + "]";
	}

	private String invoiceId;
	
	private String clientName;
	
	private String address;
	
	private String gstNo;
	
	private Double totalBill;
	
	private Double billAmount;
	
	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGstNo() {
		return gstNo;
	}

	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}

	public Double getTotalBill() {
		return totalBill;
	}

	public void setTotalBill(Double totalBill) {
		this.totalBill = totalBill;
	}

	public Double getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(Double billAmount) {
		this.billAmount = billAmount;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public Double getCgst() {
		return cgst;
	}

	public void setCgst(Double cgst) {
		this.cgst = cgst;
	}

	public Double getSgst() {
		return sgst;
	}

	public void setSgst(Double sgst) {
		this.sgst = sgst;
	}

	public Double getIgst() {
		return igst;
	}

	public void setIgst(Double igst) {
		this.igst = igst;
	}

	private Date invoiceDate;
	
	private Double cgst;
	
	private Double  sgst;
	
	private Double igst;

}
