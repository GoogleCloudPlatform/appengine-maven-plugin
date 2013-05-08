/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Delete unused indexes from application.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal vacuum_indexes
 * @execute phase="package"
 */
public class VacuumIndexes extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
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
