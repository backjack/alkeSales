package com.alkefp.sales.beans;

public class Item {

	private long id;
	private String invoiceId;
	private int fyear;
	private String groupId;
	private Double amount;
	private Double totalAmount;
	private Double taxAmount;
	
	public Double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public int getFyear() {
		return fyear;
	}
	public void setFyear(int fyear) {
		this.fyear = fyear;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getTax() {
		return tax;
	}
	public void setTax(double tax) {
		this.tax = tax;
	}
	private String itemDescription;
	private double rate;
	private int quantity;
	private double tax;
	
	private Double sgst;
	private Double igst;
	private Double cgst;

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
	public Double getCgst() {
		return cgst;
	}
	public void setCgst(Double cgst) {
		this.cgst = cgst;
	}
	
	private String hnsCode;

	public String getHnsCode() {
		return hnsCode;
	}
	public void setHnsCode(String hnsCode) {
		this.hnsCode = hnsCode;
	}
	
	
}
