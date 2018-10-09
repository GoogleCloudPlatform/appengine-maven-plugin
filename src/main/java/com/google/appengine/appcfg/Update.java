/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import com.google.appengine.gcloudapp.GCloudAppDeploy;
import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import java.io.File;
import org.apache.maven.project.MavenProject;

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

    getLog().info("Updating Google App Engine Application");
    String appDir = getAppDir();
    Boolean useCloud = false;
    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (f.exists()) {
      AppEngineWebXmlReader aewebReader = new AppEngineWebXmlReader(appDir);
      AppEngineWebXml appEngineWebXml = aewebReader.readAppEngineWebXml();
      useCloud = "java11".equals(appEngineWebXml.getRuntime());
    }

    if (useCloud) {
      CloudDeploy deploy = new CloudDeploy(mavenProject, getProjectId(), this.version);
      deploy.execute();
    } else {
      executeAppCfgCommand("update", getAppDir());
    }
  }

  private class CloudDeploy extends GCloudAppDeploy {

    CloudDeploy(MavenProject project, String app, String version) {
      this.maven_project = project;
      this.staging_directory = project.getBuild().getDirectory() + "/appengine-staging";
      this.docker_host = "localhost";
      this.gcloud_project = app;
      this.version = version;
    }

    @Override
    public void resolveAndSetSdkRoot() {

    }
  }
}
