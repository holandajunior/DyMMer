package br.ufc.lps.model;

import br.ufc.lps.model.xml.ModelID;

public class FamiliarModel extends Model{
	
	public FamiliarModel(String pathModelFile) {
		super(pathModelFile, ModelID.FAMILIAR_MODEL.getId());
	}

}
