/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Update application DoS protection configuration.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal update_dos
 * @execute phase="package"
 */
public class UpdateDos extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application DoS protection configuration");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    getLog().info("Updating DoS configuration for Google App Engine Application");

    executeAppCfgCommand("update_dos");
  }

}
