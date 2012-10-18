package org.jquant.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.jquant.portfolio.PortfolioStatistics;

/**
 * TODO : Real time StrategyRunner update view 
 * @author patrick.merheb
 *
 */
public class StrategyResultView extends ViewPart {

	public static final String ID = "org.jquant.view.backtestResults";

	private PortfolioStatistics results;
	/*
	 * Eclipse forms support. Requires org.eclipse.ui.forms
	 */
	private FormToolkit toolkit;
	private ScrolledForm scrolledForm;

	private Label initialWealth;

	private Label finalWealth;

	private Label annualizedReturn;

	private Label realizedPnL;

	private Label maxDD;

	private Label nbWiningTrades;

	private Label nbLosingTrades;

	private Label averageWiningTrade;

	private Label averageLosingTrade;

	private Label largestLoosingTrade;

	private Label largestWinningTrade; 
	
	
	public StrategyResultView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		// Create a Form API Toolkit
		toolkit = new FormToolkit(parent.getDisplay());
		
		/*
		 * Create a scrolled form widget, 
		 */
		scrolledForm = toolkit.createScrolledForm(parent);
		scrolledForm.setFont(new Font(null,"Times",18,SWT.BOLD|SWT.ITALIC));
		scrolledForm.setText("Simulation Results");
		scrolledForm.setImage(JQuantPlugin.getImageDescriptor("money.png").createImage());
		
		
		
		/*
		 * Make a nice gradient
		 */
		toolkit.decorateFormHeading(scrolledForm.getForm());
		
		readSimulationResults();
		buildStatSection();
		if(results != null)
			refreshSimulationStatistics();

		
	}

	private void buildStatSection(){
			GridLayout layout = new GridLayout();
			layout.marginHeight = 5;
			layout.marginWidth = 10;
			layout.numColumns = 2;
			
			scrolledForm.getBody().setLayout(layout);
			
			// setup bold font
			Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
			Label l1 = toolkit.createLabel(scrolledForm.getBody(), "Initial Wealth:");
			l1.setFont(boldFont);
			initialWealth = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY,SWT.WRAP);
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Final Wealth:");
			l1.setFont(boldFont);
			finalWealth = toolkit.createLabel(scrolledForm.getBody(),StringUtils.EMPTY,SWT.WRAP);
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Annualized Return:");
			l1.setFont(boldFont);
			annualizedReturn = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY,SWT.WRAP);
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Realized P&L:");
			l1.setFont(boldFont);
			realizedPnL = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Max DrawDown:");
			l1.setFont(boldFont);
			maxDD = toolkit.createLabel(scrolledForm.getBody(),StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Number Of Winning Trades :");
			l1.setFont(boldFont);
			nbWiningTrades = toolkit.createLabel(scrolledForm.getBody(),StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Number Of Losing Trades :");
			l1.setFont(boldFont);
			nbLosingTrades = toolkit.createLabel(scrolledForm.getBody(),StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Average Winning Trade :");
			l1.setFont(boldFont);
			averageWiningTrade = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Average Losing Trade :");
			l1.setFont(boldFont);
			averageLosingTrade = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Largest Winning Trade :");
			l1.setFont(boldFont);
			largestWinningTrade = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY ,SWT.WRAP);
			
			l1 = toolkit.createLabel(scrolledForm.getBody(), "Largest Losing Trade :");
			l1.setFont(boldFont);
			largestLoosingTrade = toolkit.createLabel(scrolledForm.getBody(), StringUtils.EMPTY ,SWT.WRAP);
			
	}
	
	private void refreshSimulationStatistics() {
			
			initialWealth.setText(String.format("%1$,.2f",results.getInitialWealth()));
			finalWealth.setText(String.format("%1$,.2f",results.getFinalWealth()));
			annualizedReturn.setText(String.format("%1$,.2f",results.getAnnualizedReturn()));
			realizedPnL.setText(String.format("%1$,.2f",results.getRealizedPnL()));
			maxDD.setText(String.format("%1$,.2f",results.getMaxDrawDownData().getMaxDrawDown()));
			nbWiningTrades.setText(String.valueOf(results.getWinningTrades()));
			nbLosingTrades.setText(String.valueOf(results.getLosingTrades()));
			averageWiningTrade.setText(String.format("%1$,.2f",results.getAverageWinningTrade()));
			averageLosingTrade.setText(String.format("%1$,.2f",results.getAverageLosingTrade()));
			largestLoosingTrade.setText(String.format("%1$,.2f",results.getLargestLosingTrade()));
			largestWinningTrade.setText(String.format("%1$,.2f",results.getLargestWinningTrade()));

	}

	
	
	
	@Override
	public void setFocus() {
		scrolledForm.setFocus();
	}
	
	@Override
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}

	
	public void refresh(){
		readSimulationResults();
		refreshSimulationStatistics();
		scrolledForm.layout(true, true);
	}

	private void readSimulationResults(){
		// Deserialize Simulation Statistics results from a file
		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir+File.separator+"simulation.bin");
	    try {
	    	
	    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			// Deserialize the object
			
			results = (PortfolioStatistics) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
