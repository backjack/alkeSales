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
    private static final Font HEADING=FontFactory.getFont(FontFactory.HELVETICA_BOLD,10,Font.NORMAL,INK);
    private static final Font CLIENT=FontFactory.getFont(FontFactory.HELVETICA_BOLD,9,Font.NORMAL,INK);
    private static final Font INVOICE_META=FontFactory.getFont(FontFactory.HELVETICA_BOLD,11,Font.NORMAL,INK);
    private static final Font FOOTER_HEADING=FontFactory.getFont(FontFactory.HELVETICA_BOLD,8,Font.NORMAL,INK);
    private static final Font FOOTER_TEXT=FontFactory.getFont(FontFactory.HELVETICA,8,Font.NORMAL,INK);
    private static final Font FOOTER_ADDRESS=FontFactory.getFont(FontFactory.HELVETICA,7,Font.NORMAL,INK);
    private static final String SUPPLIER_GST="27AAKCS1815L1Z2";
    private static String text(String value) { return value==null ? "" : value; }
    private static String money(Double value) { return String.format(Locale.US,"%,.2f",value==null?0:value); }
    private static String percent(Double value) { return String.format(Locale.US,"%.2f",value==null?0:value); }
    private static String date(java.util.Date value) { return value==null?"-":new SimpleDateFormat("dd MMM yyyy").format(value); }
    private static String taxAmount(Item item,Double rate) {
        double base=item.getAmount()==null ? item.getRate()*item.getQuantity() : item.getAmount();
        return money(base*(rate==null?0:rate)/100)+"\n("+percent(rate)+"%)";
    }
    private void cell(PdfPTable table,String value,boolean heading) {
        PdfPCell cell=new PdfPCell(new Phrase(text(value),heading?FontFactory.getFont(FontFactory.HELVETICA_BOLD,9):TEXT));
        cell.setPadding(7); cell.setBorderColor(new Color(226,231,239));
        if(heading) cell.setBackgroundColor(new Color(239,242,248));
        table.addCell(cell);
    }
    public byte[] build(InvoiceDetail invoice) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        Document document=new Document(PageSize.A4,36,36,109,42);
        PdfWriter writer=PdfWriter.getInstance(document,bytes);
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onStartPage(PdfWriter writer,Document document) {
                if(writer.getPageNumber()==1) {
                    ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_CENTER,
                            new Phrase("TAX INVOICE",FontFactory.getFont(FontFactory.HELVETICA_BOLD,18,INK)),
                            PageSize.A4.getWidth()/2,PageSize.A4.getTop(36),0);
                }
            }
        });
        document.open();
        Paragraph invoiceMeta=new Paragraph("Invoice No.: "+invoice.getInvoiceId()+"\nInvoice date: "+date(invoice.getInvoiceDate())+"    Financial year: "+invoice.getFyear()+"-"+(invoice.getFyear()+1),INVOICE_META);
        invoiceMeta.setAlignment(Element.ALIGN_LEFT); invoiceMeta.setSpacingAfter(8); document.add(invoiceMeta);
        Client client=invoice.getClient();
        Paragraph recipient=new Paragraph("BILL TO\n"+text(client.getClientName())+"\n"+text(client.getAddress())+"\nGST: "+text(client.getGSTno()),CLIENT);
        recipient.setSpacingAfter(12); document.add(recipient);
        PdfPTable header=new PdfPTable(2); header.setWidthPercentage(100);
        cell(header,"Buyer order: "+text(invoice.getBuyerDoc()),false); cell(header,"Order date: "+date(invoice.getBuyerDocDate()),false);
        cell(header,"Delivery note: "+text(invoice.getDeliveryDoc()),false); cell(header,"Delivery date: "+date(invoice.getDeliveryDate()),false);
        cell(header,"Dispatched via: "+text(invoice.getDespatchedVia()),false); cell(header,"Destination: "+text(invoice.getDestination()),false);
        header.setSpacingAfter(14); document.add(header);
        PdfPTable items=new PdfPTable(new float[]{2.7f,.9f,.55f,.9f,1f,1f,1f}); items.setWidthPercentage(100); items.setHeaderRows(1); items.setSplitLate(false);
        for(String label:new String[]{"Description","HSN","Qty","Rate (INR)","CGST","SGST","IGST"}) cell(items,label,true);
        for(Item item:invoice.getItems()) {
            cell(items,item.getItemDescription(),false);
            cell(items,item.getHnsCode(),false); cell(items,String.valueOf(item.getQuantity()),false); cell(items,money(item.getRate()),false);
            cell(items,taxAmount(item,item.getCgst()),false); cell(items,taxAmount(item,item.getSgst()),false); cell(items,taxAmount(item,item.getIgst()),false);
        }
        document.add(items);
        PdfPTable totals=new PdfPTable(2); totals.setWidthPercentage(55); totals.setHorizontalAlignment(Element.ALIGN_RIGHT); totals.setSpacingBefore(16); totals.setKeepTogether(true);
        cell(totals,"Subtotal (INR)",false); cell(totals,money(invoice.getBillAmount()),false);
        cell(totals,"Tax (INR)",false); cell(totals,money(invoice.getTaxAmount()),false);
        cell(totals,"Grand total (INR)",true); cell(totals,money(invoice.getGrandTotal()),true);
        document.add(totals);
        PdfPTable payment=new PdfPTable(1); payment.setWidthPercentage(100); payment.setSpacingBefore(10); payment.setKeepTogether(true);
        PdfPCell paymentCell=new PdfPCell(); paymentCell.setPadding(6); paymentCell.setBorderColor(new Color(226,231,239));
        Paragraph paymentDetails=new Paragraph();
        paymentDetails.setLeading(9);
        paymentDetails.add(new Chunk("Alke Fire Protection GST No.: "+SUPPLIER_GST+"\n",FOOTER_HEADING));
        paymentDetails.add(new Chunk("Payment remitted to below: Alke Fire Protection | THE SARASWAT CO-OPERATIVE BANK LTD\n",FOOTER_HEADING));
        paymentDetails.add(new Chunk("SHOP NO.1 & 15 BLOCK NO.1, EMERALD PLAZA, HIRANANDANI MEDOWS, OFF POKHARAN ROAD NO.2, THANE (WEST) - 400 610, Maharashtra\n",FOOTER_ADDRESS));
        paymentDetails.add(new Chunk("Account Name: Alke Fire Protection    Account No.: 148100100000505    IFSC Code: SRCB0000148",FOOTER_TEXT));
        paymentCell.addElement(paymentDetails); payment.addCell(paymentCell); document.add(payment);
        document.close(); return bytes.toByteArray();
    }
}
