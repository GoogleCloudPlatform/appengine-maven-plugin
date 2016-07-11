/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import java.io.File;
import java.util.ArrayList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Create or update an app version.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal update
 * @execute phase="package"
 */
public class Update extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Google App Engine Java SDK - Updating Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    resolveAndSetSdkRoot();

    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

    getLog().info("Updating Google App Engine Application");

    executeAppCfgCommand("update", appDir);
  }
  
  @Override
  protected ArrayList<String> collectParameters() {
    ArrayList<String> args = new ArrayList<>();
    
    // Add runtime specific extra args.
    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (f.exists()) {

      AppEngineWebXmlReader aewebReader = new AppEngineWebXmlReader(appDir);
      AppEngineWebXml appEngineWebXml = aewebReader.readAppEngineWebXml();
      String runtime = appEngineWebXml.getRuntime();
      if (runtime != null) {
        if (runtime.startsWith("java8")) {
          args.add("-R");
          args.add("--runtime=" + runtime);
          args.add("--use_java8");
        }
      }
    }
    args.addAll(super.collectParameters());
    return args;
  }
}
