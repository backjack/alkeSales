package com.alkefp.sales.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.Client;
import com.alkefp.sales.beans.InvoiceDetail;
import com.alkefp.sales.beans.Item;
import com.alkefp.sales.dao.InvoiceDetailDao;
import com.alkefp.sales.utils.NumberToWord;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class InvoiceBuilder {

	
    @Autowired
    private InvoiceDetailDao invoiceDetailDao;
	
   
    private Client getClient() {
    	
    	Client c = new Client();
    	c.setId(1);
    	c.setClientName("Rising Star Trading.");
    	c.setAddress("214/216 Nagdevi Street,Mumbai,Maharashtra- 400003,Tel:- 022-23442525,23466223");
    	return c;
    }
	
    
    
    public void buildInv() throws Exception{
    	
    	File f = new File("C:\\logs\\test_2.docx");
    	//File f = new File("C:\\logs\\doc_img.docx");
    	Client client = getClient();
    	  XWPFDocument document = new XWPFDocument(new FileInputStream(f));
    	List<XWPFParagraph> paras = document.getParagraphs();
    	  for(int i=0;i<paras.size();i++) {
    		  
    		  System.out.println(paras.get(i).getText());
    		  System.out.println("################"+i);
    	  }
    	  XWPFParagraph para = paras.get(5);
    	  
    	  XWPFRun r1 = para.createRun();
    	  //  r1.setBold(true);
    	    r1.addBreak();
    	    r1.setText(client.getClientName());
    	    r1.setFontFamily("Calibri");
    	    r1.setFontSize(11);
    	    para.setAlignment(ParagraphAlignment.RIGHT);
    	   String [] addresses = client.getAddress().split(",");

    	   for(String address:addresses) {
    		    r1.addBreak();
        	    r1.setText(address+",");
    	   }
    	   
    	   
    	   para = paras.get(12);
     	  
     	    XWPFRun r2 = para.createRun();
     	   r2.setFontFamily("Calibri");
     	   r2.setFontSize(11);
     	   r2.setBold(true);
     	   r2.setText("  One Thousand Two Hundred and Thirty only");
     	   
     	  List<XWPFTable> headerTable = document.getTables();
     	   
     	  XWPFTableRow firstRow = headerTable.get(2).getRow(1);
  		XWPFTableCell  cell = firstRow.getCell(1);
  		cell.setVerticalAlignment(XWPFVertAlign.BOTTOM);
  		this.setFont(cell,"ABCH1234666FGj");
    	   
    	    File fo = new File("C://logs//fest12.docx");
   		    OutputStream out = new FileOutputStream( fo);
   	         document.write(out);
   	     // document.write(out);
   	       out.close();
   	       System.out.println("done");
    }
    
    
    public void setClient( XWPFDocument document, Client client) {
    	
      XWPFParagraph para = document.getParagraphs().get(5);
   	  XWPFRun r1 = para.createRun();
   	    r1.addBreak();
   	    r1.setText(client.getClientName());
   	    r1.setFontFamily("Calibri");
   	    r1.setFontSize(11);
   	    para.setAlignment(ParagraphAlignment.RIGHT);
   	   String [] addresses = client.getAddress().split(",");

   	   for(String address:addresses) {
   		    r1.addBreak();
       	    r1.setText(address+",");
   	   }
    }
    
	public void buildInvoice(InvoiceDetail invoiceDetail, HttpServletResponse response,int type) throws Exception{
		
		invoiceDetail = invoiceDetailDao.getFullInvoiceDetail(invoiceDetail.getInvoiceId()
				,invoiceDetail.getFyear(),
				 invoiceDetail.getGroupId());
		InputStream in = null;
		   if(type==1) {
		        in = this.getClass().getClassLoader()
                .getResourceAsStream("test_2.docx");
                } else {
		
                	 in = this.getClass().getClassLoader()
                .getResourceAsStream("doc_img.docx");
          }
		  XWPFDocument document = new XWPFDocument(in);
		  List<XWPFTable> headerTable = document.getTables();
		  
		  setClient(document,invoiceDetail.getClient());
		  updateHeader(headerTable.get(0),invoiceDetail);
		  
		  //Map<Double,List<Item>> taxItems = getItemsGroupByTax(invoiceDetail.getItems());
		  
		  updateItems(headerTable.get(1),invoiceDetail,invoiceDetail.getItems());
		  updateClientGSTNo(headerTable.get(2),invoiceDetail.getClient().getGSTno());
		  XWPFParagraph para = document.getParagraphs().get(12);
	     	  
    	   XWPFRun r2 = para.createRun();
    	   r2.setFontFamily("Calibri");
    	   r2.setFontSize(11);
    	   r2.setBold(true);
	      
		  String str = " "+NumberToWord.convertToWord(invoiceDetail.getGrandTotal().intValue())+" only";
		  
		   r2.setText(str);
    	  //File f = new File("C://logs//fest1.docx");
		 // FileOutputStream out = new FileOutputStream( f);
	      document.write(response.getOutputStream());
	     // document.write(out);
	      // out.close();
	      System.out.println("test1.docx written successully");
	  //    return f;
	}
	
	
	public void updateHeader(XWPFTable table,InvoiceDetail invoiceDetail) {
		
		
			XWPFTableRow firstRow = table.getRow(0);
			XWPFTableCell  cell = firstRow.getCell(1);
			this.setFont(cell,invoiceDetail.getInvoiceId());
			cell.setVerticalAlignment(XWPFVertAlign.BOTTOM);
			cell = firstRow.getCell(3);
			this.setFont(cell,invoiceDetail.getFormattedDate(invoiceDetail.getInvoiceDate()));
			
			
		/*	XWPFTableRow secondRow = table.getRow(1);
		    cell = secondRow.getCell(1);
		    if(invoiceDetail.getDeliveryDoc()!=null) {
		    	this.setFont(cell,invoiceDetail.getDeliveryDoc());
		    }
			cell = secondRow.getCell(3);*/
			//commented mode of payment
			//this.setFont(cell,invoiceDetail.getFormattedDate());
			
			//SupplierRef commented
			/*XWPFTableRow thirdRow = table.getRow(2);
			cell = thirdRow.getCell(1);
			this.setFont(cell, invoiceDetail.s);
			cell = thirdRow.getCell(1);
			this.setFont(cell, invoiceDetail.getFormattedDate());*/

			XWPFTableRow forthRow = table.getRow(3);
			cell = forthRow.getCell(1);
			this.setFont(cell,invoiceDetail.getDeliveryDoc() );
			cell = forthRow.getCell(3);
			this.setFont(cell,invoiceDetail.getFormattedDate(invoiceDetail.getDeliveryDate()));
			
			XWPFTableRow fifthRow = table.getRow(4);
			cell = fifthRow.getCell(1);
			this.setFont(cell, invoiceDetail.getBuyerDoc());
			cell = fifthRow.getCell(3);
			this.setFont(cell,  invoiceDetail.getFormattedDate(invoiceDetail.getBuyerDocDate()));
			
			
			XWPFTableRow sixthRow = table.getRow(5);
			cell = sixthRow.getCell(1);
			this.setFont(cell, invoiceDetail.getDestination());
			cell = sixthRow.getCell(3);
			this.setFont(cell, invoiceDetail.getDespatchedVia());
	}
	
	
	public void updateClientGSTNo(XWPFTable table,String clientGSTNo) {
		
		if(clientGSTNo!=null) {
			XWPFTableRow firstRow = table.getRow(1);
			XWPFTableCell  cell = firstRow.getCell(1);
			cell.setVerticalAlignment(XWPFVertAlign.BOTTOM);
			this.setFont(cell,clientGSTNo);
		}
	
		
		
	/*	XWPFTableRow secondRow = table.getRow(1);
	    cell = secondRow.getCell(1);
	    if(invoiceDetail.getDeliveryDoc()!=null) {
	    	this.setFont(cell,invoiceDetail.getDeliveryDoc());
	    }
		cell = secondRow.getCell(3);*/
		//commented mode of payment
		//this.setFont(cell,invoiceDetail.getFormattedDate());
		
		//SupplierRef commented
		/*XWPFTableRow thirdRow = table.getRow(2);
		cell = thirdRow.getCell(1);
		this.setFont(cell, invoiceDetail.s);
		cell = thirdRow.getCell(1);
		this.setFont(cell, invoiceDetail.getFormattedDate());*/


		
		

}
	
	public void updateItems(XWPFTable table,InvoiceDetail invoiceDetail,List<Item> taxItems) {
		
		
		CTTblPr tblpro = table.getCTTbl().getTblPr();

		CTTblBorders borders = tblpro.addNewTblBorders();
		borders.addNewBottom().setVal(STBorder.THICK); 
		borders.addNewLeft().setVal(STBorder.THICK);
		borders.addNewRight().setVal(STBorder.THICK);
		borders.addNewTop().setVal(STBorder.THICK);
		
		 table.setInsideHBorder(XWPFBorderType.THICK, 2, 0, "000000");
	     table.setInsideVBorder(XWPFBorderType.THICK, 2, 0, "000000");
	        int j=1;
	    	double taxVal = 0d;

			for(int i=0;i<taxItems.size();i++) {
				
				Item item = taxItems.get(i);
				table.insertNewTableRow(j);
				XWPFTableRow tableOneRowOne = table.getRow(j);
				++j;
				XWPFTableCell cell = tableOneRowOne.addNewTableCell();
				this.setFont(cell, (i+1) + "");
				XWPFTableCell cell2 = tableOneRowOne.addNewTableCell();
				this.setFont(cell2, item.getItemDescription());
	
				XWPFTableCell cell3 = tableOneRowOne.addNewTableCell();
				this.setFont(cell3, item.getHnsCode() + "");
				
				XWPFTableCell cell4 = tableOneRowOne.addNewTableCell();
				this.setFont(cell4, item.getQuantity() + "");
	
				XWPFTableCell cell5 = tableOneRowOne.addNewTableCell();
				this.setFont(cell5, item.getRate() + "");
	
				XWPFTableCell cell6 = tableOneRowOne.addNewTableCell();
				DecimalFormat df = new DecimalFormat("##,##,##,###");
				this.setFont(cell6, df.format(item.getAmount()) + "");
				
				double sgstTax = item.getSgst();
				
				double cgstTax = item.getCgst();
				
				double igstTax = item.getIgst();
				
				XWPFTableCell cell7 = tableOneRowOne.addNewTableCell();
				this.setFont(cell7, sgstTax + "%");
	
				double sgstVal = sgstTax * item.getAmount()/100;
				XWPFTableCell cell8 = tableOneRowOne.addNewTableCell();
				df = new DecimalFormat("##,##,##,###");
				this.setFont(cell8, df.format(sgstVal) + "");
				
	
				XWPFTableCell cell9 = tableOneRowOne.addNewTableCell();
				this.setFont(cell9, cgstTax + "%");
	
				double cgstVal = cgstTax * item.getAmount()/100;
				XWPFTableCell cell10 = tableOneRowOne.addNewTableCell();
				df = new DecimalFormat("##,##,##,###");
				this.setFont(cell10, df.format(cgstVal) + "");
				
				
				XWPFTableCell cell11 = tableOneRowOne.addNewTableCell();
				this.setFont(cell11, igstTax + "%");
	
				double igstVal = igstTax * item.getAmount()/100;
				XWPFTableCell cell12 = tableOneRowOne.addNewTableCell();
				df = new DecimalFormat("##,##,##,###");
				this.setFont(cell12, df.format(igstVal) + "");
				
				double rowTotal = sgstVal+ igstVal +cgstVal+ item.getAmount();
				XWPFTableCell cell13 = tableOneRowOne.addNewTableCell();
				df = new DecimalFormat("##,##,##,###");
				this.setFont(cell13, df.format(rowTotal) + "");
				
				taxVal = taxVal + item.getTaxAmount();
	
			}
			
	    addGrandTotal(j,table,invoiceDetail.getGrandTotal());
	}
	
	
	public void addGrandTotal(int row,XWPFTable table, double value ) {
		 table.insertNewTableRow(row);
		 XWPFTableRow taxRow = table.getRow(row);
		 row++;
		 XWPFTableCell  cell  = taxRow.addNewTableCell();
		 this.setFont(cell,"Grand Total",true);
		  cell.getCTTc().addNewTcPr();
		  cell.getCTTc().getTcPr().addNewGridSpan();
		  cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(12));			
		  cell = taxRow.addNewTableCell();
		  
			DecimalFormat df = new DecimalFormat("##,##,##,###");
			this.setFont(cell,df.format(value)+"");
	}
	

	
    public void setFont(XWPFTableCell cell, String text) {
    	XWPFParagraph p1 = cell.getParagraphs().get(0);

        XWPFRun r1 = p1.createRun();
        //r1.setBold(true);
        r1.setText(text);
        //r1.setItalic(true);
        r1.setFontFamily("Calibri");
        r1.setFontSize(11);
    }
    
    public void setFont(XWPFTableCell cell, String text, boolean bold) {
    	XWPFParagraph p1 = cell.getParagraphs().get(0);

        XWPFRun r1 = p1.createRun();
        r1.setBold(true);
        r1.setText(text);
        //r1.setItalic(true);
        r1.setFontFamily("Calibri");
        p1.setAlignment(ParagraphAlignment.CENTER);
        r1.setFontSize(11);
    } 

/*	public static void main(String args[]) throws Exception {
		
		InvoiceBuilder v = new InvoiceBuilder();
		v.buildInv();
	
	}*/

}
