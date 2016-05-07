package br.ufc.lps.model;

import br.ufc.lps.splar.core.fm.FeatureModel;

public interface IModel {

	public FeatureModel getFeatureModel();
	public String getModelName();
	
	
	
	int nonFunctionCommonality();
	int numberOfFeatures();
	int numberOfTopFeatures();
	int numberOfLeafFeatures();
	int depthOfTree();
	int cognitiveComplexityOfFeatureModel();
	int featureExtendibility();
	double flexibilityOfConfiguration();
	int singleCyclicDependentFeatures();
	int multipleCyclicDependentFeatures();
	int cyclomaticComplexity();
	double compoundComplexity();
	int crossTreeConstraints();
	double coefficientOfConnectivityDensity();
	int numberOfVariableFeatures();
	int numberOfVariationPoints();
	int singleVariationPointsFeatures();
	int multipleVariationPointsFeatures();
	int rigidNotVariationPointsFeatures();
	double productLineTotalVariability();
	double numberOfValidConfigurations();

}
