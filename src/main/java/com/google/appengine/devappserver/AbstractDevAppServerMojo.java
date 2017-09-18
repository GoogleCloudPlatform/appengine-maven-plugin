/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine.devappserver;

import com.google.appengine.SdkResolver;
import com.google.appengine.repackaged.com.google.api.client.util.Throwables;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import com.google.appengine.tools.development.DevAppServerMain;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.apache.maven.plugin.AbstractMojo;
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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Objects.firstNonNull;
import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import static java.io.File.separator;

/**
 * Abstract class to support development server operations.
 *
 * @author Matt Stephenson <mattstep@google.com>
 */
public abstract class AbstractDevAppServerMojo extends AbstractMojo {

  protected static enum WaitDirective {
    WAIT_SERVER_STARTED,
    WAIT_SERVER_STOPPED
  }

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
   * The project's remote repositories to use for the resolution of plugins and their dependencies.
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
   * The server to use to determine the latest SDK version.
   *
   * @parameter expression="${appengine.server}"
   */
  protected String server;

  /**
   * The address of the interface on the local machine to bind to (or 0.0.0.0 for all interfaces).
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
   * Disable the check for newer SDK version.
   *
   * @parameter expression="${appengine.disableUpdateCheck}"
   */
  protected boolean disableUpdateCheck;

  /**
   * Additional flags to the JVM used to run the dev server.
   *
   * @parameter expression="${appengine.jvmFlags}"
   */
  protected List<String> jvmFlags;

  /**
   * Whether the system is currently offline.
   *
   * @parameter expression="${settings.offline}"
   */
  private boolean offline;
  
    /**
   * The web app scan delay in seconds to check for app changes for app reload.
   * A negative value will cancel the scan thread.
   *
   * @parameter expression="${appengine.fullScanSeconds}" default-value="5"
   */
  protected Integer fullScanSeconds;
  
  private boolean isVM;

  private boolean needsJetty9;
  
  private Map<String, String> processUserDefinedEnv = new HashMap<>();
  
  private String runtime = "java7";
  
  private String serviceName = "default";
  
  protected ArrayList<String> getDevAppServerCommand(String appDir) throws MojoExecutionException {

    getLog().info("Retrieving Google App Engine Java SDK from Maven");

    File sdkBaseDir = SdkResolver.getSdk(project, repoSystem, repoSession, pluginRepos, projectRepos);

    String javaExecutable = joinOnFileSeparator(System.getProperty("java.home"), "bin", "java");

    ArrayList<String> devAppServerCommand = new ArrayList<String>();
    devAppServerCommand.add(javaExecutable);

    if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
      devAppServerCommand.add("-XstartOnFirstThread");
    }
    getInfoFromAppEngineWebXml();
    if (!isVM && !needsJetty9) {
      // Add in the appengine agent
      String appengineAgentJar = new File(sdkBaseDir, joinOnFileSeparator("lib", "agent", "appengine-agent.jar")).getAbsolutePath();
      devAppServerCommand.add("-javaagent:" + appengineAgentJar);
    } else {
       devAppServerCommand.add("-D--enable_all_permissions=true");
    }
 
    if (fullScanSeconds != null) {
      devAppServerCommand.add("-Dappengine.fullscan.seconds="+fullScanSeconds);
    }
    if (needsJetty9) {
       devAppServerCommand.add("-Duse_jetty9_runtime=true");
     
    }
        
    // Setup the classpath to point to the tools jar
    String appengineToolsApiJar = new File(sdkBaseDir, joinOnFileSeparator("lib", "appengine-tools-api.jar")).getAbsolutePath();
    devAppServerCommand.add("-classpath");
    devAppServerCommand.add(appengineToolsApiJar);

    // Add jvm flags
    if(jvmFlags != null && !jvmFlags.isEmpty()) {
      devAppServerCommand.addAll(jvmFlags);
    }

    // Point to the DevAppServerMain class
    devAppServerCommand.add(DevAppServerMain.class.getCanonicalName());

    // Enable the shutdown hook
    devAppServerCommand.add("--allow_remote_shutdown");
    
    if (isVM || needsJetty9) {
       devAppServerCommand.add("--no_java_agent");
    }

    // Add in additional options for starting the DevAppServer
    if(server != null) {
      devAppServerCommand.add("-s");
      devAppServerCommand.add(server);
    }

    if(address != null) {
      devAppServerCommand.add("-a");
      devAppServerCommand.add(address);
    }

    if(port != null) {
      devAppServerCommand.add("-p");
      devAppServerCommand.add(String.valueOf(port));
    }

    if(disableUpdateCheck || offline) {
      devAppServerCommand.add("--disable_update_check");
    }

