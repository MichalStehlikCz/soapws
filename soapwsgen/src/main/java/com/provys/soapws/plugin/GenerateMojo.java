package com.provys.soapws.plugin;

import com.provys.common.exception.RegularException;
import com.provys.soapws.gen.SoapWsClassGen;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate", inheritByDefault = false, aggregator = true)
public class GenerateMojo extends AbstractMojo {

  @Parameter(readonly = true, defaultValue = "${project}")
  private MavenProject project;

  @Parameter(required=true)
  private String packageName;

  @Parameter(required=true)
  private String moduleName;

  @Parameter(required=true)
  private File xsdFile;

  /**
   * Value of field packageName.
   *
   * @return value of field packageName
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Value of field moduleName.
   *
   * @return value of field moduleName
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * Path to project being built.
   *
   * @return path to project being built
   */
  public Path getBasePath() {
    return Path.of(project.getModel().getBuild().getDirectory());
  }

  /**
   * Path to target directory.
   *
   * @return path to target directory
   */
  public Path getOutputPath() {
    return Path.of(project.getModel().getBuild().getOutputDirectory())
        .resolve("generated-sources").resolve("soapwsgen");
  }

  /**
   * Value of field xsdFile.
   *
   * @return value of field xsdFile
   */
  public File getXsdFile() {
    return xsdFile;
  }

  private static void prepareDirectory(Path directory) {
    if (Files.notExists(directory)) {
      try {
        Files.createDirectories(directory);
      } catch (IOException e) {
        throw new RegularException("SOAPWSGEN_FAILED_T_CREATE_MODULE_DIR",
            "Failed to create module directory " + directory.toString(), e);
      }
    } else {
      if (!Files.isDirectory(directory)) {
        throw new RegularException("SOAPWSGEN_TARGET_NOT_DIRECTORY", "Target module is not directory");
      }
    }
  }

  private static Path prepareJavaDirectory(Path module) {
    var path = module.resolve("src").resolve("main").resolve("java");
    prepareDirectory(path);
    return path;
  }

  private static Path prepareResourcesDirectory(Path module) {
    var path = module.resolve("src").resolve("main").resolve("resources");
    prepareDirectory(path);
    return path;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var javaOutputPath = prepareJavaDirectory(getOutputPath());
    var classGenerator = new SoapWsClassGen(packageName, moduleName);
    try {
      classGenerator.genSecurityConfig().writeToPath(javaOutputPath);
    } catch (IOException e) {
      throw new RegularException("SOAPWSGEN_CANNOT_WRITE_CLASS", "Cannot write class files", e);
    }
    project.addCompileSourceRoot(getOutputPath().toString());
    var resourceOutputPath = prepareResourcesDirectory(getOutputPath());
    try {
      Files.copy(xsdFile.toPath(), resourceOutputPath.resolve(xsdFile.getName()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RegularException("SOAPWSGEN_CANNOT_COPY_XSD", "Cannot copy xsd file", e);
    }
  }

  @Override
  public String toString() {
    return "GenerateMojo{"
        + "packageName='" + packageName + '\''
        + ", moduleName='" + moduleName + '\''
        + ", xsdFile=" + xsdFile + '}';
  }
}
