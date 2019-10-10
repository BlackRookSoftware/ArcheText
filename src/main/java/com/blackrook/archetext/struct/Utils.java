/*******************************************************************************
 * Copyright (c) 2019 Black Rook Software
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.archetext.struct;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.blackrook.archetext.ArcheTextObject;
import com.blackrook.archetext.annotation.ATIgnore;
import com.blackrook.archetext.annotation.ATName;
import com.blackrook.archetext.exception.ArcheTextExportException;
import com.blackrook.archetext.struct.TypeProfileFactory.Profile;
import com.blackrook.archetext.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.archetext.struct.TypeProfileFactory.Profile.MethodInfo;

public final class Utils
{
	/** Are we using Windows? */
	private static boolean IS_WINDOWS = false;
	
	private static final TypeProfileFactory DEFAULT_PROFILEFACTORY = new TypeProfileFactory(new TypeProfileFactory.MemberPolicy()
	{
		@Override
		public boolean isIgnored(Field field)
		{
			return field.getAnnotation(ATIgnore.class) != null;
		}

		@Override
		public boolean isIgnored(Method method)
		{
			return method.getAnnotation(ATIgnore.class) != null;
		}

		@Override
		public String getAlias(Field field)
		{
			ATName anno = field.getAnnotation(ATName.class);
			return anno != null ? anno.value() : null;
		}

		@Override
		public String getAlias(Method method)
		{
			ATName anno = method.getAnnotation(ATName.class);
			return anno != null ? anno.value() : null;
		}
	});
	
	private static final TypeConverter DEFAULT_CONVERTER = new TypeConverter(DEFAULT_PROFILEFACTORY);

	static
	{
		String osName = System.getProperty("os.name");
		IS_WINDOWS = osName.contains("Windows");
	}
	
	/** @return true if we using Windows. */
	public static boolean isWindows()
	{
		return IS_WINDOWS;
	}

	/**
	 * Reflect.creates a new instance of an object for placement in a POJO or elsewhere.
	 * @param <T> the return object type.
	 * @param object the object to convert to another object
	 * @param targetType the target class type to convert to, if the types differ.
	 * @return a suitable object of type <code>targetType</code>. 
	 * @throws ClassCastException if the incoming type cannot be converted.
	 */
	public static <T> T createForType(Object object, Class<T> targetType)
	{
		return DEFAULT_CONVERTER.createForType("source", object, targetType);
	}

	/**
	 * Opens an {@link InputStream} to a resource using the current thread's {@link ClassLoader}.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream openResource(String pathString)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
	}

	/**
	 * Tests if a class is actually an array type.
	 * @param clazz the class to test.
	 * @return true if so, false if not. 
	 */
	public static boolean isArray(Class<?> clazz)
	{
		return clazz.getName().startsWith("["); 
	}

	/**
	 * Tests if an object is actually an array type.
	 * @param object the object to test.
	 * @return true if so, false if not. 
	 */
	public static boolean isArray(Object object)
	{
		return isArray(object.getClass()); 
	}

	/**
	 * Gets how many dimensions that this array, represented by the provided type, has.
	 * @param arrayType the type to inspect.
	 * @return the number of array dimensions, or 0 if not an array.
	 */
	public static int getArrayDimensions(Class<?> arrayType)
	{
		if (!isArray(arrayType))
			return 0;
			
		String cname = arrayType.getName();
		
		int dims = 0;
		while (dims < cname.length() && cname.charAt(dims) == '[')
			dims++;
		
		if (dims == cname.length())
			return 0;
		
		return dims;
	}

	/**
	 * Gets how many array dimensions that an object (presumably an array) has.
	 * @param array the object to inspect.
	 * @return the number of array dimensions, or 0 if not an array.
	 */
	public static int getArrayDimensions(Object array)
	{
		if (!isArray(array))
			return 0;
			
		return getArrayDimensions(array.getClass());
	}

	/**
	 * Gets the class type of this array type, if this is an array type.
	 * @param arrayType the type to inspect.
	 * @return this array's type, or null if the provided type is not an array,
	 * or if the found class is not on the classpath.
	 */
	public static Class<?> getArrayType(Class<?> arrayType)
	{
		String cname = arrayType.getName();
	
		int typeIndex = getArrayDimensions(arrayType);
		if (typeIndex == 0)
			return null;
		
		char t = cname.charAt(typeIndex);
		if (t == 'L') // is object.
		{
			String classtypename = cname.substring(typeIndex + 1, cname.length() - 1);
			try {
				return Class.forName(classtypename);
			} catch (ClassNotFoundException e){
				return null;
			}
		}
		else switch (t)
		{
			case 'Z': return Boolean.TYPE; 
			case 'B': return Byte.TYPE; 
			case 'S': return Short.TYPE; 
			case 'I': return Integer.TYPE; 
			case 'J': return Long.TYPE; 
			case 'F': return Float.TYPE; 
			case 'D': return Double.TYPE; 
			case 'C': return Character.TYPE; 
		}
		
		return null;
	}

	/**
	 * Gets the class type of this array, if this is an array.
	 * @param object the object to inspect.
	 * @return this array's type, or null if the provided object is not an array, or if the found class is not on the classpath.
	 */
	public static Class<?> getArrayType(Object object)
	{
		if (!isArray(object))
			return null;
		
		return getArrayType(object.getClass());
	}

	/**
	 * Checks if a value is "empty."
	 * The following is considered "empty":
	 * <ul>
	 * <li><i>Null</i> references.
	 * <li>{@link Array} objects that have a length of 0.
	 * <li>{@link Boolean} objects that are false.
	 * <li>{@link Character} objects that are the null character ('\0', '\u0000').
	 * <li>{@link Number} objects that are zero.
	 * <li>{@link String} objects that are the empty string, or are {@link String#trim()}'ed down to the empty string.
	 * <li>{@link Collection} objects where {@link Collection#isEmpty()} returns true.
	 * </ul> 
	 * @param obj the object to check.
	 * @return true if the provided object is considered "empty", false otherwise.
	 */
	public static boolean isEmpty(Object obj)
	{
		if (obj == null)
			return true;
		else if (isArray(obj.getClass()))
			return Array.getLength(obj) == 0;
		else if (obj instanceof Boolean)
			return !((Boolean)obj);
		else if (obj instanceof Character)
			return ((Character)obj) == '\0';
		else if (obj instanceof Number)
			return ((Number)obj).doubleValue() == 0.0;
		else if (obj instanceof String)
			return ((String)obj).trim().length() == 0;
		else if (obj instanceof Collection<?>)
			return ((Collection<?>)obj).isEmpty();
		else
			return false;
	}

	/**
	 * Returns the first object if it is not null, otherwise returns the second. 
	 * @param <T> class that extends Object.
	 * @param testObject the first ("tested") object.
	 * @param nullReturn the object to return if testObject is null.
	 * @return testObject if not null, nullReturn otherwise.
	 */
	public static <T> T isNull(T testObject, T nullReturn)
	{
		return testObject != null ? testObject : nullReturn;
	}

	/**
	 * Returns a new Set that is the union of the objects in two sets,
	 * i.e. a set with all objects from both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S union(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
			out.add(val);
		for (T val : set2)
			out.add(val);
		return (S)out;
	}

	/**
	 * Returns a new Set that is the intersection of the objects in two sets,
	 * i.e. the objects that are present in both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S intersection(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		
		S bigset = set1.size() > set2.size() ? set1 : set2;
		S smallset = bigset == set1 ? set2 : set1;
		
		for (T val : smallset)
		{
			if (bigset.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the difference of the objects in two sets,
	 * i.e. the objects in the first set minus the objects in the second.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S difference(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the union minus the intersection of the objects in two sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S xor(S set1, S set2)
	{
		Set<T> out = new HashSet<T>();
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		for (T val : set2)
		{
			if (!set1.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Sets the value of a field on an object.
	 * @param instance the object instance to set the field on.
	 * @param field the field to set.
	 * @param value the value to set.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws ClassCastException if the value could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad field name, 
	 * bad target, bad argument, or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	public static void setFieldValue(Object instance, Field field, Object value)
	{
		try {
			field.set(instance, value);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the value of a field on an object.
	 * @param instance the object instance to get the field value of.
	 * @param field the field to get the value of.
	 * @return the current value of the field.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, 
	 * or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	public static Object getFieldValue(Object instance, Field field)
	{
		try {
			return field.get(instance);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Blindly invokes a method, only throwing a {@link RuntimeException} if
	 * something goes wrong. Here for the convenience of not making a billion
	 * try/catch clauses for a method invocation.
	 * @param method the method to invoke.
	 * @param instance the object instance that is the method target.
	 * @param params the parameters to pass to the method.
	 * @return the return value from the method invocation. If void, this is null.
	 * @throws ClassCastException if one of the parameters could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, or can't access the method).
	 * @see Method#invoke(Object, Object...)
	 */
	public static Object invokeBlind(Method method, Object instance, Object ... params)
	{
		Object out = null;
		try {
			out = method.invoke(instance, params);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#newInstance()}, but wraps the call
	 * in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param clazz the class type to instantiate.
	 * @return a new instance of an object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	public static <T> T create(Class<T> clazz)
	{
		Object out = null;
		try {
			out = construct(clazz.getDeclaredConstructor());
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		}
		
		return clazz.cast(out);
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#newInstance()}, but wraps the call
	 * in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param constructor the constructor to call.
	 * @param params the constructor parameters.
	 * @return a new instance of an object created via the provided constructor.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T construct(Constructor<T> constructor, Object ... params)
	{
		Object out = null;
		try {
			out = (T)constructor.newInstance(params);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return (T)out;
	}

	/**
	 * Exports the values of an object to an ArcheTextObject.
	 * @param object the object to export.
	 * @param atext the destination structure.
	 * @throws ArcheTextExportException if a problem happens during export.
	 * @throws ClassCastException if a value cannot be converted.
	 */
	public static <T> void exportTo(T object, ArcheTextObject atext)
	{
		@SuppressWarnings("unchecked")
		Profile<T> typeProfile = DEFAULT_PROFILEFACTORY.getProfile((Class<T>)object.getClass());
		
		for (Map.Entry<String, MethodInfo> entry : typeProfile.getGetterMethodsByName().entrySet())
		{
			MethodInfo mi = entry.getValue();
			Method method = mi.getMethod();
			atext.set(isNull(mi.getAlias(), entry.getKey()), invokeBlind(method, object));
		}
		
		for (Map.Entry<String, FieldInfo> entry : typeProfile.getPublicFieldsByName().entrySet())
		{
			FieldInfo fi = entry.getValue();
			Field field = fi.getField();
			atext.set(isNull(fi.getAlias(), entry.getKey()), getFieldValue(object, field));
		}
	}

	/**
	 * Applies this object to an object bean / plain ol' Java object.
	 * <p>
	 * This object is applied via the target object's public fields and setter methods.
	 * <p>
	 * For instance, if there is a member on this object called "color", its value
	 * will be applied via the public field "color" or the setter "setColor()". Public
	 * fields take precedence over setters.
	 * @param atObject the source object.
	 * @param object the target object.
	 * @return the applied object itself.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Object> T applyToObject(ArcheTextObject atObject, T object)
	{
		Profile<T> profile = DEFAULT_PROFILEFACTORY.getProfile((Class<T>)object.getClass());
		Iterator<String> it = atObject.getAvailableFieldNames().iterator();
		while (it.hasNext())
		{
			String member = it.next();
			FieldInfo fieldInfo = null; 
			MethodInfo setterInfo = null;
			if ((fieldInfo = isNull(profile.getPublicFieldsByAlias().get(member), profile.getPublicFieldsByName().get(member))) != null)
				setFieldValue(object, fieldInfo.getField(), atObject.get(member, fieldInfo.getType()));
			else if ((setterInfo = isNull(profile.getSetterMethodsByAlias().get(member), profile.getSetterMethodsByName().get(member))) != null)
				invokeBlind(setterInfo.getMethod(), object, atObject.get(member, setterInfo.getType()));
		}
		
		if (profile.getIdentityField() != null)
		{
			FieldInfo fi = profile.getIdentityField();
			setFieldValue(object, fi.getField(), atObject.getIdentity());
		}
		
		if (profile.getIdentitySetterMethod() != null)
		{
			MethodInfo mi = profile.getIdentitySetterMethod();
			invokeBlind(mi.getMethod(), object, atObject.getIdentity());
		}
		
		return object;
	}

	/**
	 * Get name for object.
	 */
	public static <T> String getIdentityFromObject(T object)
	{
		@SuppressWarnings("unchecked")
		Profile<T> typeProfile = DEFAULT_PROFILEFACTORY.getProfile((Class<T>)object.getClass());
	
		if (typeProfile.getIdentityField() != null)
			return String.valueOf(getFieldValue(object, typeProfile.getIdentityField().getField()));
		else if (typeProfile.getIdentityGetterMethod() != null)
			return String.valueOf(invokeBlind(typeProfile.getIdentityGetterMethod().getMethod(), object));
		else
			return null;
	}
	
	
	
	
	
	
	
}