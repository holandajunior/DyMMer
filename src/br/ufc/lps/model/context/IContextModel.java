package br.ufc.lps.model.context;

import br.ufc.lps.model.IModel;

public interface IContextModel extends IModel {

		int numberActivatedFeatures();
		int numberDeactivatedFeatures();
		int numberContextConstraints();
		int numberOfContexts();
}
