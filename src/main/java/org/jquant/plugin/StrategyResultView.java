package org.jquant.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO : Real time StrategyRunner update view 
 * @author patrick.merheb
 *
 */
public class StrategyResultView extends ViewPart {

	public static final String ID = "org.jquant.view.backtestResults";

	
	
	public StrategyResultView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);

	}

	@Override
	public void setFocus() {

	}

}
