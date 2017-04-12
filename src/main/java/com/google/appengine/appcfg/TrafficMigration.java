/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class TrafficMigration extends AbstractAppCfgMojo {

  /**
   * Change the default version, but more gently than set_default_version.
   *
   * @author ludo
   * @goal migrate_traffic
   * @execute phase="package"
   */
  public static class TrafficMigrationFork extends TrafficMigration {}

  /**
   * Change the default version, but more gently than set_default_version.
   *
   * @author ludo
   * @goal migrate_traffic_no_fork
   */
  public static class TrafficMigrationNoFork extends TrafficMigration {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Traffic Migration for Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Migrating Traffic for Google App Engine Application");

    executeAppCfgCommand("migrate_traffic", appDir);
  }

}

