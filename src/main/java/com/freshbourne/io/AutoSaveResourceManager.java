/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

/**
 * same as ResourceManager, except that pages don't have to be written back to disk manually.
 *
 * An implementation of this interface makes sure, that pages are persisted if they are altered.
 * This is usually done with a cache in the ResourceManager.
 *
 */
public interface AutoSaveResourceManager extends ResourceManager {
}
