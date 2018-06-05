/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class VacuumIndexes extends AbstractAppCfgMojo {

  /**
   * Delete unused indexes from application.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal vacuum_indexes
   * @execute phase="package"
   */
  public static class VacuumIndexesFork extends VacuumIndexes {}

  /**
   * Delete unused indexes from application.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal vacuum_indexes_no_fork
   */
  public static class VacuumIndexesNoFork extends VacuumIndexes {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Vacuuming Application Indexes");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Vacuuming indexes for Google App Engine Application");

    executeAppCfgCommand("vacuum_indexes", appDir);
  }

}
