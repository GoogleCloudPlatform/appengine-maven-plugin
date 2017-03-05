/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.devappserver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.ArrayList;

/**
 * Starts the App Engine development server and does not wait.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal devserver_start
 * @execute phase="validate"
 * @threadSafe false
 */
public class DevAppServerAsyncStart extends AbstractDevAppServerMojo {
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Starting the Development Server");
    getLog().info("");

    ArrayList<String> devAppServerCommand = getDevAppServerCommand();

    startDevAppServer(devAppServerCommand, WaitDirective.WAIT_SERVER_STARTED);
  }

}
