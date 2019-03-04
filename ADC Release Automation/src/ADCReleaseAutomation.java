import com.armadialogcreator.lang.Lang;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 This class is used for preparing a build for Arma Dialog Creator and building "Arma Dialog Creator.exe".
 In order to build the .exe, <a href='http://launch4j.sourceforge.net/'>Launch4J</a> will be needed.

 @author Kayler
 @since 10/10/2016. */
public class ADCReleaseAutomation {
	private final String workingDirectoryPath = new File("").getAbsolutePath();

	private final String INSTALL_JAR_DIR = "/out/artifacts/adc_installer_jar/";

	/*Steps to building the Arma Dialog Creator.exe and installer:

	1. Run ADCReleaseAutomation.main() with no program arguments
	    * This step will create adc.jar's manifest, launch4j configs for exe's, and remove old exe's previously created
	    * You will need BUILD_NUMBER defined in environment variables
	2. Build adc.jar
	3. Build adc_launcher.jar
	4. Build adc_updater.jar
	5. Run ADCReleaseAutomation.main() with "-buildADCExe" as the program arguments
	    * This step will create "Arma Dialog Creator.exe"
	6. Run ADCReleaseAutomation.main() with "-packInstaller" as the program arguments
	    * This step will copy adc.jar, adc_updater.jar, etc, to the installer's build location
	7. Build adc_installer.jar
	    * This step will take previously copied adc.jar and other files and build adc_installer.jar
	8. Run ADCReleaseAutomation.main() with "-buildInstallerExe" as the program arguments
	    * This step will create "Arma Dialog Creator Installer.exe"
	*/

	public static void main(String[] args) {
		try {
			new ADCReleaseAutomation().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void run(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("-packInstaller")) {
			packInstaller();
		} else if (args.length > 0 && args[0].equals("-buildADCExe")) {
			createADCExe();
		} else if (args.length > 0 && args[0].equals("-buildInstallerExe")) {
			createInstallerExe();
			createUpdateStuff();
		} else {
			createManifest();
			createLaunch4jConfig("release_automation/configuration_template.xml", "release_automation/configuration.xml");
			createLaunch4jConfig("release_automation/installer_configuration_template.xml", "release_automation/installer_configuration.xml");
			removeOldExeFiles();
		}

	}

	private void packInstaller() {
		String[] paths = {
				"out/artifacts/adc_jar/adc.jar",
				"out/artifacts/adc_launcher_jar/Arma Dialog Creator.exe",
				"out/artifacts/adc_updater_jar/adc_updater.jar"
		};
		ArrayList<File> filesToPack = new ArrayList<>();

		for (String path : paths) {
			filesToPack.add(new File(workingDirectoryPath + "/" + path));
		}

		for (File f : filesToPack) {
			if (!f.exists()) {
				throw new RuntimeException(new FileNotFoundException(f.toString()));
			}
		}

		ZipFile zip;
		try {
			File zipFile = new File(workingDirectoryPath + INSTALL_JAR_DIR + "adc_installation.zip");
			if (zipFile.exists()) {
				zipFile.delete();
			}
			zip = new ZipFile(zipFile.toPath().toString());

			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zip.createZipFile(filesToPack, parameters);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (File f : filesToPack) {
			File dest = new File(workingDirectoryPath + "/out/production/ADC Installer/install/" + f.getName());
			try {
				System.out.println("Copying " + f.toPath() + " to '" + dest.toPath() + "'");
				Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void createUpdateStuff() throws IOException {
		String version = Lang.Application.VERSION + "-build" + getBuildNumber(); //DO NOT INCLUDE + because GitHub will remove it from file names!
		String updateJarName = "adcupdate-" + version + ".jar";

		//create update config
		{
			File updateConfig = new File(workingDirectoryPath + INSTALL_JAR_DIR + "update.properties");
			updateConfig.createNewFile();
			Writer out = new OutputStreamWriter(new FileOutputStream(updateConfig), StandardCharsets.UTF_8);
			out.write(String.format("updateJar=%s\n", updateJarName));
			out.write(String.format("version=%s\n", version));
			out.flush();
			out.close();
		}

		//rename the current installer jar to the update jar name
		{
			String installJarPath = workingDirectoryPath + INSTALL_JAR_DIR;
			File installerJar = new File(installJarPath + "adc_installer.jar");
			Files.copy(installerJar.toPath(), new File(installJarPath + updateJarName).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/** Ask Launch4j to build the .exe for us */
	private void createADCExe() {
		createExe(String.format("\"%s\\release_automation\\configuration.xml\"", workingDirectoryPath));
	}

	/** Ask Launch4j to build the .exe for us */
	private void createInstallerExe() {
		createExe(String.format("\"%s\\release_automation\\installer_configuration.xml\"", workingDirectoryPath));
	}

	private void createExe(String path) {
		ProcessBuilder pb = new ProcessBuilder();
		try {
			pb.inheritIO();
			Process p = Runtime.getRuntime().exec(
					String.format("java -jar %s %s", "launch4j.jar", path),
					null,
					new File("D:\\DATA\\Launch4j")
			);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Create the Manifest file to be used for the adc.jar. The Manifest holds details like the build and Arma Dialog Creator version */
	private void createManifest() {
		exportNew(new File("release_automation/MANIFEST_template.mf"), new File("src/META-INF/MANIFEST.MF"));
	}

	private void removeOldExeFiles() {
		for (File f : new File[]{
				new File("out/artifacts/adc_launcher_jar/Arma Dialog Creator.exe"),
				new File("out/artifacts/adc_installer_jar/Arma Dialog Creator Installer.exe")
		}) {
			if (f.exists()) {
				try {
					Files.delete(f.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** create the config file that will be used to make "Arma Dialog Creator.exe" via Launch4j */
	private void createLaunch4jConfig(String templateFilePath, String destTemplateFilePath) throws IOException {
		exportNew(new File(templateFilePath), new File(destTemplateFilePath));
	}

	private void exportNew(File template, File fout) {
		if (!template.exists()) {
			throw new IllegalStateException("template should exist. Current path:" + template.getPath());
		}
		try {
			Reader in = new InputStreamReader(new FileInputStream(template), StandardCharsets.UTF_8);
			Writer out = new OutputStreamWriter(new FileOutputStream(fout), StandardCharsets.UTF_8);
			int c;
			boolean startVariable = false;
			StringBuilder variable = new StringBuilder();
			while ((c = in.read()) >= 0) {
				if (startVariable) {
					if (c == '$') {
						startVariable = false;
					} else {
						variable.append((char) c);
						continue;
					}

					String varName = variable.toString();

					switch (varName) {
						case "ADC_LAUNCHER_PROJECT_OUT_PATH": {
							out.write((workingDirectoryPath + "\\out\\artifacts\\adc_launcher_jar"));
							break;
						}
						case "ADC_INSTALLER_PROJECT_OUT_PATH": {
							out.write((workingDirectoryPath + "\\out\\artifacts\\adc_installer_jar"));
							break;
						}
						case "VERSION": {
							out.write(Lang.Application.VERSION);
							break;
						}
						case "BUILD_NUMBER": {
							out.write(getBuildNumber());
							break;
						}
						default: {
							System.err.println("WARNING: unknown variable:" + varName);
						}
					}
				} else {
					if (c == '$') {
						startVariable = true;
						variable = new StringBuilder();
						continue;
					}
					out.write(c);
				}
			}
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getBuildNumber() {
		String buildNumber = System.getenv("BUILD_NUMBER");
		if (buildNumber == null) {
			return "unversioned";
		}
		return buildNumber;
	}
}
