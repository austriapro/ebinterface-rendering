/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2021 AUSTRIAPRO
 */
package at.austriapro.rendering;


import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;


/**
 * Created by paul on 12/18/15.
 */
public class Translator {

  private static final Logger LOG = LoggerFactory.getLogger(Translator.class);

  private static final I18n i18n;

  static {
    i18n = I18nFactory.getI18n(Translator.class, "Messages");
  }

  /**
   * Translate the given key. In case to translation was not possible (because no key entry could be
   * found in the properties file, the I18n returns the key again - in this case log an error, but
   * continue with the key-value as translation
   * @param key Key
   * @param locale Locale to use
   * @return Never <code>null</code>
   */
  public static String translate(final String key, final Locale locale) {
    if (StringUtils.isEmpty(key)) {
      return "";
    }

    if (locale == null) {
      throw new IllegalArgumentException("Unable to proceed with empty locale");
    }

    i18n.setLocale(locale);

    String translation;
    final String transKey = key.toUpperCase();

    LOG.debug("Translating key " + transKey);
    translation = i18n.tr(transKey);

    if (translation.equals(transKey)) {
      LOG.error("Unable to translate key {} - now taking the key value as translation", transKey);
    }
    return translation;
  }
}
