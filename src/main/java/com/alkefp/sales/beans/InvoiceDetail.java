package com.alkefp.sales.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InvoiceDetail {

	private Client client;
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	private List<Item> items;
	private String invoiceId;
	private int fyear;
	private String groupId;
	
	private Double billAmount;
	private Double taxAmount;
	private Double grandTotal;
	private Date invoiceDate;
	
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public Double getBillAmount() {
		return billAmount;
	}
	public void setBillAmount(Double billAmount) {
		this.billAmount = billAmount;
	}
	public Double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}
	public Double getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(Double grandTotal) {
		this.grandTotal = grandTotal;
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
	private String deliveryDoc;
	private Date  deliveryDate;
	private String despatchedVia;
	private String buyerDoc;
	private String destination;
	private Date  buyerDocDate;
	
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	public String getDeliveryDoc() {
		return deliveryDoc;
	}
	public void setDeliveryDoc(String deliveryDoc) {
		this.deliveryDoc = deliveryDoc;
	}
	public Date getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}
	public String getDespatchedVia() {
		return despatchedVia;
	}
	public void setDespatchedVia(String despatchedVia) {
		this.despatchedVia = despatchedVia;
	}
	public String getBuyerDoc() {
		return buyerDoc;
	}
	public void setBuyerDoc(String buyerDoc) {
		this.buyerDoc = buyerDoc;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Date getBuyerDocDate() {
		return buyerDocDate;
	}
	public void setBuyerDocDate(Date buyerDocDate) {
		this.buyerDocDate = buyerDocDate;
	}
	
	public String getFormattedDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		if(date!=null) {
			return sdf.format(date);
		} else {
		   return "";
		}
	}
}
