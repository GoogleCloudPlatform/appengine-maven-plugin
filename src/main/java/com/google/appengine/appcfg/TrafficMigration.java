/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Change the default version, but more gently than set_default_version.
 * @author ludo
 * @goal migrate_traffic
 * @execute phase="package"
 */
public class TrafficMigration  extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Traffic Migration for Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Migrating Traffic for Google App Engine Application");

    executeAppCfgCommand("migrate_traffic");
  }

}

