package at.austriapro.rendering.postprocessor;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import at.austriapro.rendering.schema.XMPSchemaPDFAExtensions;
import at.austriapro.rendering.schema.XMPSchemaZugferd;

public class ZugferdPostprocessor {

  private static final String SAXON_OBJECT_MODEL_URI = "http://saxon.sf.net/jaxp/xpath/om";

  private static DocumentBuilderFactory DBF;
  private static XPathExpression ZUGFERD_TYPE_XPATH;
  private static XPathExpression ZUGFERD_DOCUMENT_NUMBER_XPATH;

  static {
    //Ensure that factories are only created once
    DBF = DocumentBuilderFactory.newInstance();
    XPathFactory XPF;
    try {
      XPF = XPathFactory.newInstance(SAXON_OBJECT_MODEL_URI);
    } catch (XPathFactoryConfigurationException e) {
      XPF = XPathFactory.newInstance();
    }

    //Create XPath expression only once, since it remains unaltered and is reused
    XPath xpath = XPF.newXPath();
    try {
      ZUGFERD_TYPE_XPATH = xpath.compile(
          "/*:CrossIndustryDocument/*:SpecifiedExchangedDocumentContext/*:GuidelineSpecifiedDocumentContextParameter/*:ID");
      ZUGFERD_DOCUMENT_NUMBER_XPATH =
          xpath.compile("/*:CrossIndustryDocument/*:HeaderExchangedDocument/*:ID");
    } catch (XPathExpressionException e) {
      throw new RuntimeException(
          "Unable to compile XPath expression for determining the type of ZUGFeRD or number of ZUGFeRD document", e);
    }
  }

  public byte[] embedXMLtoPDF(byte[] pdf, byte[] xml) throws Exception {
    PDDocument pdfBoxDocument = PDDocument.load(new ByteArrayInputStream(pdf));
    pdfBoxDocument.getDocumentInformation()
        .setProducer("austriapro/ebInterface-ZUGFeRD");
    ZugferdMetaData zMetadata = getZugferdMetaData(xml);
    PDDocumentCatalog catalog = makeA3compliant(pdfBoxDocument, zMetadata);
    attachFile(pdfBoxDocument, zMetadata, xml);
    removeCidSet(catalog);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    pdfBoxDocument.save(bos);
    pdfBoxDocument.close();

    pdfBoxDocument.close();

    return bos.toByteArray();
  }

  /**
   * Retrieve the type of ZUGFeRD and the ZUGFeRD document number
   */
  public ZugferdMetaData getZugferdMetaData(byte[] xml) throws Exception {
    ZugferdMetaData zMetadata = new ZugferdMetaData();

    Document doc = DBF.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    String zugferdType = (String) ZUGFERD_TYPE_XPATH.evaluate(doc, XPathConstants.STRING);

    if (zugferdType.contains("basic")) {
      zMetadata.setProfile("BASIC");
    } else if (zugferdType.contains("comfort")) {
      zMetadata.setProfile("COMFORT");
    } else if (zugferdType.contains("extended")) {
      zMetadata.setProfile("EXTENDED");
    } else {
      throw new Exception("XML contains no valid ZUGFeRD profile");
    }

    String
        documentNumber =
        (String) ZUGFERD_DOCUMENT_NUMBER_XPATH.evaluate(doc, XPathConstants.STRING);

    if (documentNumber == null) {
      throw new Exception("XML contains no valid invoice number");
    } else {
      zMetadata.setTitle("Invoice: " + documentNumber);
    }

    return zMetadata;
  }

  private void attachFile(PDDocument doc, ZugferdMetaData zMetadata, byte[] file)
      throws IOException {

    PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

    String filename = "ZUGFeRD-invoice.xml";

    PDComplexFileSpecification fs = new PDComplexFileSpecification();
    fs.setFile(filename);
    COSDictionary dict = fs.getCOSDictionary();
    dict.setName("AFRelationship", "Alternative");

    dict.setString("UF", filename);

    InputStream is = new ByteArrayInputStream(file);
    PDEmbeddedFile ef = new PDEmbeddedFile(doc, is);
    ef.setSubtype("text/xml");

    ef.setModDate(GregorianCalendar.getInstance());

    ef.setSize(file.length);
    ef.setCreationDate(new GregorianCalendar());
    fs.setEmbeddedFile(ef);

    PDDocumentCatalog catalog = doc.getDocumentCatalog();
    catalog.setVersion("1.7");

    // now add the entry to the embedded file tree and set in the document.
    efTree.setNames(Collections.singletonMap(zMetadata.getTitle(), fs));

    PDDocumentNameDictionary names = new PDDocumentNameDictionary(catalog);
    names.setEmbeddedFiles(efTree);
    catalog.setNames(names);

    // AF entry (Array) in catalog with the FileSpec
    COSArray cosArray = (COSArray) catalog.getCOSDictionary().getItem("AF");
    if (cosArray == null) {
      cosArray = new COSArray();
    }
    cosArray.add(fs);
    catalog.getCOSDictionary().setItem("AF", cosArray);

  }

