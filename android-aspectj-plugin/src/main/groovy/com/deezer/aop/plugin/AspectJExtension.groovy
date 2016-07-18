package com.deezer.aop.plugin;

/**
 * @author Xavier Gouchet
 */
public class AspectJExtension {
    def extraAspectJFlags = []

    def String toString() {
        return "androidAspectJ { extraAspectJFlags = " + Arrays.toString(extraAspectJFlags) + "}"
    }
}