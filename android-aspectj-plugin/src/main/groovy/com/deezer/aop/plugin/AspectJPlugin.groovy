package com.deezer.aop.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.BaseConfig
import org.gradle.api.*
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.tasks.compile.JavaCompile

/**
 * @author Xavier Gouchet
 */
public class AspectJPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        def configuration = new AndroidConfiguration(project)

        project.extensions.create("androidAspectJ", AspectJExtension);
        project.repositories {
            mavenCentral()
        }
        project.dependencies {
            compile 'org.aspectj:aspectjrt:1.8.9'
        }


        if (!project.configurations.findByName('ajInpath')) {
            def ajInpathConfiguration = project.configurations.create('ajInpath')
        }

        //def aspectjConfiguration = project.configurations.create('aspectj').extendsFrom(project.configurations.compile, project.configurations.provided)

        createConfigurations(project, project.android.buildTypes)
        createConfigurations(project, project.android.productFlavors)
        createConfigurations(project, configuration.variants)

        project.afterEvaluate {
            configuration.variants.all { variant ->

                def configurationName = "${variant.name}Aspectj"

                def variantName = variant.name.capitalize()
                def taskName = "compile${variantName}Aspectj"

                JavaCompile javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile

                def aspectjCompile = project.task(taskName, overwrite: true, group: 'build', description: 'Compiles AspectJ Source', type: AspectjCompile) {

                    sourceCompatibility = javaCompile.sourceCompatibility
                    targetCompatibility = javaCompile.targetCompatibility
                    encoding = javaCompile.options.encoding

                    aspectpath = javaCompile.classpath
                    destinationDir = javaCompile.destinationDir
                    classpath = javaCompile.classpath
                    bootclasspath = configuration.bootClasspath.join(File.pathSeparator)



                    def sourceSets = new ArrayList()
                    variant.variantData.extraGeneratedSourceFolders.each {
                        source it
                    }
                    variant.variantData.javaSources.each {
                        if (it instanceof File) {
                            source it
                        } else {
                            it.asFileTrees.each {
                                source it.dir
                            }
                        }
                    }
                    inpath = project.configurations.ajInpath
                }

                // javaCompile.classpath does not contain exploded-aar/**/jars/*.jars till first run
                javaCompile.doLast {
                    aspectjCompile.classpath = javaCompile.classpath
                }

                aspectjCompile.dependsOn javaCompile
                javaCompile.finalizedBy aspectjCompile
            }
        }
    }

    private void createConfigurations(Project project, DefaultDomainObjectSet<? extends BaseVariant> variants) {
        variants.all {
            createConfiguration(project, it)
        }
    }

    private void createConfigurations(Project project, NamedDomainObjectContainer<? extends BaseConfig> configs) {
        configs.each {
            createConfiguration(project, it)
        }
        configs.whenObjectAdded {
            createConfiguration(project, it)
        }
    }

    private void createConfiguration(Project project, def config) {
        def configurationName = "${config.name}Aspectj"
        if (!project.configurations.findByName(configurationName)) {
            project.configurations.create(configurationName)
        }
    }

    /**
     * Class used to obtain information about the project configuration.
     */
    class AndroidConfiguration {

        private final Project project
        private final boolean hasAppPlugin
        private final boolean hasLibPlugin
        private final BasePlugin plugin

        AndroidConfiguration(Project project) {
            this.project = project
            this.hasAppPlugin = project.plugins.hasPlugin(AppPlugin)
            this.hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin)

            if (!hasAppPlugin && !hasLibPlugin) {
                throw new GradleException("android-acj: The 'com.android.application' or 'com.android.library' plugin is required.")
            }
            this.plugin = project.plugins.getPlugin(hasAppPlugin ? AppPlugin : LibraryPlugin)
        }

        /**
         * Return all variants.
         *
         * @return Collection of variants.
         */
        DomainObjectCollection<BaseVariant> getVariants() {
            return hasAppPlugin ? project.android.applicationVariants : project.android.libraryVariants
        }

        /**
         * Return boot classpath.
         * @return Collection of classes.
         */
        List<File> getBootClasspath() {
            if (project.android.hasProperty('bootClasspath')) {
                return project.android.bootClasspath
            } else {
                return plugin.runtimeJarList
            }
        }
    }
}