  /**
   * This removes the CIDset from all fonts in the document. If we don't do this, we are not A-3
   * compliant, due to the following error in acrobar reader preflight validation:
   *
   * "CIDset in subset font is incomplete (font contains glyphs that are not listed)"
   *
   * @see "http://stackoverflow.com/a/31470898/965338"
   */
  private void removeCidSet(PDDocumentCatalog catalog) {

    COSName cidSet = COSName.getPDFName("CIDSet");

    for (Object object : catalog.getAllPages()) {
      if (object instanceof PDPage) {

        PDPage page = (PDPage) object;
        Map<String, PDFont> fonts = page.getResources().getFonts();
        Iterator<String> iterator = fonts.keySet().iterator();

        while (iterator.hasNext()) {
          PDFont pdFont = fonts.get(iterator.next());
          if (pdFont instanceof PDType0Font) {
            PDType0Font typedFont = (PDType0Font) pdFont;
            if (typedFont.getDescendantFont() instanceof PDCIDFontType2Font) {
              PDCIDFontType2Font f = (PDCIDFontType2Font) typedFont.getDescendantFont();
              PDFontDescriptor fontDescriptor = f.getFontDescriptor();
              if (fontDescriptor instanceof PDFontDescriptorDictionary) {
                PDFontDescriptorDictionary fontDict = (PDFontDescriptorDictionary) fontDescriptor;
                fontDict.getCOSDictionary().removeItem(cidSet);
              }
            }
          }
        }
      }
    }
  }

  private PDDocumentCatalog makeA3compliant(PDDocument doc, ZugferdMetaData zMetadata)
      throws IOException, TransformerException {
    PDDocumentCatalog cat = doc.getDocumentCatalog();
    PDMetadata metadata = new PDMetadata(doc);
    cat.setMetadata(metadata);

    XMPMetadata xmp = new XMPMetadata();
    XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
    xmp.addSchema(pdfaid);

    XMPSchemaDublinCore dc = xmp.addDublinCoreSchema();
    String creator = "austriapro/ebInterface-ZUGFeRD";
    String producer = "austriapro/ebInterface-ZUGFeRD";
    dc.addCreator(creator);
    dc.setAbout("");

    XMPSchemaBasic xsb = xmp.addBasicSchema();
    xsb.setAbout("");
    xsb.setCreatorTool(creator);
    xsb.setCreateDate(GregorianCalendar.getInstance());

    PDDocumentInformation pdi = new PDDocumentInformation();
    pdi.setProducer(producer);
    pdi.setAuthor(creator);
    doc.setDocumentInformation(pdi);

    XMPSchemaPDF pdf = xmp.addPDFSchema();
    pdf.setProducer(producer);
    pdf.setAbout("");

    PDMarkInfo markinfo = new PDMarkInfo();
    markinfo.setMarked(true);
    doc.getDocumentCatalog().setMarkInfo(markinfo);

    pdfaid.setPart(3);
    pdfaid.setConformance("A");
    pdfaid.setAbout("");

    addZugferdXMP(xmp, zMetadata);
    metadata.importXMPMetadata(xmp);

    return cat;
  }

  // based on Mustangproject's ZUGFeRD implementation (http://www.mustangproject.org/)
  // org.mustangproject.ZUGFeRD.ZUGFeRDExporter
  private void addZugferdXMP(XMPMetadata metadata, ZugferdMetaData zMetadata) {
    XMPSchemaZugferd zf = new XMPSchemaZugferd(metadata, zMetadata.getProfile());
    zf.setAbout("");
    metadata.addSchema(zf);

    XMPSchemaPDFAExtensions pdfaex = new XMPSchemaPDFAExtensions(metadata);
    pdfaex.setAbout("");
    metadata.addSchema(pdfaex);
  }

  public String getOutputDocumentType() {
    return "ZUGFeRD 1.0 PDF/A-3";
  }
}
