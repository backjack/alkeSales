package com.alkefp.sales.helper;

import com.alkefp.sales.beans.Client;
import com.alkefp.sales.beans.InvoiceDetail;
import com.alkefp.sales.beans.Item;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Produces the editable Word counterpart of the invoice PDF. */
@Component
public class InvoiceWordBuilder {
    private static final String INK="202C44";
    private static final String GRID="E2E7EF";
    private static final String HEADER="EFF2F8";
    private static final String SUPPLIER_GST="27AAKCS1815L1Z2";

    public byte[] build(InvoiceDetail invoice) throws Exception { return build(invoice,false); }
    public byte[] build(InvoiceDetail invoice,boolean letterhead) throws Exception {
        try (ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            XWPFDocument document=new XWPFDocument();
            setPageMargins(document,letterhead);
            title(document,"Tax Invoice");
            if(letterhead) addLetterhead(document);
            spacer(document,18);

            Client client=invoice.getClient()==null ? new Client() : invoice.getClient();
            XWPFTable details=table(document,2);
            details.createRow();
            put(details.getRow(0).getCell(0),"INVOICE DETAILS",true,ParagraphAlignment.LEFT);
            put(details.getRow(0).getCell(1),"BILL TO",true,ParagraphAlignment.LEFT);
            put(details.getRow(1).getCell(0),"Invoice No.: "+text(invoice.getInvoiceId())+"\nInvoice date: "+date(invoice.getInvoiceDate())+"\nFinancial year: "+invoice.getFyear()+"-"+(invoice.getFyear()+1),false,ParagraphAlignment.LEFT);
            put(details.getRow(1).getCell(1),text(client.getClientName())+"\n"+text(client.getAddress())+"\nGST: "+text(client.getGSTno()),false,ParagraphAlignment.LEFT);
            spacer(document,10);

            XWPFTable delivery=table(document,2);
            String[][] deliveryValues={{"Buyer order: "+text(invoice.getBuyerDoc()),"Order date: "+date(invoice.getBuyerDocDate())},{"Delivery note: "+text(invoice.getDeliveryDoc()),"Delivery date: "+date(invoice.getDeliveryDate())},{"Dispatched via: "+text(invoice.getDespatchedVia()),"Destination: "+text(invoice.getDestination())}};
            delivery.createRow(); delivery.createRow();
            for(int row=0;row<deliveryValues.length;row++) for(int column=0;column<2;column++) put(delivery.getRow(row).getCell(column),deliveryValues[row][column],false,ParagraphAlignment.LEFT);
            spacer(document,10);

            XWPFTable items=table(document,7);
            String[] headings={"Description","HSN","Qty","Rate (INR)","CGST","SGST","IGST"};
            for(int column=0;column<headings.length;column++) put(items.getRow(0).getCell(column),headings[column],true,ParagraphAlignment.LEFT);
            repeatHeader(items.getRow(0));
            List<Item> invoiceItems=invoice.getItems()==null ? Collections.emptyList() : invoice.getItems();
            for(Item item:invoiceItems) {
                XWPFTableRow row=items.createRow();
                put(row.getCell(0),text(item.getItemDescription()),false,ParagraphAlignment.LEFT);
                put(row.getCell(1),text(item.getHnsCode()),false,ParagraphAlignment.LEFT);
                put(row.getCell(2),String.valueOf(item.getQuantity()),false,ParagraphAlignment.CENTER);
                put(row.getCell(3),money(item.getRate()),false,ParagraphAlignment.RIGHT);
                put(row.getCell(4),taxAmount(item,item.getCgst()),false,ParagraphAlignment.RIGHT);
                put(row.getCell(5),taxAmount(item,item.getSgst()),false,ParagraphAlignment.RIGHT);
                put(row.getCell(6),taxAmount(item,item.getIgst()),false,ParagraphAlignment.RIGHT);
            }
            spacer(document,10);

            XWPFTable totals=table(document,2);
            setWidth(totals,55,true);
            totals.createRow(); totals.createRow();
            put(totals.getRow(0).getCell(0),"Subtotal (INR)",false,ParagraphAlignment.LEFT); put(totals.getRow(0).getCell(1),money(invoice.getBillAmount()),false,ParagraphAlignment.RIGHT);
            put(totals.getRow(1).getCell(0),"Tax (INR)",false,ParagraphAlignment.LEFT); put(totals.getRow(1).getCell(1),money(invoice.getTaxAmount()),false,ParagraphAlignment.RIGHT);
            put(totals.getRow(2).getCell(0),"Grand total (INR)",true,ParagraphAlignment.LEFT); put(totals.getRow(2).getCell(1),money(invoice.getGrandTotal()),true,ParagraphAlignment.RIGHT);
            spacer(document,10);

            XWPFTable payment=table(document,1);
            put(payment.getRow(0).getCell(0),"Alke Fire Protection GST No.: "+SUPPLIER_GST+"\nPayment remitted to below: Alke Fire Protection | THE SARASWAT CO-OPERATIVE BANK LTD\nSHOP NO.1 & 15 BLOCK NO.1, EMERALD PLAZA, HIRANANDANI MEDOWS, OFF POKHARAN ROAD NO.2, THANE (WEST) - 400 610, Maharashtra\nAccount Name: Alke Fire Protection    Account No.: 148100100000505    IFSC Code: SRCB0000148",false,ParagraphAlignment.LEFT);
            document.write(bytes);
            return bytes.toByteArray();
        }
    }

