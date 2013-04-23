/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.api.server.spi.tools.EndpointsTool;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the various endpoints tools commands.
 *
 * @author Ludovic Champenois ludo at google dot com
 */
public abstract class EndpointsMojo extends AbstractMojo {

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
   * The project's remote repositories to use for the resolution of project dependencies.
   *
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> projectRepos;
  /**
   * The project's remote repositories to use for the resolution of plugins and their
   * dependencies.
   *
   * @parameter default-value="${project.remotePluginRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> pluginRepos;
  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;
  /**
   * The classpath of the service-classes.
   *
   * @parameter expression="${classPath}" default-value=""
   */
  protected String classPath;
  /**
   * The directory for the generated api-file.
   *
   * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/${project.build.finalName}/WEB-INF"
   */
  protected String outputDirectory;
  /**
   * The full qualified names of the service endpoints classes( comma separated).
   *
   * @parameter
   */
  protected String serviceClassNames;

  protected void handleClassPath(ArrayList<String> arguments, String appDir) {
    arguments.add("-cp");
    if (classPath != null && !classPath.isEmpty()) {
      arguments.add(classPath);
    } else {
      File libArea = new File(appDir + "/WEB-INF/lib");
      String entirePath = appDir + "/WEB-INF/classes:";
      if (libArea.exists() && libArea.isDirectory()) {
        File[] files = libArea.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("jar");
          }
        });
        for (File f : files) {
          entirePath = entirePath + File.pathSeparator + f.getAbsolutePath();
        }
      }
      arguments.add(entirePath);
    }
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
}
