package com.alkefp.sales.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

//import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.SaleSummary;
import com.alkefp.sales.dao.SummaryDao;

@Component
public class XlsBuilder {

	@Autowired
	private SummaryDao summaryDao;

	public void downloadXls(String groupId, List<Integer>fyyears, HttpServletResponse response) throws Exception{
		
		List<SaleSummary> salesSummaries = summaryDao.getSummaryView(fyyears, groupId,null);
		 XSSFWorkbook workbook = new XSSFWorkbook();
	     XSSFSheet sheet = workbook.createSheet("Invoice Details");
	     
	     Collections.sort(salesSummaries, new Comparator<SaleSummary>(){

			@Override
			public int compare(SaleSummary summary1, SaleSummary summary2) {
			   
				String invoiceId1 = summary1.getInvoiceId();
				String invoiceId2 = summary2.getInvoiceId();
				
				Integer val1 =Integer.valueOf(invoiceId1.split("/")[2]);
				Integer val2 =Integer.valueOf(invoiceId2.split("/")[2]);
				return val1.compareTo(val2);
			}
	    	 
	     });

	     CellStyle cellStyle = workbook.createCellStyle();
	     CreationHelper createHelper = workbook.getCreationHelper();
	     cellStyle.setDataFormat(
	         createHelper.createDataFormat().getFormat("dd-MMM-yyyy"));
	     
	     CellStyle cellDoubleStyle = workbook.createCellStyle();
	     cellDoubleStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("##,##,###.#0"));
	     
	     int rowCount =0;
	     double totalAmt =0d;
	     double totalPayedAmt  =0d;
	     
	     Row row = sheet.createRow(++rowCount);
    	 int columnCount = 0;

    			
    		   Cell cell = row.createCell(++columnCount);
    		   cell.setCellValue("Invoice Id");
    		   cell = row.createCell(++columnCount);
    		   cell.setCellValue("ClientName");
    		   cell = row.createCell(++columnCount);
    		   cell.setCellValue("Total Amount");
    		   cell = row.createCell(++columnCount);
    		   cell.setCellValue("Total Payed Amount");
    		   cell = row.createCell(++columnCount);
    		   cell.setCellValue("Invoice Date");
    
    	for(SaleSummary saleSummary:salesSummaries) {
	    	row = sheet.createRow(++rowCount);
			columnCount = 0;

			cell = row.createCell(++columnCount);
			cell.setCellValue((String) saleSummary.getInvoiceId());
			cell = row.createCell(++columnCount);
			cell.setCellValue(saleSummary.getClientName());
			cell = row.createCell(++columnCount);
			cell.setCellStyle(cellDoubleStyle);
			cell.setCellValue(saleSummary.getTotalAmt());
			totalAmt = totalAmt + saleSummary.getTotalAmt();

			cell = row.createCell(++columnCount);
			cell.setCellStyle(cellDoubleStyle);
			cell.setCellValue(saleSummary.getPayedAmt());

			totalPayedAmt = totalPayedAmt + saleSummary.getPayedAmt();

			cell = row.createCell(++columnCount);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(saleSummary.getInvoiceDate());
	    	

	     }
		
	        row = sheet.createRow(++rowCount);
	        columnCount = 0;
	        cell = row.createCell(++columnCount);
		  
		   cell = row.createCell(++columnCount);
		   cell = row.createCell(++columnCount);
		   cell.setCellStyle(cellDoubleStyle);
		   cell.setCellValue(totalAmt);
		  
		
		   
		   cell = row.createCell(++columnCount);
		   cell.setCellStyle(cellDoubleStyle);
		   cell.setCellValue(totalPayedAmt);

		  
		     sheet.autoSizeColumn(1);
		     sheet.autoSizeColumn(2);
		     sheet.autoSizeColumn(3);
		     sheet.autoSizeColumn(4);
		     sheet.autoSizeColumn(5);
		     sheet.autoSizeColumn(0);
	     workbook.write(response.getOutputStream());
	}
	
	/**
	 * @param args
	 *//*
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}*/

}
