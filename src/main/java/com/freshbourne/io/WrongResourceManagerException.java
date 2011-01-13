/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.io;

import java.io.IOException;

public class WrongResourceManagerException extends IOException {
    public WrongResourceManagerException(ResourceManager thisRM, RawPage page){
        super("The Page with the id " + page.id() + " of ResourceManager " + page.resourceManager() +
                " cannot be writting to ResourceManager " + thisRM);
    }
}
