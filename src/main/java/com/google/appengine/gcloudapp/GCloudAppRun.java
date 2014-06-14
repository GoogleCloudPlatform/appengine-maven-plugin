/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 */
package com.google.appengine.gcloudapp;

import com.google.appengine.repackaged.com.google.api.client.util.Throwables;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

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
   * The project's remote repositories to use for the resolution of project
   * dependencies.
   *
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> projectRepos;

  /**
   * The project's remote repositories to use for the resolution of plugins and
   * their dependencies.
   *
   * @parameter default-value="${project.remotePluginRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> pluginRepos;

  /**
   * The server to use to determine the latest SDK version.
   *
   * @parameter expression="${appengine.server}"
   */
  protected String server;

  /**
   * The address of the interface on the local machine to bind to (or 0.0.0.0
   * for all interfaces).
   *
   * @parameter expression="${appengine.address}"
   */
  protected String address;

  /**
   * The port number to bind to on the local machine.
   *
   * @parameter expression="${appengine.port}"
   */
  protected Integer port;

  /**
   * host to which the server for API calls should bind (default: locahost)
   *
   * @parameter expression="${appengine.gcloud_app_api_host}"
   */
  protected String gcloud_app_api_host;

  /**
   * gcloud installation directory
   *
   * @parameter expression="${appengine.gcloud_directory}"
   */
  protected String gcloud_directory;

  /**
   * Additional directories containing App Engine modules to be run.
   * 
   * @parameter expression="${appengine.gcloud_modules}"
   */
  private List<String> gcloud_modules;
  ///////
  /**
   * host name to which application modules should bind (default: localhost)
   *
   * @parameter expression="${appengine.gcloud_app_host}"
   */
  protected String gcloud_app_host;

  /**
   * lowest port to which application modules should bind (default: 8080)
   *
   * @parameter expression="${appengine.gcloud_app_port}"
   */
  protected Integer gcloud_app_port;
  /**
   * host name to which the admin server should bind (default: localhost)
   *
   * @parameter expression="${appengine.gcloud_app_admin_host}"
   */
  protected String gcloud_app_admin_host;

  /**
   * port to which the admin server should bind (default: 8000)
   *
   * @parameter expression="${appengine.gcloud_app_admin_port}"
   */
  protected Integer gcloud_app_admin_port;

  /**
   * The path to the datastore, or blobstore to use for this application.
   *
   * @parameter expression="${appengine.gcloud_app_storage_path}"
   */
  protected String gcloud_app_storage_path;

  /**
   * The minimum verbosity of logs from your app that will be displayed in the
   * terminal. (debug, info, warning, critical, error) Defaults to current
   * verbosity setting.
   *
   * @parameter expression="${appengine.gcloud_app_log_level}"
   */
  protected String gcloud_app_log_level;

  /**
   * Google Cloud Platform project to use for this invocation.
   *
   * @parameter expression="${appengine.gcloud_project}"
   */
  
  protected String gcloud_project;
  /**
   * Override the default verbosity for this command. 
   * This must be a standard logging verbosity level: [debug, info,
   *  warning, error, critical, none] (Default: [warning]).
   *
   * @parameter expression="${appengine.gcloud_verbosity}"
   */
  protected String gcloud_verbosity;
  
  /**
   * name of the authorization domain to use (default: gmail.com)
   *
   * @parameter expression="${appengine.gcloud_app_auth_domain}"
   */
