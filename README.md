# Black Rook ArcheText

Copyright (c) 2014 Black Rook Software. All rights reserved.  
[http://blackrooksoftware.com/projects.htm?name=archetext](http://blackrooksoftware.com/projects.htm?name=archetext)  
[https://github.com/BlackRookSoftware/ArcheText](https://github.com/BlackRookSoftware/ArcheText)

### Required Libraries

Black Rook Commons 2.20.0+  
[https://github.com/BlackRookSoftware/Common](https://github.com/BlackRookSoftware/Common)

### Introduction

This library reads and writes structured, hierarchical, textual data and 
contains utilities to convert them to POJOs and back.

This project was designed to replace DL. Unlike DL, ArcheText maintains 
definition hierarchy and supports value expressions.

### Structure

ArcheText's Structure is similar to most curly-brace language structures,
except that its members and values are defined differently. The primary function
of these structures are to define object characteristics and potentially object
inheritance hierarchies.

For example, a car might be described as:

	car "Honda Civic"
	{
		make = "Honda";
		model = "Civic";
		type = "Sedan";
		color = "Black";
		weight = 2200;
		weightUnit = "lbs";
	}
	
Where "car" is the *type* of structure, and "Honda Civic" is a *name* for that
particular structure, which will be important later.

ArcheType's structure is freeform, so the following is still read the same way as
the above:

	car 
	"Honda Civic" { make 
	= "Honda"
	; model = 
	"Civic"
	; 
	type = 
	"Sedan"		;
		color 
		= 
		"Black"
		
		;
	weight=2200;weightUnit="lbs";}

...but that way isn't nearly as human-readable.

If you so wished, you could create a structure without any fields in it, like so:

	object "Empty";

Just substitute a semicolon (;) instead of the main curly-brace enclosed body.

Defining another structure with the same name overwrites the first structure.

	pair "a"
	{
		x = 2;
		y = 5;
	}

	pair "a"
	{
		y = 3;
	}

After those two structures are parsed, the value of pair "a" is a single field,
*y*, that equals *3*. The field *x* doesn't exist in it.

Structures can also not even have a name attached to it like so:

	pair
	{
		x = 0;
		y = 0;
	}


#### Types, Names, and Fields

As briefly touched upon, the three main elements of every structure are 
**types**, **names**, and **fields**.

A **type** defines a structure's category. Each type is stored together in an 
ArcheType *root*, which is the structure that holds all structures. Each 
structure is defined by a **type** and **name**.

A **name** is an identifying name within a category that, together with a 
**type**, defines a unique structure.

A **field** is a member of a structure that contains a value, and this can
be manipulated by inheritance or other means.

A *Type* or *Field* can only contain alphanumeric and underscore characters. 
They cannot contain spaces, nor can they start with a number, and they cannot 
be surrounded by quotes. The following are valid type/field names:

	x y hello butt1 apple456 x5564 this_is_valid_too      

A *Name* can be any string, number, or pattern of characters. If it contains any 
whitespace or special characters, it MUST be surrounded by quotes. Internally,
these are all converted to strings, so *45* is equivalent to "45", as far as
structure names go. This is VERY important, in terms of inheritance.

#### Inheritance

If you were to define an ordered pair object like so:

	pair "origin"
	{
		x = 0;
		y = 0;
	}

You also could define another pair as such:

	pair "xunit" : pair "origin"
	{
		x = 1;
	} 

...which only defines a single field, but the ":" (colon) operator defines which
structure it is inheriting from. So, in the end, these two structures are 
equivalent, value-wise:

	pair "xunit"
	{
		x = 1;
		y = 0;
	}
 
	pair "xunit" : pair "origin"
	{
		x = 1;
	} 

...except one is part of an object hierarchy. In either case, when the fields
*x* and *y* are queried, their values are *1* and *0*, respectively.

You can inherit from multiple structures. If you defined the following
structures:

	triple "xunit"
	{
		x = 1;
		y = 0;
		z = 0;
	}
	
	triple "yunit"
	{
		x = 0;
		y = 1;
		z = 0;
	}

You could make an ordered triple (x, y, z) of (1, 1, 1) by defining:

	triple "all ones" : triple "yunit" : triple "xunit" 
	{
		z = 1;
	}
	
Every member in a structure need not exist in order for inheritance to work.
If *triple* structures *xunit* and *yunit* were defined as:

	triple "xunit"
	{
		x = 1;
	}
	
	triple "yunit"
	{
		y = 1;
	}

...the definition of *all ones* would still hold the same values. 

Field value priority goes from first defined to last-defined in the inheritance 
clauses (in this example, *all ones* (defined fields) is searched, then *yunit*, 
then *xunit*.

If you do NOT wish to preserve hierarchy, and would like a structure to be 
**flattened** into its current values, then the first colon should be changed
to an arrow:

	triple "all ones" <- triple "yunit" : triple "xunit" 
	{
		z = 1;
	}

This still creates the same structure with the correct values, but the 
hierarchy is lost after parsing.


Inheritance does not even need to be among like types. 


### Reflection and POJO Conversion	


### Library

Contained in this release is a series of libraries that allow reading, writing,
and extracting data in ArcheText Objects, found in the com.blackrook.archetext
package. 

The ArcheTextObject class is used for the manipulation of the data once it
has been read into Java via ArcheTextReader. 

### Other

This program and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 
