package com.excusemi.resourcepackdownscaler;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Main {

    public static void main(String... commandLineArguments) throws IOException, ParseException {
        Options options = new Options();
        options.addOption("a", "appendFile-meta", false, "Append size to pack.mcmeta");
        options.addOption("v", "version", false, "Display the version");
        options.addOption("o", "output-directory", true, "Output to this directory");
        options.addOption("h", "help", false, "Display usage");
        CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine = parser.parse(options, commandLineArguments);
        if (commandLine.hasOption("version")) {
            final Attributes myManifestAttributes = getMyManifestAttributes();
            final String version = myManifestAttributes.getValue("Implementation-Version");
            System.out.println(version);
        } else if (commandLine.hasOption("help")) {
            showUsage(options);
        } else {
            final List<String> argList = commandLine.getArgList();
            if (argList.size() >= 2) {
                final String resourcePack = argList.get(0);
                if (resourcePack.toLowerCase().endsWith(".zip")) {

                    final File file = new File(resourcePack);
                    if (file.exists()) {
                        final boolean updatePackMeta = commandLine.hasOption("appendFile-meta");

                        final List<String> arguments = commandLine.getArgList();
                        for (int i = 1; i < arguments.size(); i++) {
                            final int size = Integer.valueOf(arguments.get(i));
                            final File outputFile = createOutputFile(commandLine, file, size);
                            if (outputFile.exists()) {
                                outputFile.delete();
                            }
                            final FileTransformer fileTransformer = new FileTransformer(
                                    new ZipOutput(outputFile.toPath()),
                                    size,
                                    updatePackMeta);
                            ZipVisitor.visitZipFiles(file.toPath(), fileTransformer);

                        }

                    } else {
                        System.out.println(file.getAbsolutePath() + " does not exist");
                        System.exit(1);
                    }
                } else {
                    System.out.println("The source-resourcepack is not a .zip file");
                    System.exit(1);
                }
            } else {
                System.out.println("Not enough arguments provided");
                showUsage(options);
                System.exit(1);
            }

        }
    }

    private static File createOutputFile(CommandLine commandLine, File resourcePack, int size) {
        if(commandLine.hasOption("output-directory")) {
            final String optionValue = commandLine.getOptionValue("output-directory");
            final File outputDirectory = new File(optionValue);
            if(outputDirectory.exists() && outputDirectory.isDirectory()) {
                final String fileName = removeExtention(resourcePack.getName()) + "-" + size + "x.zip";
                return new File(outputDirectory, fileName);
            } else {
                System.out.println("output-directory '" + optionValue + "' doesn't is not a directory or doesn't exist");
                System.exit(1);
            }
        } else {
            return new File(removeExtention(resourcePack.getAbsolutePath()) + "-" + size + "x.zip");
        }
        return null;
    }

    private static void showUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("resourcepack-downscaler <source-resourcepack> <size1> <size2> ... ", options);
    }

    private static Attributes getMyManifestAttributes() throws IOException {
        String className = Main.class.getSimpleName() + ".class";
        String classPath = Main.class.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            throw new IOException("I don't live in a jar file");
        }
        URL url = new URL(classPath);
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        Manifest manifest = jarConnection.getManifest();
        return manifest.getMainAttributes();
    }

    private static String removeExtention(String filePath) {
        File f = new File(filePath);
        if (f.isDirectory()) return filePath;

        String name = f.getName();
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            return filePath;
        } else {
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }

}
