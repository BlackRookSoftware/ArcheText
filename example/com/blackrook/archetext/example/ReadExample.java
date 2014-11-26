package com.blackrook.archetext.example;

import com.blackrook.archetext.ArcheTextObject;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;

public final class ReadExample
{
	public static void main(String[] args) throws Exception
	{
		ArcheTextRoot root = ArcheTextReader.readResource("com/blackrook/archetext/example/data/data.txt");

		for (String type : root.getTypes())
			for (ArcheTextObject obj : root.getAllByType(type))
				System.out.println(obj);
		
	}
}
