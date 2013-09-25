/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * App Engine endpoints get-client-lib ... command.
 *
 * @author Ludovic Champenois ludo at google dot com
 * @goal endpoints_get_client_lib
 * @phase compile
 */
public class EndpointsGetClientLib extends EndpointsMojo {

  /**
   * The directory for the generated Maven client lib projects.
   *
   * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/endpoints-client-libs"
   */
  protected String clientLibsDirectory;  
  @Override
  protected ArrayList<String> collectParameters(String command) {
    ArrayList<String> arguments = new ArrayList<String>();
    arguments.add(command);
    handleClassPath(arguments);

    if (outputDirectory != null && !outputDirectory.isEmpty()) {
      arguments.add("-o");
      arguments.add(outputDirectory + "/WEB-INF");
      arguments.add("-O");
      arguments.add(outputDirectory + "/WEB-INF");
      new File(outputDirectory).mkdirs();
    }
    arguments.add("-w");
    arguments.add(outputDirectory);
    arguments.add("-l");
    arguments.add("java");
    arguments.add("-bs");
    arguments.add("maven");
    return arguments;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Generate endpoints get client lib");

    List<String> classNames = getAPIServicesClasses();
    if (classNames.isEmpty()) {
      getLog().info("No Endpoints classes detected.");
      return;
    }

    try {
      executeEndpointsCommand("get-client-lib",
              classNames.toArray(new String[classNames.size()]));
      File webInf = new File(outputDirectory + "/WEB-INF");
      if (webInf.exists() && webInf.isDirectory()) {
        File[] files = webInf.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("-java.zip");
          }
        });
        File mavenProjectsDir = new File(clientLibsDirectory);
        mavenProjectsDir.mkdirs();
        for (File source : files) {
          unjar(source, mavenProjectsDir);
        }
      }
    } catch (MojoExecutionException e) {
      getLog().error(e);
      throw new MojoExecutionException(
              "Error while generating Google App Engine endpoint get client lib", e);
    }
    getLog().info("Endpoint get client lib generation done. See the maven projects under:"+clientLibsDirectory);
  }
  
  private void unjar(File jar, File destdir) {
    JarFile jarfile;
    try {
      jarfile = new JarFile(jar);
    } catch (IOException ex) {
      Logger.getLogger(EndpointsGetClientLib.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }

    Enumeration<JarEntry> enu = jarfile.entries();
    while (enu.hasMoreElements()) {
      InputStream is = null;
      try {
        JarEntry je = enu.nextElement();
        File fl = new File(destdir, je.getName());
        if (!fl.exists()) {
          fl.getParentFile().mkdirs();
          fl = new java.io.File(destdir , je.getName());
        }
        if (je.isDirectory()) {
          continue;
        }
        is = jarfile.getInputStream(je);
        FileOutputStream fo = new FileOutputStream(fl);
        while (is.available() > 0) {
          fo.write(is.read());
        }
      } catch (IOException ex) {
        Logger.getLogger(EndpointsGetClientLib.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        try {
         if (is!=null) is.close();
        } catch (IOException ex) {
          Logger.getLogger(EndpointsGetClientLib.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
}
