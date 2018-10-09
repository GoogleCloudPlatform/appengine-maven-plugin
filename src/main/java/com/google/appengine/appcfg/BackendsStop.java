/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stop the specified backend.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal backends_stop
 * @execute phase="package"
 */
public class BackendsStop extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Stop Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Stop Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("stop", getAppDir());
  }

}
