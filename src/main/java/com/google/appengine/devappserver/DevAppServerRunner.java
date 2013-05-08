/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.devappserver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;

/**
 * Runs the App Engine development server.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal devserver
 * @execute phase="package"
 * @threadSafe false
 */
public class DevAppServerRunner extends AbstractDevAppServerMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Running Development Server");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    File appDirFile = new File(appDir);

    if(!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + appDir);
    }

    if(!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + appDir);
    }

    ArrayList<String> devAppServerCommand = getDevAppServerCommand(appDir);

    startDevAppServer(appDirFile, devAppServerCommand, true);
  }

}
