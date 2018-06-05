/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class UpdateDispatch extends AbstractAppCfgMojo {

  /**
   * Update application dispatch.xml.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal update_dispatch
   * @execute phase="package"
   */
  public static class UpdateDispatchFork extends UpdateDispatch {}

  /**
   * Update application dispatch.xml.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal update_dispatch_no_fork
   */
  public static class UpdateDispatchNoFork extends UpdateDispatch {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Dispatch");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating Dispatch for Google App Engine Application");

    executeAppCfgCommand("update_dispatch", appDir);
  }

}
