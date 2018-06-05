/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class StartModuleVersion extends AbstractAppCfgMojo {

  /**
   * Start the specified module version.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal start_module_version
   * @execute phase="package"
   */
  public static class StartModuleVersionFork extends StartModuleVersion {}

  /**
   * Start the specified module version.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal start_module_version_no_fork
   */
  public static class StartModuleVersionNoFork extends StartModuleVersion {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Starting the specified module version.");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Starting the specified module version.");

    executeAppCfgCommand("start_module_version", appDir);
  }

}

