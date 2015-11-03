package at.austriapro.rendering;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

import at.austriapro.rendering.util.OSDetector;

/**
 * Created by Paul on 29.10.2015.
 */
public class RenderZUGFeRDTest {

  private static boolean OPENFILES = true;

  private static final Logger LOG = LoggerFactory.getLogger(RenderZUGFeRDTest.class.getName());

  @Test
  public void testJasperRenderer() throws Exception {
    ZugferdRenderer zRenderer = new ZugferdRenderer();

    InputStream isReport = RenderZUGFeRDTest.class.getClassLoader()
        .getResourceAsStream(
            "reports/zugferd_sample.jrxml");

    InputStream isXML = RenderZUGFeRDTest.class.getClassLoader()
        .getResourceAsStream(
            "xml/zugferd.xml");

    InputStream logo = RenderZUGFeRDTest.class.getClassLoader()
        .getResourceAsStream(
            "logos/logo.jpg");

    byte[]
        zugferdPdf =
        zRenderer.renderReport(IOUtils.toByteArray(isReport), IOUtils.toByteArray(isXML), logo);

    File file = File.createTempFile("zugferd", ".pdf");
    FileUtils.writeByteArrayToFile(file, zugferdPdf);

    LOG.info("File written to " + file.getAbsolutePath());

    openFile(file);

  }


  /**
   * Opens a file, if the operating system is either windows or mac (= local development)
   */
  private boolean openFile(File file) {

    // change the flag for easier local development of pdf files
    if (OPENFILES == false) {
      return false;
    }

    try {
      if (OSDetector.isWindows()) {
        Runtime.getRuntime().exec(new String[]
                                      {"rundll32", "url.dll,FileProtocolHandler",
                                       file.getAbsolutePath()});
        return true;
      } else if (OSDetector.isMac()) {
        Runtime.getRuntime().exec(new String[]{"/usr/bin/open",
                                               file.getAbsolutePath()});
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return false;
    }
  }

}
