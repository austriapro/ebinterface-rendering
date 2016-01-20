package at.austriapro.rendering;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.type.PdfaConformanceEnum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Paul on 29.10.2015.
 */
public class BaseRenderer {

  protected String xmlDatePattern = "yyyy-MM-dd";
  protected String xmlNumberPattern = "#,###,##0.##";
  protected Locale xmlLocale = Locale.ENGLISH;

  protected Locale reportLocale = Locale.GERMAN;
  protected String reportLanguage = "de";
  protected String reportTitle = "Title";
  protected String reportAuthor = "Mustermann GmbH";
  protected String reportSubject = "Invoice";

  private static final String iccProfilePath = "sRGB_IEC61966-2-1_black_scaled.icc";

  public byte[] renderReport(byte[] jasperTemplate, byte[] xML, InputStream logo)
      throws Exception {
    JasperDesign jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(jasperTemplate));
    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

    return renderReport(jasperReport, xML, logo);
  }


  public byte[] renderReport(JasperReport jasperReport, byte[] xML, InputStream logo)
      throws Exception {
    if (jasperReport == null) {
      throw new RuntimeException("jasperReport cannot be rendered!");
    }

    HashMap<String, Object> parameters = new HashMap<String, Object>();

    //Set report locale
    parameters.put(JRParameter.REPORT_LOCALE, reportLocale);
    parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, JRXmlUtils
        .parse(new ByteArrayInputStream(xML)));
    parameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, this.xmlDatePattern);
    parameters.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, this.xmlNumberPattern);
    parameters.put(JRXPathQueryExecuterFactory.XML_LOCALE, this.xmlLocale);
    parameters.put("senderLogo", logo);

    JasperPrint jasperPrint = JasperFillManager.fillReport(
        jasperReport,
        parameters
    );

    JRPdfExporter exporter = new JRPdfExporter();
    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

    SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();

    configuration.setTagLanguage(reportLanguage);
    configuration.setMetadataAuthor(reportAuthor);

    //configuration.setMetadataSubject(reportSubject);
    //configuration.setMetadataTitle(reportTitle);

    // Include structure tags for PDF/A-1a compliance; unnecessary for PDF/A-1b
    configuration.setTagged(true);

    configuration.setPdfaConformance(PdfaConformanceEnum.PDFA_1A);

    //Set color Profile
    configuration.setIccProfilePath(iccProfilePath);

    exporter.setConfiguration(configuration);

    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(bOut));
    exporter.exportReport();

    byte[] pdfA1A = bOut.toByteArray();

    return pdfA1A;
  }

  public String getReportSubject() {
    return reportSubject;
  }

  public void setReportSubject(String reportSubject) {
    this.reportSubject = reportSubject;
  }

  public String getXmlDatePattern() {
    return xmlDatePattern;
  }

  public void setXmlDatePattern(String xmlDatePattern) {
    this.xmlDatePattern = xmlDatePattern;
  }

  public String getXmlNumberPattern() {
    return xmlNumberPattern;
  }

  public void setXmlNumberPattern(String xmlNumberPattern) {
    this.xmlNumberPattern = xmlNumberPattern;
  }

  public Locale getXmlLocale() {
    return xmlLocale;
  }

  public void setXmlLocale(Locale xmlLocale) {
    this.xmlLocale = xmlLocale;
  }

  public Locale getReportLocale() {
    return reportLocale;
  }

  public void setReportLocale(Locale reportLocale) {
    this.reportLocale = reportLocale;
  }

  public String getReportLanguage() {
    return reportLanguage;
  }

  public void setReportLanguage(String reportLanguage) {
    this.reportLanguage = reportLanguage;
  }

  public String getReportTitle() {
    return reportTitle;
  }

  public void setReportTitle(String reportTitle) {
    this.reportTitle = reportTitle;
  }

  public String getReportAuthor() {
    return reportAuthor;
  }

  public void setReportAuthor(String reportAuthor) {
    this.reportAuthor = reportAuthor;
  }
}
