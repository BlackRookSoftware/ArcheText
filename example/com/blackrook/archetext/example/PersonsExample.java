/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext.example;

import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;
import com.blackrook.archetext.example.types.Person;

public final class PersonsExample
{
	public static void main(String[] args) throws Exception
	{
		ArcheTextRoot root = ArcheTextReader.readResource("com/blackrook/archetext/example/data/persons.txt");
		Person[] persons = root.exportByType("person", Person.class);
		
		for (Person p : persons)
			System.out.println(p);
		
	}
}
