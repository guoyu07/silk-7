package de.jbee.inject.bind;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.jbee.inject.Dependency;
import de.jbee.inject.DependencyResolver;
import de.jbee.inject.Type;
import de.jbee.inject.service.Service;
import de.jbee.inject.service.ServiceModule;

public class TestServiceBinds {

	static class ServiceBindsModule
			extends ServiceModule {

		@Override
		protected void configure() {
			bindServices( SomeTestService.class );
			bindServices( AnotherTestService.class );
		}

	}

	private static class AnotherTestService {

		public Integer negate( Number value ) {
			return -value.intValue();
		}
	}

	private static class SomeTestService {

		public Integer mul2( Integer value, Service<Float, Integer> service ) {
			return value * 2 + service.invoke( 2.8f );
		}

		public Integer round( Float value ) {
			return Math.round( value );
		}
	}

	@Test
	public void test() {
		DependencyResolver injector = Bootstrap.injector( ServiceBindsModule.class );
		Dependency<Service> dependency = Dependency.dependency( Type.raw( Service.class ).parametized(
				Integer.class, Integer.class ) );
		Service<Integer, Integer> service = injector.resolve( dependency );
		assertNotNull( service );
		assertThat( service.invoke( 3 ), is( 9 ) );
		Dependency<Service> dependency2 = Dependency.dependency( Type.raw( Service.class ).parametized(
				Number.class, Integer.class ) );
		Service<Number, Integer> negate = injector.resolve( dependency2 );
		assertNotNull( service );
		assertThat( negate.invoke( 3 ), is( -3 ) );
	}
}