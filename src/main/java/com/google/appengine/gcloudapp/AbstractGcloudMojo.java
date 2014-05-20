/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 *
 * @author ludo
 */
public abstract class AbstractGcloudMojo extends AbstractMojo {

  /**
   *
   * @param appDir
   * @return
   */
  protected abstract ArrayList<String> getCommand(String appDir) throws MojoExecutionException;

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
      final Process devServerProcess = processBuilder.start();
      final CountDownLatch waitStartedLatch = new CountDownLatch(1);
      final Scanner stdOut = new Scanner(devServerProcess.getInputStream());
      stdOutThread = new Thread("standard-out-redirection-devappserver") {
        public void run() {
          try {
            while (stdOut.hasNextLine() && !Thread.interrupted()) {
              String line = stdOut.nextLine();
              getLog().info(line);
              if (line.contains("Dev App Server is now running")) {
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
      } else if (waitDirective == WaitDirective.WAIT_SERVER_STARTED) {
        waitStartedLatch.await();
      }
      int status = devServerProcess.exitValue();
      if (status != 0) {
        getLog().error("Error: gcloud app run exit code= " + status);
        throw new MojoExecutionException("Error: gcloud app run exit code= " + status);
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Could not start the dev app server", e);
    } catch (InterruptedException e) {
    }
  }

}
