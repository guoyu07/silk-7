package de.jbee.inject.service;

import static de.jbee.inject.Dependency.dependency;
import static de.jbee.inject.Source.source;
import static de.jbee.inject.Type.parameterTypes;
import static de.jbee.inject.Type.raw;
import static de.jbee.inject.Type.returnType;
import static de.jbee.inject.bind.Bootstrap.nonnullThrowsReentranceException;
import static de.jbee.inject.util.Scoped.APPLICATION;
import static de.jbee.inject.util.Scoped.DEPENDENCY_TYPE;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import de.jbee.inject.Dependency;
import de.jbee.inject.Injector;
import de.jbee.inject.Injectron;
import de.jbee.inject.Supplier;
import de.jbee.inject.Type;
import de.jbee.inject.bind.Binder;
import de.jbee.inject.bind.BinderModule;
import de.jbee.inject.bind.Bindings;
import de.jbee.inject.bind.Bootstrapper;
import de.jbee.inject.bind.Bundle;
import de.jbee.inject.bind.ConstructionStrategy;
import de.jbee.inject.bind.Extend;
import de.jbee.inject.bind.Module;
import de.jbee.inject.bind.Binder.RootBinder;
import de.jbee.inject.bind.Binder.TypedBinder;
import de.jbee.inject.service.ServiceInvocation.ServiceInvocationExtension;
import de.jbee.inject.service.ServiceMethod.ServiceClassExtension;
import de.jbee.inject.util.Scoped;
import de.jbee.inject.util.TypeReflector;

/**
 * When binding {@link ServiceMethod}s this {@link Module} can be extended.
 * 
 * It provides service-related build methods.
 * 
 * @author Jan Bernitt (jan.bernitt@gmx.de)
 */
