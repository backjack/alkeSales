package com.alkefp.sales.beans;

import java.util.List;

public class Tax {

	private String taxName ="Tax";
	public String getTaxName() {
		return taxName;
	}
	public void setTaxName(String taxName) {
		this.taxName = taxName;
	}
	public double getTaxPercentage() {
		return taxPercentage;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(taxPercentage);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tax other = (Tax) obj;
		if (Double.doubleToLongBits(taxPercentage) != Double
				.doubleToLongBits(other.taxPercentage))
			return false;
		return true;
	}
	public void setTaxPercentage(double taxPercentage) {
		this.taxPercentage = taxPercentage;
	}
	public double getTaxTotal() {
		return taxTotal;
	}
	public void setTaxTotal(double taxTotal) {
		this.taxTotal = taxTotal;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	private double taxPercentage;
	private double taxTotal;
	private List<Item> items;

}
