/*
 *  Copyright (c) 2012-2017, Jan Bernitt 
 *			
 *  Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
 */
package se.jbee.inject.bootstrap;

/**
 * Determines / extracts the {@link Module}s from a root {@link Bundle}.
 * 
 * @author Jan Bernitt (jan@jbee.se)
 */
@FunctionalInterface
public interface Modulariser {

	/**
	 * @return All {@link Module}s that result from expanding the given root
	 *         {@link Bundle} to the module level.
	 */
	Module[] modularise( Class<? extends Bundle> root );
}
