package br.ufc.lps.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import br.ufc.lps.gui.export.ExportOfficeExcel;
import br.ufc.lps.model.FamiliarModel;
import br.ufc.lps.model.IModel;
import br.ufc.lps.model.SplotModel;
import br.ufc.lps.model.context.FamiliarContextModel;
import br.ufc.lps.model.context.SplotContextModel;
import br.ufc.lps.model.xml.ModelID;
import br.ufc.lps.model.xml.XMLFamiliarModel;
import br.ufc.lps.model.xml.XMLSplotModel;
import br.ufc.lps.splar.core.fm.FeatureModelException;

public class Main extends JFrame {

	
	private JTabbedPane tabbedPane;
	//Identifica o viewer atual para definir o modelo em questão a ser utilizado nas metricas
	private ViewerPanel currentViewer;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
										
					Main frame = new Main();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

		
	/**
	 * Create the frame.
	 */
	public Main() {
		
		initXMLmodels();
	

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 480);

		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
		        int index = sourceTabbedPane.getSelectedIndex();
		        if(index != -1) {
					Component componentAt = sourceTabbedPane.getComponentAt(index);
					if(componentAt instanceof ViewerPanel)
						currentViewer = (ViewerPanel) componentAt;
		        }
				
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu mnMeasures = new JMenu("Measures");
		final JMenu mnFile = new JMenu("File");
		
		menuBar.add(mnFile);

		JMenuItem mntmOpenSplotxml = new JMenuItem("Open SPLOT .xml");
		mntmOpenSplotxml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						
						if(f.isDirectory())
							return true;
						
						String fileName = f.getName();
						
						int firstIdentifier = fileName.lastIndexOf('.');
						
						if( firstIdentifier > -1 && firstIdentifier < fileName.length() - 1)
							if(fileName.substring(firstIdentifier+1).equalsIgnoreCase("xml"))
								return true;
						
