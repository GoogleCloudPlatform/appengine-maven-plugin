/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class UpdateIndexes extends AbstractAppCfgMojo {

  /**
   * Update application indexes.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_indexes
   * @execute phase="package"
   */
  public static class UpdateIndexesFork extends UpdateIndexes {}

  /**
   * Update application indexes.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_indexes_no_fork
   */
  public static class UpdateIndexesNoFork extends UpdateIndexes {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Indexes");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating indexes for Google App Engine Application");

    executeAppCfgCommand("update_indexes", appDir);
  }

}
