package de.jbee.inject.bind;

import de.jbee.inject.Scoped;
import de.jbee.inject.bind.Context;

public class TestContext {

	void test( Context context ) {
		// we simply cannot know what is a realistic assumption but we want to validate it so we can warn about injections that will not work as expected. 
		context.consider( Scoped.THREAD ).within( Scoped.APPLICATION );
	}
}