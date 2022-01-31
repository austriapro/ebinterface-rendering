/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 AUSTRIAPRO
 */
package at.austriapro.rendering.util;

import java.io.File;
import java.io.IOException;

/**
 * Created by paul on 12/11/15.
 */
public class RenderUtil {

  /*
   * Opens a file, if the operating system is either windows or mac (= local development)
   */
  public static void openFile(File file) throws IOException {
    if (OSDetector.isWindows()) {
      Runtime.getRuntime()
          .exec(new String[]{"rundll32", "url.dll,FileProtocolHandler",
                             file.getAbsolutePath()});
    } else if (OSDetector.isMac()) {
      Runtime.getRuntime().exec(new String[]{"/usr/bin/open", file.getAbsolutePath()});

    } else if (OSDetector.isLinux()) {
      java.awt.Desktop.getDesktop().open(file);
    }
  }
}
