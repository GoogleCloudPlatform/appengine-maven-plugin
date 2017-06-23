/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Stop the specified module version.
 *
 * @author Ludovic Champenois <ludo@google.com>
 * @goal stop_module_version
 * @execute phase="package"
 */
public class StopModuleVersion extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Stopping the specified module version.");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Stopping the specified module version.");

    executeAppCfgCommand("stop_module_version");
  }

}

