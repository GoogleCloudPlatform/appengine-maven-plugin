/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import java.util.ArrayList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class Debug extends AbstractAppCfgMojo {

  /**
   * Debug the specified VM Runtime instance.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal debug
   * @execute phase="package"
   */
  public static class DebugFork extends Debug {}

  /**
   * Debug the specified VM Runtime instance.
   *
   * @author Ludovic Champenois <ludo@google.com>
   * @goal debug_no_fork
   */
  public static class DebugNoFork extends Debug {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Debug the specified VM Runtime instance.");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();


    executeAppCfgCommand("debug", appDir);
  }
}
