# ArcheText Quick Guide

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

If you wished, you could create a structure without any fields in it, like so:

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

A *Type* defines a structure's category. Each type is stored together in an 
ArcheType *root*, which is the structure that holds all structures. Each 
structure is defined by a **type** and **name**.

A *Name* is an identifying name within a category that, together with a 
**type**, defines a unique structure.

A *Field* is a member of a structure that contains a value, and this can
be manipulated by inheritance or other means.

A *Type* or *Field* can only contain alphanumeric and underscore characters. 
They cannot contain spaces, nor can they start with a number, and they cannot 
be surrounded by quotes. The following are valid type/field names:

	x 
	y 
	hello 
	butt1 
	apple456 
	x5564 
	this_is_valid_too      

A *Name* can be any string, number, or a token that is a valid *type* or *field*. If it 
contains any whitespace or other special characters, it MUST be surrounded by quotes. 
Internally, these are all converted to strings, so *45* is equivalent to "45", as far as
structure names go. This is VERY important, in terms of inheritance. The following
are valid names:

	"apple" 
	pear 
	orange 
	_tomato 
	100 
	2.56 
	"green tea"

#### Fields and Values

A *Field* is defined in an object that is assigned a value. Values can have different *types*,
which affects how they are interpreted or converted.

**Boolean** - Holds only *true* and *false*.

	true
	false
   
**Integer** - Whole numbers, no decimal point. 64-bit precision. Can be expressed as hexadecimal.

	5
	424
	6577
	0xaf44534
	0x8000dad3

**Float** - Floating-point decimal numbers. 64-bit precision.

	5.0
	0.5565
	9.5453112
	0.00000004

**String** - A quote-surrounded string of characters. Can contain characters escaped using backslashes.

	"apple"
	"pear"
	"this is a string"
	"This has special characters \\ \n \t"

**List** - A square bracket-surrounded, comma-separated list of other values. Lists can contain any type and can contain duplicate entries.

	[0, 1, 2, 2, 4, 6]
	["apple", "pear", "orange"]
	[0, 0.2, 5.8, false]
	[[0, 1, 3], 7, [false, true, 6767]]

**Set** - An angle-bracket-surrounded, comma-separated list of other values. Sets can contain any type, but cannot contain duplicate entries (duplicates will not be added).

	<0, 1, 2, 2, 4, 6>
	<"apple", "pear", "orange">
	<0, 0.2, 5.8, false>
	<<0, 1, 3>, 7, [false, true, 6767]>
	
**Object** - A reference or anonymously-typed object, formatted exactly like the body of a structure.

	@{pair "y"}
	{ x = 9; y = 5.6; z = [5, 6, 7] }
	

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
	}
	
	triple "yunit"
	{
		y = 1;
	}

You could make an ordered triple (x, y, z) of (1, 1, 1) by defining:

	triple "all ones" : triple "yunit" : triple "xunit" 
	{
		z = 1;
	}
	
Every field in a structure does not need to exist in order for inheritance to work.

Field value priority goes from first defined to last-defined in the inheritance 
clauses (in this example, *all ones* (defined fields) is searched, then *yunit*, 
then *xunit*).

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


#### Prototyping

You can prototype the fields for a type so that you can write an abbreviated 
structure later. Prototype statements look like the following:

	.triple(x, y, z)
	
The statement must start with a period, then the type, then a parenthesis-wrapped 
list of field identifiers. An object can then be declared like this:

	triple "example" (1, 2, 3)

Which is equivalent to:

	triple "example"
	{
		x = 1;
		y = 2;
		z = 3;
	}

You can even use expressions in them!

	triple "example" (9 - 8, 4 / 2, 3 * 3 / 3)

If you don't specify all of the fields, not all get defined. The following definition:

	triple "example" (1, 2)

Is equivalent to:

	triple "example"
	{
		x = 1;
		y = 2;
	}


### Reflection and POJO Conversion	

ArcheText objects can be applied to other Java objects via reflection. The conversion is implemented
by Black Rook Common Reflect's TypeConverter, so all common conversions will work from ArcheText Values
to Java-native ones, including enums!

