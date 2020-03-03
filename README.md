# Black Rook ArcheText

Copyright (c) 2016-2020 Black Rook Software.  
[https://github.com/BlackRookSoftware/ArcheText](https://github.com/BlackRookSoftware/ArcheText)

[Latest Release](https://github.com/BlackRookSoftware/ArcheText/releases/latest)  
[Online Javadoc](https://blackrooksoftware.github.io/ArcheText/javadoc/)  
[Quick Guide](https://github.com/BlackRookSoftware/ArcheText/blob/master/QUICK-GUIDE.md)


### Required Libraries

NONE

### Required Java Modules

[java.base](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html)  

### Introduction

This library reads and writes structured, hierarchical, textual data and 
contains utilities to convert them to POJOs and back.

This project was designed to replace [DL](https://github.com/BlackRookSoftware/DL). Unlike DL, ArcheText maintains 
definition hierarchy and supports value expressions.

### Why?

JSON and XML are okay for laying out hierarchical data, but not when that data can be
similar to other data or if that data is redundant. ArcheText seeks to eliminate this,
as well as providing a means to de-serialize that data into Java Objects.  

### Library

Contained in this release is a series of libraries that allow reading, writing,
and extracting data in ArcheText Objects, found in the com.blackrook.archetext
package. 

The ArcheTextObject class is used for the manipulation of the data once it
has been read into Java via ArcheTextReader. 

### Compiling with Ant

To compile this library with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this library (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To compile, JAR, test, and Zip up everything:

	ant release

To clean up everything:

	ant clean
	
### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 

This contains code copied from Black Rook Base, under the terms of the MIT License (docs/LICENSE-BlackRookBase.txt).
