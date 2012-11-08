/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * @goal dev-server-start
 */
public class DevAppServerRunner extends AbstractMojo {

  /**
   * The entry point to Aether, i.e. the component doing all the work.
   *
   * @component
   */
  private RepositorySystem repoSystem;

  /**
   * The current repository/network configuration of Maven.
   *
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  private RepositorySystemSession repoSession;

  /**
   * The project's remote repositories to use for the resolution of project dependencies.
   *
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  private List<RemoteRepository> projectRepos;

  /**
   * The project's remote repositories to use for the resolution of plugins and their dependencies.
   *
   * @parameter default-value="${project.remotePluginRepositories}"
   * @readonly
   */
  private List<RemoteRepository> pluginRepos;

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Running Development Server");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");
    File sdkBaseDir = SdkResolver.getSdk(repoSystem, repoSession, pluginRepos, projectRepos);

    File devAppServerExecutable = new File(sdkBaseDir, "bin/dev_appserver.sh");
    devAppServerExecutable.setExecutable(true);

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    Process proc = null;
    try {
      ProcessBuilder pb = new ProcessBuilder(devAppServerExecutable.getCanonicalPath(), appDir);
      pb.redirectErrorStream(true);
      proc = pb.start();
    } catch (IOException e) {
      throw new MojoExecutionException("Could not start the dev app server", e);
    }

    final Scanner in = new Scanner(proc.getInputStream());
    new Thread() {
      public void run() {
        while (in.hasNextLine())
          getLog().info(in.nextLine());
      }
    }.start();

    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Interrupted!", e);
    }
  }
}
