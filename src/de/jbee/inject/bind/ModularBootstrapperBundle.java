package de.jbee.inject.bind;

import de.jbee.inject.bind.Bootstrapper.ModularBootstrapper;

public abstract class ModularBootstrapperBundle<M>
		implements ModularBundle<M>, ModularBootstrapper<M> {

	private ModularBootstrapper<M> bootstrap;

	@Override
	public void bootstrap( ModularBootstrapper<M> bootstrap ) {
		BootstrappingModule.nonnullThrowsReentranceException( this.bootstrap );
		this.bootstrap = bootstrap;
		bootstrap();
	}

	@Override
	public void install( Class<? extends Bundle> bundle, M module ) {
		bootstrap.install( bundle, module );
	}

	protected abstract void bootstrap();
}