public abstract class ServiceModule
		implements Module, Bundle {

	protected final void bindServiceMethodsIn( Class<?> service ) {
		binder.extend( ServiceClassExtension.class, service );
	}

	protected final void extend( ServiceInvocationExtension type,
			Class<? extends ServiceInvocation<?>> invocation ) {
		//TODO we need a special binder for this
		binder.extend( type, invocation );
	}

	protected final <T> TypedBinder<T> starbind( Class<T> service ) {
		return binder.per( DEPENDENCY_TYPE ).starbind( service );
	}

	private RootBinder binder;

	@Override
	public final void bootstrap( Bootstrapper bootstrap ) {
		bootstrap.install( ServiceMethodModule.class );
		bootstrap.install( this );
	}

	@Override
	public void declare( Bindings bindings, ConstructionStrategy strategy ) {
		nonnullThrowsReentranceException( binder );
		binder = Binder.create( bindings, strategy, source( getClass() ), Scoped.APPLICATION );
		declare();
	}

	protected abstract void declare();

	static class ServiceMethodModule
			extends BinderModule {

		static final ServiceStrategy DEFAULT_SERVICE_STRATEGY = new BuildinServiceStrategy();

		@Override
		public void declare() {
			per( APPLICATION ).bind( ServiceProvider.class ).toSupplier(
					ServiceProviderSupplier.class );
			per( DEPENDENCY_TYPE ).starbind( ServiceMethod.class ).toSupplier(
					ServiceSupplier.class );
			asDefault().per( APPLICATION ).bind( ServiceStrategy.class ).to(
					DEFAULT_SERVICE_STRATEGY );
		}

	}

	private static final class BuildinServiceStrategy
			implements ServiceStrategy {

		BuildinServiceStrategy() {
			// make visible
		}

		@Override
		public Method[] serviceMethodsIn( Class<?> serviceClass ) {
			return serviceClass.getDeclaredMethods();
		}

	}

	private static final class ServiceProviderSupplier
			implements Supplier<ServiceProvider> {

		@Override
		public ServiceProvider supply( Dependency<? super ServiceProvider> dependency,
				Injector context ) {
			return new ServiceMethodProvider( context );
		}

	}

	private static final class ServiceMethodProvider
			implements ServiceProvider {

		/**
		 * A list of service methods for each service class.
		 */
		private final Map<Class<?>, Method[]> methodsCache = new IdentityHashMap<Class<?>, Method[]>();
		/**
		 * All already created service methods identified by a unique function signature.
		 * 
		 * OPEN why not put the {@link ServiceMethod} in cache ?
		 */
		private final Map<String, Method> methodCache = new HashMap<String, Method>();

		private final Injector context;
		private final Class<?>[] serviceClasses;
		private final ServiceStrategy strategy;

		ServiceMethodProvider( Injector context ) {
			super();
			this.context = context;
			this.serviceClasses = context.resolve( Extend.dependency( ServiceClassExtension.class ) );
			this.strategy = context.resolve( dependency( ServiceStrategy.class ).injectingInto(
					ServiceMethodProvider.class ) );
		}

		@Override
		public <P, R> ServiceMethod<P, R> provide( Type<P> parameterType, Type<R> returnType ) {
			Method service = serviceMethod( parameterType, returnType );
			return create( service, parameterType.getRawType(), returnType.getRawType(), context );
		}

		private <P, T> ServiceMethod<P, T> create( Method service, Class<P> parameterType,
				Class<T> returnType, Injector context ) {
			return new LazyServiceMethod<P, T>(
					TypeReflector.newInstance( service.getDeclaringClass() ), service,
					parameterType, returnType, context );
		}

		private <P, T> Method serviceMethod( Type<P> parameterType, Type<T> returnType ) {
			String signatur = parameterType + "->" + returnType; // haskell like function signature
			Method method = methodCache.get( signatur );
			if ( method != null ) {
				return method;
			}
			synchronized ( methodCache ) {
				method = methodCache.get( signatur );
				if ( method == null ) {
					method = resolveServiceMethod( parameterType, returnType );
					methodCache.put( signatur, method );
				}
			}
			return method;
		}

		private <P, T> Method resolveServiceMethod( Type<P> parameterType, Type<T> returnType ) {
			for ( Class<?> service : serviceClasses ) {
				for ( Method sm : serviceClassMethods( service ) ) {
					Type<?> rt = returnType( sm );
					if ( rt.equalTo( returnType ) ) {
						for ( Type<?> pt : parameterTypes( sm ) ) {
							if ( pt.equalTo( parameterType ) ) {
								return sm;
							}
						}
					}
				}
			}
			//FIXME primitives types aren't covered...but ... they can be used as parameter for Type 
			return null;
		}

		private Method[] serviceClassMethods( Class<?> service ) {
			Method[] methods = methodsCache.get( service );
			if ( methods != null ) {
				return methods;
			}
			synchronized ( methodsCache ) {
				methods = methodsCache.get( service );
				if ( methods == null ) {
					methods = strategy.serviceMethodsIn( service );
					methodsCache.put( service, methods );
				}
			}
			return methods;
		}

	}

	//OPEN move this to test-code since it is more of an example using the SM directly as the service interface
	private static class ServiceSupplier
			implements Supplier<ServiceMethod<?, ?>> {

		@Override
		public ServiceMethod<?, ?> supply( Dependency<? super ServiceMethod<?, ?>> dependency,
				Injector context ) {
			ServiceProvider serviceProvider = context.resolve( dependency.anyTyped( ServiceProvider.class ) );
			Type<?>[] parameters = dependency.getType().getParameters();
			return serviceProvider.provide( parameters[0], parameters[1] );
		}
	}

	private static class LazyServiceMethod<P, T>
			implements ServiceMethod<P, T> {

		private final Object object;
		private final Method method;
		private final Class<P> parameterType;
		private final Class<T> returnType;
		private final Injector context;
		private final Type<?>[] parameterTypes;
		private final Injectron<?>[] argumentInjectrons;
		private final Object[] argumentTemplate;

		LazyServiceMethod( Object object, Method service, Class<P> parameterType,
				Class<T> returnType, Injector context ) {
			super();
			this.object = object;
			this.method = service;
			TypeReflector.makeAccessible( method );
			this.parameterType = parameterType;
			this.returnType = returnType;
			this.context = context;
			this.parameterTypes = parameterTypes( service );
			this.argumentInjectrons = argumentInjectrons();
			this.argumentTemplate = argumentTemplate();
		}

		private Object[] argumentTemplate() {
			Object[] template = new Object[parameterTypes.length];
			for ( int i = 0; i < template.length; i++ ) {
				Injectron<?> injectron = argumentInjectrons[i];
				if ( injectron != null && injectron.getExpiry().isNever() ) {
					template[i] = instance( injectron, dependency( parameterTypes[i] ) );
				}
			}
			return template;
		}

		@Override
		public T invoke( P params ) {
			Object[] args = actualArguments( params );
			try {
				return returnType.cast( method.invoke( object, args ) );
			} catch ( Exception e ) {
				throw new RuntimeException( "Failed to invoke service:\n" + e.getMessage(), e );
			}
		}

		private Injectron<?>[] argumentInjectrons() {
			Injectron<?>[] res = new Injectron<?>[parameterTypes.length];
			for ( int i = 0; i < res.length; i++ ) {
				Type<?> paramType = parameterTypes[i];
				res[i] = paramType.getRawType() == parameterType
					? null
					: context.resolve( dependency( raw( Injectron.class ).parametized( paramType ) ) );
			}
			return res;
		}

		private Object[] actualArguments( P params ) {
			Object[] args = argumentTemplate.clone();
			for ( int i = 0; i < args.length; i++ ) {
				Type<?> paramType = parameterTypes[i];
				if ( paramType.getRawType() == parameterType ) {
					args[i] = params;
				} else if ( args[i] == null ) {
					args[i] = instance( argumentInjectrons[i], dependency( paramType ) );
				}
			}
			return args;
		}

		private static <I> I instance( Injectron<I> injectron, Dependency<?> dependency ) {
			return injectron.instanceFor( (Dependency<? super I>) dependency );
		}

		@Override
		public String toString() {
			return method.getDeclaringClass().getSimpleName() + ": " + returnType.getSimpleName()
					+ " " + method.getName() + "(" + parameterType.getSimpleName() + ")";
		}
	}
}
