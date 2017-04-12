
package com.google.appengine;

import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Maven project version is dot based, e.g "1.9.15" ,
 * whereas appengine-web.xml is dash based, e.g. "1-9-15"
 * This goal creates a Maven property with a correct app engine version.
 * @goal create-property
 */
public class CreateGoodVersionFromPomVersionMojo extends AbstractMojo {

  /**
   * @parameter property="project" required="true"
   */
  private MavenProject mavenProject;

  /**
   * @parameter property="$appengine.create-property-version"
   * default-value="AppEngineFriendlyVersion"
   */
  private String propertyName;

  @Override
  public void execute() throws MojoExecutionException {
    String version = mavenProject.getVersion();
    Properties props = mavenProject.getProperties();

    String newVersion = version.toLowerCase();
    newVersion = newVersion.replace('.', '-');
    newVersion = newVersion.toLowerCase();
    props.put(propertyName, newVersion);

    getLog().info("added property " + propertyName + "=" + newVersion);
  }
}
