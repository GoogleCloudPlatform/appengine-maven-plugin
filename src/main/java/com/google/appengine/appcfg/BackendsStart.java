/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Start the specified backend.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal backends_start
 * @execute phase="package"
 */
public class BackendsStart extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Start Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Start Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("start", getAppDir());
  }

}
