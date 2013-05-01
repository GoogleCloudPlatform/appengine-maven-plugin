/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.api.server.spi.tools.EndpointsTool;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * App Engine endpoints gen-api-config and gen-discovery-doc combined commands.
 *
 * @author Ludovic Champenois ludo at google dot com
 * @goal endpoints_get_discovery_doc
 * @phase compile
 */
public class EndpointsGetDiscoveryDoc extends EndpointsMojo {

  @Override
  protected ArrayList<String> collectParameters(String command) {
    ArrayList<String> arguments = new ArrayList<String>();
    arguments.add(command);
    handleClassPath(arguments);
    if (outputDirectory != null && !outputDirectory.isEmpty()) {
      arguments.add("-o");
      arguments.add(outputDirectory + "/WEB-INF");
      new File(outputDirectory).mkdirs();
    }
    arguments.add("-w");
    arguments.add(outputDirectory);
    return arguments;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Generate endpoints api config...");
    List<String> classNames = getAPIServicesClasses();
    if (classNames.isEmpty()) {
      getLog().info("No Endpoints classes detected.");
      return;
    }
    executeEndpointsCommand("gen-api-config",
            classNames.toArray(new String[classNames.size()]));
    File webInf = new File(outputDirectory + "/WEB-INF");
    if (webInf.exists() && webInf.isDirectory()) {
      File[] files = webInf.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("api");
        }
      });
      for (File f : files) {
        genDiscoveryDoc("rest", f.getAbsolutePath());
        genDiscoveryDoc("rpc", f.getAbsolutePath());
      }
    }
    getLog().info("Endpoint client lib generation done.");
  }

  private void genDiscoveryDoc(String format, String apiConfigFile)
          throws MojoExecutionException, MojoFailureException {
    getLog().info("Google App Engine Java SDK - Generate endpoints " + format
            + " discovery doc for apiConfigFile=");
    try {

      ArrayList<String> arguments = new ArrayList<String>();
      arguments.add("gen-discovery-doc");
      arguments.add("-f");
      arguments.add(format);

      if (outputDirectory != null && !outputDirectory.isEmpty()) {
        arguments.add("-o");
        arguments.add(outputDirectory + "/WEB-INF");
      }
      arguments.add(apiConfigFile);
      EndpointsTool.main(arguments.toArray(new String[arguments.size()]));
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(
              "Error while generating Google App Engine endpoint discovery doc", e);
    }
    getLog().info("Endpoint discovery doc generation done.");

  }
}
