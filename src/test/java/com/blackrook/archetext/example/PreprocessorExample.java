/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext.example;

import com.blackrook.archetext.ArcheTextObject;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;

public final class PreprocessorExample
{
	public static void main(String[] args) throws Exception
	{
		ArcheTextRoot root = ArcheTextReader.readResource("com/blackrook/archetext/example/data/preprocessor.txt");

		for (String type : root.getTypes())
			for (ArcheTextObject obj : root.getAllByType(type))
				System.out.println(obj);
		
	}
}
