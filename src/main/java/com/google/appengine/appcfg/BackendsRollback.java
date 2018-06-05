/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class BackendsRollback extends AbstractAppCfgMojo {

  /**
   * Roll back a previously in-progress update.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_rollback
   * @execute phase="package"
   */
  public static class BackendsRollbackFork extends BackendsRollback {}

  /**
   * Roll back a previously in-progress update.
   *
   * @author Matt Stephenson <mattstep@google.com>
   * @goal backends_rollback_no_fork
   */
  public static class BackendsRollbackNoFork extends BackendsRollback {}


  @Override
  public final void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Rollback Application Backends");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Rolling Back Google App Engine Application Backend " + backendName);

    executeAppCfgBackendsCommand("rollback", appDir);
  }

}
