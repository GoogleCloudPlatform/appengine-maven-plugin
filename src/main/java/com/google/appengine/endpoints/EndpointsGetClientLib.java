/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * App Engine endpoints get-client-lib ... command.
 *
 * @author Ludovic Champenois ludo at google dot com
 * @goal endpoints_get_client_lib
 * @execute phase="package"
 */
public class EndpointsGetClientLib extends EndpointsMojo {

  @Override
  protected ArrayList<String> collectParameters(String command) {
    ArrayList<String> arguments = new ArrayList<String>();
    arguments.add(command);
    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    handleClassPath(arguments, appDir);

    if (outputDirectory != null && !outputDirectory.isEmpty()) {
      arguments.add("-o");
      arguments.add(outputDirectory);
    }
    arguments.add("-w");
    arguments.add(appDir);
    arguments.add("-l");
    arguments.add("java");
    return arguments;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Generate endpoints get client lib");

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    List<String> classNames = Arrays.asList(serviceClassNames.split(","));
    try {
      executeEndpointsCommand("get-client-lib",
          classNames.toArray(new String[classNames.size()]));
      File webInf = new File(appDir + "/WEB-INF");
      if (webInf.exists() && webInf.isDirectory()) {
        File[] files = webInf.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("-java.zip");
          }
        });
        for (File source : files) {
          File target = new File(project.getBasedir(), source.getName());
          target.delete();
          Files.move(source, target);
          getLog().info("Endpoint library available at:"+target.getAbsolutePath());
        }
      }
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(
          "Error while generating Google App Engine endpoint get client lib", e);
    }
    getLog().info("Endpoint get client lib generation done.");
  }
}
