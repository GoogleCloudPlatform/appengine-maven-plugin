/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Deploy an application via gcloud deploy.
 *
 * @author Ludo
 * @goal gcloud_app_deploy
 * @execute phase="package"
 * @threadSafe false
 */
public class GCloudAppDeploy extends AbstractGcloudMojo {

  /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.server}"
   */
  protected String server;

  /**
   * version The version of the app that will be created or replaced by this deployment.
   *
   * @parameter expression="${appengine.gcloud_app_version}"
   */
  protected String gcloud_app_version;

    /**
   * env-vars ENV_VARS Environment variable overrides for your app.
   *
   * @parameter expression="${appengine.gcloud_app_env_vars}"
   */
  protected String gcloud_app_env_vars;
  
   /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.gcloud_app_server}"
   */
  protected String gcloud_app_server;
  
   /**
   * force Force deploying, overriding any previous in-progress deployments to this version.
   *
   * @parameter expression="${appengine.gcloud_app_force}"
   */
  protected boolean gcloud_app_force;
  

  /**
   * Override the default verbosity for this command. 
   * This must be a standard logging verbosity level: [debug, info,
   *  warning, error, critical, none] (Default: [warning]).
   *
   * @parameter expression="${appengine.gcloud_verbosity}"
   */
  protected String gcloud_verbosity;
  
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    File appDirFile = new File(appDir);
    if (!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + appDir);
    }
    if (!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + appDir);
    }
    ArrayList<String> devAppServerCommand = getCommand(appDir);
    startCommand(appDirFile, devAppServerCommand, WaitDirective.WAIT_SERVER_STOPPED);
  }

  @Override
  protected ArrayList<String> getCommand(String appDir) throws MojoExecutionException {

    getLog().info("Running gcloud app deploy...");

    ArrayList<String> devAppServerCommand = new ArrayList<>();
    setupInitialCommands(devAppServerCommand);
    

    devAppServerCommand.add("deploy");

    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (!f.exists()) { // EAR project possibly, add all modules one by one:
      File ear = new File(appDir);
      for (File w : ear.listFiles()) {
        if (new File(w, "WEB-INF/appengine-web.xml").exists()) {
          devAppServerCommand.add(w.getAbsolutePath());
        }
      }
    } else {
      // Point to our application
      devAppServerCommand.add(appDir);
    }
    setupExtraCommands(devAppServerCommand);

    // Add in additional options for starting the DevAppServer

    if (gcloud_app_version != null) {
      devAppServerCommand.add("--version=" + gcloud_app_version);
    }
    if (gcloud_app_env_vars != null) {
      devAppServerCommand.add("--env-vars=" + gcloud_app_env_vars);
    }
    if (gcloud_app_server != null) {
      devAppServerCommand.add("--server=" + gcloud_app_server);
    } else if (server != null) {
      devAppServerCommand.add("--server=" + server);
    }  
    if (gcloud_app_force) {
      devAppServerCommand.add("--force" );
    } 
    return devAppServerCommand;
  }

}
