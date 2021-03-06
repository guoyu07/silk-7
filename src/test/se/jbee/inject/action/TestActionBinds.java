package se.jbee.inject.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static se.jbee.inject.Type.raw;
import static se.jbee.inject.action.ActionModule.actionDependency;

import org.junit.Test;

import se.jbee.inject.Dependency;
import se.jbee.inject.Injector;
import se.jbee.inject.UnresolvableDependency.SupplyFailed;
import se.jbee.inject.bootstrap.Bootstrap;

public class TestActionBinds {

	private static class ActionBindsModule
			extends ActionModule {

		@Override
		protected void declare() {
			bindActionsIn( MyService.class );
			bindActionsIn( MyOtherService.class );
		}

	}

	static class MyService {

		public Integer negate( Number value ) {
			return -value.intValue();
		}
		
		public Void error() {
			throw new IllegalStateException("This should be wrapped!");
		}
	}

	static class MyOtherService {

		public int mul2( int value, Action<Float, Integer> service ) {
			return value * 2 + service.exec( 2.8f );
		}

		public int round( float value ) {
			return Math.round( value );
		}
	}

	@Test
	public void actionsDecoupleConcreteMethods() {
		Injector injector = Bootstrap.injector( ActionBindsModule.class );
		Dependency<Action<Integer, Integer>> p1 = actionDependency(raw(Integer.class), raw(Integer.class));
		Action<Integer, Integer> mul2 = injector.resolve( p1 );
		assertNotNull( mul2 );
		assertEquals( 9, mul2.exec( 3 ).intValue() );
		Dependency<Action<Number, Integer>> p2 = actionDependency(raw(Number.class), raw(Integer.class));
		Action<Number, Integer> negate = injector.resolve( p2 );
		assertNotNull( mul2 );
		assertEquals( -3, negate.exec( 3 ).intValue() );
		assertEquals( 11, mul2.exec( 4 ).intValue() );
	}
	
	@Test
	public void exceptionsAreWrappedInActionMalfunction() {
		Injector injector = Bootstrap.injector( ActionBindsModule.class );
		Dependency<Action<Void, Void>> de = actionDependency(raw(Void.class), raw(Void.class));
		Action<Void, Void> error = injector.resolve( de );
		try {
			error.exec(null);
			fail("Expected an exception...");
		} catch (ActionMalfunction e) {
			assertSame(IllegalStateException.class, e.getCause().getClass());
		}
	}
}
