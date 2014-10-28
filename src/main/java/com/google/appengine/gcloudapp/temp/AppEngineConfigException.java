// Copyright 2008 Google Inc. All Rights Reserved.
package com.google.appengine.gcloudapp.temp;

/**
 * Describes a problem with the configuration of App Engine.
 *
 */
public class AppEngineConfigException extends RuntimeException {

  public AppEngineConfigException(String message) {
    super(message);
  }

  public AppEngineConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppEngineConfigException(Throwable cause) {
    super(cause);
  }
}
