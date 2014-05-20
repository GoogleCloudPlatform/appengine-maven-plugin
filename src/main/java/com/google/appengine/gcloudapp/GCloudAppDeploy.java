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
   * The server to use to determine the latest SDK version.
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
   * docker_daemon_url
   *
   * @parameter expression="${appengine.gcloud_app_docker_daemon_url}"
   */
  protected String gcloud_app_docker_daemon_url;

  /**
   * enable_cloud_datastore
   *
   * @parameter expression="${appengine.gcloud_app_enable_cloud_datastore}"
   */
  protected boolean gcloud_app_enable_cloud_datastore;

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Cloud SDK - Running Development Server");
    getLog().info("project=" + project);
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

    if (gcloud_directory != null) {
      devAppServerCommand.add(gcloud_directory + "/bin/gcloud");
    } else {
      String gcloud = System.getProperty("user.home") + "/google-cloud-sdk/bin/gcloud";
      getLog().info("Warning, gcloud_directory was not set, so taking: " + gcloud);
      devAppServerCommand.add(gcloud);
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
//    if (gcloud_app_docker_daemon_url != null) {
//      devAppServerCommand.add("--docker-daemon-url=" + gcloud_app_docker_daemon_url);
//    }
    return devAppServerCommand;
  }

}
