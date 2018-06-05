/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class BackendsStart extends AbstractAppCfgMojo {

  /**
   * Start the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_start
   * @execute phase="package"
   */
  public static class BackendsStartFork extends BackendsStart {}

  /**
   * Start the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_start_no_fork
   */
  public static class BackendsStartNoFork extends BackendsStart {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Start Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Start Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("start", appDir);
  }

}
