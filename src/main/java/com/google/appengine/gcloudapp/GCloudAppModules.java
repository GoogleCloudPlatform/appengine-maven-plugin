/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Manage an application via gcloud app modules.
 *
 * @author Ludo
 */
public abstract class GCloudAppModules extends AbstractGcloudMojo {

  /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.server}"
   */
  private String server;

  /**
   * version The version of the app that will be created or replaced by this
   * deployment.
   *
   * @parameter expression="${appengine.gcloud_app_version}"
   */
  private String gcloud_app_version;

  /**
   * server The App Engine server to connect to.
   *
   * @parameter expression="${appengine.gcloud_app_server}"
   */
  private String gcloud_app_server;

  protected abstract String getSubCommand();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    File appDirFile = new File(appDir);
    if (!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + appDir);
    }
    if (!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + appDir);
    }
    ArrayList<String> devAppServerCommand = createCommand(appDir, getSubCommand());
    startCommand(appDirFile, devAppServerCommand, WaitDirective.WAIT_SERVER_STOPPED);
  }

  protected ArrayList<String> createCommand(String appDir, String subCommand) throws MojoExecutionException {

    getLog().info("Running gcloud app modules...");

    ArrayList<String> devAppServerCommand = new ArrayList<>();
    setupInitialCommands(devAppServerCommand);

    devAppServerCommand.add("modules");

    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (!f.exists()) { // EAR project possibly, add all modules one by one:
      File ear = new File(appDir);
      for (File w : ear.listFiles()) {
        if (new File(w, "WEB-INF/appengine-web.xml").exists()) {
          System.out.println("Need to handle EAR for module " + w.getAbsolutePath());
        }
      }
    } else {
      // get module name and module version
      AppEngineWebXmlReader reader = new AppEngineWebXmlReader(appDir);
      AppEngineWebXml xmlContent = reader.readAppEngineWebXml();
      String module = xmlContent.getModule();
      String version = xmlContent.getMajorVersionId();
      devAppServerCommand.add(subCommand);
      if (module == null) {
        module = "default";
      }
      devAppServerCommand.add(module);

      // Add in additional options for starting the DevAppServer
      if (gcloud_app_version != null) {
        devAppServerCommand.add("--version=" + gcloud_app_version);
      } else {
        devAppServerCommand.add("--version=" + version);

      }
    }
    setupExtraCommands(devAppServerCommand);

    if (gcloud_app_server != null) {
      devAppServerCommand.add("--server=" + gcloud_app_server);
    } else if (server != null) {
      devAppServerCommand.add("--server=" + server);
    }

    return devAppServerCommand;
  }

  @Override
  protected ArrayList<String> getCommand(String appDir) throws MojoExecutionException {
    return null; //not used
  }

  /**
   * Delete the module of this app.
   *
   * @goal gcloud_app_delete_module
   * @execute phase="package"
   * @threadSafe false
   */
  static public class Delete extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "delete";
    }
  }

  /**
   * Cancel Deployment.
   *
   * @goal gcloud_app_module_cancel_deployment
   * @execute phase="package"
   * @threadSafe false
   */
  static public class CancelDeployment extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "cancel-deployment";
    }
  }

  /**
   * set default.
   *
   * @goal gcloud_app_module_set_default
   * @execute phase="package"
   * @threadSafe false
   */
  static public class SetDefault extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "set_default";
    }
  }

  /**
   * set Managed (google or self).
   *
   * @goal gcloud_app_module_set_managed
   * @execute phase="package"
   * @threadSafe false
   */
  static public class SetManaged extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "set_managed";
    }
  }

  /**
   * start a module
   *
   * @goal gcloud_app_module_start
   * @execute phase="package"
   * @threadSafe false
   */
  static public class Start extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "start";
    }
  }

  /**
   * stop a module
   *
   * @goal gcloud_app_module_stop
   * @execute phase="package"
   * @threadSafe false
   */
  static public class Stop extends GCloudAppModules {

    @Override
    protected String getSubCommand() {
      return "stop";
    }
  }
}
