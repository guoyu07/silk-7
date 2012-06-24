package de.jbee.inject;

import static de.jbee.inject.Dependency.dependency;
import static de.jbee.inject.Resource.resource;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestScopes {

	private static class ConstantInjectable<T>
			implements Injectable<T> {

		private final T instance;

		ConstantInjectable( T instance ) {
			super();
			this.instance = instance;
		}

		@Override
		public T instanceFor( Injection<T> injection ) {
			return instance;
		}
	}

	private static class A {
		// just for test
	}

	private static class B {
		// just for test
	}

	@Test
	public void thatDependencyTypeScopeEnsuresSingletonPerExactGenericType() {
		Repository r = Scoped.DEPENDENCY_TYPE.init();
		Injection<A> ai = new Injection<A>( resource( A.class ), dependency( A.class ), 1, 2 );
		Injection<B> bi = new Injection<B>( resource( B.class ), dependency( B.class ), 2, 2 );
		A a = new A();
		B b = new B();
		Injectable<A> ia = new ConstantInjectable<A>( a );
		Injectable<B> ib = new ConstantInjectable<B>( b );
		assertThat( r.serve( ai, ia ), sameInstance( a ) );
		assertThat( r.serve( ai, null ), sameInstance( a ) );
		assertThat( r.serve( bi, ib ), sameInstance( b ) );
		assertThat( r.serve( bi, null ), sameInstance( b ) );
	}
}