//  protected String gcloud_app_auth_domain;
//  /**
//   * path to the data (datastore, blobstore, etc.) associated with the
//   * application. (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_storage_path}"
//   */
//  protected String gcloud_app_storage_path;
//  /**
//   * the log level below which logging messages generated by application code
//   * will not be displayed on the console (default: info)
//   *
//   * @parameter expression="${appengine.gcloud_app_log_level}"
//   */
//  protected String gcloud_app_log_level;
//  /**
//   * the maximum number of runtime instances that can be started for a
//   * particular module - the value can be an integer, in what case all modules
//   * are limited to that number of instances or a comma-separated list of
//   * module:max_instances e.g. "default:5,backend:3" (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_max_module_instances}"
//   */
//  protected String gcloud_app_max_module_instances;
//
//  /**
//   * use mtime polling for detecting source code changes - useful if modifying
//   * code from a remote machine using a distributed file system (default: False)
//   *
//   * @parameter expression="${appengine.gcloud_app_use_mtime_file_watcher}"
//   */
//  protected boolean gcloud_app_use_mtime_file_watcher;
//  /**
//   * override the application's threadsafe configuration - the value can be a
//   * boolean, in which case all modules threadsafe setting will be overridden or
//   * a comma- separated list of module:threadsafe_override e.g.
//   * "default:False,backend:True" (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_threadsafe_override}"
//   */
//  protected String gcloud_app_threadsafe_override;
//
//  /**
//   * email address associated with a service account that has a downloadable
//   * key. May be None for no local application identity. (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_appidentity_email_address}"
//   */
//  protected String gcloud_app_appidentity_email_address;
//
//  /**
//   * path to private key file associated with service account (.pem format).
//   * Must be set if appidentity_email_address is set. (default: None)
//   *
//   *
//   * @parameter
//   * expression="${appengine.gcloud_app_appidentity_private_key_path}"
//   */
//  protected String gcloud_app_appidentity_private_key_path;
//
//  /**
//   * the script to run at the startup of new Python runtime instances (useful
//   * for tools such as debuggers. (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_python_startup_script}"
//   */
//  protected String gcloud_app_python_startup_script;
//
//  /**
//   * the arguments made available to the script specified in
//   * --python_startup_script. (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_python_startup_args}"
//   */
//  protected String gcloud_app_python_startup_args;
//  /**
//   * path to directory used to store blob contents (defaults to a subdirectory
//   * of --storage_path if not set) (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_blobstore_path}"
//   */
//  protected String gcloud_app_blobstore_path;
//  /**
//   * host name of a running MySQL server used for simulated Google Cloud SQL
//   * storage (default: localhost)
//   *
//   * @parameter expression="${appengine.gcloud_app_mysql_host}"
//   */
//  protected String gcloud_app_mysql_host;
//  /**
//   * port number of a running MySQL server used for simulated Google Cloud SQL
//   * storage (default: 3306)
//   *
//   * @parameter expression="${appengine.gcloud_app_mysql_port}"
//   */
//  protected Integer gcloud_app_mysql_port;
//  /**
//   * username to use when connecting to the MySQL server specified in
//   * --mysql_host and --mysql_port or --mysql_socket (default: )
//   *
//   * @parameter expression="${appengine.gcloud_app_mysql_user}"
//   */
//  protected String gcloud_app_mysql_user;
//  /**
//   * password to use when connecting to the MySQL server specified in
//   * --mysql_host and --mysql_port or --mysql_socket (default: )
//   *
//   * @parameter expression="${appengine.gcloud_app_mysql_password}"
//   */
//  protected String gcloud_app_mysql_password;
//  /**
//   * path to a Unix socket file to use when connecting to a running MySQL server
//   * used for simulated Google Cloud SQL storage (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_mysql_socket}"
//   */
//  protected String gcloud_app_mysql_socket;
//
//  /**
//   * path to a file used to store datastore contents (defaults to a file in
//   * --storage_path if not set) (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_datastore_path}"
//   */
//  protected String gcloud_app_datastore_path;
//  /**
//   * clear the datastore on startup (default: False)
//   *
//   *
//   * @parameter expression="${appengine.gcloud_app_clear_datastore}"
//   */
//  protected boolean gcloud_app_clear_datastore;
//
//  ////////Miscellaneous
//  ////////
//  ////////
//  ////////
//  ////////
//  /**
//   * make files specified in the app.yaml "skip_files" or "static" handles
//   * readable by the application. (default: False)
//   *
//   * @parameter expression="${appengine.gcloud_app_allow_skipped_files}"
//   */
//  protected boolean gcloud_app_allow_skipped_files;

