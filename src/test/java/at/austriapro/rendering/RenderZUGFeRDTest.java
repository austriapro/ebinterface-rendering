/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 AUSTRIAPRO
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
public class RenderZUGFeRDTest {

  private static final boolean OPENFILES = true;

  private static final Logger LOG = LoggerFactory.getLogger(RenderZUGFeRDTest.class.getName());

  @Test
  public void testJasperRenderer() throws Exception {
    final ZugferdRenderer zRenderer = new ZugferdRenderer();

    final InputStream isReport =
        RenderZUGFeRDTest.class.getClassLoader()
            .getResourceAsStream("reports/zugferd_sample.jrxml");

    final InputStream
        isXML =
        RenderZUGFeRDTest.class.getClassLoader().getResourceAsStream("xml/zugferd_demo.xml");

    final InputStream
        logo =
        RenderZUGFeRDTest.class.getClassLoader().getResourceAsStream("logos/logo.jpg");

    final byte[]
        zugferdPdf =
        zRenderer.renderReport(StreamHelper.getAllBytes(isReport), StreamHelper.getAllBytes(isXML), logo);

    final File file = File.createTempFile("zugferd", ".pdf");
    SimpleFileIO.writeFile (file, zugferdPdf);

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
