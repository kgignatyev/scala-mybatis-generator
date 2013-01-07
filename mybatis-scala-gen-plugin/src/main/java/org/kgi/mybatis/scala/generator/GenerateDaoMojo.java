package org.kgi.mybatis.scala.generator;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "generate-dao", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateDaoMojo extends AbstractMojo {


    @Component
    protected MavenProject project;

    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter( property = "destinationPackage")
    private String destinationPackage;


    public List collectDependencies(DependencyNode n) {
        ArrayList res = new ArrayList();
        res.add(n.getArtifact());
        for (DependencyNode dependencyNode : n.getChildren()) {
            res.addAll(collectDependencies(dependencyNode));
        }
        return res;
    }

    public void execute() throws MojoExecutionException {
        String scaladocParamFileName = project.getBuild().getOutputDirectory() + File.separator + "myb-doclet.txt";

        try {
            File f = outputDirectory;
            getLog().info("writing generated files to directory:" + outputDirectory.getAbsolutePath());
            if (!f.exists()) {
                f.mkdirs();
            }


            File sourcesDir = new File(project.getBasedir(), "src" + File.separator + "main");
            getLog().info("sources located in:" + sourcesDir.getAbsolutePath());
            Collection<File> sourceFiles = FileUtils.listFiles(sourcesDir, new String[]{"scala"}, true);

            PrintWriter scaladocParamFileWriter = new PrintWriter(new FileWriter(scaladocParamFileName));
            scaladocParamFileWriter.println("-d");
            scaladocParamFileWriter.println("src");
            scaladocParamFileWriter.println("-doc-generator");
            scaladocParamFileWriter.println("org.kgi.mybatis.scala.generator.doclet.MyBatisMappingDoclet");
            for (File sourceFile : sourceFiles) {
                scaladocParamFileWriter.println(sourceFile.getAbsolutePath());
            }
            scaladocParamFileWriter.flush();
            scaladocParamFileWriter.close();


            DependencyNode depTree = dependencyGraphBuilder.buildDependencyGraph(project, new ArtifactFilter() {
                public boolean include(Artifact artifact) {
                    return "jar".equals(artifact.getType());
                }
            });

            List deps = collectDependencies(depTree);

            Iterator depIterator = deps.iterator();
            StringBuilder cpBuilder = new StringBuilder();
            String docletPath = null;

            while (depIterator.hasNext()) {
                Artifact dep = (Artifact) depIterator.next();

                String path = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository" +
                        File.separator +
                        dep.getGroupId().replace('.', File.separatorChar) +
                        File.separator + dep.getArtifactId() +
                        File.separator + dep.getVersion() +
                        File.separator + dep.getArtifactId() + "-" + dep.getVersion() + "." + dep.getType();
                if (cpBuilder.length() > 0) {
                    cpBuilder.append(File.pathSeparator);
                }
                cpBuilder.append(path);
                if ("mybatis-scala-gen-doclet".equals(dep.getArtifactId())) {
                    docletPath = path;
                }
            }
            CommandLine cmdl = new CommandLine("scaladoc");
            cmdl.addArgument("-Dmyb-gen-destination=" + outputDirectory.getAbsolutePath());
            cmdl.addArgument("-Dmyb-gen-destination-package=" + destinationPackage);
            cmdl.addArgument("-classpath");
            cmdl.addArgument(cpBuilder.toString());
            cmdl.addArgument("-toolcp");
            cmdl.addArgument(docletPath);
            cmdl.addArgument("@" + scaladocParamFileName);

            getLog().info("generation command:\n" + cmdl.toString());
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.execute(cmdl);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Problems generating DAO sources" + scaladocParamFileName);
        }
    }
}
