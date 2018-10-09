/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.appcfg;

import com.google.appengine.tools.admin.Application;
import com.google.apphosting.utils.config.EarHelper;
import com.google.apphosting.utils.config.EarInfo;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Rollback an in-progress update.
 *
 * @author Matt Stephenson <mattstep@google.com>
 * @goal rollback
 * @execute phase="package"
 */
public class Rollback extends AbstractAppCfgMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    getLog().info("");
    getLog().info("Google App Engine Java SDK - Rolling Back Application");
    getLog().info("");

    getLog().info("Retrieving Google App Engine Java SDK from Maven");
    resolveAndSetSdkRoot();

    getLog().info("Rolling Back Google App Engine Application");

    if (EarHelper.isEar(getAppDir(), false)) {
      EarInfo earInfo = EarHelper.readEarInfo(getAppDir(),
              new File(Application.getSdkDocsDir(), "appengine-application.xsd"));
      if (appId == null) {
        appId = earInfo.getAppengineApplicationXml().getApplicationId();
      }
      File ear = new File(getAppDir());
      for (File w : ear.listFiles()) {
        if (new File(w, "WEB-INF/appengine-web.xml").exists()) {
          getLog().info("Rolling Back Google App Engine module: " + w.getAbsolutePath());
          executeAppCfgCommand("rollback", w.getAbsolutePath());
        }
      }
    } else {
      // rollback the application
      getLog().info("Rolling Back Google App Engine Application");
      executeAppCfgCommand("rollback", getAppDir());
    }
  }

}
