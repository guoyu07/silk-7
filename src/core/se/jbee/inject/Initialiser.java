package se.jbee.inject;

/**
 * Users can bind an implementation for this interface. The {@link Injector}
 * will resolve all of them and call their {@link #init(Injector)} method as
 * soon as the context is done.
 * 
 * This gives users the possibility to run initialization code once and build
 * more powerful mechanisms on top of it.
 * 
 * @author jan
 */
@FunctionalInterface
public interface Initialiser {

	/**
	 * Is called by the {@link Injector} as soon as the context itself is
	 * initialized and ready to be used by the implementation of this interface.
	 * 
	 * @param context
	 *            use to receive instances that require further initialization
	 *            setup
	 */
	void init(Injector context);
}
