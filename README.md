# Android-Aspectj-Plugin

[![Release](https://jitpack.io/v/deezer/Android-AspectJ-Plugin.svg)](https://jitpack.io/#deezer/Android-AspectJ-Plugin)

A Gradle plugin which enables AspectJ for Android builds. This plugin is largely based on the [deprecated plugin]() by [uPhyca]()

## Usage 

You need to add the plugin's classpath to your project's `build.gradle`

    buildscript {
        repositories {
            maven { url "https://jitpack.io" }
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.1.2'
            classpath 'com.github.xgouchet:gradle-android-aspectj-plugin:0.9.14'
        }
    }
    
Then apply the `android-aspectj` plugin to your app or library module : 

    apply plugin: 'android-aspectj'
    
## Configuration

You can specify additional AspectJ compilation flags like this (see [here](http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html) for a list of available flags.

    androidAspectJ {
        extraAspectJFlags = [
                "-Xlint:ignore", // errors | warning | ignore
                "-preserveAllLocals", // preserve local variable names
                "-showWeaveInfo", // show information about weaving
                "-g:lines,vars,source", // add debug info for any/all of [lines, vars, source]
        ]
    }

## Contributions

Feel free to open issues, or to send pull requests our way. 
 
## License

This plugin is distributed under the [MIT License](https://opensource.org/licenses/MIT)