//  /**
//   * restart instances automatically when files relevant to their module are
//   * changed (default: True)
//   *
//   * @parameter expression="${appengine.gcloud_app_automatic_restart}"
//   */
//  protected boolean gcloud_app_automatic_restart;
//  /**
//   * {debug,info,warning,critical,error} the log level below which logging
//   * messages generated by the development server will not be displayed on the
//   * console (this flag is more useful for diagnosing problems in
//   * dev_appserver.py rather than in application code) (default: info)
//   *
//   * @parameter expression="${appengine.gcloud_app_dev_appserver_log_level}"
//   */
//  protected String gcloud_app_dev_appserver_log_level;
//  /**
//   * skip checking for SDK updates (if false, use .appcfg_nag to decide)
//   * (default: true)
//   *
//   * @parameter expression="${appengine.gcloud_app_skip_sdk_update_check}"
//   */
//  protected boolean gcloud_app_skip_sdk_update_check=true;
//  /**
//   * default Google Cloud Storgage bucket name (default: None)
//   *
//   * @parameter expression="${appengine.gcloud_app_default_gcs_bucket_name}"
//   */
//  protected String gcloud_app_default_gcs_bucket_name;
  /**
   * docker_host
   *
   * @parameter expression="${appengine.gcloud_app_docker_host}"
   */
  protected String gcloud_app_docker_host;

  /**
   * enable_cloud_datastore
   *
   * @parameter expression="${appengine.gcloud_app_enable_cloud_datastore}"
   */
  protected boolean gcloud_app_enable_cloud_datastore;
  /*
   Common:
   --host HOST           host name to which application modules should bind
   (default: localhost)
   --port PORT           lowest port to which application modules should bind
   (default: 8080)
   --admin_host ADMIN_HOST
   host name to which the admin server should bind
   (default: localhost)
   --admin_port ADMIN_PORT
   port to which the admin server should bind (default:
   8000)
   --auth_domain AUTH_DOMAIN
   name of the authorization domain to use (default:
   gmail.com)
   --storage_path PATH   path to the data (datastore, blobstore, etc.)
   associated with the application. (default: None)
   --log_level {debug,info,warning,critical,error}
   the log level below which logging messages generated
   by application code will not be displayed on the
   console (default: info)
   --max_module_instances MAX_MODULE_INSTANCES
   the maximum number of runtime instances that can be
   started for a particular module - the value can be an
   integer, in what case all modules are limited to that
   number of instances or a comma-seperated list of
   module:max_instances e.g. "default:5,backend:3"
   (default: None)
   --use_mtime_file_watcher [USE_MTIME_FILE_WATCHER]
   use mtime polling for detecting source code changes -
   useful if modifying code from a remote machine using a
   distributed file system (default: False)
   --threadsafe_override THREADSAFE_OVERRIDE
   override the application's threadsafe configuration -
   the value can be a boolean, in which case all modules
   threadsafe setting will be overridden or a comma-
   separated list of module:threadsafe_override e.g.
   "default:False,backend:True" (default: None)

   PHP:
   --php_executable_path PATH
   path to the PHP executable (default: None)
   --php_remote_debugging [PHP_REMOTE_DEBUGGING]
   enable XDebug remote debugging (default: False)

   Application Identity:
   --appidentity_email_address APPIDENTITY_EMAIL_ADDRESS
   email address associated with a service account that
   has a downloadable key. May be None for no local
   application identity. (default: None)
   --appidentity_private_key_path APPIDENTITY_PRIVATE_KEY_PATH
   path to private key file associated with service
   account (.pem format). Must be set if
   appidentity_email_address is set. (default: None)

   Python:
   --python_startup_script PYTHON_STARTUP_SCRIPT
   the script to run at the startup of new Python runtime
   instances (useful for tools such as debuggers.
   (default: None)
   --python_startup_args PYTHON_STARTUP_ARGS
   the arguments made available to the script specified
   in --python_startup_script. (default: None)

   Blobstore API:
   --blobstore_path BLOBSTORE_PATH
   path to directory used to store blob contents
   (defaults to a subdirectory of --storage_path if not
   set) (default: None)

   Cloud SQL:
   --mysql_host MYSQL_HOST
   host name of a running MySQL server used for simulated
   Google Cloud SQL storage (default: localhost)
   --mysql_port MYSQL_PORT
   port number of a running MySQL server used for
   simulated Google Cloud SQL storage (default: 3306)
   --mysql_user MYSQL_USER
   username to use when connecting to the MySQL server
   specified in --mysql_host and --mysql_port or
   --mysql_socket (default: )
   --mysql_password MYSQL_PASSWORD
   passpord to use when connecting to the MySQL server
   specified in --mysql_host and --mysql_port or
   --mysql_socket (default: )
   --mysql_socket MYSQL_SOCKET
   path to a Unix socket file to use when connecting to a
   running MySQL server used for simulated Google Cloud
   SQL storage (default: None)

   Datastore API:
   --datastore_path DATASTORE_PATH
   path to a file used to store datastore contents
   (defaults to a file in --storage_path if not set)
   (default: None)
   --clear_datastore [CLEAR_DATASTORE]
   clear the datastore on startup (default: False)
   --datastore_consistency_policy {consistent,random,time}
   the policy to apply when deciding whether a datastore
   write should appear in global queries (default: time)
   --require_indexes [REQUIRE_INDEXES]
   generate an error on datastore queries that requires a
   composite index not found in index.yaml (default:
   False)
   --auto_id_policy {sequential,scattered}
   the type of sequence from which the datastore stub
   assigns automatic IDs. NOTE: Sequential IDs are
   deprecated. This flag will be removed in a future
   release. Please do not rely on sequential IDs in your
   tests. (default: scattered)
   --enable_cloud_datastore [ENABLE_CLOUD_DATASTORE]
   enable GCD api support for the datastore. (default:
   False)

   Logs API:
   --logs_path LOGS_PATH
   path to a file used to store request logs (defaults to
   a file in --storage_path if not set) (default: None)

   Mail API:
   --show_mail_body [SHOW_MAIL_BODY]
   logs the contents of e-mails sent using the Mail API
   (default: False)
   --enable_sendmail [ENABLE_SENDMAIL]
   use the "sendmail" tool to transmit e-mail sent using
   the Mail API (ignored if --smpt_host is set) (default:
   False)
   --smtp_host SMTP_HOST
   host name of an SMTP server to use to transmit e-mail
   sent using the Mail API (default: )
   --smtp_port SMTP_PORT
   port number of an SMTP server to use to transmit
   e-mail sent using the Mail API (ignored if --smtp_host
   is not set) (default: 25)
   --smtp_user SMTP_USER
   username to use when connecting to the SMTP server
   specified in --smtp_host and --smtp_port (default: )
   --smtp_password SMTP_PASSWORD
   password to use when connecting to the SMTP server
   specified in --smtp_host and --smtp_port (default: )

   Prospective Search API:
   --prospective_search_path PROSPECTIVE_SEARCH_PATH
   path to a file used to store the prospective search
   subscription index (defaults to a file in
   --storage_path if not set) (default: None)
   --clear_prospective_search [CLEAR_PROSPECTIVE_SEARCH]
   clear the prospective search subscription index
   (default: False)

   Search API:
   --search_indexes_path SEARCH_INDEXES_PATH
   path to a file used to store search indexes (defaults
   to a file in --storage_path if not set) (default:
   None)
   --clear_search_indexes [CLEAR_SEARCH_INDEXES]
   clear the search indexes (default: False)

   Task Queue API:
   --enable_task_running [ENABLE_TASK_RUNNING]
   run "push" tasks created using the taskqueue API
   automatically (default: True)


  
   usage: dev_appserver.py [-h] 
   [--host HOST] 
   [--port PORT]
   [--admin_host ADMIN_HOST] 
   [--admin_port ADMIN_PORT]
   [--auth_domain AUTH_DOMAIN] [--storage_path PATH]
   [--log_level {debug,info,warning,critical,error}]
   [--max_module_instances MAX_MODULE_INSTANCES]
   [--use_mtime_file_watcher [USE_MTIME_FILE_WATCHER]]
   [--threadsafe_override THREADSAFE_OVERRIDE]
   [--php_executable_path PATH]
   [--php_remote_debugging [PHP_REMOTE_DEBUGGING]]
   [--appidentity_email_address APPIDENTITY_EMAIL_ADDRESS]
   [--appidentity_private_key_path APPIDENTITY_PRIVATE_KEY_PATH]
   [--python_startup_script PYTHON_STARTUP_SCRIPT]
   [--python_startup_args PYTHON_STARTUP_ARGS]
   [--blobstore_path BLOBSTORE_PATH]
   [--mysql_host MYSQL_HOST] [--mysql_port MYSQL_PORT]
   [--mysql_user MYSQL_USER]
   [--mysql_password MYSQL_PASSWORD]
   [--mysql_socket MYSQL_SOCKET]
   [--datastore_path DATASTORE_PATH]
   [--clear_datastore [CLEAR_DATASTORE]]
   [--datastore_consistency_policy {consistent,random,time}]
   [--require_indexes [REQUIRE_INDEXES]]
   [--auto_id_policy {sequential,scattered}]
   [--enable_cloud_datastore [ENABLE_CLOUD_DATASTORE]]
   [--logs_path LOGS_PATH]
   [--show_mail_body [SHOW_MAIL_BODY]]
   [--enable_sendmail [ENABLE_SENDMAIL]]
   [--smtp_host SMTP_HOST] [--smtp_port SMTP_PORT]
   [--smtp_user SMTP_USER]
   [--smtp_password SMTP_PASSWORD]
   [--prospective_search_path PROSPECTIVE_SEARCH_PATH]
   [--clear_prospective_search [CLEAR_PROSPECTIVE_SEARCH]]
   [--search_indexes_path SEARCH_INDEXES_PATH]
   [--clear_search_indexes [CLEAR_SEARCH_INDEXES]]
   [--enable_task_running [ENABLE_TASK_RUNNING]]
   [--allow_skipped_files [ALLOW_SKIPPED_FILES]]
   [--api_port API_PORT]
   [--automatic_restart [AUTOMATIC_RESTART]]
   [--dev_appserver_log_level {debug,info,warning,critical,error}]
   [--skip_sdk_update_check [SKIP_SDK_UPDATE_CHECK]]
   [--default_gcs_bucket_name DEFAULT_GCS_BUCKET_NAME]
   yaml_or_war_path [yaml_or_war_path ...]  
  
   */

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * The location of the appengine application to run.
   *
   * @parameter expression="${appengine.appDir}"
   */
  protected String appDir;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    if(appDir == null) {
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

    ArrayList<String> devAppServerCommand = new ArrayList<String>();
    devAppServerCommand.add("python");
    devAppServerCommand.add("-S");
    if (gcloud_directory != null) {
      devAppServerCommand.add(gcloud_directory + "/lib/googlecloudsdk/gcloud/gcloud.py");
    } else {
      String gcloud = System.getProperty("user.home") + "/google-cloud-sdk/lib/googlecloudsdk/gcloud/gcloud.py";
      getLog().info("Warning, gcloud_directory was not set, so taking: " + gcloud);
    }
    if (gcloud_project != null) {
      devAppServerCommand.add("--project=" + gcloud_project);
    }
    if (gcloud_verbosity != null) {
      devAppServerCommand.add("--verbosity=" + gcloud_verbosity);
    }

    devAppServerCommand.add("preview");
    devAppServerCommand.add("app");
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
    // Add in additional options for starting the DevAppServer
    if (gcloud_app_docker_host != null) {
      devAppServerCommand.add("--docker-host=" + gcloud_app_docker_host);
    }

    if (gcloud_app_api_host != null) {
      devAppServerCommand.add("--api-host=" + gcloud_app_api_host);
    }

    if (gcloud_app_enable_cloud_datastore) {
      devAppServerCommand.add("--enable-cloud-datastore");
    }

    if (gcloud_app_admin_host != null) {
      devAppServerCommand.add("--admin-host=" + gcloud_app_admin_host);
    }
    if (gcloud_app_host != null) {
      devAppServerCommand.add("--host=" + gcloud_app_host);
    }
    if (gcloud_app_log_level != null) {
      devAppServerCommand.add("--log-level=" + gcloud_app_log_level);
    }
    if (gcloud_app_storage_path != null) {
      devAppServerCommand.add("--storage_path=" + gcloud_app_storage_path);
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
