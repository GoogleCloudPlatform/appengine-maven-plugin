/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * App Engine endpoints get-swagger-doc command.
 *
 * @author Ludovic Champenois ludo at google dot com
 * @goal endpoints_get_swagger_doc
 * @phase compile
 */
public class EndpointsGetSwaggerDoc extends EndpointsMojo {
  
  @Override
  protected ArrayList<String> collectParameters(String command) {
    ArrayList<String> arguments = new ArrayList<>();
    arguments.add(command);
    handleClassPath(arguments);
    if (outputDirectory != null && !outputDirectory.isEmpty()) {
      arguments.add("-o");
      arguments.add(outputDirectory + "/WEB-INF/swagger.json");
      new File(outputDirectory).mkdirs();
    }
    arguments.add("-w");
    arguments.add(outputDirectory);
    return arguments;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - get swagger doc...");
    List<String> classNames = getAPIServicesClasses();
    if (classNames.isEmpty()) {
      getLog().info("No Endpoints classes detected.");
      return;
    }

    executeEndpointsCommand("get-swagger-doc", new String[0],
                    classNames.toArray(new String[classNames.size()]));

    
    getLog().info("Endpoints swagger doc generation done.");

  }
}
