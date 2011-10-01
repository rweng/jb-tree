# Description

Flexible BTree-MultiMap implementation 

# Dev Requirements

- gradle 0.9-rc3 (install on mac with homebrew: `brew install gradle --HEAD`)

# Getting started

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

# Documentation

You can find the Javadoc [here](http://rweng.github.com/multimap/doc/) - If you click on Network, you can see a branch called gh-pages on which you can see the commit the Javadocs are about (not always completely up-to-date).

# Features

- you can easily order your tree by providing a different comparator for the keys.
- ...
