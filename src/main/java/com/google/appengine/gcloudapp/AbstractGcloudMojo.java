/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author ludo
 */
public abstract class AbstractGcloudMojo extends AbstractMojo {

  /**
   * @parameter property="project"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * gcloud installation directory
   *
   * @parameter
   */
  protected String gcloud_directory;

  /**
   * docker_host
   *
   * @parameter default-value="ENV_or_default"
   */
  protected String gcloud_app_docker_host;
  /**
   * docker_host
   *
   * @parameter default-value="ENV_or_default"
   */
  protected String gcloud_app_docker_tls_verify;

  /**
   * docker_host
   *
   * @parameter default-value="ENV_or_default"
   */
  protected String gcloud_app_docker_cert_path;

  //DOCKER_TLS_VERIFY=1
//DOCKER_CERT_PATH=/Users/ludo/.boot2docker/certs/boot2docker-vm
  /**
   * Override the default verbosity for this command. This must be a standard
   * logging verbosity level: [debug, info, warning, error, critical, none]
   * (Default: [warning]).
   *
   * @parameter default-value="warning"
   */
  protected String gcloud_verbosity;

  /**
   * Google Cloud Platform project to use for this invocation.
   *
   * @parameter
   */
  protected String gcloud_project;

  protected AppEngineWebXml appengineWebXml = null;

  protected String applicationDirectory = null;

  /**
   *
   * @param appDir
   * @return
   */
  protected abstract ArrayList<String> getCommand(String appDir) throws MojoExecutionException;

  protected ArrayList<String> setupInitialCommands(ArrayList<String> commands) throws MojoExecutionException {
    commands.add("python");
    commands.add("-S");
    if (gcloud_directory == null) {
      gcloud_directory = System.getProperty("user.home") + "/google-cloud-sdk";
      getLog().info("gcloud_directory was not set, so taking: " + gcloud_directory);
    }
    commands.add(gcloud_directory + "/lib/googlecloudsdk/gcloud/gcloud.py");

    if (gcloud_project != null) {
      commands.add("--project=" + gcloud_project);
    } else {
       commands.add("--project=" + getAppEngineWebXml().getAppId());  
    }
    if (gcloud_verbosity != null) {
      commands.add("--verbosity=" + gcloud_verbosity);
    }

    commands.add("preview");
    commands.add("app");
    return commands;
  }

  protected ArrayList<String> setupExtraCommands(ArrayList<String> commands) throws MojoExecutionException {
    return commands;
  }

  protected static enum WaitDirective {

    WAIT_SERVER_STARTED,
    WAIT_SERVER_STOPPED
  }

  protected void startCommand(File appDirFile, ArrayList<String> devAppServerCommand, WaitDirective waitDirective) throws MojoExecutionException {
    getLog().info("Running " + Joiner.on(" ").join(devAppServerCommand));

    Thread stdOutThread = null;
    Thread stdErrThread = null;
    try {

      ProcessBuilder processBuilder = new ProcessBuilder(devAppServerCommand);

      processBuilder.directory(appDirFile);

      processBuilder.redirectErrorStream(true);
      Map<String, String> env = processBuilder.environment();
      if ("ENV_or_default".equals(gcloud_app_docker_host)) {
        if (env.get("DOCKER_HOST") == null) {
          env.put("DOCKER_HOST", "tcp://192.168.59.103:2376");
        }
      } else {
        env.put("DOCKER_HOST", gcloud_app_docker_host);
      }
      if ("ENV_or_default".equals(gcloud_app_docker_host)) {
        if (env.get("DOCKER_HOST") == null) {
          env.put("DOCKER_HOST", "tcp://192.168.59.103:2376");
        }
      } else {
        env.put("DOCKER_HOST", gcloud_app_docker_host);
      }

      if ("ENV_or_default".equals(gcloud_app_docker_tls_verify)) {
        if (env.get("DOCKER_TLS_VERIFY") == null) {
          env.put("DOCKER_TLS_VERIFY", "1");
        }
      } else {
        env.put("DOCKER_TLS_VERIFY", gcloud_app_docker_tls_verify);
      }

      if ("ENV_or_default".equals(gcloud_app_docker_cert_path)) {
        if (env.get("DOCKER_CERT_PATH") == null) {
          env.put("DOCKER_CERT_PATH",
                  System.getProperty("user.home")
                  + File.separator
                  + ".boot2docker"
                  + File.separator
                  + "certs"
                  + File.separator
                  + "boot2docker-vm"
          );
        }
      } else {
        env.put("DOCKER_CERT_PATH", gcloud_app_docker_cert_path);
      }
      //export DOCKER_CERT_PATH=/Users/ludo/.boot2docker/certs/boot2docker-vm
      //export DOCKER_TLS_VERIFY=1
      //export DOCKER_HOST=tcp://192.168.59.103:2376

      final Process devServerProcess = processBuilder.start();

      final CountDownLatch waitStartedLatch = new CountDownLatch(1);

      final Scanner stdOut = new Scanner(devServerProcess.getInputStream());
      stdOutThread = new Thread("standard-out-redirection-devappserver") {
        public void run() {
          try {
            while (stdOut.hasNextLine() && !Thread.interrupted()) {
              String line = stdOut.nextLine();
              getLog().info(line);
              if (line.contains("Starting new HTTP connection")) {
                waitStartedLatch.countDown();
              }
            }
          } finally {
            waitStartedLatch.countDown();
          }
        }
      };
      stdOutThread.setDaemon(true);
      stdOutThread.start();

      final Scanner stdErr = new Scanner(devServerProcess.getErrorStream());
      stdErrThread = new Thread("standard-err-redirection-devappserver") {
        public void run() {
          while (stdErr.hasNextLine() && !Thread.interrupted()) {
            getLog().error(stdErr.nextLine());
          }
        }
      };
      stdErrThread.setDaemon(true);
      stdErrThread.start();
      if (waitDirective == WaitDirective.WAIT_SERVER_STOPPED) {
        Runtime.getRuntime().addShutdownHook(new Thread("destroy-devappserver") {
          @Override
          public void run() {
            if (devServerProcess != null) {
              devServerProcess.destroy();
            }
          }
        });

        devServerProcess.waitFor();
        int status = devServerProcess.exitValue();
        if (status != 0) {
          getLog().error("Error: gcloud app run exit code= " + status);
          throw new MojoExecutionException("Error: gcloud app run exit code= " + status);
        }
      } else if (waitDirective == WaitDirective.WAIT_SERVER_STARTED) {
        waitStartedLatch.await();
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Could not start the dev app server", e);
    } catch (InterruptedException e) {
    }
  }

  protected String getApplicationDirectory() throws MojoExecutionException {
    if (applicationDirectory != null) {
      return applicationDirectory;
    }
    applicationDirectory = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    File appDirFile = new File(applicationDirectory);
    if (!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + applicationDirectory);
    }
    if (!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + applicationDirectory);
    }
    return applicationDirectory;
  }

  protected AppEngineWebXml getAppEngineWebXml() throws MojoExecutionException {

    if (appengineWebXml == null) {
      AppEngineWebXmlReader reader = new AppEngineWebXmlReader(getApplicationDirectory());
      appengineWebXml = reader.readAppEngineWebXml();
    }
    return appengineWebXml;
  }

}
