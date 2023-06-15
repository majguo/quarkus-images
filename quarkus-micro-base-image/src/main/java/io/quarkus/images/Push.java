///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus.images:jdock:1.0-SNAPSHOT
//DEPS info.picocli:picocli:4.7.4
//SOURCES QuarkusMicro.java
package io.quarkus.images;

import picocli.CommandLine;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "build")
public class Push implements Callable<Integer> {

    @CommandLine.Option(names = { "--ubi-minimal" }, description = "The UBI Minimal base image")
    private String minimal;

    @CommandLine.Option(names = { "--ubi-micro" }, description = "The UBI Micro base image")
    private String micro;

    @CommandLine.Option(names = { "--out" }, description = "The output image")
    private String output;

    @CommandLine.Option(names = { "--alias" }, description = "An optional alias for the output image")
    private Optional<String> alias;

    @CommandLine.Option(names = {
            "--dockerfile-dir" }, description = "The location where the docker file should be created", defaultValue = "target/docker")
    private File dockerFileDir;

    @CommandLine.Option(names = "--dry-run", description = "Just generate the docker file and skip the container build")
    private boolean dryRun;

    @Override
    public Integer call() {
        JDock.setDockerFileDir(dockerFileDir);
        MultiArchImage image = QuarkusMicro.define(minimal, micro, output);
        image.buildAndPush();
        alias.ifPresent(s -> {
            if (!s.isBlank()) {
                MultiArchImage.createAndPushManifest(s, image.getLocalImages());
            }
        });
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new Push()).execute(args);
        System.exit(exitCode);
    }
}
