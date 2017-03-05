/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Set the default serving version.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal set_default_version
 * @execute phase="package"
 */
public class SetDefaultVersion extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Setting Default Version for Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Setting default version for Google App Engine Application");

    executeAppCfgCommand("set_default_version");
  }

}
