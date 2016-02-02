/*
 * Copyright (C) 2016 AXON IVY AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ivyteam.ivy.maven;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import ch.ivyteam.ivy.maven.engine.MavenProjectBuilderProxy;
import ch.ivyteam.ivy.maven.engine.Slf4jSimpleEngineProperties;
import ch.ivyteam.ivy.maven.util.ClasspathJar;
import ch.ivyteam.ivy.maven.util.CompilerResult;
import ch.ivyteam.ivy.maven.util.SharedFile;

/**
 * Compiles the test sources.
 * 
 * @author Reguel Wermelinger
 * @since 6.1.0
 */
@Mojo(name=CompileTestProjectMojo.GOAL, requiresDependencyResolution=ResolutionScope.TEST)
public class CompileTestProjectMojo extends CompileProjectMojo
{
  @SuppressWarnings("hiding")
  public static final String GOAL = "test-compile";
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    getLog().info("Compiling test sources...");
    compileTests();
  }

  private void compileTests() throws MojoExecutionException
  {
    Slf4jSimpleEngineProperties.install();
    try
    {
      MavenProjectBuilderProxy projectBuilder = getMavenProjectBuilder();
      Map<String, Object> result = projectBuilder.testCompile(project.getBasedir(), getIarJars(), getOptions());
      CompilerResult.store(result, project);
    }
    catch (Exception ex)
    {
      throw new MojoExecutionException("Failed to compile project '"+project.getBasedir()+"'.", ex);
    }
    finally
    {
      Slf4jSimpleEngineProperties.reset();
    }
  }

  /**
   * @return persistent IAR-JARs from {@link CompileProjectMojo}.
   */
  private List<File> getIarJars()
  {
    File iarJarClasspath = new SharedFile(project).getIarDependencyClasspathJar();
    if (!iarJarClasspath.exists())
    {
      return Collections.emptyList();
    }
    List<File> iarJars = new ClasspathJar(iarJarClasspath).getFiles();
    return iarJars;
  }

}