/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 */
package com.google.appengine.endpoints;

import com.google.api.server.spi.tools.EndpointsTool;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * App Engine endpoints gen-api-config and gen-discovery-doc combined commands.
 *
 * @goal endpoints_get_discovery_doc
 * @phase compile
 * @author Ludovic Champenois ludo at google dot com
 */
public class EndpointsGetDiscoveryDoc extends EndpointsMojo {

    @Override
    protected ArrayList<String> collectParameters(String command) {
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add(command);
        String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();

        arguments.add("-w");
        arguments.add(appDir);
        handleClassPath(arguments, appDir);
        if (outputDirectory != null && !outputDirectory.isEmpty()) {
            arguments.add("-o");
            arguments.add(outputDirectory);
        }
        return arguments;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("");
        getLog().info("Google App Engine Java SDK - Generate endpoints api config...");
        if (serviceClassNames == null || serviceClassNames.isEmpty()) {
            throw new MojoExecutionException(
                    "\n<serviceClassNames>Your Endpoints classes</serviceClassNames>"
                    + " needs to be defined in the <configuration> section"
                    + " of the app engine maven plugin.\n");
        }
        List<String> classNames = Arrays.asList(serviceClassNames.split(","));
        try {
            executeEndpointsCommand("gen-api-config",
                    classNames.toArray(new String[classNames.size()]));
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(
                    "Error while generating Google App Engine endpoints api config:", e);
        }
        String appDir = project.getBuild().getDirectory() + "/" + project.getBuild().getFinalName();
        File webInf = new File(appDir + "/WEB-INF");
        if (webInf.exists() && webInf.isDirectory()) {
            File[] files = webInf.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("api");
                }
            });
            for (File f : files) {
                genDiscoveryDoc("rest", f.getAbsolutePath());
                genDiscoveryDoc("rpc", f.getAbsolutePath());
            }
        }
        getLog().info("Endpoint client lib generation done.");
    }

    private void genDiscoveryDoc(String format, String apiConfigFile)
            throws MojoExecutionException, MojoFailureException {
        getLog().info("Google App Engine Java SDK - Generate endpoints " + format
                + " discovery doc for apiConfigFile=");
        try {

            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add("gen-discovery-doc");
            arguments.add("-f");
            arguments.add(format);

            if (outputDirectory != null && !outputDirectory.isEmpty()) {
                arguments.add("-o");
                arguments.add(outputDirectory);
            }
            arguments.add(apiConfigFile);
            EndpointsTool.main(arguments.toArray(new String[arguments.size()]));
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(
                    "Error while generating Google App Engine endpoint discovery doc", e);
        }
        getLog().info("Endpoint discovery doc generation done.");

    }
}
