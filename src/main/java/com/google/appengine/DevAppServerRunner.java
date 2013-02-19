/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine;

import static java.io.File.separator;

import com.google.common.collect.ImmutableList;
import com.google.appengine.tools.development.DevAppServerMain;
import com.google.common.base.Joiner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Runs the App Engine development server.
 *
 * @goal devserver
 * @execute phase="package"
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

  /**
   * The server to use to determine the latest SDK version.
   *
   * @parameter
   */
  private String server;

  /**
   * The address of the interface on the local machien to bind to (or 0.0.0.0 for all interfaces).
   *
   * @parameter
   */
  private String address;

  /**
   * The port number to bind to on the local machine.
   *
   * @parameter
   */
  private String port;

  /**
   * Disable the check for newer SDK version.
   *
   * @parameter
   */
  private boolean disableUpdateCheck;

  /**
   * Additional flags to the JVM used to run the dev server.
   *
   * @parameter
   */
  private List<String> jvmFlags;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Running Development Server");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    File sdkBaseDir = SdkResolver.getSdk(project, repoSystem, repoSession, pluginRepos, projectRepos);

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    String javaExecutable = joinOnFileSeparator(System.getProperty("java.home"), "bin", "java");

    ArrayList<String> devAppServerCommand = new ArrayList<String>();
    devAppServerCommand.add(javaExecutable);

    if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
      devAppServerCommand.add("-XstartOnFirstThread");
    }

    // Add in the appengine agent
    String appengineAgentJar = new File(sdkBaseDir, joinOnFileSeparator("lib", "agent", "appengine-agent.jar")).getAbsolutePath();
    devAppServerCommand.add("-javaagent:" + appengineAgentJar);

    // Setup the overrides jar for jdk classes
    String appengineDevJdkOverridesJar = new File(sdkBaseDir, joinOnFileSeparator("lib", "override", "appengine-dev-jdk-overrides.jar")).getAbsolutePath();
    devAppServerCommand.add("-Xbootclasspath/p:" + appengineDevJdkOverridesJar);

    // Setup the classpath to point to the tools jar
    String appengineToolsApiJar = new File(sdkBaseDir, joinOnFileSeparator("lib", "appengine-tools-api.jar")).getAbsolutePath();
    devAppServerCommand.add("-classpath");
    devAppServerCommand.add(appengineToolsApiJar);

    // Add jvm flags
    if(jvmFlags != null && !jvmFlags.isEmpty()) {
      devAppServerCommand.addAll(jvmFlags);
    }

    // Point to the DevAppServerMain class
    devAppServerCommand.add(DevAppServerMain.class.getCanonicalName());

    // Add in additional options for starting the DevAppServer
    if(server != null) {
      devAppServerCommand.add("-s");
      devAppServerCommand.add(server);
    }

    if(address != null) {
      devAppServerCommand.add("-a");
      devAppServerCommand.add(address);
    }

    if(port != null) {
      devAppServerCommand.add("-p");
      devAppServerCommand.add(port);
    }

    if(disableUpdateCheck) {
      devAppServerCommand.add("--disable_update_check");
    }

    // Point to our application
    devAppServerCommand.add(appDir);

    getLog().info("Running " + Joiner.on(" ").join(devAppServerCommand));

    Thread stdOutThread = null;
    Thread stdErrThread = null;
    try {

      ProcessBuilder processBuilder = new ProcessBuilder(devAppServerCommand);

      processBuilder.redirectErrorStream(true);
      final Process devServerProcess = processBuilder.start();

      Runtime.getRuntime().addShutdownHook(new Thread("destroy-devappserver") {
        @Override
        public void run() {
          if (devServerProcess != null) {
            devServerProcess.destroy();
          }
        }
      });

      final Scanner stdOut = new Scanner(devServerProcess.getInputStream());
      stdOutThread = new Thread("standard-out-redirection-devappserver") {
        public void run() {
          while (stdOut.hasNextLine() && !Thread.interrupted()) {
            getLog().info(stdOut.nextLine());
          }
        }
      };
      stdOutThread.start();

      final Scanner stdErr = new Scanner(devServerProcess.getErrorStream());
      stdErrThread = new Thread("standard-err-redirection-devappserver") {
        public void run() {
          while (stdErr.hasNextLine() && !Thread.interrupted()) {
            getLog().error(stdErr.nextLine());
          }
        }
      };
      stdErrThread.start();

      devServerProcess.waitFor();

    } catch (IOException e) {
      throw new MojoExecutionException("Could not start the dev app server", e);
    } catch (InterruptedException e) {

    } finally {
      stdOutThread.interrupt();
      stdErrThread.interrupt();
    }
  }

  private String joinOnFileSeparator(String... pathComponents) {
    return Joiner.on(separator).join(ImmutableList.copyOf(pathComponents));
  }
}
