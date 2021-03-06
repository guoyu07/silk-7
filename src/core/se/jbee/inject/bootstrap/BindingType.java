/*
 *  Copyright (c) 2012-2017, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject.bootstrap;

import se.jbee.inject.Supplier;

/**
 * What is suppling instances for a {@link Binding}?
 * 
 * As we cannot look into the implementation of a {@link Supplier} this type
 * groups concrete ways to supply instances.
 */
public enum BindingType {

	/**
	 * The binding expresses a need, it is unclear if another binding can
	 * fulfill it.
	 */
	REQUIRED,

	/**
	 * The binding is a virtual or generic instance factory like one for lists
	 * or other type parameterized "bridges".
	 */
	LINK,

	/**
	 * The instances are supplied from a {@link Supplier} that has been defined
	 * before macro expansion. This might be user defined or hard-wired one
	 * within the binder API.
	 */
	PREDEFINED,

	/**
	 * The instances are supplied by constructing new ones using a constructor.
	 */
	CONSTRUCTOR,

	/**
	 * The instances are supplied using a factory method.
	 */
	METHOD,

	/**
	 * The binding is an incomplete macro so far that changes to one of the
	 * other types later on.
	 */
	MACRO,
}
