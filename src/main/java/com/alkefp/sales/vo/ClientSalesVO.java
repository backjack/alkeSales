package com.alkefp.sales.vo;

import java.util.List;

public class ClientSalesVO {

	private List<String> clientName;
	
	private List<ClientSalesDataSetVO> dataSets;

	public List<ClientSalesDataSetVO> getDataSets() {
		return dataSets;
	}

	public void setDataSets(List<ClientSalesDataSetVO> dataSets) {
		this.dataSets = dataSets;
	}

	public List<String> getClientName() {
		return clientName;
	}

	public void setClientName(List<String> clientName) {
		this.clientName = clientName;
	}

}
