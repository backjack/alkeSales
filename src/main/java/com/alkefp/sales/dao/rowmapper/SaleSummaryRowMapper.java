package com.alkefp.sales.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.alkefp.sales.beans.SaleSummary;

public class SaleSummaryRowMapper implements RowMapper<SaleSummary> {

	    @Override
		public SaleSummary mapRow(ResultSet rs, int arg1) throws SQLException {
			
			SaleSummary saleSummary = new SaleSummary();
			saleSummary.setInvoiceId(rs.getString("invoiceId"));
			saleSummary.setClientName(rs.getString("clientName"));
			saleSummary.setTotalAmt(rs.getDouble("totalAmt"));
			saleSummary.setPayedAmt(rs.getDouble("partPay"));
			saleSummary.setFyYear(rs.getString("fyYearDesc"));
			saleSummary.setInvoiceDate(rs.getDate("invoiceDate"));
			saleSummary.setFyYearId(rs.getInt("fyYear"));
			saleSummary.setClientId(rs.getInt("clientId"));
			saleSummary.setDetails(rs.getString("Details"));
			double totalAmount = saleSummary.getTotalAmt();
			double payedAmount = saleSummary.getPayedAmt();
			if(payedAmount>=totalAmount) {
				saleSummary.setPayReceived(true);
			} else {
				saleSummary.setPayReceived(false);
			}
			saleSummary.setPayRecoveryDays(rs.getInt("payrecoveryDays"));
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE,rs.getInt("payrecoveryDays"));
			saleSummary.setPayDateFinal(cal.getTime());
			
			if(saleSummary.isPayReceived()) {
				saleSummary.setRagStatus("#00ff00");
			} else if(saleSummary.getPayDateFinal().after(new Date())) {
				saleSummary.setRagStatus("#ff944d");
			} else {
				saleSummary.setRagStatus("#ff4d4d");
			}
			return saleSummary;
		}

}
