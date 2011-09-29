/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;
import java.util.HashMap;

@Singleton
public class FileResourceManagerFactory {
    private int pageSize;
    private boolean doLock;
    private HashMap<File, FileResourceManager> map = new HashMap<File, FileResourceManager>();

    @Inject
    FileResourceManagerFactory(@PageSize int pageSize, @Named("doLock") boolean doLock){
        this.pageSize = pageSize;
        this.doLock = doLock;
    }

    public FileResourceManager get(File file){
        if(map.containsKey(file))
            return map.get(file);

        FileResourceManager frm = new FileResourceManager(file, pageSize, doLock);
        map.put(file, frm);
        return frm;
    }
}
