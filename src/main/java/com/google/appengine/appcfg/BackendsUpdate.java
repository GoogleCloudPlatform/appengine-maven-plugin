/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class BackendsUpdate extends AbstractAppCfgMojo {

  /**
   * Update the specified backend or all backends.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_update
   * @execute phase="package"
   */
  public static class BackendsUpdateFork extends BackendsUpdate {}

  /**
   * Update the specified backend or all backends.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_update_no_fork
   */
  public static class BackendsUpdateNoFork extends BackendsUpdate {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("update", appDir);
  }

}
