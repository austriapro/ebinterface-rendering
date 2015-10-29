package at.austriapro.rendering;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Paul on 29.10.2015.
 */
public class ZugferdRendererTest {

  @Test
  public void testJasperRenderer() throws Exception{
    ZugferdRenderer zRenderer = new ZugferdRenderer();

    InputStream isReport = ZugferdRendererTest.class.getClassLoader()
        .getResourceAsStream(
            "reports/zugferd_sample.jrxml");

    InputStream isXML = ZugferdRendererTest.class.getClassLoader()
        .getResourceAsStream(
            "xml/zugferd.xml");

    InputStream logo = ZugferdRendererTest.class.getClassLoader()
        .getResourceAsStream(
            "logos/logo.jpg");

    byte[] zugferdPdf = zRenderer.renderReport(IOUtils.toByteArray(isReport), IOUtils.toByteArray(isXML), logo);

    File file = File.createTempFile("zugferd", ".pdf");
    FileUtils.writeByteArrayToFile(file, zugferdPdf);
    System.err.println("File written to " + file.getAbsolutePath());
  }

}
