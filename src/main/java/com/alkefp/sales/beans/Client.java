package com.alkefp.sales.beans;

public class Client {

	private int id;
	
	private String GSTno;
	
	public String getGSTno() {
		return GSTno;
	}
	public void setGSTno(String gSTno) {
		GSTno = gSTno;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
	public String getTelehoneNo() {
		return telehoneNo;
	}
	public void setTelehoneNo(String telehoneNo) {
		this.telehoneNo = telehoneNo;
	}
	public String getContactPerson() {
		return contactPerson;
	}
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	private String shortName;
	private String groupId;
	private String clientName;
	private String address;
	private String telehoneNo;
	private String contactPerson;
	private String emailAddress;
	private boolean active;

}
