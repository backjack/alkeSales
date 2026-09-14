package com.alkefp.sales.controller;

import com.alkefp.sales.beans.*;
import com.alkefp.sales.dao.*;
import com.alkefp.sales.helper.InvoicePdfBuilder;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

@RestController
@RequestMapping("/invoice/editor")
public class InvoiceEditorController {
    private final JdbcTemplate jdbc;
    private final SummaryDao summaries;
    private final InvoiceDetailDao details;
    private final InvoicePdfBuilder pdf;
    public InvoiceEditorController(JdbcTemplate jdbc, SummaryDao summaries, InvoiceDetailDao details, InvoicePdfBuilder pdf) {
        this.jdbc=jdbc; this.summaries=summaries; this.details=details; this.pdf=pdf;
    }
    public record Reference(String invoiceId, int fyear) {}
    public record SaveRequest(SaleSummary summary, InvoiceDetail detail) {}
    private String group(Authentication auth) {
        User user=summaries.getUserMap().get(auth.getName());
        if(user==null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return user.getGroupId();
    }
    private static <T> BaseResponse<T> response(T data) {
        BaseResponse<T> result=new BaseResponse<>(); result.setData(data); result.setSuccess(true); return result;
    }
    public static String nextNumber(List<String> ids) {
        Pattern pattern=Pattern.compile("^(.*?)(\\d+)$");
        BigInteger max=BigInteger.ZERO; String prefix=""; int width=0;
        for(String id:ids) {
            Matcher match=pattern.matcher(id);
            if(match.matches()) {
                BigInteger number=new BigInteger(match.group(2));
                if(number.compareTo(max)>0) { max=number; prefix=match.group(1); width=match.group(2).length(); }
            }
        }
        String next=max.add(BigInteger.ONE).toString();
        return prefix+"0".repeat(Math.max(0,width-next.length()))+next;
    }
    @GetMapping("/next-id/{year}")
    public BaseResponse<String> nextId(@PathVariable int year, Authentication auth) {
        return response(nextNumber(jdbc.queryForList("select invoiceId from sales where fyYear=? and groupId=? order by invoiceDate desc, invoiceId desc",String.class,year,group(auth))));
    }
    @GetMapping("/items")
    public BaseResponse<List<Item>> items(Authentication auth) {
        String group=group(auth);
        List<Item> result=jdbc.query("select item,HNS_code,max(tax) tax,max(cgst) cgst,max(sgst) sgst,max(igst) igst from invoice_item where groupId=? and item is not null and item<>'' group by item,HNS_code order by item", (rs,n)-> {
            Item item=new Item(); item.setItemDescription(rs.getString("item")); item.setHnsCode(rs.getString("HNS_code")); item.setTax(rs.getDouble("tax")); item.setCgst((Double)rs.getObject("cgst")); item.setSgst((Double)rs.getObject("sgst")); item.setIgst((Double)rs.getObject("igst")); return item;
        }, group);
        return response(result);
    }
    private void requireInvoice(Reference ref,String group) {
        if(ref==null || ref.invoiceId()==null || jdbc.queryForObject("select count(*) from sales where invoiceId=? and fyYear=? and groupId=?",Integer.class,ref.invoiceId(),ref.fyear(),group)==0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invoice not found");
    }
    @PostMapping("/create") @Transactional
    public BaseResponse<InvoiceDetail> create(@RequestBody SaveRequest request, Authentication auth) { return save(request,group(auth),true); }
    @PostMapping("/save") @Transactional
    public BaseResponse<InvoiceDetail> update(@RequestBody SaveRequest request, Authentication auth) { return save(request,group(auth),false); }

    private BaseResponse<InvoiceDetail> save(SaveRequest request,String group,boolean create) {
        SaleSummary s=request.summary(); InvoiceDetail d=request.detail();
        if(s==null || d==null || s.getInvoiceId()==null || !s.getInvoiceId().matches("[A-Za-z0-9_./-]{1,50}") || s.getFyYearId()==null || s.getInvoiceDate()==null || s.getClientId()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invoice number, financial year, client and invoice date are required");
        if(d.getItems()==null || d.getItems().isEmpty() || d.getItems().size()>500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Provide between 1 and 500 line items");
        if(jdbc.queryForObject("select count(*) from fy where fyYear=?",Integer.class,s.getFyYearId())==0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown financial year");
        if(jdbc.queryForObject("select count(*) from client where clientId=? and groupId=? and isActive='Y'",Integer.class,s.getClientId(),group)==0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose an active client");
        for(Item item:d.getItems()) {
            if(item==null || item.getItemDescription()==null || item.getItemDescription().isBlank() || item.getQuantity()<=0 || !Double.isFinite(item.getRate()) || item.getRate()<0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Each item needs a description, positive quantity and non-negative rate");
            for(Double tax:Arrays.asList(item.getTax(),item.getCgst(),item.getSgst(),item.getIgst()))
                if(tax!=null && (!Double.isFinite(tax) || tax<0 || tax>100)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tax percentages must be between 0 and 100");
            item.setInvoiceId(s.getInvoiceId()); item.setFyear(s.getFyYearId()); item.setGroupId(group);
        }
        // Serialize editor writes within the tenant, including the duplicate-number check.
        jdbc.queryForList("select username from user_profile where groupId=? order by username for update",group);
        Reference ref=new Reference(s.getInvoiceId(),s.getFyYearId());
        if(create) {
            if(jdbc.queryForObject("select count(*) from sales where invoiceId=? and fyYear=? and groupId=?",Integer.class,ref.invoiceId(),ref.fyear(),group)>0)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Invoice number already exists. Choose another number.");
            s.setTotalAmt(0d); summaries.insertInvoice(s,group);
        } else {
            requireInvoice(ref,group);
            // Preserve payments and identity; update only editable sales header fields.
            jdbc.update("update sales set clientId=?, invoiceDate=?, Details=? where invoiceId=? and fyYear=? and groupId=?",s.getClientId(),new java.sql.Date(s.getInvoiceDate().getTime()),s.getDetails(),ref.invoiceId(),ref.fyear(),group);
        }
        d.setInvoiceId(ref.invoiceId()); d.setFyear(ref.fyear()); d.setGroupId(group);
        if(details.getInvoiceDetail(d)==0) details.addInvoiceDetail(d); else details.updateInvoiceDetail(d);
        jdbc.update("delete from invoice_item where invoiceId=? and fyYear=? and groupId=?",ref.invoiceId(),ref.fyear(),group);
        details.addInvoiceItem(d.getItems(),group);
        return response(details.getFullInvoiceDetail(ref.invoiceId(),ref.fyear(),group));
    }
    @PostMapping("/pdf")
    public ResponseEntity<byte[]> download(@RequestBody Reference ref, Authentication auth) throws Exception {
        String group=group(auth); requireInvoice(ref,group);
        return attachment(pdf.build(details.getFullInvoiceDetail(ref.invoiceId(),ref.fyear(),group)),"application/pdf",safeName(ref)+".pdf");
    }
    @PostMapping("/zip")
    public ResponseEntity<byte[]> zip(@RequestBody List<Reference> refs,Authentication auth) throws Exception {
        if(refs==null || refs.isEmpty() || refs.size()>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select 1 to 100 invoices");
        String group=group(auth); ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(bytes)) {
            int index=1;
            for(Reference ref:new LinkedHashSet<>(refs)) {
                requireInvoice(ref,group);
                zip.putNextEntry(new ZipEntry(index++ + "-"+safeName(ref)+".pdf"));
                zip.write(pdf.build(details.getFullInvoiceDetail(ref.invoiceId(),ref.fyear(),group))); zip.closeEntry();
            }
        }
        return attachment(bytes.toByteArray(),"application/zip","invoices.zip");
    }
    private static String safeName(Reference ref) { return "invoice-"+ref.fyear()+"-"+ref.invoiceId().replaceAll("[^A-Za-z0-9_-]","_"); }
    private static ResponseEntity<byte[]> attachment(byte[] bytes,String type,String filename) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type)).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+filename+"\"").body(bytes);
    }
}
