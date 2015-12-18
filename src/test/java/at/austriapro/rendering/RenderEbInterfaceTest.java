package at.austriapro.rendering;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

import at.austriapro.rendering.util.RenderUtil;

/**
 * Created by Paul on 29.10.2015.
 */
public class RenderEbInterfaceTest {

  private static boolean OPENFILES = true;

  private static final Logger
      LOG =
      LoggerFactory.getLogger(RenderEbInterfaceTest.class.getName());

  @Test
  public void testJasperRenderer() throws Exception {
    BaseRenderer renderer = new BaseRenderer();

    InputStream isReport =
        RenderEbInterfaceTest.class.getClassLoader()
            .getResourceAsStream("reports/ebInterface_sample.jrxml");

    InputStream
        isXML =
        RenderEbInterfaceTest.class.getClassLoader()
            .getResourceAsStream("xml/ebInterface_4p1_demo.xml");

    InputStream
        logo =
        RenderEbInterfaceTest.class.getClassLoader().getResourceAsStream("logos/logo.jpg");

    byte[]
        renderedPdf =
        renderer.renderReport(IOUtils.toByteArray(isReport), IOUtils.toByteArray(isXML), logo);

    File file = File.createTempFile("ebInterface", ".pdf");
    FileUtils.writeByteArrayToFile(file, renderedPdf);

    LOG.info("File written to " + file.getAbsolutePath());

    if (OPENFILES) {
      try {
        RenderUtil.openFile(file);

      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }
}
