/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

/**
 * Package implementing a B+-Tree.
 *
 * BTree, LeafNodes and Innernodes are auto-saving, means they manage their own rawpage and don't have to be saved
 * exernally.
 */
package com.freshbourne.btree;