package at.austriapro.rendering.schema;

import com.itextpdf.text.xml.xmp.XmpSchema;

/**
 * @author Robert Gruendler <r.gruendler@edistream.com> Date: 9/24/13 Time: 11:30 AM
 * @see "http://www.pdfa.org/wp-content/uploads/2012/11/Leitfaden-PDFA3-und-ZUGFeRD-v201307011.pdf"
 */
public class ZugferdSchema extends XmpSchema {

  public static final String DEFAULT_XPATH_ID = "zf";

  public static final String DEFAULT_XPATH_URI = "urn:ferd:pdfa:invoice:rc#";

  public static final String DOCUMENT_ID = "zf:DocumentID";

  public static final String DOCUMENT_DATE = "zf:DocumentDate";

  public static final String DOCUMENT_TYPE = "zf:DocumentType";

  public static final String DOCUMENT_FILENAME = "zf:DocumentFileName";

  public static final String VERSION = "zf:Version";

  public static final String CONFORMANCE_LEVEL = "zf:ConformanceLevel";

  /**
   * Constructs a ZuGFeRD XMP schema.
   */
  public ZugferdSchema() {
    super("xmlns:" + DEFAULT_XPATH_ID + "=\"" + DEFAULT_XPATH_URI + "\"");
    setProperty(DOCUMENT_TYPE, "INVOICE");
    setProperty(CONFORMANCE_LEVEL, "BASIC");
    setProperty(VERSION, "0.11");
  }

  public ZugferdSchema setFilename(String filename) {
    setProperty(DOCUMENT_FILENAME, filename);
    return this;
  }

  public ZugferdSchema setDocumentDate(String date) {
    setProperty(DOCUMENT_DATE, date);
    return this;
  }

  public ZugferdSchema setDocumentID(String ID) {
    setProperty(DOCUMENT_ID, ID);
    return this;
  }
}
