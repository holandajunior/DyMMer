package br.ufc.lps.model.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.ufc.lps.contextaware.Constraint;
import br.ufc.lps.contextaware.Context;
import br.ufc.lps.contextaware.Resolution;
import br.ufc.lps.model.ModelFactory;
import br.ufc.lps.splar.core.constraints.BooleanVariable;
import br.ufc.lps.splar.core.constraints.CNFFormula;
import br.ufc.lps.splar.core.constraints.PropositionalFormula;
import br.ufc.lps.splar.core.fm.FeatureModel;
import br.ufc.lps.splar.core.fm.FeatureModelException;
import br.ufc.lps.splar.core.fm.FeatureModelStatistics;
import br.ufc.lps.splar.core.fm.FeatureTreeNode;
import br.ufc.lps.splar.core.fm.XMLFeatureModel;
import br.ufc.lps.splar.core.heuristics.FTPreOrderSortedECTraversalHeuristic;
import br.ufc.lps.splar.core.heuristics.VariableOrderingHeuristic;
import br.ufc.lps.splar.core.heuristics.VariableOrderingHeuristicsManager;
import br.ufc.lps.splar.plugins.reasoners.bdd.javabdd.FMReasoningWithBDD;
import br.ufc.lps.splar.plugins.reasoners.bdd.javabdd.ReasoningWithBDD;
import br.ufc.lps.splar.plugins.reasoners.sat.sat4j.ReasoningWithSAT;

public abstract class ContextModel implements IContextModel {

	public static final String DEFAULT_CONTEXT = "default";
	private FeatureModel featureModel;
	private ReasoningWithBDD bddReasoner;
	private ReasoningWithSAT satReasoner;
	private FeatureModelStatistics featureModelStatistics;
	
	private String pathModelFile;
	
	
	private int modelID;
	
    /*
     * Context-Aware
     */
    
    private Map<String, Context> contexts;
    private Map<Context, FeatureModelStatistics> statisticsByContext;
    private Map<Context, ReasoningWithBDD> bddByContext;
	private Context currentContext;
	private String modelName;
	
    
	public ContextModel(){}
	
	public ContextModel(String pathModelFile, int modelID) {
		
				
		this.modelID = modelID;
		
		this.pathModelFile = pathModelFile;
		contexts = new HashMap<String, Context>();
		statisticsByContext = new HashMap<Context, FeatureModelStatistics>();
		bddByContext = new HashMap<Context, ReasoningWithBDD>();
		
		parseXMLToGetContexts();
		createFeatureModelByContext();
		
		
		
		
			
	}
	
	public FeatureModel setFeatureModel(Context context){
		this.featureModel = context.getFeatureModel();
		this.currentContext = context;		
		
		if(!statisticsByContext.containsKey(context)){
			
			FeatureModelStatistics stats = new FeatureModelStatistics(featureModel);
			stats.update();
			ReasoningWithBDD reasoner = createBDDReasoner(context);
			
			statisticsByContext.put(context, stats);
			bddByContext.put(context, reasoner);
		}
		
			
			
		featureModelStatistics = statisticsByContext.get(context);
		bddReasoner = bddByContext.get(context);
		
		return context.getFeatureModel();
	}
	
	public String getModelName(){
		return modelName;
	}
	
	public Map<String, Context> getContexts(){
		return contexts;
	}
	
	public void setContexts(Map<String, Context> contexts) {
		this.contexts = contexts;
	}

