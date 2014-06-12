/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
   * The entry point to Aether, i.e. the component doing all the work.
   *
   * @component
   */
  protected RepositorySystem repoSystem;

  /**
   * The current repository/network configuration of Maven.
   *
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  protected RepositorySystemSession repoSession;

  /**
   * The project's remote repositories to use for the resolution of project
   * dependencies.
   *
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> projectRepos;

  /**
   * The project's remote repositories to use for the resolution of plugins and
   * their dependencies.
   *
   * @parameter default-value="${project.remotePluginRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> pluginRepos;

  /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.server}"
   */
  protected String server;

  /**
   * gcloud installation directory
   *
   * @parameter expression="${appengine.gcloud_directory}"
   */
  protected String gcloud_directory;

  /**
   * docker_host
   *
   * @parameter expression="${appengine.gcloud_app_docker_host}"
   */
  protected String gcloud_app_docker_host;

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
   * Google Cloud Platform project to use for this invocation.
   *
   * @parameter expression="${appengine.gcloud_project}"
   */
  
  protected String gcloud_project;
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

    ArrayList<String> devAppServerCommand = new ArrayList<String>();

    devAppServerCommand.add("python");
    devAppServerCommand.add("-S");
    if (gcloud_directory != null) {
      devAppServerCommand.add(gcloud_directory + "/lib/googlecloudsdk/gcloud/gcloud.py");
    } else {
      String gcloud = System.getProperty("user.home") + "/google-cloud-sdk/lib/googlecloudsdk/gcloud/gcloud.py";
      getLog().info("Warning, gcloud_directory was not set, so taking: " + gcloud);
    }
    
    if (gcloud_project != null) {
      devAppServerCommand.add("--project=" + gcloud_project);
    }
    if (gcloud_verbosity != null) {
      devAppServerCommand.add("--verbosity=" + gcloud_verbosity);
    }
    
    devAppServerCommand.add("preview");
    devAppServerCommand.add("app");
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

    // Add in additional options for starting the DevAppServer
    if (gcloud_app_docker_host != null) {
      devAppServerCommand.add("--docker-host=" + gcloud_app_docker_host);
    }  
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
