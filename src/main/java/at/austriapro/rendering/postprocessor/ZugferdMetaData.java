/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 AUSTRIAPRO
 */
package at.austriapro.rendering.postprocessor;

/**
 * Metadata of a ZUGFeRD file
 */
public class ZugferdMetaData {

  private String title;

  private String profile;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

}
