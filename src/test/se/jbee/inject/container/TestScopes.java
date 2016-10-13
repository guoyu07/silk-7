package se.jbee.inject.container;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static se.jbee.inject.Dependency.dependency;
import static se.jbee.inject.Resource.resource;
import static se.jbee.inject.Source.source;

import org.junit.Test;

import se.jbee.inject.Expiry;
import se.jbee.inject.InjectronInfo;
import se.jbee.inject.Source;

public class TestScopes {

	private static class ConstantProvider<T>
			implements Provider<T> {

		private final T instance;

		ConstantProvider( T instance ) {
			super();
			this.instance = instance;
		}

		@Override
		public T provide() {
			return instance;
		}
	}
	
	static class A {
		// just for test
	}

	static class B {
		// just for test
	}

	@Test
	public void thatDependencyTypeScopeEnsuresSingletonPerExactGenericType() {
		Repository r = Scoped.DEPENDENCY_TYPE.init();
		Source source = source(TestScopes.class);
		Expiry expiry = Expiry.NEVER;
		InjectronInfo<A> da = new InjectronInfo<>(resource( A.class ), source, expiry, 1, 2 );
		InjectronInfo<B> db = new InjectronInfo<>( resource( B.class ), source, expiry, 2, 2 );
		A a = new A();
		B b = new B();
		Provider<A> ia = new ConstantProvider<>( a );
		Provider<B> ib = new ConstantProvider<>( b );
		assertThat( r.serve( dependency( A.class ), da, ia ), sameInstance( a ) );
		assertThat( r.serve( dependency( A.class ), da, null ), sameInstance( a ) ); // the null Provider shouldn't be called now 
		assertThat( r.serve( dependency( B.class ), db, ib ), sameInstance( b ) );
		assertThat( r.serve( dependency( B.class ), db, null ), sameInstance( b ) ); // the null Provider shouldn't be called now
	}
}
