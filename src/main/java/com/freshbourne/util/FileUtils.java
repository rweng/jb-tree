/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.util;

import java.io.File;

public class FileUtils {
    public static void recursiveDelete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                recursiveDelete(child);
            }

        }
        file.delete();
    }
}
