package org.jquant.plugin;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;

public class JQuantLaunchListener implements ILaunchesListener2 {



	@Override
	public void launchesRemoved(ILaunch[] launches) {
		
	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
		
	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
		
	}

	@Override
	public void launchesTerminated(ILaunch[] launches) {
		
		// Display StrategyResultViewPart
		JQuantPlugin.displayStrategyResultView();

	}

}
