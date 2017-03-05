/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.devappserver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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

    ArrayList<String> devAppServerCommand = getDevAppServerCommand();

    startDevAppServer(devAppServerCommand, WaitDirective.WAIT_SERVER_STOPPED);
  }

}
