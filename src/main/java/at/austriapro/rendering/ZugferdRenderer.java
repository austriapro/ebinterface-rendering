package at.austriapro.rendering;

import net.sf.jasperreports.engine.JasperReport;

import java.io.InputStream;

import at.austriapro.rendering.postprocessor.ZugferdPostprocessor;

/**
 * Created by Paul on 29.10.2015.
 */
public class ZugferdRenderer extends BaseRenderer {

  private static final String iccProfilePath = "sRGB_IEC61966-2-1_black_scaled.icc";

  public ZugferdRenderer() {
    super();
    reportTitle = "ZUGFeRD";
    xmlDatePattern = "yyyy-MM-dd'T'HH:mm:ss";
  }

  @Override
  public byte[] renderReport(byte[] jasperTemplate, byte[] zugferdXML, InputStream logo)
      throws Exception {
    byte[] pdfA1A = super.renderReport(jasperTemplate, zugferdXML, logo);

    ZugferdPostprocessor zProcessor = new ZugferdPostprocessor();
    byte[] pdfA3A = zProcessor.embedXMLtoPDF(pdfA1A, zugferdXML);

    return pdfA3A;
  }

  @Override
  public byte[] renderReport(JasperReport jasperReport, byte[] zugferdXML, InputStream logo)
      throws Exception {
    byte[] pdfA1A = super.renderReport(jasperReport, zugferdXML, logo);

    ZugferdPostprocessor zProcessor = new ZugferdPostprocessor();
    byte[] pdfA3A = zProcessor.embedXMLtoPDF(pdfA1A, zugferdXML);

    return pdfA3A;
  }
}
