/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Update application cron jobs.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal update_cron
 * @execute phase="package"
 */
public class UpdateCron extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Cron Jobs");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Updating Cron Jobs for Google App Engine Application");

    executeAppCfgCommand("update_cron");
  }

}
