package com.deezer.aop.plugin

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile

/**
 * @author Xavier Gouchet
 */
public class AspectjCompile extends AbstractCompile {


    private String encoding
    private FileCollection inpath
    private FileCollection aspectpath
    private String bootclasspath

//    @Override
    @TaskAction
    protected void compile() {

        final def log = project.logger

        String[] args = generateAspectJFlags()
        log.debug "AspectJ args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args as String[], handler);
        for (IMessage message : handler.getMessages(null, true)) {
            String context = buildMessageContext(message)

            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    if (message.message.contains("[Xlint:")) {
                        log.error "AspectJ Lint : " + message.message;
                    } else {
                        log.error "AspectJ Error : " + context + "\n" + message.message, message.thrown
                    }
                    throw new GradleException(message.message, message.thrown)
                case IMessage.WARNING:
                    if (message.message.contains("[Xlint:")) {
                        log.error "AspectJ Lint : " + message.message;
                    } else {
                        log.error "AspectJ Warning : " + context + "\n" + message.message;
                    }
                    break;
                case IMessage.INFO:
                    if (message.message.contains("[Xlint:")) {
                        log.info "AspectJ Lint : " + message.message;
                    } else {
                        log.info "AspectJ Info : " + context + "\n" + message.message;
                    }
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }

    private String[] generateAspectJFlags() {
        def defaultArgs = [
                "-encoding", getEncoding(),
                "-source", getSourceCompatibility(),
                "-target", getTargetCompatibility(),
                "-d", destinationDir.absolutePath,
                "-classpath", classpath.asPath,
                "-bootclasspath", bootclasspath,
                "-sourceroots", sourceRoots.join(File.pathSeparator)
        ]
        def extraFlags = project.androidAspectJ.extraAspectJFlags;

        def composedFlags = []
        defaultArgs.each {
            composedFlags.add(it)
        }
        extraFlags.each {
            composedFlags.add(it)
        }

        if (!getInpath().isEmpty()) {
            composedFlags << '-inpath'
            composedFlags << getInpath().asPath
        }
        if (!getAspectpath().isEmpty()) {
            composedFlags << '-aspectpath'
            composedFlags << getAspectpath().asPath
        }


        return composedFlags as String[];
    }

    private String buildMessageContext(IMessage message) {
        String context = ""
        if (message.sourceLocation != null) {
            if (message.sourceLocation.sourceFile != null) {
                context = message.sourceLocation.sourceFile.path + " line " + message.sourceLocation.line;
            } else if (message.sourceLocation.sourceFile != null) {
                context = message.sourceLocation.sourceFileName + " line " + message.sourceLocation.line;
            }
            if (message.sourceLocation.context != null) {
                context += "\n" + message.sourceLocation.context;
            }
        }
        context
    }

    @Input
    String getEncoding() {
        return encoding
    }

    void setEncoding(String encoding) {
        this.encoding = encoding
    }

    @InputFiles
    FileCollection getInpath() {
        return inpath
    }

    void setInpath(FileCollection inpath) {
        this.inpath = inpath
    }

    @InputFiles
    FileCollection getAspectpath() {
        return aspectpath
    }

    void setAspectpath(FileCollection aspectpath) {
        this.aspectpath = aspectpath
    }

    @Input
    String getBootclasspath() {
        return bootclasspath
    }

    void setBootclasspath(String bootclasspath) {
        this.bootclasspath = bootclasspath
    }

    File[] getSourceRoots() {
        def sourceRoots = []
        source.sourceCollections.each {
            it.asFileTrees.each {
                sourceRoots << it.dir
            }
        }
        return sourceRoots
    }
}
