package com.alkefp.sales.helper;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.SaleSummary;
import com.alkefp.sales.dao.SummaryDao;
import com.alkefp.sales.vo.MonthlySales;
//import org.apache.poi.hssf.usermodel.HSSFDataFormat;

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
	

	
	public void buildMonthlySales(Date startDate, Date endDate , int year, String month,HttpServletResponse response) throws IOException {
		
		
		List<MonthlySales> sales = summaryDao.getMonthlySales(startDate, endDate, year);
		System.out.println("saless......." + sales.size());
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Sales Details for month of "
				+ month + "-" + year);

		String[] headerName = {"Invoice Id","ClientName","Address","GST no",
				"Total Amount","Bill Amount","Invoice Date","CGST","SGST","IGST"};
		
		int rowCount = 0;
		int columnCount = 0;
		Row row = sheet.createRow(rowCount++);
		
		for(String header: headerName) {
			
			Cell cell = row.createCell(++columnCount);
			cell.setCellValue(header);
			cell.setCellStyle(this.getHeaderCellFormat(workbook));
		}
		
		Collections.sort(sales, new Comparator<MonthlySales>(){

			@Override
			public int compare(MonthlySales sale1, MonthlySales sale2) {
				
				int val = sale1.getInvoiceId().compareTo(sale2.getInvoiceId());
				return val;
			}
			
		});
		
		for(MonthlySales sale:sales) {
			
			System.out.println("Sales loggging" + sale);
			
			row = sheet.createRow(rowCount++);
			columnCount = 0;

			CellStyle doubleCellStyle = this.getDoubleCellFormat(workbook);
			CellStyle dateCellStyle = this.getDateCellFormat(workbook);
			
			Cell cell = row.createCell(++columnCount);
			cell.setCellValue((String) sale.getInvoiceId());
			
			cell = row.createCell(++columnCount);
			cell.setCellValue(sale.getClientName());
			
			cell = row.createCell(++columnCount);
			cell.setCellValue(sale.getAddress());
			
			cell = row.createCell(++columnCount);
			cell.setCellValue(sale.getGstNo());
			
			cell = row.createCell(++columnCount);
			cell.setCellStyle(doubleCellStyle);
			cell.setCellValue(sale.getTotalBill());
			
			cell = row.createCell(++columnCount);
			cell.setCellStyle(doubleCellStyle);
			cell.setCellValue(sale.getBillAmount());
			
			
			cell = row.createCell(++columnCount);
			cell.setCellStyle(dateCellStyle);
			cell.setCellValue(sale.getInvoiceDate());
			

			cell = row.createCell(++columnCount);
			cell.setCellStyle(doubleCellStyle);
			cell.setCellValue(sale.getCgst());
			

			cell = row.createCell(++columnCount);
			cell.setCellStyle(doubleCellStyle);
			cell.setCellValue(sale.getSgst());
			
			cell = row.createCell(++columnCount);
			cell.setCellStyle(doubleCellStyle);
			cell.setCellValue(sale.getIgst());
		}

	     sheet.autoSizeColumn(1);
	     sheet.autoSizeColumn(2);
	     sheet.autoSizeColumn(3);
	     sheet.autoSizeColumn(4);
	     sheet.autoSizeColumn(5);
	     sheet.autoSizeColumn(6);
	     sheet.autoSizeColumn(7);
	     sheet.autoSizeColumn(8);
	     sheet.autoSizeColumn(9);
	     sheet.autoSizeColumn(10);
	     
		workbook.write(response.getOutputStream());
		workbook.close();
	}
	
	
	private CellStyle getHeaderCellFormat(XSSFWorkbook workbook) {
		
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND); 
		style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
		return style;
	}
	
	private CellStyle getDateCellFormat(XSSFWorkbook workbook) {
		
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				"dd-MMM-yyyy"));
		return cellStyle;
	}
	
	private CellStyle getDoubleCellFormat(XSSFWorkbook workbook) {
		
		CellStyle cellDoubleStyle = workbook.createCellStyle();
		cellDoubleStyle.setDataFormat(HSSFDataFormat
				.getBuiltinFormat("##,##,###.#0"));

		return cellDoubleStyle;
	}
	
	

}