    // Point to our application
    devAppServerCommand.add(appDir);
    return devAppServerCommand;
  }

  protected void stopDevAppServer() throws MojoExecutionException {
    HttpURLConnection connection = null;
    try {
      Integer port = firstNonNull(this.port, 8080);
      URL url = new URL("http", firstNonNull(address, "localhost"), port, "/_ah/admin/quit");
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setRequestMethod("POST");
      connection.getOutputStream().write(10);
      ByteStreams.toByteArray(connection.getInputStream());
      connection.getOutputStream().flush();
      connection.getOutputStream().close();
      connection.getInputStream().close();
      connection.disconnect();
      getLog().warn("Shutting down devappserver on port " + port);
      Thread.sleep(2000);
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("URL malformed attempting to stop the devserver : " + e.getMessage());
    } catch (IOException e) {
      getLog().debug("Was not able to contact the devappserver to shut it down.  Most likely this is due to it simply not running anymore. ", e);
    } catch (InterruptedException e) {
      Throwables.propagate(e);
    }
  }

  protected void startDevAppServer(File appDirFile, ArrayList<String> devAppServerCommand, WaitDirective waitDirective) throws MojoExecutionException {
    getLog().info("Running " + Joiner.on(" ").join(devAppServerCommand));

    Thread stdOutThread = null;
    Thread stdErrThread = null;
    try {

      ProcessBuilder processBuilder = new ProcessBuilder(devAppServerCommand);
      
      Map<String, String> env = processBuilder.environment();
      env.put("GAE_ENV", "localdev");
      env.put("GAE_RUNTIME", runtime);
      env.put("GAE_SERVICE", serviceName);
      if (processUserDefinedEnv != null) {
        env.putAll(processUserDefinedEnv);
      }
       processBuilder.directory(appDirFile);

      processBuilder.redirectErrorStream(true);

      //Just before starting, just to make sure, shut down any running devserver on this port.
      stopDevAppServer();

      final Process devServerProcess = processBuilder.start();

      final CountDownLatch waitStartedLatch = new CountDownLatch(1);

      final Scanner stdOut = new Scanner(devServerProcess.getInputStream());
      stdOutThread = new Thread("standard-out-redirection-devappserver") {
        public void run() {
          try {
            while(stdOut.hasNextLine() && !Thread.interrupted()) {
              String line = stdOut.nextLine();
              getLog().info(line);
              if(line.contains("Dev App Server is now running")) {
                waitStartedLatch.countDown();
              }
            }
          } finally {
            waitStartedLatch.countDown();
          }
        }
      };
      stdOutThread.setDaemon(true);
      stdOutThread.start();

      final Scanner stdErr = new Scanner(devServerProcess.getErrorStream());
      stdErrThread = new Thread("standard-err-redirection-devappserver") {
        public void run() {
          while (stdErr.hasNextLine() && !Thread.interrupted()) {
            getLog().error(stdErr.nextLine());
          }
        }
      };
      stdErrThread.setDaemon(true);
      stdErrThread.start();

      if(waitDirective == WaitDirective.WAIT_SERVER_STOPPED) {
        Runtime.getRuntime().addShutdownHook(new Thread("destroy-devappserver") {
          @Override
          public void run() {
            if (devServerProcess != null) {
              devServerProcess.destroy();
            }
          }
        });

        devServerProcess.waitFor();
      } else if(waitDirective == WaitDirective.WAIT_SERVER_STARTED) {
        waitStartedLatch.await();
      }

    } catch (IOException e) {
      throw new MojoExecutionException("Could not start the dev app server", e);
    } catch (InterruptedException e) {
    }
  }

  private String joinOnFileSeparator(String... pathComponents) {
    return Joiner.on(separator).join(ImmutableList.copyOf(pathComponents));
  }
  
  private void getInfoFromAppEngineWebXml() {
    String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
    File f = new File(appDir, "WEB-INF/appengine-web.xml");
    if (!f.exists()) { // EAR project possibly.
      return ;
    }

    AppEngineWebXmlReader aewebReader = new AppEngineWebXmlReader(appDir);
    AppEngineWebXml appEngineWebXml = aewebReader.readAppEngineWebXml();
    isVM = appEngineWebXml.getUseVm() || appEngineWebXml.isFlexible();
    runtime = appEngineWebXml.getRuntime();
    if (appEngineWebXml.getService() != null) {
      serviceName = appEngineWebXml.getService();
    } else if (appEngineWebXml.getModule() != null) {
      serviceName = appEngineWebXml.getModule();
    }
    needsJetty9 = isVM || runtime.startsWith("java8");  
    processUserDefinedEnv = appEngineWebXml.getEnvironmentVariables();
  }
}
