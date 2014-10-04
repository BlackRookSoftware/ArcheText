package com.blackrook.archetext.example;

import com.blackrook.archetext.ArcheTextObject;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;
import com.blackrook.archetext.annotation.ATName;

public final class ArcheTextReadExample
{
	public static class Person
	{
		@ATName
		public String name;
		public int age;
		public char sex;
		
		@Override
		public String toString() {
			return name + ", " + age + " " + sex;
		}
		
	}
	
	public static void main(String[] args) throws Exception
	{
		ArcheTextRoot root = ArcheTextReader.readResource("com/blackrook/archetext/example/data/data.txt");

		for (Person obj : root.exportByType("person", Person.class))
			System.out.println(obj);
		
		for (ArcheTextObject obj : root.getAllByType("pair"))
			System.out.println(obj);
		
		for (ArcheTextObject obj : root.getAllByType("junk"))
		{
			System.out.println("===="+obj.getType() + " " + obj.getName());
			for (String s : obj.getAvailiableFieldNames())
				System.out.println(s + ": " + obj.get(s, Object.class));
		}
			
	}
}
