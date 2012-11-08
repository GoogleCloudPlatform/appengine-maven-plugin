/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 */
package com.google.appengine;

import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SdkResolver {
  private static final String SDK_GROUP_ID = "com.google.appengine";
  private static final String SDK_ARTIFACT_ID = "appengine-java-sdk";
  private static final String SDK_EXTENSION = "zip";

  private static final String SDK_VERSION = "1.7.2.1";

  static File getSdk(RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository>... repos)
      throws MojoExecutionException {

    List<RemoteRepository> allRepos = ImmutableList.copyOf(Iterables.concat(repos));

    ArtifactRequest request = new ArtifactRequest(new DefaultArtifact(SDK_GROUP_ID, SDK_ARTIFACT_ID, SDK_EXTENSION, SDK_VERSION), allRepos, null);

    ArtifactResult result;
    try {
      result = repoSystem.resolveArtifact(repoSession, request);
    } catch (ArtifactResolutionException e) {
      throw new MojoExecutionException("Could not resolve SDK artifact in Maven.", e);
    }

    File sdkArchive = result.getArtifact().getFile();
    File sdkRepoDir = sdkArchive.getParentFile();
    File sdkBaseDir = new File(sdkRepoDir, SDK_ARTIFACT_ID);

    if (sdkBaseDir.exists() && !sdkBaseDir.isDirectory()) {
      throw new MojoExecutionException("Could not unpack the SDK because there is an unexpected file at "
          + sdkBaseDir + " which conflicts with where we plan to unpack the SDK.");
    }

    if (!sdkBaseDir.exists()) {
      sdkBaseDir.mkdirs();
    }

    // While processing the zip archive, if we find an initial entry that is a directory, and all entries are a child
    // of this directory, then we append this to the sdkBaseDir we return.
    String sdkBaseDirSuffix = null;

    try {
      ZipFile sdkZipArchive = new ZipFile(sdkArchive);
      Enumeration<? extends ZipEntry> zipEntries = sdkZipArchive.entries();

      if (!zipEntries.hasMoreElements()) {
        throw new MojoExecutionException("The SDK zip archive appears corrupted.  There are no entries in the zip index.");
      }

      ZipEntry firstEntry = zipEntries.nextElement();
      if (firstEntry.isDirectory()) {
        sdkBaseDirSuffix = firstEntry.getName();
      } else {
        //Reinitialize entries
        zipEntries = sdkZipArchive.entries();
      }

      while (zipEntries.hasMoreElements()) {
        ZipEntry zipEntry = zipEntries.nextElement();

        if (!zipEntry.isDirectory()) {
          File zipEntryDestination = new File(sdkBaseDir, zipEntry.getName());

          if (!zipEntry.getName().startsWith(sdkBaseDirSuffix)) {
            //We found an entry that doesn't use this initial base directory, oh well, just set it to null.
            sdkBaseDirSuffix = null;
          }

          if (!zipEntryDestination.exists()) {
            Files.createParentDirs(zipEntryDestination);
            Files.write(ByteStreams.toByteArray(sdkZipArchive.getInputStream(zipEntry)), zipEntryDestination);
          }
        }
      }

    } catch (IOException e) {
      throw new MojoExecutionException("Could not open SDK zip archive.", e);
    }

    if (sdkBaseDirSuffix == null) {
      return sdkBaseDir;
    }

    return new File(sdkBaseDir, sdkBaseDirSuffix);
  }
}