/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.appengine.repackaged.com.google.api.client.util.Throwables;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.google.appengine.repackaged.com.google.common.base.Objects.firstNonNull;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Runs the App Engine development server.
 *
 * @author Ludo
 * @goal gcloud_app_run
 * @execute phase="package"
 * @threadSafe false
 */
public class GCloudAppRun extends AbstractGcloudMojo {

  /**
   * The address of the interface on the local machine to bind to (or 0.0.0.0
   * for all interfaces).
   *
   * @parameter expression="${appengine.address}"
   */
  private String address;

  /**
   * The host and port on which to start the API server (in the format
   * host:port)
   *
   * @parameter expression="${appengine.gcloud_app_api_host}"
   */
  private String gcloud_app_api_host;

  /**
   * Additional directories containing App Engine modules to be run.
   *
   * @parameter expression="${appengine.gcloud_modules}"
   */
  private List<String> gcloud_modules;

  /**
   * The host and port on which to start the local web server (in the format
   * host:port)
   *
   * @parameter expression="${appengine.gcloud_app_host}"
   */
  private String gcloud_app_host;

  /**
   * The host and port on which to start the admin server (in the format
   * host:port)
   *
   * @parameter expression="${appengine.gcloud_app_admin_host}"
   */
  private String gcloud_app_admin_host;

  /**
   * The default location for storing application data. Can be overridden for
   * specific kinds of data using --datastore-path, --blobstore-path, and/or
   * --logs-path
   *
   * @parameter expression="${appengine.gcloud_app_storage_path}"
   */
  private String gcloud_app_storage_path;

  /**
   * The minimum verbosity of logs from your app that will be displayed in the
   * terminal. (debug, info, warning, critical, error) Defaults to current
   * verbosity setting.
   *
   * @parameter expression="${appengine.gcloud_app_log_level}"
   */
  private String gcloud_app_log_level;
  /**
   * Path to a file used to store request logs (defaults to a file in
   * --storage-path if not set)
   *
   * @parameter expression="${appengine.gcloud_app_logs_path}"
   */
  private String gcloud_app_logs_path;
  /**
   * name of the authorization domain to use (default: gmail.com)
   *
   * @parameter expression="${appengine.gcloud_app_auth_domain}"
   */
  private String gcloud_app_auth_domain;

  /**
   * the maximum number of runtime instances that can be started for a
   * particular module - the value can be an integer, in what case all modules
   * are limited to that number of instances or a comma-separated list of
   * module:max_instances e.g. "default:5,backend:3" (default: None)
   *
   * @parameter expression="${appengine.gcloud_app_max_module_instances}"
   */
  private String gcloud_app_max_module_instances;

  /**
   * email address associated with a service account that has a downloadable
   * key. May be None for no local application identity. (default: None)
   *
   * @parameter expression="${appengine.gcloud_app_appidentity_email_address}"
   */
  private String gcloud_app_appidentity_email_address;

  /**
   * path to private key file associated with service account (.pem format).
   * Must be set if appidentity_email_address is set. (default: None)
   *
   *
   * @parameter
   * expression="${appengine.gcloud_app_appidentity_private_key_path}"
   */
  private String gcloud_app_appidentity_private_key_path;

  /**
   * path to directory used to store blob contents (defaults to a subdirectory
   * of --storage_path if not set) (default: None)
   *
   * @parameter expression="${appengine.gcloud_app_blobstore_path}"
   */
  private String gcloud_app_blobstore_path;

  /**
   * path to a file used to store datastore contents (defaults to a file in
   * --storage_path if not set) (default: None)
   *
   * @parameter expression="${appengine.gcloud_app_datastore_path}"
   */
  private String gcloud_app_datastore_path;
  /**
   * clear the datastore on startup (default: False)
   *
   *
   * @parameter expression="${appengine.gcloud_app_clear_datastore}"
   */
  private boolean gcloud_app_clear_datastore;

  /**
   * make files specified in the app.yaml "skip_files" or "static" handles
   * readable by the application. (default: False)
   *
   * @parameter expression="${appengine.gcloud_app_allow_skipped_files}"
   */
  private boolean gcloud_app_allow_skipped_files;

  /**
   * Enable logs collection and display in local Admin Console for Managed VM
   * modules.
   *
   * @parameter expression="${appengine.gcloud_app_enable_mvm_logs}"
   */
  private boolean gcloud_app_enable_mvm_logs;

  /**
   * Use the "sendmail" tool to transmit e-mail sent using the Mail API (ignored
   * if --smtp-host is set)
   *
   * @parameter expression="${appengine.gcloud_app_enable_sendmail}"
   */
  private boolean gcloud_app_enable_sendmail;
  /**
   * Use mtime polling for detecting source code changes - useful if modifying
   * code from a remote machine using a distributed file system
   *
   * @parameter expression="${appengine.gcloud_app_use_mtime_file_watcher}"
   */
  private boolean gcloud_app_use_mtime_file_watcher;
  /**
   * JVM_FLAG Additional arguments to pass to the java command when launching an
   * instance of the app. May be specified more than once. Example: "-Xmx1024m
   * --jvm-flag=-Xms256m"
   *
   * @parameter expression="${appengine.gcloud_app_jvm_flag}"
   */
  private String gcloud_app_jvm_flag;

