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
  private String server;

  /**
   * version The version of the app that will be created or replaced by this
   * deployment.
   *
   * @parameter expression="${appengine.gcloud_app_version}"
   */
  private String gcloud_app_version;

  /**
   * env-vars ENV_VARS Environment variable overrides for your app.
   *
   * @parameter expression="${appengine.gcloud_app_env_vars}"
   */
  private String gcloud_app_env_vars;

  /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.gcloud_app_server}"
   */
  private String gcloud_app_server;

  /**
   * force Force deploying, overriding any previous in-progress deployments to
   * this version.
   *
   * @parameter expression="${appengine.gcloud_app_force}"
   */
  private boolean gcloud_app_force;
  /**
   * Set the encoding to be used when compiling Java source files (default
   * "UTF-8")
   *
   * @parameter expression="${appengine.gcloud_app_compile_encoding}"
   */
  private String gcloud_app_compile_encoding;
  /**
   * Delete the JSP source files after compilation
   *
   * @parameter expression="${appengine.gcloud_app_delete_jsps}"
   */
  private boolean gcloud_app_delete_jsps;
  /**
   * Do not jar the classes generated from JSPs
   *
   * @parameter expression="${appengine.gcloud_app_disable_jar_jsps}"
   */
  private boolean gcloud_app_disable_jar_jsps;
  /**
   * Jar the WEB-INF/classes content
   *
   * @parameter expression="${appengine.gcloud_app_enable_jar_classes}"
   */
  private boolean gcloud_app_enable_jar_classes;
  /**
   * Split large jar files (> 32M) into smaller fragments
   *
   * @parameter expression="${appengine.gcloud_app_enable_jar_splitting}"
   */
  private boolean gcloud_app_enable_jar_splitting;
  /**
   * Do not use symbolic links when making the temporary (staging) directory
   * used in uploading Java apps
   *
   * @parameter expression="${appengine.gcloud_app_no_symlinks}"
   */
  private boolean gcloud_app_no_symlinks;
  /**
   * Do not delete temporary (staging) directory used in uploading Java apps
   *
   * @parameter expression="${appengine.gcloud_app_retain_upload_dir}"
   */
  private boolean gcloud_app_retain_upload_dir;
  /**
   * When --enable-jar-splitting is specified and --jar-splitting-excludes
   * specifies a comma-separated list of suffixes, a file in a jar whose name
   * ends with one of the suffixes will not be included in the split jar
   * fragments
   *
   * @parameter expression="${appengine.gcloud_app_jar_splitting_excludes}"
   */
  private String gcloud_app_jar_splitting_excludes;

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
      devAppServerCommand.add("--force");
    }
    if (gcloud_app_delete_jsps) {
      devAppServerCommand.add("--delete-jsps");
    }
    if (gcloud_app_disable_jar_jsps) {
      devAppServerCommand.add("--disable-jar-jsps");
    }
    if (gcloud_app_enable_jar_classes) {
      devAppServerCommand.add("--enable-jar-classes");
    }
    if (gcloud_app_enable_jar_splitting) {
      devAppServerCommand.add("--enable-jar-splitting");
    }
    if (gcloud_app_compile_encoding != null) {
      devAppServerCommand.add("--compile-encoding=" + gcloud_app_compile_encoding);
    }
    if (gcloud_app_retain_upload_dir) {
      devAppServerCommand.add("--retain-upload-dir");
    }
    if (gcloud_app_no_symlinks) {
      devAppServerCommand.add("--no-symlinks");
    }
    if (gcloud_app_jar_splitting_excludes != null) {
      devAppServerCommand.add("--jar-splitting-excludes=" + gcloud_app_jar_splitting_excludes);
    }
    return devAppServerCommand;
  }

}
