/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.api.server.spi.tools.EndpointsTool;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the various endpoints tools commands.
 *
 * @author Ludovic Champenois ludo at google dot com
 *
 * @requiresDependencyResolution compile
 */
public abstract class EndpointsMojo extends AbstractMojo {

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * The classpath of the service-classes.
   *
   * @parameter expression="${classes}" default-value="${project.build.directory}/classes"
   */
  protected String classes;

  /**
   * The directory for the generated api-file.
   *
   * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/appengine-endpoints"
   */
  protected String outputDirectory;

  /**
   * The source directory containing the web.xml file.
   *
   * @parameter expression="${warSourceDirectory}" default-value="${basedir}/src/main/webapp/WEB-INF/web.xml"
   */
  private String webXmlSourcePath;

  protected void handleClassPath(ArrayList<String> arguments) {
    Iterable<File> jars = Iterables.transform(
            Iterables.filter(project.getArtifacts(), new Predicate<Artifact>() {
              @Override
              public boolean apply(Artifact artifact) {
                return artifact.getScope().equals("compile");
              }
            }), new Function<Artifact, File>() {
      @Override
      public File apply(Artifact artifact) {
        return artifact.getFile();
      }
    });

    String cp = Joiner.on(System.getProperty("path.separator")).join(jars);
    arguments.add("-cp");
    arguments.add(classes + System.getProperty("path.separator") + cp);
  }
  
  abstract protected ArrayList<String> collectParameters(String command);
 
  protected void executeEndpointsCommand(String action, String[] lastParam)
      throws MojoExecutionException {
    ArrayList<String> arguments = collectParameters(action);

    for (String param : lastParam) {
      arguments.add(param);
      getLog().info("Using Class Name:" + param);
    }
    try {
      getLog().info("Executing endpoints Command=" + arguments);
      EndpointsTool.main(arguments.toArray(new String[arguments.size()]));
    } catch (Exception ex) {
      getLog().error(ex);
      throw new MojoExecutionException("Error executing endpoints command="
          + arguments, ex);
    }
  }
  
  protected List<String> getAPIServicesClasses() {
    return new WebXmlProcessing(getLog(), webXmlSourcePath,
            outputDirectory, project).getAPIServicesClasses();
  }
}