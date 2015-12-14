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
public class RenderZUGFeRDTest {

    private static boolean OPENFILES = true;

    private static final Logger LOG = LoggerFactory.getLogger(RenderZUGFeRDTest.class.getName());

    @Test
    public void testJasperRenderer() throws Exception {
        ZugferdRenderer zRenderer = new ZugferdRenderer();

        InputStream isReport =
            RenderZUGFeRDTest.class.getClassLoader()
                .getResourceAsStream("reports/zugferd_sample.jrxml");

        InputStream
            isXML =
            RenderZUGFeRDTest.class.getClassLoader().getResourceAsStream("xml/zugferd_demo.xml");

        InputStream
            logo =
            RenderZUGFeRDTest.class.getClassLoader().getResourceAsStream("logos/logo.jpg");

        byte[]
            zugferdPdf =
            zRenderer.renderReport(IOUtils.toByteArray(isReport), IOUtils.toByteArray(isXML), logo);

        File file = File.createTempFile("zugferd", ".pdf");
        FileUtils.writeByteArrayToFile(file, zugferdPdf);

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
