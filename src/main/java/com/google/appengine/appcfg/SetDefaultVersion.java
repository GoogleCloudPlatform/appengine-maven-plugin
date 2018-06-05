/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class SetDefaultVersion extends AbstractAppCfgMojo {

  /**
   * Set the default serving version.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal set_default_version
   * @execute phase="package"
   */
  public static class SetDefaultVersionFork extends SetDefaultVersion {}

  /**
   * Set the default serving version.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal set_default_version_no_fork
   */
  public static class SetDefaultVersionNoFork extends SetDefaultVersion {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Setting Default Version for Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Setting default version for Google App Engine Application");

    executeAppCfgCommand("set_default_version", appDir);
  }

}