    private static XWPFTable table(XWPFDocument document,int columns) {
        XWPFTable table=document.createTable(1,columns);
        setWidth(table,100,false);
        table.setInsideHBorder(XWPFBorderType.SINGLE,1,0,GRID);
        table.setInsideVBorder(XWPFBorderType.SINGLE,1,0,GRID);
        CTTblPr properties=table.getCTTbl().getTblPr();
        CTTblBorders borders=properties.isSetTblBorders() ? properties.getTblBorders() : properties.addNewTblBorders();
        borders.addNewTop().setVal(STBorder.SINGLE); borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE); borders.addNewRight().setVal(STBorder.SINGLE);
        borders.getTop().setColor(GRID); borders.getBottom().setColor(GRID);
        borders.getLeft().setColor(GRID); borders.getRight().setColor(GRID);
        borders.getTop().setSz(BigInteger.valueOf(4)); borders.getBottom().setSz(BigInteger.valueOf(4));
        borders.getLeft().setSz(BigInteger.valueOf(4)); borders.getRight().setSz(BigInteger.valueOf(4));
        return table;
    }

    private static void setWidth(XWPFTable table,int percent,boolean rightAligned) {
        CTTblPr properties=table.getCTTbl().getTblPr();
        CTTblWidth width=properties.isSetTblW() ? properties.getTblW() : properties.addNewTblW();
        width.setType(STTblWidth.PCT); width.setW(BigInteger.valueOf(percent*50L));
        properties.addNewJc().setVal(rightAligned ? STJc.RIGHT : STJc.LEFT);
    }

    private static void put(XWPFTableCell cell,String value,boolean heading,ParagraphAlignment alignment) {
        clear(cell);
        cell.setVerticalAlignment(XWPFVertAlign.CENTER);
        if(heading) shade(cell,HEADER);
        String[] lines=text(value).split("\\n",-1);
        for(int line=0;line<lines.length;line++) {
            XWPFParagraph paragraph=line==0 ? cell.getParagraphs().get(0) : cell.addParagraph();
            paragraph.setAlignment(alignment);
            paragraph.setSpacingAfter(0);
            paragraph.setSpacingBefore(0);
            XWPFRun run=paragraph.createRun();
            run.setText(lines[line]);
            run.setFontFamily("Arial");
            run.setFontSize(heading ? 9 : 9);
            run.setColor(INK);
            run.setBold(heading);
        }
    }

    private static void clear(XWPFTableCell cell) {
        XWPFParagraph paragraph=cell.getParagraphs().get(0);
        while(paragraph.getRuns().size()>0) paragraph.removeRun(0);
    }

    private static void shade(XWPFTableCell cell,String color) {
        CTTc ctTc=cell.getCTTc(); CTTcPr properties=ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
        CTShd shading=properties.isSetShd() ? properties.getShd() : properties.addNewShd();
        shading.setVal(STShd.CLEAR); shading.setFill(color);
    }

    private static void repeatHeader(XWPFTableRow row) {
        CTTrPr properties=row.getCtRow().isSetTrPr() ? row.getCtRow().getTrPr() : row.getCtRow().addNewTrPr();
        properties.addNewTblHeader().setVal(STOnOff.TRUE);
    }

    private static void setPageMargins(XWPFDocument document,boolean letterhead) {
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr section=document.getDocument().getBody().isSetSectPr() ? document.getDocument().getBody().getSectPr() : document.getDocument().getBody().addNewSectPr();
        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar margins=section.isSetPgMar() ? section.getPgMar() : section.addNewPgMar();
        margins.setTop(BigInteger.valueOf(letterhead?3800:720)); margins.setBottom(BigInteger.valueOf(letterhead?2000:720)); margins.setLeft(BigInteger.valueOf(720)); margins.setRight(BigInteger.valueOf(720));
    }

    private static void addLetterhead(XWPFDocument document) throws Exception {
        XWPFHeader header=new XWPFHeaderFooterPolicy(document).createHeader(STHdrFtr.DEFAULT);
        header.setXWPFDocument(document);
        XWPFParagraph paragraph=header.getParagraphs().get(0); paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run=paragraph.createRun();
        String relationshipId;
        try(InputStream image=InvoiceWordBuilder.class.getResourceAsStream("/invoice-letterhead.png")) {
            if(image==null) throw new IllegalStateException("Invoice letterhead resource is missing");
            relationshipId=header.addPictureData(image,XWPFDocument.PICTURE_TYPE_PNG);
        }
        CTPicture picture=run.getCTR().addNewPict();
        org.w3c.dom.Document xml=picture.getDomNode().getOwnerDocument();
        org.w3c.dom.Element shapeElement=xml.createElementNS("urn:schemas-microsoft-com:vml","v:shape");
        shapeElement.setAttribute("id","InvoiceLetterhead"); shapeElement.setAttribute("type","#_x0000_t75");
        shapeElement.setAttribute("style","position:absolute;left:0;top:0;width:8.27in;height:11.69in;z-index:-251654144;mso-position-horizontal-relative:page;mso-position-vertical-relative:page");
        shapeElement.setAttribute("filled","f"); shapeElement.setAttribute("stroked","f");
        org.w3c.dom.Element imageElement=xml.createElementNS("urn:schemas-microsoft-com:vml","v:imagedata");
        imageElement.setAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships","r:id",relationshipId);
        imageElement.setAttributeNS("urn:schemas-microsoft-com:office:office","o:title","Invoice letterhead");
        shapeElement.appendChild(imageElement); picture.getDomNode().appendChild(shapeElement);
    }

    private static void title(XWPFDocument document,String value) {
        XWPFParagraph paragraph=document.createParagraph(); paragraph.setAlignment(ParagraphAlignment.CENTER); paragraph.setSpacingAfter(0);
        XWPFRun run=paragraph.createRun(); run.setText(value); run.setBold(true); run.setFontFamily("Arial"); run.setFontSize(18); run.setColor(INK);
    }

    private static void spacer(XWPFDocument document,int points) { XWPFParagraph paragraph=document.createParagraph(); paragraph.setSpacingAfter(points); paragraph.setSpacingBefore(0); }
    private static String text(String value) { return value==null ? "" : value; }
    private static String date(java.util.Date value) { return value==null ? "-" : new SimpleDateFormat("dd MMM yyyy").format(value); }
    private static String money(Double value) { return String.format(Locale.US,"%,.2f",value==null ? 0 : value); }
    private static String percent(Double value) { return String.format(Locale.US,"%.2f",value==null ? 0 : value); }
    private static String taxAmount(Item item,Double rate) { double base=item.getAmount()==null ? item.getRate()*item.getQuantity() : item.getAmount(); return money(base*(rate==null ? 0 : rate)/100)+"\n("+percent(rate)+"%)"; }
}
