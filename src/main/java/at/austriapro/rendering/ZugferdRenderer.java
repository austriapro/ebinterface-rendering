package at.austriapro.rendering;

import java.io.InputStream;

import at.austriapro.rendering.postprocessor.ZugferdPostprocessor;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Created by Paul on 29.10.2015.
 */
public class ZugferdRenderer extends BaseRenderer {
  public ZugferdRenderer() {
    super();
    reportTitle = "ZUGFeRD";
    xmlDatePattern = "yyyy-MM-dd'T'HH:mm:ss";
  }

  @Override
  public byte[] renderReport(final byte[] jasperTemplate, final byte[] zugferdXML, final InputStream logo)
      throws Exception {
    final byte[] pdfA1A = super.renderReport(jasperTemplate, zugferdXML, logo);

    final ZugferdPostprocessor zProcessor = new ZugferdPostprocessor();
    final byte[] pdfA3A = zProcessor.embedXMLtoPDF(pdfA1A, zugferdXML);

    return pdfA3A;
  }

  @Override
  public byte[] renderReport(final JasperReport jasperReport, final byte[] zugferdXML, final InputStream logo)
      throws Exception {
    final byte[] pdfA1A = super.renderReport(jasperReport, zugferdXML, logo);

    final ZugferdPostprocessor zProcessor = new ZugferdPostprocessor();
    final byte[] pdfA3A = zProcessor.embedXMLtoPDF(pdfA1A, zugferdXML);

    return pdfA3A;
  }
}
