# Description

This project naming is actually not correct yet.
This repository is more about an implementation of B-Tree in line with it's interface, MultiMap.
Every Class implementing the MultiMap interface should provide a way of storeing and accessing key-value Objects.
The difference to a normal map is mainly that one key can have multiple values.

# Dev Requirements

- gradle 0.9-rc3 (install on mac with homebrew: `brew install gradle --HEAD`)

# Getting started

## This Repository

get started by cloning the repository:

    git clone git://github.com/rweng/multimap.git
    cd index
    # look at all tasks you can do with the build manager
    gradle tasks
    # if you are in eclipse, generate the eclipse files to be able to open the repository as eclipse project
    gradle eclipse
    # run the tests
    gradle test
    # generate javadoc
    gradle javadoc
    open build/doc/javadoc/index.html

## Creating a BTree Instance

### With a Factory (favorite)

This is the favorite way of creating a btree. The factory can be reused for different files.
One important note: getting a btree for a file that has already been used with get() will ignore
the Serializers and the Comparator and just return the original instance.

The test for creating a BTree with a factory displays this best:

    @Test
	public void factoryConstructor() throws IOException {
		File file = new File("/tmp/defaultBTreeModule");
		file.delete();


		Injector i = Guice.createInjector(new BTreeModule());
		BTreeFactory factory = i.getInstance(BTreeFactory.class);
		BTree<Integer, String> btree = factory.get(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		btree.initialize();
		btree.sync();

		assertTrue(file.exists());
	}

### Manually

    FileResourceManager pm = new FileResourceManager(file);
	pm.open();

	BTree<Integer, String> btree =
		new BTree<Integer, String>(pm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
	        IntegerComparator.INSTANCE);

    btree.initialize();
    btree.sync();

### With the Guice BTreeModule

This is kinda ugly, because getting generics from guice requires you to use Key.get together with new TypeLiteral.
If anyone knows how to enhance that, let me know!

    Injector i = Guice.createInjector(new BTreeModule("/tmp/myfile"));
    BTree<Integer, Integer> t = i.getInstance(
        Key.get(new TypeLiteral<BTree<Integer, Integer>>() {}));
    t.initialize();

Look at the *BTreeSmallTest* class to see the code in action.

# Documentation

You can find the Javadoc [here](http://rweng.github.com/multimap/doc/) -
If you click on Network, you can see a branch called gh-pages on which you can see the commit the Javadocs are about (not always completely up-to-date).

# Features

- you can easily order your tree by providing a different comparator for the keys.
- ...
