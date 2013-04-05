/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import com.google.appengine.AbstractAppCfgMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Delete the specified backend.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal backends_delete
 * @execute phase="package"
 */
public class BackendsDelete extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Deleting Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Deleting Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("delete", appDir);
  }

}
