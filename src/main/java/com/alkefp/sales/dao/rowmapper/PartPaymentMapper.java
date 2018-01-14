package com.alkefp.sales.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.alkefp.sales.beans.PartPayment;


public class PartPaymentMapper implements RowMapper<PartPayment> {

	@Override
	public PartPayment mapRow(ResultSet rs, int arg1) throws SQLException {
	
		PartPayment partPayment = new PartPayment();
		partPayment.setComments(rs.getString("Comments"));
		partPayment.setPartPayDate(rs.getDate("payDate"));
		partPayment.setPartPayment(rs.getDouble("partPymt"));
		partPayment.setInvoiceId(rs.getString("invoiceId"));
		partPayment.setFyYearId(rs.getInt("fyYear"));
		return partPayment;
	}

	

}
