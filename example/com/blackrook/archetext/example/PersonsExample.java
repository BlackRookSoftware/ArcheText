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
