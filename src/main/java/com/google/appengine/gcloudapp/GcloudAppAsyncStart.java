/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;

/**
 * Starts the Gcloud App Engine development server and does not wait.
 *
 * @author Ludo
 * @goal gcloud_app_run_start
 * @execute phase="validate"
 * @threadSafe false
 */
public class GcloudAppAsyncStart extends GCloudAppRun {
  /**
   * The location of the appengine application to run.
   *
   * @parameter expression="${appengine.appDir}"
   */
  protected String appDir;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Gcloud SDK - Starting the Development Server");
    getLog().info("");

    if(appDir == null) {
      appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    }
    
    File appDirFile = new File(appDir);

    if(!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + appDir);
    }

    if(!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + appDir);
    }

    ArrayList<String> devAppServerCommand = getCommand(appDir);

    startCommand(appDirFile, devAppServerCommand, WaitDirective.WAIT_SERVER_STARTED);
  }

}
