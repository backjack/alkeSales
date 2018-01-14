package com.alkefp.sales.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.Client;
import com.alkefp.sales.beans.InvoiceDetail;
import com.alkefp.sales.beans.Item;
import com.google.common.collect.Lists;


@Component
public class InvoiceDetailDao {

	@Autowired
	public JdbcTemplate jdbcTemplate;
	
	
	
	public InvoiceDetail getFullInvoiceDetail(final String invoiceId, final int fyyear, final String groupId) {
	
		InvoiceDetail invoiceDetail = getInvoiceDetail(invoiceId, fyyear, groupId);
		
		
		if(invoiceDetail ==null) {
			invoiceDetail = new InvoiceDetail();
		}

		List<Item> items = getInvoiceItems(invoiceId, fyyear, groupId);
		invoiceDetail.setItems(items);
		enrichInvoiceDetail(invoiceDetail);
		return invoiceDetail;
	}
	
	
	 public void enrichInvoiceDetail(InvoiceDetail invoiceDetail) {
		 
		 List<Item> items = invoiceDetail.getItems();
		 double totalAmt =0;
		 double taxAmt =0;
		 double billAmt = 0;
		 for(Item item :items) {
			 
			 billAmt = billAmt + item.getAmount();
			 System.out.println("taxAmt "+item.getTaxAmount());
			 System.out.println("TotaltaxAmt "+taxAmt);
			 taxAmt = taxAmt + item.getTaxAmount();
			 totalAmt = totalAmt + item.getTotalAmount();
			 
		 }
		 
		 Double truncatedTaxAmout = BigDecimal.valueOf(taxAmt)
				    .setScale(1, RoundingMode.HALF_UP)
				    .doubleValue();
		 totalAmt = billAmt + truncatedTaxAmout;
		 Double truncatedTotalAmout = BigDecimal.valueOf(totalAmt)
				    .setScale(0, RoundingMode.HALF_UP)
				    .doubleValue();
		 invoiceDetail.setBillAmount(billAmt);
		 invoiceDetail.setTaxAmount(truncatedTaxAmout);
		 invoiceDetail.setGrandTotal(truncatedTotalAmout);
		 
	 }
	
	public InvoiceDetail getInvoiceDetail(final String invoiceId, final int fyyear, final String groupId) {
		
		String sql = "select c.clientId, c.clientName,c.address,c.shortName,delivery_note,delivery_date,buyer_order,c.gstNo," +
				"buyer_date,destination,despatched,s.invoiceDate " +
				"from alke.invoice_header h RIGHT join sales s " +
				" on h.invoiceId = s.invoiceId and h.groupId = s.groupId and h.fyYear= s.fyYear join client c " +
				"on c.clientId= s.clientId and c.groupId= s.groupId " +
				"where s.invoiceId =? and s.fyYear =? and s.groupId=?";
		
		List<InvoiceDetail> invoiceDetails = jdbcTemplate.query(sql,  new Object[]{invoiceId,fyyear,groupId}, new RowMapper<InvoiceDetail>(){

			@Override
			public InvoiceDetail mapRow(ResultSet rs, int arg1)
					throws SQLException {
				
				InvoiceDetail invoiceDetail = new InvoiceDetail();
				invoiceDetail.setFyear(fyyear);
				invoiceDetail.setInvoiceId(invoiceId);
				invoiceDetail.setGroupId(groupId);
				invoiceDetail.setBuyerDoc(rs.getString("buyer_order"));
				invoiceDetail.setBuyerDocDate(rs.getDate("buyer_date"));
				invoiceDetail.setDeliveryDate(rs.getDate("delivery_date"));
				invoiceDetail.setInvoiceDate(rs.getDate("invoiceDate"));
				invoiceDetail.setDeliveryDoc(rs.getString("delivery_note"));
				invoiceDetail.setDestination(rs.getString("destination"));
				invoiceDetail.setDespatchedVia(rs.getString("despatched"));
				
				Client client = new Client();
				client.setAddress(rs.getString("address"));
				client.setClientName(rs.getString("clientName"));
				client.setShortName(rs.getString("shortName"));
				client.setId(rs.getInt("clientId"));
				client.setGSTno(rs.getString("gstNo"));
				invoiceDetail.setClient(client);
				return invoiceDetail;
			}
			
		});
		
		if(invoiceDetails !=null && invoiceDetails.size()>0)
     		return invoiceDetails.get(0);
		else
			return null;
	}
	
	
    public List<Item> getInvoiceItems(String invoiceId, int fyyear, String groupId) {
    	
    	String sql = "select id,fyYear,invoiceId,groupId,HNS_code,item,rate,qty,tax,sgst,igst,cgst from alke.invoice_item where invoiceId=? and fyYear =? and groupId=?";
    	List<Item> items = jdbcTemplate.query(sql,  new Object[]{invoiceId,fyyear,groupId}, new RowMapper<Item>(){

			@Override
			public Item mapRow(ResultSet rs, int arg1) throws SQLException {
				
				Item item = new Item();
				item.setId(rs.getInt("id"));
				item.setFyear(rs.getInt("fyYear"));
				item.setGroupId(rs.getString("groupId"));
				item.setItemDescription(rs.getString("item"));
				double rate = rs.getDouble("rate");
				int qty  = rs.getInt("qty");
				double tax = rs.getDouble("tax");
				item.setTax(tax);
				Double igst = rs.getDouble("igst");
				Double cgst = rs.getDouble("cgst");
				Double sgst = rs.getDouble("sgst");
				
				item.setHnsCode(rs.getString("HNS_code"));
				
				if(cgst==null) {
				
					cgst=0.0;
				}
				item.setCgst(cgst);
				
				if(igst==null) {
					
					igst=0.0;
				}
				item.setIgst(igst);
				
				if(sgst==null) {
					
					sgst=0.0;
				}
				item.setSgst(sgst);
				
				double amt  = rate * qty;
				double taxVal =0;
				tax = tax+ igst+cgst+sgst;
				if(tax>0) {
				    taxVal = amt *tax/100;
				}
				Double taxValue = BigDecimal.valueOf(taxVal)
					    .setScale(2, RoundingMode.HALF_UP)
					    .doubleValue();
				double totalAmt = taxValue +amt;
				
				Double totalAmountDisplay = BigDecimal.valueOf(totalAmt)
					    .setScale(0, RoundingMode.HALF_UP)
					    .doubleValue();
				
				item.setAmount(amt);
				item.setQuantity(qty);
				item.setRate(rate);
				
				item.setTotalAmount(totalAmountDisplay);
				item.setTaxAmount(taxValue);
				return item;
			}
    		
    		
    	});
    	
    	return items;
    }
	