  /**
   * default Google Cloud Storage bucket name (default: None)
   *
   * @parameter expression="${appengine.gcloud_app_default_gcs_bucket_name}"
   */
  private String gcloud_app_default_gcs_bucket_name;
  /**
   * enable_cloud_datastore
   *
   * @parameter expression="${appengine.gcloud_app_enable_cloud_datastore}"
   */
  private boolean gcloud_app_enable_cloud_datastore;

  /**
   * datastore_consistency_policy The policy to apply when deciding whether a
   * datastore write should appear in global queries (default="time")
   *
   * @parameter
   * expression="${appengine.gcloud_app_datastore_consistency_policy}"
   */
  private String gcloud_app_datastore_consistency_policy;

  /**
   * The full path to the PHP executable to use to run your PHP module
   *
   * @parameter expression="${appengine.gcloud_app_php_executable_path}"
   */
  private String gcloud_app_php_executable_path;
  /**
   * The script to run at the startup of new Python runtime instances (useful
   * for tools such as debuggers)
   *
   * @parameter expression="${appengine.gcloud_app_python_startup_script}"
   */
  private String gcloud_app_python_startup_script;
  /**
   * Generate an error on datastore queries that require a composite index not
   * found in index.yaml
   *
   * @parameter expression="${appengine.gcloud_app_require_indexes}"
   */
  private boolean gcloud_app_require_indexes;
  /**
   * Logs the contents of e-mails sent using the Mail API
   *
   * @parameter expression="${appengine.gcloud_app_show_mail_body}"
   */
  private boolean gcloud_app_show_mail_body;
  /**
   * Allow TLS to be used when the SMTP server announces TLS support (ignored if
   * --smtp-host is not set)
   *
   * @parameter expression="${appengine.gcloud_app_smtp_allow_tls}"
   */
  private boolean gcloud_app_smtp_allow_tls;
  /**
   * The host and port of an SMTP server to use to transmit e-mail sent using
   * the Mail API, in the format host:port
   *
   * @parameter expression="${appengine.gcloud_app_smtp_host}"
   */
  private String gcloud_app_smtp_host;
  /**
   * Password to use when connecting to the SMTP server specified with
   * --smtp-host
   *
   * @parameter expression="${appengine.gcloud_app_smtp_password}"
   */
  private String gcloud_app_smtp_password;
  /**
   * Username to use when connecting to the SMTP server specified with
   * --smtp-host
   *
   * @parameter expression="${appengine.gcloud_app_smtp_user}"
   */
  private String gcloud_app_smtp_user;

