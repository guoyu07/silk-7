package de.jbee.inject.util;

import org.junit.Test;

import de.jbee.inject.Binder;
import de.jbee.inject.Module;
import de.jbee.inject.Name;
import de.jbee.inject.Resource;
import de.jbee.inject.Scope;
import de.jbee.inject.Scoped;
import de.jbee.inject.Source;
import de.jbee.inject.Supplier;
import de.jbee.inject.util.RichBinder.RichRootBinder;

public class TestRichBinder {

	static final Binder BINDER = new ToStringBinder();

	static class ToStringBinder
			implements Binder {

		@Override
		public <T> void bind( Resource<T> resource, Supplier<? extends T> supplier, Scope scope,
				Source source ) {
			System.out.println( source + " / " + scope + " / " + resource + " -> " + supplier );
		}

	}

	static interface Leg {

	}

	static class LeftLeg
			implements Leg {

	}

	@Test
	public void testBinder() {
		RichRootBinder binder = RichBinder.root( BINDER, Source.source( Module.class ) );
		binder.in( Scoped.APPLICATION ).injectingInto( Source.class ).bind( String.class ).to(
				"FooBar" );

		binder.bind( Integer.class ).to( 1 );
		binder.bind( Number.class ).to( Integer.class );

		Name left = Name.named( "left" );
		binder.bind( left, Leg.class ).to( LeftLeg.class );

		binder.inPackageOf( Leg.class ).bind( CharSequence.class ).to( String.class );
	}
}
