package com.alkefp.sales.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.Pair;
import com.alkefp.sales.beans.PartPayment;
import com.alkefp.sales.beans.SaleSummary;
import com.alkefp.sales.beans.User;
import com.alkefp.sales.dao.rowmapper.FYRowMapper;
import com.alkefp.sales.dao.rowmapper.PartPaymentMapper;
import com.alkefp.sales.dao.rowmapper.SaleSummaryRowMapper;
import com.alkefp.sales.vo.MonthlySales;

@Component
public class SummaryDao {

	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private NamedParameterJdbcTemplate namedJdbcTemplate;
	
	private Map<String,User> userMap;
	
	public Map<String, User> getUserMap() {
		return userMap;
	}


	@PostConstruct
	public void getUsers() {
		
		String  sqlString = "select username,first_name,last_name,groupId, dob from user_profile";
		userMap = jdbcTemplate.query(sqlString, new ResultSetExtractor<Map<String,User>>(){

			@Override
			public Map<String,User> extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				
				Map<String,User> userMap = new HashMap<>();
				
				while(rs.next()) {
					User user = new User();
					user.setFirstName(rs.getString("first_name"));
					user.setLastName(rs.getString("last_name"));
					user.setGroupId(rs.getString("groupId"));
					user.setDob(rs.getDate("dob"));
					String userId = rs.getString("username");
					userMap.put(userId, user);
				}
				return userMap;
			}});
	}
	
	public List<SaleSummary> getSummaryView(List<Integer> fyyears,String groupId,List<Integer> clientId) {
		
		List<SaleSummary> saleSummary = new ArrayList<>();
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("fyyears", fyyears);	
		parameters.addValue("groupId", groupId);	
		
		String sqlQuery = "select k.clientId,k.fyYearDesc,k.invoiceId,k.clientName,k.fyYear,k.totalAmt,k.invoiceDate,k.Details, k.payrecoveryDays,p.partPay,k.groupId from ( select f.fyYearDesc,s.invoiceId,c.clientId,c.clientName,s.fyYear,s.totalAmt,s.invoiceDate,s.Details, s.payrecoveryDays,s.groupId from sales s join client c on s.clientId= c.clientId and s.groupId=c.groupId join fy f on f.fyYear = s.fyYear ) k left join ( select fyyear,InvoiceId,groupId,sum(partPymt) as partPay from partPayment f group by fyyear,InvoiceId,groupId ) p on k.fyYear = p.fyYear and p.InvoiceId =k.invoiceId and k.groupId=p.groupId where k.fyyear in(:fyyears) and k.groupId =(:groupId)";
		
		if(clientId!=null && clientId.size()>0) {
			parameters.addValue("clientId", clientId);
			sqlQuery = sqlQuery +" and k.clientId in (:clientId)";
		}
		saleSummary = namedJdbcTemplate.query(sqlQuery, parameters,new SaleSummaryRowMapper());
		return saleSummary;
	}
	
	
	public List<Pair<Integer,String>> getFYYear() {
		
		List<Pair<Integer,String>> fyYears = jdbcTemplate.query("select fyYear,fyYearDesc from fy", new FYRowMapper());
		return fyYears;
	}
	
	
    public int removeInvoice(SaleSummary saleSummary, String groupId) {
    	
    	removeInvoiceItems(saleSummary.getInvoiceId(),saleSummary.getFyYearId(),groupId);
    	removeInvoiceHeaders(saleSummary.getInvoiceId(),saleSummary.getFyYearId(),groupId);
    	removePartPayment(saleSummary.getInvoiceId(),saleSummary.getFyYearId(),groupId);
    	return removeInvoice(saleSummary.getInvoiceId(),saleSummary.getFyYearId(),groupId);
    }
	
	public int removePartPayment(String invoiceId,int fyYear,String groupId) {
		return jdbcTemplate.update("delete from partPayment where invoiceId =? and fyYear =? and groupId =?", 
				new Object[]{invoiceId,fyYear,groupId});
	}
	
	public int removeInvoice(String invoiceId,int fyYear,String groupId) {
		return jdbcTemplate.update("delete from sales where invoiceId =? and fyYear =? and groupId =?", 
				new Object[]{invoiceId,fyYear,groupId});
	}
	
	public int removeInvoiceItems(String invoiceId,int fyYear,String groupId) {
		return jdbcTemplate.update("delete from invoice_item where invoiceId =? and fyYear =? and groupId =?", 
				new Object[]{invoiceId,fyYear,groupId});
	}
	
	public int removeInvoiceHeaders(String invoiceId,int fyYear,String groupId) {
		return jdbcTemplate.update("delete from invoice_header where invoiceId =? and fyYear =? and groupId =?", 
				new Object[]{invoiceId,fyYear,groupId});
	}
	
	
	public Map<String,Object> getOverview(int fyYear,String groupId) {
		
		
		
		
		Map<String,Object> resultSet =  jdbcTemplate.query("select sumBill,sumPartPymt,s.fyYear,s.invoiceCount,s.groupId from" +
				" ((select fyYear,groupId,sum(totalAmt) as sumBill, count(invoiceId) as invoiceCount from sales  group by fyYear,groupId ) s" +
				" left outer join (select fyYear,groupId,sum(partPymt) as sumPartPymt from partPayment p group by fyYear,groupId) p " +
				"on s.fyYear =p.fyYear and s.groupId=p.groupId) where s.fyYear= ? and s.groupId =?", new Object[]{fyYear,groupId}, new ResultSetExtractor<Map<String,Object>>(){

					@Override
					public Map<String, Object> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
					
						Map<String, Object> output = new HashMap<String,Object>();
						if(rs.next()) {
							output.put("sumBill", rs.getDouble("sumBill"));
							output.put("sumPartPymt", rs.getDouble("sumPartPymt"));
							output.put("invoiceCount", rs.getInt("invoiceCount"));
						}
						else {
							output.put("sumBill", 0);
							output.put("sumPartPymt", 0);
							output.put("invoiceCount", 0);
						}
						return output;
					}
			
		});
	    return resultSet;
	}
	
