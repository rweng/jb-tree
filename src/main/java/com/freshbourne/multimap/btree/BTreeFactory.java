/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.FileResourceManagerFactory;
import com.freshbourne.io.PagePointer;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;


@Singleton
public class BTreeFactory {

    private FileResourceManagerFactory frmFactory;
    private HashMap<File, BTree> map = new HashMap<File, BTree>();
    private FixLengthSerializer<PagePointer, byte[]> pagePointSerializer;

    @Inject
    BTreeFactory(FileResourceManagerFactory frmFactory, FixLengthSerializer<PagePointer, byte[]> pps){
        this.frmFactory = frmFactory;
        this.pagePointSerializer = pps;
    }

    public <K,V> BTree<K,V> get(File file, FixLengthSerializer<K, byte[]> ks, FixLengthSerializer<V, byte[]> vs,
                                Comparator<K> comparator){
        if(map.containsKey(file))
            return map.get(file);

        FileResourceManager frm = frmFactory.get(file);

        LeafPageManager<K,V> lpm = new LeafPageManager<K, V>(frm,vs, ks, comparator);
        DataPageManager<K> kdpm = new DataPageManager<K>(frm, pagePointSerializer, ks);
        DataPageManager<V> vdpm = new DataPageManager<V>(frm, pagePointSerializer, vs);

        InnerNodeManager<K,V> npm = new InnerNodeManager<K, V>(frm, kdpm, vdpm, lpm, ks, comparator);

        BTree<K, V> tree = new BTree<K, V>(frm, lpm, npm, comparator);
        map.put(file, tree);

        return tree;
    }
}
