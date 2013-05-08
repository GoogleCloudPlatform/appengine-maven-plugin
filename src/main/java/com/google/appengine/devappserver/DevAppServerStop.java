/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.devappserver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stops the App Engine development server.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal devserver_stop
 * @execute phase="package"
 * @threadSafe false
 */
public class DevAppServerStop extends AbstractDevAppServerMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Stopping the Development Server");
    getLog().info("");

    stopDevAppServer();
  }

}
