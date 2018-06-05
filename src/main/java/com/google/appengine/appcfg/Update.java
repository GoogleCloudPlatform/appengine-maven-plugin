/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class Update extends AbstractAppCfgMojo {

  /**
   * Create or update an app version.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update
   * @execute phase="package"
   */
  public static class UpdateFork extends Update {}

  /**
   * Create or update an app version.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_no_fork
   */
  public static class UpdateNoFork extends Update {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating Google App Engine Application");

    executeAppCfgCommand("update", appDir);
  }
}