						return false;
					}
				});
				
				int returnValue = fileChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					final ViewerPanel viewer = new ViewerPanel(new SplotContextModel(path));
					currentViewer = viewer;
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							mnMeasures.setEnabled(true);
							
							String tabName = viewer.getModelName();
							long time = System.currentTimeMillis();
							
							tabbedPane.addTab(tabName+time, viewer);	
							
							int index = tabbedPane.indexOfTab(tabName+time);
							JPanel pnlTab = new JPanel(new GridBagLayout());
							pnlTab.setOpaque(false);
							JLabel lblTitle = new JLabel(tabName);
							JButton btnClose = new JButton("x");

							GridBagConstraints gbc = new GridBagConstraints();
							gbc.gridx = 0;
							gbc.gridy = 0;
							gbc.weightx = 1;

							pnlTab.add(lblTitle, gbc);

							gbc.gridx++;
							gbc.weightx = 0;
							pnlTab.add(btnClose, gbc);

							tabbedPane.setTabComponentAt(index, pnlTab);

							btnClose.addActionListener(new MyCloseActionHandler(tabName+time));
							
							Main.this.repaint();
						}
					});
					
					
					
					
					
				}
			}
		});
		
		
		mnFile.add(mntmOpenSplotxml);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenu mnNewMenu = new JMenu("Export");
		mnFile.add(mnNewMenu);
		
		JMenuItem mntmExportToOffice = new JMenuItem("Export to Office Excel Without Contexts");
		mntmExportToOffice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						
						if(f.isDirectory())
							return true;
						
						String fileName = f.getName();
						
						int firstIdentifier = fileName.lastIndexOf('.');
						
						if( firstIdentifier > -1 && firstIdentifier < fileName.length() - 1)
							if(fileName.substring(firstIdentifier+1).equalsIgnoreCase("xml"))
								return true;
						
						return false;
					}
				});
				
				int returnValue = fileChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					
					File files[] = fileChooser.getSelectedFiles();
					
					if(files.length > 50){
						JOptionPane.showMessageDialog(Main.this, "Please, do not choose more than 50 model files.", "Exceeded!", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					
					
					ExportOfficeExcel exportExcel = new ExportOfficeExcel(files);
					
					String excelFileName = (String)JOptionPane.showInputDialog("Type the file name");
					
					if(excelFileName == null || excelFileName.isEmpty())
						excelFileName = "exportedExcelFile_" + System.currentTimeMillis();
					
					exportExcel.standardExport(excelFileName);
					
					
				}
				
			}
		});
		mnNewMenu.add(mntmExportToOffice);
		
		JMenuItem mntmExportToOffice_1 = new JMenuItem("Export to Office Excel With Contexts");
		mntmExportToOffice_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						
						if(f.isDirectory())
							return true;
						
						String fileName = f.getName();
						
						int firstIdentifier = fileName.lastIndexOf('.');
						
						if( firstIdentifier > -1 && firstIdentifier < fileName.length() - 1)
							if(fileName.substring(firstIdentifier+1).equalsIgnoreCase("xml"))
								return true;
						
						return false;
					}
				});
				
				int returnValue = fileChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					
									
					ExportOfficeExcel exportExcel = new ExportOfficeExcel(new File[]{new File(filePath)});
					
					String excelFileName = (String)JOptionPane.showInputDialog("Type the file name");
					
					if(excelFileName == null || excelFileName.isEmpty())
						excelFileName = "exportedExcelFile_" + System.currentTimeMillis();
					
					exportExcel.exportWithContexts(excelFileName);
					
					
				}
				
			}
		});
		mnNewMenu.add(mntmExportToOffice_1);

		
		menuBar.add(mnMeasures);
		mnMeasures.setEnabled(false);

		constructMeasuresMenuItem(mnMeasures);
		
				
		JMenu mnEditor = new JMenu("Editor");
		menuBar.add(mnEditor);
		
		JMenuItem mntmEditSplotModel = new JMenuItem("Open SPLOT's xml");
		mnEditor.add(mntmEditSplotModel);
		
		mntmEditSplotModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						
						if(f.isDirectory())
							return true;
						
						String fileName = f.getName();
						
						int firstIdentifier = fileName.lastIndexOf('.');
						
						if( firstIdentifier > -1 && firstIdentifier < fileName.length() - 1)
							if(fileName.substring(firstIdentifier+1).equalsIgnoreCase("xml"))
								return true;
						
						return false;
					}
				});
				
				int returnValue = fileChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					IModel model = new SplotModel(path);
					final EditorPanel editor = new EditorPanel(model, ModelID.SPLOT_MODEL.getId(), path);
					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							mnMeasures.setEnabled(true);
							String tabName = editor.getModelName();
							long time = System.currentTimeMillis();
							
							tabbedPane.addTab(tabName+time, editor);	
							
							int index = tabbedPane.indexOfTab(tabName+time);
							JPanel pnlTab = new JPanel(new GridBagLayout());
							pnlTab.setOpaque(false);
							JLabel lblTitle = new JLabel(tabName);
							JButton btnClose = new JButton("x");

							GridBagConstraints gbc = new GridBagConstraints();
							gbc.gridx = 0;
							gbc.gridy = 0;
							gbc.weightx = 1;

							pnlTab.add(lblTitle, gbc);

							gbc.gridx++;
							gbc.weightx = 0;
							pnlTab.add(btnClose, gbc);

							tabbedPane.setTabComponentAt(index, pnlTab);

							btnClose.addActionListener(new MyCloseActionHandler(tabName+time));
						}
					});
					
					
					
					
					
				}
			}
		});
		
		
		
		
	}

	private void initXMLmodels() {
		new XMLSplotModel();
		new XMLFamiliarModel();
	}

	public class MyCloseActionHandler implements ActionListener {

	    private String tabName;

	    public MyCloseActionHandler(String tabName) {
	        this.tabName = tabName;
	    }

	    public String getTabName() {
	        return tabName;
	    }

	    public void actionPerformed(ActionEvent evt) {

	        int index = tabbedPane.indexOfTab(getTabName());
	        if (index >= 0) {

	            tabbedPane.removeTabAt(index);
	            // It would probably be worthwhile getting the source
	            // casting it back to a JButton and removing
	            // the action handler reference ;)

	        }

	    }

	}   


	private void constructMeasuresMenuItem(final JMenu mnMeasures) {
		JMenuItem mntmNonfunctionalCommonality = new JMenuItem(
				"Non-Functional Commonality");
		mnMeasures.add(mntmNonfunctionalCommonality);

		// Number of Features
		JMenuItem mntmnumberOfFeatures = new JMenuItem("Number of Features");
		mntmnumberOfFeatures.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentViewer.getLblResultReasoning().setText("Number of Features: " + currentViewer.getModel().numberOfFeatures());				
			}
		});
		mnMeasures.add(mntmnumberOfFeatures);

		//Number of Top Features
		JMenuItem mntmNumberOfTop = new JMenuItem("Number of Top features");
		mntmNumberOfTop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentViewer.getLblResultReasoning().setText("Number of Top Features: " + currentViewer.getModel().numberOfTopFeatures());
				
			}
		});
		mnMeasures.add(mntmNumberOfTop);

		JMenuItem mntmNumberOfLeaf = new JMenuItem("Number of Leaf Features");
		mntmNumberOfLeaf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of Leaf Features: " + currentViewer.getModel().numberOfLeafFeatures());
			}
		});
		mnMeasures.add(mntmNumberOfLeaf);

		JMenuItem mntmDepthOfTree = new JMenuItem("Depth of tree");
		mntmDepthOfTree.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Depth of tree: " + currentViewer.getModel().depthOfTree());
			}
		});
		mnMeasures.add(mntmDepthOfTree);

		JMenuItem mntmCognitiveComplexityOf = new JMenuItem(
				"Cognitive Complexity of a Feature Model");
		mntmCognitiveComplexityOf.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Cognitive Complexity of a Feature Model: " + currentViewer.getModel().cognitiveComplexityOfFeatureModel());
			}
		});
		mnMeasures.add(mntmCognitiveComplexityOf);

		JMenuItem mntmNewMenuItem = new JMenuItem("Feature Extendibility");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Feature Extendibility: " + currentViewer.getModel().featureExtendibility());
			}
		});
		mnMeasures.add(mntmNewMenuItem);

		JMenuItem mntmFlexibilityOfConguration = new JMenuItem(
				"Flexibility of con\uFB01guration");
		mntmFlexibilityOfConguration.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Flexibility of Configuration: " + currentViewer.getModel().flexibilityOfConfiguration());
			}
		});
		mnMeasures.add(mntmFlexibilityOfConguration);

		JMenuItem mntmSingleCyclicDependent = new JMenuItem(
				"Single Cyclic Dependent Features");
		mntmSingleCyclicDependent.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentViewer.getLblResultReasoning().setText("Single Cyclic Dependent Features: " + currentViewer.getModel().singleCyclicDependentFeatures());
				
			}
		});
		mnMeasures.add(mntmSingleCyclicDependent);

		JMenuItem mntmMultipleCyclicDependent = new JMenuItem(
				"Multiple Cyclic Dependent Features");
		mntmMultipleCyclicDependent.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentViewer.getLblResultReasoning().setText("Multiple Cyclic Dependent Features: " + currentViewer.getModel().multipleCyclicDependentFeatures());
				
			}
		});
		mnMeasures.add(mntmMultipleCyclicDependent);

		JMenuItem mntmCyclomaticComplexity = new JMenuItem(
				"Cyclomatic complexity");
		mntmCyclomaticComplexity.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Cyclomatic complexity: " + currentViewer.getModel().cyclomaticComplexity());
			}
		});
		mnMeasures.add(mntmCyclomaticComplexity);

		JMenuItem mntmCompoundComplexity = new JMenuItem("Compound Complexity");
		mntmCompoundComplexity.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				currentViewer.getLblResultReasoning().setText("Compound complexity: " + currentViewer.getModel().compoundComplexity());
			}
		});
		
		mnMeasures.add(mntmCompoundComplexity);

		JMenuItem mntmCrosstreeConstraints = new JMenuItem(
				"Cross-tree constraints");
		mntmCrosstreeConstraints.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Cross-tree Constraints: " + currentViewer.getModel().crossTreeConstraints());
			}
		});		
		mnMeasures.add(mntmCrosstreeConstraints);

		JMenuItem mntmCoefcientOfConnectivitydensity = new JMenuItem(
				"Coeficient of Connectivity-Density");
		mntmCoefcientOfConnectivitydensity.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				currentViewer.getLblResultReasoning().setText("Coeficient of Connectivity-Density: " + currentViewer.getModel().coefficientOfConnectivityDensity());
			}
		});
		mnMeasures.add(mntmCoefcientOfConnectivitydensity);

		JMenuItem mntmNumberOfVariable = new JMenuItem(
				"Number of variable features");
		mntmNumberOfVariable.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of variable features: " + currentViewer.getModel().numberOfVariableFeatures());
			}
		});
		mnMeasures.add(mntmNumberOfVariable);

		JMenuItem mntmNumberOfVariation = new JMenuItem(
				"Number of variation points");
		mntmNumberOfVariation.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of variation points: " + currentViewer.getModel().numberOfVariationPoints());
			}
		});
		mnMeasures.add(mntmNumberOfVariation);

		final JMenuItem mntmSingleHotspotFeatures = new JMenuItem(
				"Single Variation Points Features");
		mntmSingleHotspotFeatures.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Single Variation Points Features: " + currentViewer.getModel().singleVariationPointsFeatures());
			}
		});
		mnMeasures.add(mntmSingleHotspotFeatures);

		final JMenuItem mntmMultipleHotspotFeatures = new JMenuItem(
				"Multiple Variation Points Features");
		mntmMultipleHotspotFeatures.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Multiple Variation Points Features" + ": " + currentViewer.getModel().multipleVariationPointsFeatures());
			}
		});
		
		mnMeasures.add(mntmMultipleHotspotFeatures);

		JMenuItem mntmRigidNoVariation = new JMenuItem(
				"Rigid No Variation Points Features");
		mntmRigidNoVariation.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentViewer.getLblResultReasoning().setText("Rigid No Variation Points Features: " + currentViewer.getModel().rigidNotVariationPointsFeatures());
				
			}
		});
		
		mnMeasures.add(mntmRigidNoVariation);

		JMenuItem mntmNumberOfValid = new JMenuItem(
				"Number of valid Configurations");
		
		mntmNumberOfValid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
								
				currentViewer.getLblResultReasoning().setText("Number of valid configurations: " + currentViewer.getModel().numberOfValidConfigurations());
			}
		});
		
		mnMeasures.add(mntmNumberOfValid);

		JMenuItem mntmProductLineTotal = new JMenuItem(
				"Product Line Total Variability");
		
		mntmProductLineTotal.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Product Line Total Variability: " + currentViewer.getModel().productLineTotalVariability());
			}
		});
		
		mnMeasures.add(mntmProductLineTotal);
		
		JMenuItem mntmNumberOfContexts = new JMenuItem("Number of Contexts");
		mntmNumberOfContexts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				currentViewer.getLblResultReasoning().setText("Number of Contexts: " + currentViewer.getModel().numberOfContexts());				
			}
		});
		mnMeasures.add(mntmNumberOfContexts);
		
		JMenuItem mntmNumberOfActivated = new JMenuItem("Number of Activated Features");
		mntmNumberOfActivated.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of Activated Features: " + currentViewer.getModel().numberActivatedFeatures());
			}
		});
		mnMeasures.add(mntmNumberOfActivated);
		
		JMenuItem mntmNumberOfDeactivated = new JMenuItem("Number of Deactivated Features");
		mntmNumberOfDeactivated.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of Deactivated Features: " + currentViewer.getModel().numberDeactivatedFeatures());
			}
		});
		mnMeasures.add(mntmNumberOfDeactivated);
		
		JMenuItem mntmNumberOfContextConstraints = new JMenuItem("Number of Context Constraints");
		mntmNumberOfContextConstraints.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentViewer.getLblResultReasoning().setText("Number of Context Constraints: " + currentViewer.getModel().numberContextConstraints());
			}
		});
		mnMeasures.add(mntmNumberOfContextConstraints);
	}
	

}
