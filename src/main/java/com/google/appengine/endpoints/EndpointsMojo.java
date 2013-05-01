/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.api.server.spi.tools.EndpointsTool;
import com.google.common.base.Joiner;
import com.jcabi.aether.Classpath;
import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import java.util.ArrayList;
import java.util.Collection;
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
   * @parameter expression="${classPath}" default-value="${project.build.directory}/classes"
   */
  protected String classPath;
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
    Collection<File> jars = new Classpath(project,
            repoSession.getLocalRepository().getBasedir(),
            "compile");
    String cp = Joiner.on(System.getProperty("path.separator")).join(jars);
    arguments.add("-cp");
    arguments.add(classPath + System.getProperty("path.separator") + cp);
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