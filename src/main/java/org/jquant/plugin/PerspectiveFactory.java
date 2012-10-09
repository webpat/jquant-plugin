package org.jquant.plugin;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.addView(StrategyListView.ID, IPageLayout.LEFT, 0.2f, editorArea);
		layout.addView(StrategyResultView.ID, IPageLayout.RIGHT, 0.2f, editorArea);

	}

}
