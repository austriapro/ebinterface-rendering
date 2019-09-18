package at.austriapro.rendering.postprocessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import at.austriapro.rendering.schema.XMPSchemaPDFAExtensions;
import at.austriapro.rendering.schema.XMPSchemaZugferd;

public class ZugferdPostprocessor {
  private static final Logger LOG = LoggerFactory.getLogger (ZugferdPostprocessor.class);

  private static final DocumentBuilderFactory DBF;
  private static final XPathExpression ZUGFERD_TYPE_XPATH;
  private static final XPathExpression ZUGFERD_DOCUMENT_NUMBER_XPATH;

  static {
    //Ensure that factories are only created once
    DBF = DocumentBuilderFactory.newInstance();
    XPathFactory XPF;
    try {
      /*
       * Quote from http://www.saxonica.com/documentation/#!xpath-api/jaxp-xpath/factory:
       * If you want to use Saxon as your XPath implementation, you must instantiate the class net.sf.saxon.xpath.XPathFactoryImpl directly.
       */
      XPF = new net.sf.saxon.xpath.XPathFactoryImpl ();
    } catch (final Exception e) {
      LOG.warn ("Failed to instantiate Saxon OM", e);
      XPF = XPathFactory.newInstance();
    }

    //Create XPath expression only once, since it remains unaltered and is reused
    final XPath xpath = XPF.newXPath();
    try {
      ZUGFERD_TYPE_XPATH = xpath.compile(
          "/*:CrossIndustryDocument/*:SpecifiedExchangedDocumentContext/*:GuidelineSpecifiedDocumentContextParameter/*:ID");
      ZUGFERD_DOCUMENT_NUMBER_XPATH =
          xpath.compile("/*:CrossIndustryDocument/*:HeaderExchangedDocument/*:ID");
    } catch (final XPathExpressionException e) {
      throw new RuntimeException(
          "Unable to compile XPath expression for determining the type of ZUGFeRD or number of ZUGFeRD document", e);
    }
  }