public Map<String,Object> getOverviewByClient(int fyYear,String groupId, List<Integer> clientIds) {
	
	
	MapSqlParameterSource parameters = new MapSqlParameterSource();
	parameters.addValue("fyyear", fyYear);	
	parameters.addValue("groupId", groupId);
	String sqlQuery = "select sumBill,sumPartPymt,s.fyYear,s.invoiceCount,s.groupId from" +
			" ((select clientId,fyYear,groupId,sum(totalAmt) as sumBill, count(invoiceId) as invoiceCount from sales  group by fyYear,groupId,clientId ) s" +
			" left outer join (select fyYear,groupId,sum(partPymt) as sumPartPymt from partPayment p where invoiceId in (select invoiceId from sales where clientId in (:clientId) and groupId =(:groupId) and fyYear = (:fyyear))group by fyYear,groupId) p " +
			"on s.fyYear =p.fyYear and s.groupId=p.groupId) where s.fyYear= (:fyyear) and s.groupId = (:groupId)";
	
	if(clientIds!=null && clientIds.size()>0) {
		parameters.addValue("clientId", clientIds);
		sqlQuery = sqlQuery +" and s.clientId in (:clientId)";
	}
	
	Map<String,Object> resultSet =  namedJdbcTemplate.query(sqlQuery, parameters, new ResultSetExtractor<Map<String,Object>>(){

					@Override
					public Map<String, Object> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
					
						Map<String, Object> output = new HashMap<String,Object>();
						if(rs.next()) {
							output.put("sumBill", rs.getDouble("sumBill"));
							output.put("sumPartPymt", rs.getDouble("sumPartPymt"));
							output.put("invoiceCount", rs.getInt("invoiceCount"));
						}
						else {
							output.put("sumBill", 0);
							output.put("sumPartPymt", 0);
							output.put("invoiceCount", 0);
						}
						return output;
					}
			
		});
	    return resultSet;
	}
	
	public List<PartPayment> getPartPayment(String invoiceId, int fyYear,String groupId) {
		
		List<PartPayment> partPayments = jdbcTemplate.query("select inVoiceId,fyYear,partPymt,payDate,Comments from partPayment where inVoiceId= ? and fyYear = ? and groupId= ?", new PartPaymentMapper(),new Object[]{invoiceId,fyYear,groupId});
		return partPayments;
	}
	
	
	public List<Pair<Integer,String>> getClients(String groupId) {
		
		List<Pair<Integer,String>> clients = jdbcTemplate.query("select clientId,clientName from client where groupId=?",new Object[]{groupId},new RowMapper<Pair<Integer,String>>(){

			@Override
			public Pair<Integer,String> mapRow(ResultSet rs, int arg1) throws SQLException {
				
				Pair<Integer,String> client = new Pair<>();
				client.setKey(rs.getInt("clientId"));
				client.setValue(rs.getString("clientName"));
				
				return client;
			}});
		return clients;
	}
	
	public void insertInvoice(SaleSummary saleSumamry,String groupId){
		
		
		String sql = "INSERT INTO sales " +
				"(invoiceId, fyYear, clientId,totalAmt,payrecoveryDays,invoiceDate,Details,groupId) VALUES (?, ?,?,?,?,?,?,?)";
		jdbcTemplate.update(sql, new Object[]{saleSumamry.getInvoiceId(),saleSumamry.getFyYearId(),saleSumamry.getClientId(),
				saleSumamry.getTotalAmt(),saleSumamry.getPayRecoveryDays(),
				new java.sql.Date(saleSumamry.getInvoiceDate().getTime()),saleSumamry.getDetails(),groupId});
	}
	
    public void updateInvoice(SaleSummary saleSumamry, String groupId){
		
		System.out.println(saleSumamry.getInvoiceId()+""+saleSumamry.getFyYearId());
		String sql = "update sales " +
				" set  clientId =? ,totalAmt =?,payrecoveryDays =?,invoiceDate =?,Details =? where invoiceId=? and fyYear=? and groupId=?";
		int val = jdbcTemplate.update(sql, new Object[]{saleSumamry.getClientId(),
				saleSumamry.getTotalAmt(),saleSumamry.getPayRecoveryDays(),
				new java.sql.Date(saleSumamry.getInvoiceDate().getTime()),saleSumamry.getDetails(),saleSumamry.getInvoiceId(),saleSumamry.getFyYearId(),groupId});
		System.out.println(val);
	}
    
    
	
	public void insertPartPayment(final List<PartPayment> partPayments,final String groupId){

		  String sql = "INSERT INTO partPayment " +
			"(invoiceId, fyYear, partPymt,payDate,Comments,groupId) VALUES (?, ?, ?,?,?,?)";

		  int [] vales= jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				PartPayment partPayment = partPayments.get(i);
				ps.setString(1, partPayment.getInvoiceId());
				ps.setInt(2, partPayment.getFyYearId());
				ps.setDouble(3, partPayment.getPartPayment() );
				ps.setDate(4, new java.sql.Date(partPayment.getPartPayDate().getTime()));
				ps.setString(5, partPayment.getComments() );
				ps.setString(6, groupId);
				
			}

			@Override
			public int getBatchSize() {
				return partPayments.size();
			}
		  });
		
		  for(int i=0;i<vales.length;i++) {
			 System.out.println(i);
		 }
		}
	
	public List<MonthlySales>  getMonthlySales(Date startDate, Date endDate, int fyYear) {
		
		List<MonthlySales> sales = null;
		
		String startDateStr = this.getFormattedDate(startDate);
		String endDateStr = this.getFormattedDate(endDate);
		
		if(startDateStr !=null && endDateStr!=null ) {
	    
			String monthlySalesSQL = "select distinct s.invoiceId,c.clientName,c.address,c.gstNo,totalAmt,billAmt,invoiceDate,cgst,sgst,igst from "+
		"(select invoiceId,fyYear,clientId,totalAmt,billAmt,invoiceDate from sales where  (invoiceDate BETWEEN ? AND ? )) s "+
		 "left outer join invoice_item i on s.invoiceId = i.invoiceId and s.fyYear = i.fyYear join "+ 
		 "client c on c.clientId = s.clientId where i.fyYear = ? order by invoiceDate, invoiceId";
	
	      sales = jdbcTemplate.query(monthlySalesSQL, new Object[]{startDateStr,endDateStr,fyYear}, new RowMapper<MonthlySales>(){

			@Override
			public MonthlySales mapRow(ResultSet rs, int arg1)
					throws SQLException {
				
				MonthlySales sale = new MonthlySales();
				sale.setInvoiceId(rs.getString("invoiceId"));
				sale.setClientName(rs.getString("clientName"));
				sale.setGstNo(rs.getString("gstNo"));
				sale.setInvoiceDate(rs.getDate("invoiceDate"));
				sale.setAddress(rs.getString("address"));
				sale.setTotalBill(rs.getDouble("totalAmt"));
				sale.setBillAmount(rs.getDouble("billAmt"));
				sale.setCgst(rs.getDouble("cgst"));
				sale.setIgst(rs.getDouble("igst"));
				sale.setSgst(rs.getDouble("sgst"));
				return sale;
			}
	    	  
	      });
		}
	    return sales;
	}
	
	
	public Map<String,Long>  getMonthlyTaxes(Date startDate, Date endDate, int fyYear, String groupId) {
		
		String startDateStr = this.getFormattedDate(startDate);
		String endDateStr = this.getFormattedDate(endDate);
		
		String sql = "select sum(rate * qty * sgst/100) as sgst,sum(rate * qty *igst/100) as igst," +
				"sum(rate * qty *cgst/100) as cgst from invoice_item  where invoiceId in" +
				" (select invoiceId from sales where fyYear=? and groupId=? and invoiceDate between ? and ?) and fyYear=? and groupId=?";
		
		return jdbcTemplate.query(sql, new Object[]{fyYear,groupId,startDateStr,endDateStr,fyYear,groupId}, new ResultSetExtractor<Map<String,Long>>(){

			@Override
			public Map<String, Long> extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				
						Map<String, Long> map = new HashMap<String, Long>();
						if (rs.next()) {

							Double sgst = rs.getDouble("sgst");
							Double igst = rs.getDouble("igst");
							Double cgst = rs.getDouble("cgst");
							map.put("sgst", Math.round(sgst));
							map.put("igst", Math.round(igst));
							map.put("cgst", Math.round(cgst));
							Long total = Math.round(sgst) + Math.round(igst) + Math.round(cgst);
							map.put("total", total);
						}
						return map;
			}
			
		});
        
				
	}
	
	private String getFormattedDate(Date date) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		if (date!=null) {
		
			return sdf.format(date);
		}
		return null;
	}
	
}
