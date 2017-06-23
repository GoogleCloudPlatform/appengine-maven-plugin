/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Configure the specified backend.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal backends_configure
 * @execute phase="package"
 */
public class BackendsConfigure extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Configure Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Configuring Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("configure");
  }

}