  /**
   * The location of the appengine application to run.
   *
   * @parameter expression="${appengine.appDir}"
   */
  protected String appDir;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    if (appDir == null) {
      appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    }
    File appDirFile = new File(appDir);
    if (!appDirFile.exists()) {
      throw new MojoExecutionException("The application directory does not exist : " + appDir);
    }
    if (!appDirFile.isDirectory()) {
      throw new MojoExecutionException("The application directory is not a directory : " + appDir);
    }
    ArrayList<String> devAppServerCommand = getCommand(appDir);
    startCommand(appDirFile, devAppServerCommand, WaitDirective.WAIT_SERVER_STOPPED);
  }

  protected ArrayList<String> getCommand(String appDir) throws MojoExecutionException {

    getLog().info("Running gcloud app run...");

    ArrayList<String> devAppServerCommand = new ArrayList<>();
    setupInitialCommands(devAppServerCommand);

    devAppServerCommand.add("run");

    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (!f.exists()) { // EAR project possibly, add all modules one by one:
      File ear = new File(appDir);
      for (File w : ear.listFiles()) {
        if (new File(w, "WEB-INF/appengine-web.xml").exists()) {
          devAppServerCommand.add(w.getAbsolutePath());

        }
      }

    } else {
      // Point to our application
      devAppServerCommand.add(appDir);
    }

    if ((gcloud_modules != null) && !gcloud_modules.isEmpty()) {
      for (String modDir : gcloud_modules) {
        getLog().info("Running gcloud app run with extra module in " + modDir);
        devAppServerCommand.add(modDir);

      }

    }
    setupExtraCommands(devAppServerCommand);

    // Add in additional options for starting the DevAppServer
    if (gcloud_app_admin_host != null) {
      devAppServerCommand.add("--admin-host=" + gcloud_app_admin_host);
    }
    if (gcloud_app_api_host != null) {
      devAppServerCommand.add("--api-host=" + gcloud_app_api_host);
    }

    if (gcloud_app_storage_path != null) {
      devAppServerCommand.add("--storage-path=" + gcloud_app_storage_path);
    }
    if (gcloud_app_host != null) {
      devAppServerCommand.add("--host=" + gcloud_app_host);
    }
    if (gcloud_app_admin_host != null) {
      devAppServerCommand.add("--admin-host=" + gcloud_app_admin_host);
    }
    if (gcloud_app_storage_path != null) {
      devAppServerCommand.add("--storage-path=" + gcloud_app_storage_path);
    }
    if (gcloud_app_log_level != null) {
      devAppServerCommand.add("--log-level=" + gcloud_app_log_level);
    }
    if (gcloud_app_logs_path != null) {
      devAppServerCommand.add("--logs-path=" + gcloud_app_logs_path);
    }
    if (gcloud_app_auth_domain != null) {
      devAppServerCommand.add("--auth-domain=" + gcloud_app_auth_domain);
    }
    if (gcloud_app_max_module_instances != null) {
      devAppServerCommand.add("--max-module-instances=" + gcloud_app_max_module_instances);
    }
    if (gcloud_app_appidentity_email_address != null) {
      devAppServerCommand.add("--appidentity-email-address=" + gcloud_app_appidentity_email_address);
    }

    if (gcloud_app_appidentity_private_key_path != null) {
      devAppServerCommand.add("--appidentity-private-key-path=" + gcloud_app_appidentity_private_key_path);
    }
    if (gcloud_app_blobstore_path != null) {
      devAppServerCommand.add("--blobstore-path=" + gcloud_app_blobstore_path);
    }
    if (gcloud_app_datastore_path != null) {
      devAppServerCommand.add("--datastore-path=" + gcloud_app_datastore_path);
    }

    if (gcloud_app_clear_datastore) {
      devAppServerCommand.add("--clear-datastore");
    }
    if (gcloud_app_allow_skipped_files) {
      devAppServerCommand.add("--allow-skipped-files");
    }
    if (gcloud_app_enable_mvm_logs) {
      devAppServerCommand.add("--enable-mvm-logs");
    }
    if (gcloud_app_enable_sendmail) {
      devAppServerCommand.add("--enable-sendmail");
    }
    if (gcloud_app_use_mtime_file_watcher) {
      devAppServerCommand.add("--use-mtime-file-watcher");
    }
    if (gcloud_app_jvm_flag != null) {
      devAppServerCommand.add("--host=" + gcloud_app_host);
    }
    if (gcloud_app_default_gcs_bucket_name != null) {
      devAppServerCommand.add("--default-gcs-bucket-name=" + gcloud_app_default_gcs_bucket_name);
    }
    if (gcloud_app_enable_cloud_datastore) {
      devAppServerCommand.add("--enable-cloud-datastore");
    }
    if (gcloud_app_datastore_consistency_policy != null) {
      devAppServerCommand.add("--datastore-consistency-policy=" + gcloud_app_datastore_consistency_policy);
    }
    if (gcloud_app_php_executable_path != null) {
      devAppServerCommand.add("--php-executable-path=" + gcloud_app_php_executable_path);
    }
    if (gcloud_app_python_startup_script != null) {
      devAppServerCommand.add("--python-startup-script=" + gcloud_app_python_startup_script);
    }
    if (gcloud_app_require_indexes) {
      devAppServerCommand.add("--require-indexes");
    }
    if (gcloud_app_show_mail_body) {
      devAppServerCommand.add("--show-mail-body");
    }
    if (gcloud_app_smtp_allow_tls) {
      devAppServerCommand.add("--smtp-allow-tls");
    }
    if (gcloud_app_smtp_host != null) {
      devAppServerCommand.add("--smtp-host=" + gcloud_app_smtp_host);
    }
    if (gcloud_app_smtp_password != null) {
      devAppServerCommand.add("--smtp-password=" + gcloud_app_smtp_password);
    }
    if (gcloud_app_smtp_user != null) {
      devAppServerCommand.add("--smtp-user=" + gcloud_app_smtp_user);
    }
    return devAppServerCommand;
  }

  protected void stopDevAppServer() throws MojoExecutionException {
    HttpURLConnection connection = null;
    try {
      URL url = new URL("http", firstNonNull(address, "localhost"), 8000, "/quit");
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestMethod("GET");
      //     connection.getOutputStream().write(110);
      ByteStreams.toByteArray(connection.getInputStream());
      connection.getOutputStream().flush();
      connection.getOutputStream().close();
      connection.getInputStream().close();
      connection.disconnect();
      getLog().warn("Shutting down gcloud devappserver on port " + 8000);
      Thread.sleep(4000);
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("URL malformed attempting to stop the devserver : " + e.getMessage());
    } catch (IOException e) {
      getLog().debug("Was not able to contact the devappserver to shut it down.  Most likely this is due to it simply not running anymore. ", e);
    } catch (InterruptedException e) {
      Throwables.propagate(e);
    }
  }

}
