/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class BackendsConfigure extends AbstractAppCfgMojo {

  /**
   * Configure the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_configure
   * @execute phase="package"
   */
  public static class BackendsConfigureFork extends BackendsConfigure {}

  /**
   * Configure the specified backend.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_configure_no_fork
   */
  public static class BackendsConfigureNoFork extends BackendsConfigure {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Configure Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Configuring Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("configure", appDir);
  }

}
