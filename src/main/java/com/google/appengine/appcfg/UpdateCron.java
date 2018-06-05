/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class UpdateCron extends AbstractAppCfgMojo {

  /**
   * Update application cron jobs.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_cron
   * @execute phase="package"
   */
  public static class UpdateCronFork extends UpdateCron {}

  /**
   * Update application cron jobs.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_cron_no_fork
   */
  public static class UpdateCronNoFork extends UpdateCron {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application Cron Jobs");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating Cron Jobs for Google App Engine Application");

    executeAppCfgCommand("update_cron", appDir);
  }

}
