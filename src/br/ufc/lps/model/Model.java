package br.ufc.lps.model;

import javax.naming.OperationNotSupportedException;

import br.ufc.lps.splar.core.fm.FeatureModel;
import br.ufc.lps.splar.core.fm.FeatureModelException;
import br.ufc.lps.splar.core.fm.FeatureModelStatistics;
import br.ufc.lps.splar.core.heuristics.FTPreOrderSortedECTraversalHeuristic;
import br.ufc.lps.splar.core.heuristics.VariableOrderingHeuristic;
import br.ufc.lps.splar.core.heuristics.VariableOrderingHeuristicsManager;
import br.ufc.lps.splar.plugins.reasoners.bdd.javabdd.FMReasoningWithBDD;
import br.ufc.lps.splar.plugins.reasoners.bdd.javabdd.ReasoningWithBDD;
import br.ufc.lps.splar.plugins.reasoners.sat.sat4j.ReasoningWithSAT;

public abstract class Model implements IModel{

	private FeatureModel featureModel;
	private ReasoningWithBDD bddReasoner;
	private ReasoningWithSAT satReasoner;
	private FeatureModelStatistics featureModelStatistics;
	
	private int modelID;
	
	public Model(){}
	
	public Model(String pathModelFile, int modelID) {

		this.modelID = modelID;
		
		featureModel = (FeatureModel)ModelFactory.getInstance().createModel(modelID, pathModelFile);
		try {
			featureModel.loadModel();
			featureModelStatistics = new FeatureModelStatistics(featureModel);
			featureModelStatistics.update();
			createBDDReasoner();
			
		} catch (FeatureModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FeatureModel getFeatureModel(){
		return featureModel;
	}

	@Override
	public String getModelName() {
		
		return featureModel.getName();
	}

	private void createBDDReasoner(){
		
		
		try {
			
			// create BDD variable order heuristic
			new FTPreOrderSortedECTraversalHeuristic("Pre-CL-MinSpan", featureModel, FTPreOrderSortedECTraversalHeuristic.FORCE_SORT);		
			VariableOrderingHeuristic heuristic = VariableOrderingHeuristicsManager.createHeuristicsManager().getHeuristic("Pre-CL-MinSpan");
			
			// Creates the BDD reasoner
			bddReasoner = new FMReasoningWithBDD(featureModel, heuristic, 50000, 50000, 60000, "pre-order");
					
			// Initialize the reasoner (BDD is created at this moment)
			
			bddReasoner.init();
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	@Override
	public int nonFunctionCommonality() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int numberOfFeatures() {
		
		return featureModelStatistics.countFeatures();
	}

	@Override
	public int numberOfTopFeatures() {		
		
		return featureModel.getNodesAtLevel(1).size(); 
	}

	@Override
	public int numberOfLeafFeatures() {
		
		return featureModel.getLeaves().size();
	}

	@Override
	public int depthOfTree() {
		
		return featureModelStatistics.depth();
	
	}

	@Override
	public int cognitiveComplexityOfFeatureModel() {
		
		return featureModelStatistics.countGroups11()+featureModelStatistics.countGroups1N();
	}

	@Override
	public int featureExtendibility() {
		
		return numberOfLeafFeatures()+singleCyclicDependentFeatures()+multipleCyclicDependentFeatures();
	}

	@Override
	public double flexibilityOfConfiguration() {
		
		return (double)featureModelStatistics.countOptional() / featureModelStatistics.countFeatures();
	}

	@Override
	public int singleCyclicDependentFeatures() {
		
		return featureModelStatistics.countSingleCyclicDependentFeatures();
	}

	@Override
	public int multipleCyclicDependentFeatures() {
		
		return featureModelStatistics.countMultipleCyclicDependentFeatures();
	}

	@Override
	public int cyclomaticComplexity() {
		
		return featureModelStatistics.countConstraints();
	}

	@Override
	public double compoundComplexity() {
		
		int features = featureModelStatistics.countFeatures();
		int mandatoryFeatures = featureModelStatistics.countMandatory();
		int orFeatures = featureModelStatistics.countGroups1N();
		int xorFeatures = featureModelStatistics.countGroups11();
		int groups = orFeatures + xorFeatures;
		int constraints = featureModelStatistics.countConstraints();
		
		int allRelationship = mandatoryFeatures + orFeatures + xorFeatures + groups + constraints;
		
		double result = Math.pow(features, 2) + (Math.pow(mandatoryFeatures, 2) + 2*Math.pow(orFeatures, 2) + 3*Math.pow(xorFeatures, 2) + 3*Math.pow(groups, 2) + 3*Math.pow(allRelationship, 2))/9;
		
		return result;
	}

	@Override
	public int crossTreeConstraints() {
					
		return featureModelStatistics.countConstraints();
	}

	@Override
	public double coefficientOfConnectivityDensity() {
				
		return featureModelStatistics.getECClauseDensity();
		
	}

	@Override
	public int numberOfVariableFeatures() {
		
		return featureModelStatistics.countGrouped();
	}

	@Override
	public int numberOfVariationPoints() {
		
		return featureModelStatistics.countGroups11()+featureModelStatistics.countGroups1N();
	}

	@Override
	public int singleVariationPointsFeatures() {
		
		return featureModelStatistics.countGrouped11();
		
	}

	@Override
	public int multipleVariationPointsFeatures() {
		
		return featureModelStatistics.countGrouped1n();
	}

	@Override
	public int rigidNotVariationPointsFeatures() {
		
		return featureModelStatistics.countFeatures() - featureModelStatistics.countGrouped();
	}

	@Override
	public double productLineTotalVariability() {
		
		
		return numberOfValidConfigurations()/(Math.pow(2, numberOfFeatures()));
	}

	@Override
	public double numberOfValidConfigurations() {
					
		try {
			return bddReasoner.countValidConfigurations();
		} catch (OperationNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	

	
}