  public byte[] embedXMLtoPDF(final byte[] pdf, final byte[] xml) throws Exception {
    try (final PDDocument pdfBoxDocument = PDDocument.load(new ByteArrayInputStream(pdf))) {
      pdfBoxDocument.getDocumentInformation()
          .setProducer("austriapro/ebInterface-ZUGFeRD");
      final ZugferdMetaData zMetadata = getZugferdMetaData(xml);
      final PDDocumentCatalog catalog = makeA3compliant(pdfBoxDocument, zMetadata);
      attachFile(pdfBoxDocument, zMetadata, xml);
      removeCidSet(catalog);

      try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()){
        pdfBoxDocument.save(bos);
        return bos.toByteArray();
      }
    }
  }

  /**
   * Retrieve the type of ZUGFeRD and the ZUGFeRD document number
   * @param xml byte array with XML document to retrieve the metadata from
   * @return Metadata
   * @throws Exception in case of error
   */
  public ZugferdMetaData getZugferdMetaData(final byte[] xml) throws Exception {
    final ZugferdMetaData zMetadata = new ZugferdMetaData();

    final Document doc = DBF.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    final String zugferdType = (String) ZUGFERD_TYPE_XPATH.evaluate(doc, XPathConstants.STRING);

    if (zugferdType.contains("basic")) {
      zMetadata.setProfile("BASIC");
    } else if (zugferdType.contains("comfort")) {
      zMetadata.setProfile("COMFORT");
    } else if (zugferdType.contains("extended")) {
      zMetadata.setProfile("EXTENDED");
    } else {
      throw new IllegalArgumentException("XML contains no valid ZUGFeRD profile");
    }

    final String
        documentNumber =
        (String) ZUGFERD_DOCUMENT_NUMBER_XPATH.evaluate(doc, XPathConstants.STRING);
    if (documentNumber == null) {
      throw new IllegalArgumentException("XML contains no valid invoice number");
    }

    zMetadata.setTitle("Invoice: " + documentNumber);
    return zMetadata;
  }

  private void attachFile(final PDDocument doc, final ZugferdMetaData zMetadata, final byte[] file)
      throws IOException {

    final PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

    final String filename = "ZUGFeRD-invoice.xml";

    final PDComplexFileSpecification fs = new PDComplexFileSpecification();
    fs.setFile(filename);
    final COSDictionary dict = fs.getCOSDictionary();
    dict.setName("AFRelationship", "Alternative");

    dict.setString("UF", filename);

    final InputStream is = new ByteArrayInputStream(file);
    final PDEmbeddedFile ef = new PDEmbeddedFile(doc, is);
    ef.setSubtype("text/xml");

    ef.setModDate(Calendar.getInstance());

    ef.setSize(file.length);
    ef.setCreationDate(new GregorianCalendar());
    fs.setEmbeddedFile(ef);

    final PDDocumentCatalog catalog = doc.getDocumentCatalog();
    catalog.setVersion("1.7");

    // now add the entry to the embedded file tree and set in the document.
    efTree.setNames(Collections.singletonMap(zMetadata.getTitle(), fs));

    final PDDocumentNameDictionary names = new PDDocumentNameDictionary(catalog);
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
  private void removeCidSet(final PDDocumentCatalog catalog) {

    final COSName cidSet = COSName.getPDFName("CIDSet");

    for (final Object object : catalog.getAllPages()) {
      if (object instanceof PDPage) {

        final PDPage page = (PDPage) object;
        final Map<String, PDFont> fonts = page.getResources().getFonts();
        final Iterator<String> iterator = fonts.keySet().iterator();

        while (iterator.hasNext()) {
          final PDFont pdFont = fonts.get(iterator.next());
          if (pdFont instanceof PDType0Font) {
            final PDType0Font typedFont = (PDType0Font) pdFont;
            if (typedFont.getDescendantFont() instanceof PDCIDFontType2Font) {
              final PDCIDFontType2Font f = (PDCIDFontType2Font) typedFont.getDescendantFont();
              final PDFontDescriptor fontDescriptor = f.getFontDescriptor();
              if (fontDescriptor instanceof PDFontDescriptorDictionary) {
                final PDFontDescriptorDictionary fontDict = (PDFontDescriptorDictionary) fontDescriptor;
                fontDict.getCOSDictionary().removeItem(cidSet);
              }
            }
          }
        }
      }
    }
  }

  private static PDDocumentCatalog makeA3compliant(final PDDocument doc, final ZugferdMetaData zMetadata)
      throws IOException, TransformerException {
    final PDDocumentCatalog cat = doc.getDocumentCatalog();
    final PDMetadata metadata = new PDMetadata(doc);
    cat.setMetadata(metadata);

    final XMPMetadata xmp = new XMPMetadata();
    final XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
    xmp.addSchema(pdfaid);

    final XMPSchemaDublinCore dc = xmp.addDublinCoreSchema();
    final String creator = "austriapro/ebInterface-ZUGFeRD";
    final String producer = "austriapro/ebInterface-ZUGFeRD";
    dc.addCreator(creator);
    dc.setAbout("");

    final XMPSchemaBasic xsb = xmp.addBasicSchema();
    xsb.setAbout("");
    xsb.setCreatorTool(creator);
    xsb.setCreateDate(Calendar.getInstance());

    final PDDocumentInformation pdi = new PDDocumentInformation();
    pdi.setProducer(producer);
    pdi.setAuthor(creator);
    doc.setDocumentInformation(pdi);

    final XMPSchemaPDF pdf = xmp.addPDFSchema();
    pdf.setProducer(producer);
    pdf.setAbout("");

    final PDMarkInfo markinfo = new PDMarkInfo();
    markinfo.setMarked(true);
    doc.getDocumentCatalog().setMarkInfo(markinfo);

    pdfaid.setPart(Integer.valueOf(3));
    pdfaid.setConformance("A");
    pdfaid.setAbout("");

    addZugferdXMP(xmp, zMetadata);
    xmp.save(metadata.createOutputStream ());

    return cat;
  }

  // based on Mustangproject's ZUGFeRD implementation (http://www.mustangproject.org/)
  // org.mustangproject.ZUGFeRD.ZUGFeRDExporter
  private static void addZugferdXMP(final XMPMetadata metadata, final ZugferdMetaData zMetadata) {
    final XMPSchemaZugferd zf = new XMPSchemaZugferd(metadata, zMetadata.getProfile());
    zf.setAbout("");
    metadata.addSchema(zf);

    final XMPSchemaPDFAExtensions pdfaex = new XMPSchemaPDFAExtensions(metadata);
    pdfaex.setAbout("");
    metadata.addSchema(pdfaex);
  }

  public String getOutputDocumentType() {
    return "ZUGFeRD 1.0 PDF/A-3";
  }
}
