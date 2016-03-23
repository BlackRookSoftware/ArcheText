/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.archetext.example.types;

import com.blackrook.archetext.annotation.ATName;

/**
 * A person. 
 */
public class Person
{
	public enum Sex
	{
		MALE,
		FEMALE,
		NEUTER;
	}
	
	private String name;
	private int age;
	private Sex sex;
	private String location;
	
	public Person()
	{
	}

	@ATName
	public String getName()
	{
		return name;
	}

	public int getAge()
	{
		return age;
	}

	public Sex getSex()
	{
		return sex;
	}

	public String getLocation()
	{
		return location;
	}

	@ATName
	public void setName(String name)
	{
		this.name = name;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public void setSex(Sex sex)
	{
		this.sex = sex;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}
	
	@Override
	public String toString()
	{
		return name + ", Age: " + age + ",  Sex: " + sex + " @" + location;
	}
	
}
