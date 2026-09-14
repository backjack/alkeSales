package com.alkefp.sales.helper;

import com.alkefp.sales.beans.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Builds downloads from persisted invoice values; tables wrap and repeat headers across pages. */
@Component
public class InvoicePdfBuilder {
    private static final Color INK=new Color(32,44,68);
    private static final Font TEXT=FontFactory.getFont(FontFactory.HELVETICA,9,Font.NORMAL,INK);
    private static String text(String value) { return value==null ? "" : value; }
    private static String money(Double value) { return String.format(Locale.US,"%.2f",value==null?0:value); }
    private static String date(java.util.Date value) { return value==null?"-":new SimpleDateFormat("dd MMM yyyy").format(value); }
    private void cell(PdfPTable table,String value,boolean heading) {
        PdfPCell cell=new PdfPCell(new Phrase(text(value),heading?FontFactory.getFont(FontFactory.HELVETICA_BOLD,9):TEXT));
        cell.setPadding(7); cell.setBorderColor(new Color(226,231,239));
        if(heading) cell.setBackgroundColor(new Color(239,242,248));
        table.addCell(cell);
    }
    public byte[] build(InvoiceDetail invoice) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        Document document=new Document(PageSize.A4,36,36,36,42);
        PdfWriter writer=PdfWriter.getInstance(document,bytes);
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter writer,Document document) {
                ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_RIGHT,new Phrase("Page "+writer.getPageNumber(),TEXT),559,22,0);
            }
        });
        document.open();
        document.add(new Paragraph("ALKE",FontFactory.getFont(FontFactory.HELVETICA_BOLD,24,INK)));
        Paragraph title=new Paragraph("INVOICE  /  "+invoice.getInvoiceId(),FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,INK));
        title.setSpacingAfter(10); document.add(title);
        document.add(new Paragraph("Invoice date: "+date(invoice.getInvoiceDate())+"    Financial year: "+invoice.getFyear()+"-"+(invoice.getFyear()+1),TEXT));
        Client client=invoice.getClient();
        Paragraph recipient=new Paragraph("\nBILL TO\n"+text(client.getClientName())+"\n"+text(client.getAddress())+"\nGST: "+text(client.getGSTno()),TEXT);
        recipient.setSpacingAfter(16); document.add(recipient);
        PdfPTable header=new PdfPTable(2); header.setWidthPercentage(100);
        cell(header,"Buyer order: "+text(invoice.getBuyerDoc()),false); cell(header,"Order date: "+date(invoice.getBuyerDocDate()),false);
        cell(header,"Delivery note: "+text(invoice.getDeliveryDoc()),false); cell(header,"Delivery date: "+date(invoice.getDeliveryDate()),false);
        cell(header,"Dispatched via: "+text(invoice.getDespatchedVia()),false); cell(header,"Destination: "+text(invoice.getDestination()),false);
        header.setSpacingAfter(18); document.add(header);
        PdfPTable items=new PdfPTable(new float[]{3.2f,1.2f,.7f,1.2f,1.2f,1.4f}); items.setWidthPercentage(100); items.setHeaderRows(1); items.setSplitLate(false);
        for(String label:new String[]{"Description / taxes","HSN","Qty","Rate (INR)","Tax (INR)","Total (INR)"}) cell(items,label,true);
        for(Item item:invoice.getItems()) {
            cell(items,item.getItemDescription()+"\nCGST "+money(item.getCgst())+"% / SGST "+money(item.getSgst())+"%\nIGST "+money(item.getIgst())+"% / Other "+money(item.getTax())+"%",false);
            cell(items,item.getHnsCode(),false); cell(items,String.valueOf(item.getQuantity()),false); cell(items,money(item.getRate()),false);
            cell(items,money(item.getTaxAmount()),false); cell(items,money(item.getTotalAmount()),false);
        }
        document.add(items);
        PdfPTable totals=new PdfPTable(2); totals.setWidthPercentage(55); totals.setHorizontalAlignment(Element.ALIGN_RIGHT); totals.setSpacingBefore(16); totals.setKeepTogether(true);
        cell(totals,"Subtotal (INR)",false); cell(totals,money(invoice.getBillAmount()),false);
        cell(totals,"Tax (INR)",false); cell(totals,money(invoice.getTaxAmount()),false);
        cell(totals,"Grand total (INR)",true); cell(totals,money(invoice.getGrandTotal()),true);
        document.add(totals); document.close(); return bytes.toByteArray();
    }
}
