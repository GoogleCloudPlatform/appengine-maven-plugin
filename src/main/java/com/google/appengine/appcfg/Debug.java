/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Debug the specified VM Runtime instance.
 *
 * @author Ludovic Champenois <ludo@google.com>
 * @goal debug
 * @execute phase="package"
 */
public class Debug extends AbstractAppCfgMojo {
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Debug the specified VM Runtime instance.");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();


    executeAppCfgCommand("debug", appDir);
  }

}

