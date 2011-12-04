# Description

This Project provides two cool things:

- A flexible way of creating and handling pages on different resources (like the file system)
- An implementation of a B+-Tree that can
    - be ordered arbitrarily by providing a comparator
    - serializers can be handed in as dependency
    - the resource manager to which the B-Tree is persisted is handed in as interface.

# Dev Requirements

- gradle >= 1.0-milestone-6 (install on mac with homebrew: `brew install gradle --HEAD`)

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

## Creating a ResourceManager

    // creates a cached resource manager. All values are the default values and can be ommited (except file())
    new ResourceManagerBuilder().file("/tmp/test").useLock(true).useCache(true).cacheSize(100)
        .pageSize(PageSize.DEFAULT_PAGE_SIZE).build();

    // creates a plain FileResourceManager without Caching
    new ResourceManagerBuilder().file("/tmp/test").useCache(false).build();

## Creating a BTree Instance

	@Test
	public void staticMethodConstructor() throws IOException {
		final BTree<Integer, String> btree =
				BTree.create(createResourceManager(true), IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
						IntegerComparator.INSTANCE);
		btree.initialize();
	}

	private AutoSaveResourceManager createResourceManager(final boolean reset) {
		if (reset)
			file.delete();
		return new ResourceManagerBuilder().file(file).buildAutoSave();
	}
	
# Documentation

You can find the Javadoc [here](http://rweng.github.com/jb-tree/doc/) -
If you click on Network, you can see a branch called gh-pages on which you can see the commit the Javadocs are about (not always completely up-to-date).
