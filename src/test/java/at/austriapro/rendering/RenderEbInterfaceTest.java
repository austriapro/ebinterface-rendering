/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 AUSTRIAPRO
 */
package at.austriapro.rendering;


import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.StreamHelper;

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
    final BaseRenderer renderer = new BaseRenderer();

    final InputStream isReport =
        RenderEbInterfaceTest.class.getClassLoader()
            .getResourceAsStream("reports/ebInterface_5p0_QR_sample.jrxml");

    final InputStream
        isXML =
        RenderEbInterfaceTest.class.getClassLoader()
            .getResourceAsStream("xml/ebinterface_5p0_sample.xml");

    final InputStream
        logo =
        RenderEbInterfaceTest.class.getClassLoader().getResourceAsStream("logos/logo.jpg");

    final byte[]
        renderedPdf =
        renderer.renderReport(StreamHelper.getAllBytes(isReport), StreamHelper.getAllBytes(isXML), logo);

    final File file = File.createTempFile("ebInterface", ".pdf");
    SimpleFileIO.writeFile (file, renderedPdf);

    LOG.info("File written to " + file.getAbsolutePath());

    if (OPENFILES) {
      try {
        RenderUtil.openFile(file);

      } catch (final Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }
}
