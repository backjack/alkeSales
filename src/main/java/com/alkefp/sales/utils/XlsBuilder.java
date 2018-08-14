package com.alkefp.sales.utils;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lowagie.text.Font;
//import org.apache.poi.hssf.usermodel.HSSFDataFormat;


public class XlsBuilder {


/*	public static void main(String[] args) throws Exception {
		
		test();
	}*/

	public static void test() throws Exception{
		
		File f = new File("C:/alke_ui/sales.xlsx");
		FileOutputStream fos = new FileOutputStream(f);
	
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Sales Details for month of");

		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND); 
		style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
	//	style.setFont(Font.BOLD);
		
		
		CellStyle cellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(
				"dd-MMM-yyyy"));

		CellStyle cellDoubleStyle = workbook.createCellStyle();
		cellDoubleStyle.setDataFormat(HSSFDataFormat
				.getBuiltinFormat("##,##,###.#0"));

		int rowCount = 0;
		Row row = sheet.createRow(++rowCount);
		int columnCount = 0;

		Cell cell = row.createCell(++columnCount);
		cell.setCellValue("Invoice Id");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("ClientName");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("Address");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("GST no");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("Total Amount");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("Bill Amount");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("Invoice Date");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("CGST");
		cell.setCellStyle(style);
		
		cell = row.createCell(++columnCount);
		cell.setCellValue("SGST");
		cell.setCellStyle(style);

		cell = row.createCell(++columnCount);
		cell.setCellValue("IGST");
		cell.setCellStyle(style);
		/**/

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
	     
		workbook.write(fos);
		workbook.close();
		fos.close();
	}
	

}
