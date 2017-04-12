/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class BackendsStop extends AbstractAppCfgMojo {

  /**
   * Stop the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_stop
   * @execute phase="package"
   */
  public static class BackendsStopFork extends BackendsStop {}

  /**
   * Stop the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_stop_no_fork
   */
  public static class BackendsStopNoFork extends BackendsStop {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Stop Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Stop Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("stop", appDir);
  }

}
