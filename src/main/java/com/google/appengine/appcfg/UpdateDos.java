/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class UpdateDos extends AbstractAppCfgMojo {

  /**
   * Update application DoS protection configuration.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_dos
   * @execute phase="package"
   */
  public static class UpdateDosFork extends UpdateDos {}

  /**
   * Update application DoS protection configuration.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal update_dos_no_fork
   */
  public static class UpdateDosNoFork extends UpdateDos {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application DoS protection configuration");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating DoS configuration for Google App Engine Application");

    executeAppCfgCommand("update_dos", appDir);
  }

}