	public void addInvoiceDetail(InvoiceDetail invoiceDetail) {
		String sql = "insert into alke.invoice_header(invoiceId,fyYear,groupId,delivery_note,delivery_date," +
				"buyer_order,buyer_date,destination,despatched) values(?,?,?,?,?,?,?,?,?)";
		
		java.sql.Date delDate = invoiceDetail.getDeliveryDate()!=null? new java.sql.Date(invoiceDetail.getDeliveryDate().getTime()):null;
		java.sql.Date buyerDocDate = invoiceDetail.getBuyerDocDate()!=null? new java.sql.Date(invoiceDetail.getBuyerDocDate().getTime()):null;
		
		
		jdbcTemplate.update(sql,new Object[]{invoiceDetail.getInvoiceId(),invoiceDetail.getFyear(),invoiceDetail.getGroupId()
				,invoiceDetail.getDeliveryDoc(),delDate,
				 invoiceDetail.getBuyerDoc(),buyerDocDate
				,invoiceDetail.getDestination(),invoiceDetail.getDespatchedVia()});
		
	}
	
	public int getInvoiceDetail(InvoiceDetail invoiceDetail) {
		
		String sql = "select count(*) from alke.invoice_header where invoiceId=? and fyYear=? and groupId = ? ";
		return jdbcTemplate.queryForObject(sql,new Object[]{invoiceDetail.getInvoiceId(),invoiceDetail.getFyear(),invoiceDetail.getGroupId()},
				Integer.class);
	}
	
