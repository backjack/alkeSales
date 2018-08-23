package com.alkefp.sales.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.graph.ClientSales;
import com.alkefp.sales.vo.SalesByMonthDataSetVO;
import com.google.common.collect.Lists;


@Component
public class GraphDao {

	@Autowired
	public JdbcTemplate jdbcTemplate;
	
	
	/*select s.fyyear,clientId, sum(totalAmt),sum(partPymt) from partPayment p 
	right join sales s on s.invoiceId=p.invoiceId and s.fyyear = p.fyyear group by clientId,fyyear*/
	
    public List<ClientSales> getClientSales(int fyYear,String groupId) {
    	
    	
    	String sqlQuery = "select shortName,totalAmt,partPayment,a.fyYear,a.clientId,a.groupId from(select fyYear, groupId, clientId,sum(totalAmt) as totalAmt from sales where fyYear=? and groupId = ? group by fyYear, groupId, clientId) a left join (select p.fyYear, p.groupId, clientId,sum(partPymt) as partPayment from partPayment p join sales s on p.invoiceId = s.invoiceId and p.fyYear= s.fyYear and p.groupId = s.groupId where p.fyYear=? and p.groupId =? group by fyYear, groupId, clientId) b  on a.fyYear=b.fyYear and a.groupId=b.groupId and a.clientId = b.clientId join client cl on cl.clientid= b.clientId and cl.groupId= b.groupId";
    	
    	List<ClientSales> clientSales = jdbcTemplate.query(sqlQuery,new Object[]{fyYear,groupId,fyYear,groupId}, new RowMapper<ClientSales>(){

			@Override
			public ClientSales mapRow(ResultSet rs, int arg1)
					throws SQLException {
			
				ClientSales clientSales = new ClientSales();
				clientSales.setClientName(rs.getString("shortName"));
				
				double billAmt = rs.getDouble("totalAmt");
				double partPayment = rs.getDouble("partPayment");
				double remainingAmt = billAmt - partPayment;
				clientSales.setBillAmt(billAmt);
				clientSales.setPartPayAmt(partPayment);
				clientSales.setRemainingPayAmt(remainingAmt);
				return clientSales;
			}
    		
    	});
    	
    	return clientSales;
    	
    }
/*
    select  a.fmonth,a.fyYear,part, bill from(
    		(select month(payDate) as fmonth,fyYear,sum(partPymt) as part from partPayment p group by month(payDate),fyYear) 
    		a   join 
    		(select month(invoiceDate) as fmonth, fyYear,sum(totalAmt) as bill from sales p group by month(invoiceDate),fyYear) s 
    		on a.fmonth = s.fmonth and a.fyYear=s.fyYear) where a.fyYear =2014*/
    public   List<SalesByMonthDataSetVO> getSalesByMonth(int fyfyear,String groupId) {
    	
    	//String sqlQuery = "select  s.fmonth,s.fyYear,part, bill from((select month(payDate) as fmonth,fyYear,sum(partPymt) as part from partPayment p group by month(payDate),fyYear) a  right  join (select month(invoiceDate) as fmonth, fyYear,sum(totalAmt) as bill from sales p group by month(invoiceDate),fyYear) s on a.fmonth = s.fmonth and a.fyYear=s.fyYear) where s.fyYear =?";
    	String sqlQuery ="(select month(payDate) as fmonth,fyYear,sum(partPymt) as amt,'part' as type from partPayment p where fyYear =? and groupId= ? group by fyYear,groupId,month(payDate) ) union all(select month(invoiceDate) as fmonth, fyYear,sum(totalAmt) as amt,'sales' as type from sales p  where fyYear =? and groupId= ? group by fyYear,groupId,month(invoiceDate))";
    	List<SalesByMonthDataSetVO> salesByMonth = jdbcTemplate.query(sqlQuery,new Object[]{fyfyear,groupId,fyfyear,groupId}, new ResultSetExtractor<List<SalesByMonthDataSetVO>>(){

			@Override
			public  List<SalesByMonthDataSetVO> extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				
				List<SalesByMonthDataSetVO> salesBymonth = new LinkedList<>();
				SalesByMonthDataSetVO salesAmt = new SalesByMonthDataSetVO();
				salesAmt.setLabel("sales");
				List<Double> salesData = new LinkedList<>();
				salesAmt.setData(salesData);
				SalesByMonthDataSetVO paymentAmt = new SalesByMonthDataSetVO();
				paymentAmt.setLabel("payment");
				List<Double> payData = Lists.newArrayListWithCapacity(12);

				for(int i=0;i<12;i++) {
					payData.add(0d);
					salesData.add(0d);
				}
				paymentAmt.setData(payData);
				while(rs.next()) {
				
					int month = rs.getInt("fmonth");
					if(month <=3) {
						month = 12 - (3-month);
					}
					else {
						 month = month -3;
					}
					int index = month -1; 
					String type = rs.getString("type");
					if(!"part".equalsIgnoreCase(type)) {
					  salesData.set(index,rs.getDouble("amt"));
					}
					else {
					   payData.set(index,rs.getDouble("amt"));
					}
				}
				salesBymonth.add(salesAmt);
				salesBymonth.add(paymentAmt);
				
				return salesBymonth;
			}
		     
    	
    	});
    	 return salesByMonth;
    }
    
}
