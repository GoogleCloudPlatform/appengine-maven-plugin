/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Update application indexes.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal update_indexes
 * @execute phase="package"
 */
public class UpdateIndexes extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Indexes");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Updating indexes for Google App Engine Application");

    executeAppCfgCommand("update_indexes", getAppDir());
  }

}