	//Parsea o XML para criar os contextos
	private void parseXMLToGetContexts(){
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			
			db = dbf.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			
			Document doc = db.parse(pathModelFile);
			Element rootEle = doc.getDocumentElement();
			
			//parse contexts
			NodeList contexts = rootEle.getElementsByTagName("context");
			for(int i=0; i < contexts.getLength(); i++){
				Element elContext = (Element) contexts.item(i);
				String nameContext = elContext.getAttribute("name");
				List<Resolution> resolutions = new ArrayList<Resolution>();
				List<Constraint> constraintsContext = new ArrayList<Constraint>();
				
				NodeList elsResolutions = elContext.getElementsByTagName("resolution");
				for(int countRes = 0; countRes < elsResolutions.getLength(); countRes++){
					Element elResolution = (Element) elsResolutions.item(countRes);
					String nameFeature = elResolution.getAttribute("feature");
					String idFeature = elResolution.getAttribute("id");
					boolean statusFeature = elResolution.getAttribute("status").equals("false") ? false : true;
					
					Resolution resolution = new Resolution(idFeature, nameFeature, statusFeature);
					resolutions.add(resolution);
				}
				
				
				NodeList elsConstraints = elContext.getElementsByTagName("constraint");
				for(int countCons = 0; countCons < elsConstraints.getLength(); countCons++){
					Element elConstraint = (Element) elsConstraints.item(countCons);
					String stringConstraint = elConstraint.getTextContent();
					
					
					Constraint newConstraint = new Constraint(countCons, stringConstraint);
					constraintsContext.add(newConstraint);
				}
				
				
				Context context = new Context(nameContext, resolutions, constraintsContext);
				this.contexts.put(nameContext, context);
				
			}
			
			this.contexts.put(DEFAULT_CONTEXT, new Context(DEFAULT_CONTEXT, new ArrayList<Resolution>(), new ArrayList<Constraint>()));
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	//Cria um modelo de feature para cada contexto, setando as features ativas e desativadas;
	private void createFeatureModelByContext(){
		
				
		for(Map.Entry<String, Context> conts : contexts.entrySet()){
			FeatureModel otherModel = (FeatureModel)ModelFactory.getInstance().createModel(modelID, pathModelFile);
			try {
			
				otherModel.loadModel();
				modelName = otherModel.getName();
			} catch (FeatureModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Context context = conts.getValue();
			Map<String, Boolean> status = new HashMap<String, Boolean>();
			for(Resolution resolution : context.getResolutions()){
				status.put(resolution.getIdFeature(), resolution.getStatus());
			}
			
			for(Map.Entry<String, FeatureTreeNode> node : otherModel.getNodesMap().entrySet()){
				
				String idNode = node.getKey();
				if(status.containsKey(idNode) && status.get(idNode) == false)
					node.getValue().setActiveInContext(false);
				
			}
			
			context.setFeatureModel(otherModel);
			
		}
		
		
		
	}
	
	private ReasoningWithBDD createBDDReasoner(Context context){
		
		FeatureModel otherModel = createFeatureModel(context);
		try {
			
			// create BDD variable order heuristic
			new FTPreOrderSortedECTraversalHeuristic("Pre-CL-MinSpan", otherModel, FTPreOrderSortedECTraversalHeuristic.FORCE_SORT);		
			VariableOrderingHeuristic heuristic = VariableOrderingHeuristicsManager.createHeuristicsManager().getHeuristic("Pre-CL-MinSpan");
			
			// Creates the BDD reasoner
			ReasoningWithBDD reasoner = new FMReasoningWithBDD(otherModel, heuristic, 50000, 50000, 60000, "pre-order");
					
			// Initialize the reasoner (BDD is created at this moment)
			
			reasoner.init();
			
			return reasoner;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return null;
	}
	
	/*
	 * Cria um novo feature model e retira os n�s e restri��es que n�o compoem o contexto para repassar na cria��o do BDD
	 */
	private FeatureModel createFeatureModel(Context context){
		
		//Cria um novo modelo
		FeatureModel otherModel = (FeatureModel)ModelFactory.getInstance().createModel(modelID, pathModelFile);
		
		try {
			otherModel.loadModel();
		} catch (FeatureModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Configura os n�s que n�o far�o parte do modelo no devido contexto
		Map<String, Boolean> status = new HashMap<String, Boolean>();
		for(Resolution resolution : context.getResolutions()){
			status.put(resolution.getIdFeature(), resolution.getStatus());
		}
		
		for(Map.Entry<String, FeatureTreeNode> node : otherModel.getNodesMap().entrySet()){
			
			String idNode = node.getKey();
			if(status.containsKey(idNode) && status.get(idNode) == false)
				node.getValue().setActiveInContext(false);
			
		}
		
		//Busca os n�s que ser�o deletados conforme sejam desativados pelo modelo
		List<FeatureTreeNode> nodesToDelete = new ArrayList<FeatureTreeNode>();
		
		for(FeatureTreeNode node : otherModel.getNodes()){
			if(!node.isActiveInContext()){
				if(!nodesToDelete.contains(node)){
					nodesToDelete.add(node);
				
					otherModel.getSubtreeNodes(node, nodesToDelete);
				}
			}
		}
		
		
		//Busca as constraints que possuem n�s que n�o fazem parte do modelo
		List<PropositionalFormula> constraintsToDelete = new ArrayList<PropositionalFormula>();
		
		for(PropositionalFormula constraint : otherModel.getConstraints()){
			for(BooleanVariable variable : constraint.getVariables()){
				FeatureTreeNode auxNode = new FeatureTreeNode(variable.getID(), null, null);
				if(nodesToDelete.contains(auxNode)){
					constraintsToDelete.add(constraint);
					break;
				}
			}
		}
		
		
		//Deleta os n�s da arvore
		for(FeatureTreeNode node : nodesToDelete){
			otherModel.removeNodeFromParent(node);
			
		}
		
		
		//Deleta todos os n�s e constraints registrados
		otherModel.getNodes().removeAll(nodesToDelete);
		otherModel.getConstraints().removeAll(constraintsToDelete);
		
		return otherModel;
		
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
		
		return countActiveFeatures(featureModel.getNodesAtLevel(1)); 
	}

	@Override
	public int numberOfLeafFeatures() {
		
		return countActiveFeatures(featureModel.getLeaves());
	}

	@Override
	public int depthOfTree() {
		
		int depth = featureModel.depth();
		
		boolean lastActiveDepth = false;
		for(FeatureTreeNode node : featureModel.getNodesAtLevel(depth)){
				
			if(FeatureTreeNode.isActiveHierarchy(node)){
				lastActiveDepth = true;
				break;
			}	
		}
			
		if(lastActiveDepth)
			return depth;
		else
			return depth-1;
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
				
		if ( featureModel.countConstraints() == 0 )
			return 0;
		
		CNFFormula cnf = new CNFFormula();
		boolean canAdd = true;
		
		for( PropositionalFormula pf : featureModel.getConstraints() ) {
			for(Resolution resolution : currentContext.getResolutions()){
				if(pf.getVariable(resolution.getIdFeature()) != null && !resolution.getStatus())
					canAdd = false;
			}
				
			if(canAdd)
				cnf.addClauses(pf.toCNFClauses());
		}
		
		return (cnf == null) ? 0 : cnf.getClauseDensity();
		
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
	
	private int countActiveFeatures(Collection<FeatureTreeNode> features){
		int count = 0;
		for(FeatureTreeNode feature : features)
			if(FeatureTreeNode.isActiveHierarchy(feature))
				count++;
		
		return count;
	}
	
	public int numberActivatedFeatures(){
		
		int count = 0;
		
		for(Resolution resolution : currentContext.getResolutions()){
			if(resolution.getStatus())
				count++;
		}
		
		return count;
		
	}
	
	public int numberDeactivatedFeatures(){
		int count = 0;
		
		for(Resolution resolution : currentContext.getResolutions()){
			if(!resolution.getStatus())
				count++;
		}
		
		return count;
	}
	
	public int numberContextConstraints(){
		
		return currentContext.getConstraints().size();
	}
	
	//-1 devido ao contexto Default
	public int numberOfContexts(){
		return contexts.size()-1;
	}
	

	@Override
	public FeatureModel getFeatureModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