	public void updateInvoiceDetail(InvoiceDetail invoiceDetail) {
		String sql = "update alke.invoice_header set delivery_note =? ,delivery_date =?," +
				"buyer_order=? ,buyer_date=?,destination=?,despatched=? where invoiceId=? and fyYear=? and groupId = ?";
		
		java.sql.Date delDate = invoiceDetail.getDeliveryDate()!=null? new java.sql.Date(invoiceDetail.getDeliveryDate().getTime()):null;
		java.sql.Date buyerDocDate = invoiceDetail.getBuyerDocDate()!=null? new java.sql.Date(invoiceDetail.getBuyerDocDate().getTime()):null;
		
		
		jdbcTemplate.update(sql,new Object[]{
				invoiceDetail.getDeliveryDoc(), delDate,
				 invoiceDetail.getBuyerDoc(),buyerDocDate
				,invoiceDetail.getDestination(),invoiceDetail.getDespatchedVia()
				,invoiceDetail.getInvoiceId(),invoiceDetail.getFyear(),invoiceDetail.getGroupId()		
		});
	}
	
	
	
	
	public void addInvoiceItem(final List<Item> items,final String groupId) {
		
		String sql = "insert into alke.invoice_item (invoiceId,fyYear,groupId,item,rate," +
				"qty,tax,HNS_code,sgst,cgst,igst) values(?,?,?,?,?,?,?,?,?,?,?)";
		
		jdbcTemplate.batchUpdate(sql,  new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return items.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
			
				Item item = items.get(i);
				ps.setString(1, item.getInvoiceId());
				ps.setInt(2, item.getFyear());
				ps.setString(3, groupId);
				ps.setString(4, item.getItemDescription());
				ps.setDouble(5, item.getRate());
				ps.setInt(6, item.getQuantity());
				ps.setDouble(7, item.getTax());
				
				
				
				
				if(item.getHnsCode()!=null) {
					ps.setString(8, item.getHnsCode());
				}else {
					ps.setNull(8, java.sql.Types.VARCHAR);
				}
				
		
				
				if(item.getSgst()!=null) {
					ps.setDouble(9, item.getSgst());
				}else {
					ps.setNull(9, java.sql.Types.DOUBLE);
				}
				
				if(item.getCgst()!=null) {
					ps.setDouble(10, item.getCgst());
				}else {
					ps.setNull(10, java.sql.Types.DOUBLE);
				}

				
				if(item.getIgst()!=null) {
				  ps.setDouble(11, item.getIgst());
				} else {
					ps.setNull(11, java.sql.Types.DOUBLE);
				}
				
			}
			
		});
		
		updateSales(items,groupId);
	}
	

public void updateSales(final List<Item> items,final String groupId) {
	
	Item item = items.get(0);
	List<Item> dbItems = getInvoiceItems(item.getInvoiceId(), item.getFyear(), groupId);
	
	double billAmount = 0d;
	double taxAmt = 0d;
	for(Item itm:dbItems) {
		
		billAmount = billAmount +itm.getAmount();
		
		double tax = itm.getCgst()+ itm.getSgst()+ itm.getIgst()+itm.getTax();
		taxAmt=  taxAmt +(itm.getAmount() * tax/100);
	}
	double totalAmt = billAmount + taxAmt;
	Double truncatedDouble = BigDecimal.valueOf(totalAmt)
		    .setScale(0, RoundingMode.HALF_UP)
		    .doubleValue();
	
	String sql = "update alke.sales set billAmt=?,totalAmt=? where invoiceId =? and fyYear =? and groupId =?";
	jdbcTemplate.update(sql, new Object[]{billAmount,truncatedDouble,item.getInvoiceId(), item.getFyear(), groupId});
}
	
public void updateInvoiceItem(final List<Item> items,final String groupId) {

		
		String sql = "update alke.invoice_item set item =?, rate= ?, qty =? , tax =?, sgst=?, cgst=?, igst=?, HNS_code =? where invoiceId=? " +
				"and fyYear=? and groupId=? and id=?";
		
		jdbcTemplate.batchUpdate(sql,  new BatchPreparedStatementSetter() {

			@Override
			public int getBatchSize() {
				return items.size();
			}

			@Override
			public void setValues(PreparedStatement ps, int i)
					throws SQLException {
			
				Item item = items.get(i);
				ps.setString(9, item.getInvoiceId());
				ps.setInt(10, item.getFyear());
				ps.setString(11, groupId);
				ps.setLong(12, item.getId());
				ps.setString(1, item.getItemDescription());
				ps.setDouble(2, item.getRate());
				ps.setInt(3, item.getQuantity());
				ps.setDouble(4, item.getTax());
				
				if(item.getSgst()!=null) {
					ps.setDouble(5, item.getSgst());
				}else {
					ps.setNull(5, java.sql.Types.DOUBLE);
				}
				
				if(item.getCgst()!=null) {
					ps.setDouble(6, item.getCgst());
				}else {
					ps.setNull(6, java.sql.Types.DOUBLE);
				}
				if(item.getHnsCode()!=null) {
					ps.setString(8, item.getHnsCode());
				} else {
					ps.setNull(8, java.sql.Types.VARCHAR);
				}
				
				
				if(item.getIgst()!=null) {
				  ps.setDouble(7, item.getIgst());
				} else {
					ps.setNull(7, java.sql.Types.DOUBLE);
				}
				
			}
			
		});
		
		updateSales(items,groupId);
		
	}



public void deleteInvoiceItem(final Item item) {
	
	String sql = "delete from alke.invoice_item  where invoiceId=? " +
			"and fyYear=? and groupId=? and id=?";
	
	jdbcTemplate.update(sql,new Object[]{item.getInvoiceId(),item.getFyear(),item.getGroupId(),item.getId()});
	List<Item> items = Lists.newArrayList(item);
	updateSales(items,item.getGroupId());
}




}
