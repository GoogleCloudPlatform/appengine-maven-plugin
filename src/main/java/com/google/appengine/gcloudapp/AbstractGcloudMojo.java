/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.appengine.gcloudapp.temp.AppEngineWebXml;
import com.google.appengine.gcloudapp.temp.AppEngineWebXmlReader;
import com.google.appengine.tools.admin.Application;
import com.google.apphosting.utils.config.AppEngineApplicationXml;
import com.google.apphosting.utils.config.AppEngineApplicationXmlReader;
import com.google.apphosting.utils.config.EarHelper;
import com.google.apphosting.utils.config.EarInfo;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
   * docker_tls_verify
   *
   * @parameter default-value="ENV_or_default"
   */
  protected String gcloud_app_docker_tls_verify;

  /**
   * docker_host_cert_path
   *
   * @parameter default-value="ENV_or_default"
   */
  protected String gcloud_app_docker_cert_path;

  /**
   * Override the default verbosity for this command. This must be a standard
   * logging verbosity level: [debug, info, warning, error, critical, none]
   * (Default: [info]).
   *
   * @parameter
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
    String pythonLocation = "python"; //default in the path for Linux
    boolean isWindows = System.getProperty("os.name").contains("Windows");
    if (isWindows) {
      pythonLocation = System.getenv("CLOUDSDK_PYTHON");
      if (pythonLocation == null) {
        getLog().info("CLOUDSDK_PYTHON env variable is not defined. Choosing a default python.exe interpreter.");
        getLog().info("If this does not work, please set CLOUDSDK_PYTHON to a correct Python interpreter location.");

        pythonLocation = "python.exe";
      }
    }
    commands.add(pythonLocation);
    commands.add("-S");

    boolean error = false;
    if (gcloud_directory == null) {
      if (isWindows) {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null) {
          programFiles = System.getenv("ProgramFiles(x86)");
        }
        if (programFiles == null) {
          error = true;
        } else {
          gcloud_directory = programFiles + "\\Google\\Cloud SDK\\google-cloud-sdk";
        }
      } else {
        gcloud_directory = System.getProperty("user.home") + "/google-cloud-sdk";
      }
    }
    getLog().info("gcloud_directory=" + gcloud_directory);
    File s = new File(gcloud_directory);
    getLog().info("" + s.exists());
    File script = new File(s, "/lib/googlecloudsdk/gcloud/gcloud.py");
    getLog().info("" + script.exists());

    if (error || !script.exists()) {
      getLog().error("Cannot determine the location of the Google Cloud SDK.");
      getLog().error("You can set it via <gcloud_directory> </gcloud_directory> in the pom.xml");
      getLog().info("If you need to install the Google Cloud SDK, follow the instructions located at https://cloud.google.com/appengine/docs/java/managed-vms");
      throw new MojoExecutionException("Unkown Google Cloud SDK location.");
    }

    commands.add(gcloud_directory + "/lib/googlecloudsdk/gcloud/gcloud.py");

    if (gcloud_project != null) {
      commands.add("--project=" + gcloud_project);
    } else {
      commands.add("--project=" + getAppId());
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
      String docker_host = env.get("DOCKER_HOST");
      String docker_host_tls_verify = env.get("DOCKER_TLS_VERIFY");
      String docker_host_cert_path = env.get("DOCKER_CERT_PATH");
      boolean userDefined = (docker_host != null)
              || (docker_host_tls_verify != null)
              || (docker_host_cert_path != null);

      if (!userDefined) {
        if ("ENV_or_default".equals(gcloud_app_docker_host)) {
          if (docker_host == null) {
            docker_host = "tcp://192.168.59.103:2376";
          }
        } else {
          docker_host = gcloud_app_docker_host;
        }
        env.put("DOCKER_HOST", docker_host);

        // we handle TLS extra variables only when we are tcp:
        if (docker_host.startsWith("tcp")) {
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
        }
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
            long healthCount = 0;
            while (stdOut.hasNextLine() && !Thread.interrupted()) {
              String line = stdOut.nextLine();
              // emit this every 30 times, no need for more...
              if (line.contains("GET /_ah/health?IsLastSuccessful=yes HTTP/1.1\" 200 2")) {
                waitStartedLatch.countDown();
                if (healthCount % 20 == 0) {
                  getLog().info(line);
                }
                healthCount++;
              } else {
                getLog().info(line);
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
        getLog().info("");
        getLog().info("App Engine Dev Server started in Async mode and running.");
        getLog().info("you can stop it with this command: mvn appengine:gcloud_app_run_stop");
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

  protected String getAppId() throws MojoExecutionException {

    String appDir = getApplicationDirectory();
    if (EarHelper.isEar(appDir)) { // EAR project
      AppEngineApplicationXmlReader reader
              = new AppEngineApplicationXmlReader();
      AppEngineApplicationXml appEngineApplicationXml = reader.processXml(
              getInputStream(new File(appDir, "META-INF/appengine-application.xml")));
      return appEngineApplicationXml.getApplicationId();

    }

    return getAppEngineWebXml().getAppId();
  }

  private static InputStream getInputStream(File file) {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException fnfe) {
      throw new IllegalStateException("File should exist - '" + file + "'");
    }
  }

  protected AppEngineWebXml getAppEngineWebXml() throws MojoExecutionException {
    if (appengineWebXml == null) {
      AppEngineWebXmlReader reader = new AppEngineWebXmlReader(getApplicationDirectory());
      appengineWebXml = reader.readAppEngineWebXml();
    }
    return appengineWebXml;
  }
}
