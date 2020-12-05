package inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import checkexistence.EChecker;
import checkfrequency.FChecker;
import concepts.AtomicConcept;
import concepts.BottomConcept;
import concepts.TopConcept;
import roles.AtomicRole;
import roles.BottomRole;
import roles.TopRole;
import simplification.Simplifier;
import connectives.And;
import connectives.Exists;
import connectives.Forall;
import connectives.Geq;
import connectives.Leq;
import connectives.Negation;
import connectives.Or;
import convertion.BackConverter;
import convertion.Converter;
import formula.Formula;
import individual.Individual;

public class Inferencer {
	
	public Inferencer() {

	}

	public List<List<Formula>> getCombinations(List<Formula> input_list) {

		List<List<Formula>> output_list = new ArrayList<>();

		int nCnt = input_list.size();

		int nBit = (0xFFFFFFFF >>> (32 - nCnt));

		for (int i = 1; i <= nBit; i++) {
			output_list.add(new ArrayList<>());
			for (int j = 0; j < nCnt; j++) {
				if ((i << (31 - j)) >> 31 == -1) {
					output_list.get(i - 1).add(input_list.get(j));
				}
			}
		}

		return output_list;
	}
	
	
	public List<Formula> combination_A(AtomicConcept concept, List<Formula> formula_list)
			throws CloneNotSupportedException {
		
		//System.out.println("combine formula_list = " + formula_list);

		List<Formula> output_list = new ArrayList<>();
				
		// C or A
		List<Formula> positive_star_premises = new ArrayList<>();
		// C or >m r.(A or k1)
		List<Formula> positive_Geq_premises = new ArrayList<>();
		// C or <m r.(~A or k3)
		List<Formula> positive_Leq_premises = new ArrayList<>();
		// C or ~A
		List<Formula> negative_star_premises = new ArrayList<>();
		// C or >m r.(~A or k2)
		List<Formula> negative_Geq_premises = new ArrayList<>();
		// C or <m r.(A or k4)
		List<Formula> negative_Leq_premises = new ArrayList<>();


		EChecker ec = new EChecker();
		Simplifier simp = new Simplifier();

		for (Formula formula : formula_list) {
			//If concept is not present in formula, then formula is directly put into the output_list. 
			if (!ec.isPresent(concept, formula)) {
				output_list.add(formula);
            //if formula has the form C or A, where C is the bottom concept (false)
			} else if (formula.equals(concept)) {
				positive_star_premises.add(formula);
			//if formula has the form C or ~A, where C is the bottom concept (false)
			} else if (formula.equals(new Negation(concept))) {
				negative_star_premises.add(formula);
			
			} else if (formula instanceof Geq && formula.getSubFormulas().get(1).equals(concept)) {
				positive_Geq_premises.add(formula);

			} else if (formula instanceof Geq && formula.getSubFormulas().get(1) instanceof Or
					&& formula.getSubFormulas().get(1).getSubFormulas().contains(concept)) {
				positive_Geq_premises.add(formula);
				
			} else if (formula instanceof Leq && formula.getSubFormulas().get(1).equals(new Negation(concept))) {
				positive_Leq_premises.add(formula);

			} else if (formula instanceof Leq && formula.getSubFormulas().get(1) instanceof Or 
					&& formula.getSubFormulas().get(1).getSubFormulas().contains(new Negation(concept))) {
				positive_Leq_premises.add(formula);
				
			} else if (formula instanceof Geq && formula.getSubFormulas().get(1).equals(new Negation(concept))) {
				negative_Geq_premises.add(formula);

			} else if (formula instanceof Geq && formula.getSubFormulas().get(1) instanceof Or
					&& formula.getSubFormulas().get(1).getSubFormulas().contains(new Negation(concept))) {
				negative_Geq_premises.add(formula);
				
			} else if (formula instanceof Leq && formula.getSubFormulas().get(1).equals(concept)) {
				negative_Leq_premises.add(formula);

			} else if (formula instanceof Leq && formula.getSubFormulas().get(1) instanceof Or
					&& formula.getSubFormulas().get(1).getSubFormulas().contains(concept)) {
				negative_Leq_premises.add(formula);
				
			} else if (formula instanceof Or) {
				
				List<Formula> disjunct_list = formula.getSubFormulas();

				if (disjunct_list.contains(concept)) {
					positive_star_premises.add(formula);

				} else if (disjunct_list.contains(new Negation(concept))) {
					negative_star_premises.add(formula);
			
				} else {
					for (Formula disjunct : disjunct_list) {
						if (disjunct instanceof Geq && disjunct.getSubFormulas().get(1).equals(concept)) {
							positive_Geq_premises.add(formula);
							break;

						} else if (disjunct instanceof Geq && disjunct.getSubFormulas().get(1) instanceof Or
								&& disjunct.getSubFormulas().get(1).getSubFormulas().contains(concept)) {
							positive_Geq_premises.add(formula);
							break;
							
						} else if (disjunct instanceof Leq && disjunct.getSubFormulas().get(1).equals(new Negation(concept))) {
							positive_Leq_premises.add(formula);
							break;

						} else if (disjunct instanceof Leq && disjunct.getSubFormulas().get(1) instanceof Or 
								&& disjunct.getSubFormulas().get(1).getSubFormulas().contains(new Negation(concept))) {
							positive_Leq_premises.add(formula);
							break;
							
						} else if (disjunct instanceof Geq && disjunct.getSubFormulas().get(1).equals(new Negation(concept))) {
							negative_Geq_premises.add(formula);
							break;

						} else if (disjunct instanceof Geq && disjunct.getSubFormulas().get(1) instanceof Or
								&& disjunct.getSubFormulas().get(1).getSubFormulas().contains(new Negation(concept))) {
							negative_Geq_premises.add(formula);
							break;
							
						} else if (disjunct instanceof Leq && disjunct.getSubFormulas().get(1).equals(concept)) {
							negative_Leq_premises.add(formula);
							break;

						} else if (disjunct instanceof Leq && disjunct.getSubFormulas().get(1) instanceof Or
								&& disjunct.getSubFormulas().get(1).getSubFormulas().contains(concept)) {
							negative_Leq_premises.add(formula);
							break;
							
						}
					}
				}

			} else {
				output_list.add(formula);
			}
		}
		//System.out.println("=====================================================");
		//System.out.println("positive_star_premises = " + positive_star_premises);
		//System.out.println("positive_Geq_premises = " + positive_Geq_premises);
		//System.out.println("positive_Leq_premises = " + positive_Leq_premises);
		//System.out.println("negative_star_premises = " + negative_star_premises);
		//System.out.println("negative_Geq_premises = " + negative_Geq_premises);
		//System.out.println("negative_Leq_premises = " + negative_Leq_premises);
		
		//
		//[6]
		if (!negative_star_premises.isEmpty()) {  // 1,4,7
			
			if (negative_star_premises.contains(new Negation(concept))) {
				
				if (!positive_star_premises.isEmpty()) {
					for (Formula ps_premise : positive_star_premises) {
						output_list.add(AckermannReplace(concept, ps_premise, BottomConcept.getInstance()));
					}
				}
				if (!positive_Geq_premises.isEmpty()) {
					for (Formula pgp_premise : positive_Geq_premises) {
						output_list.add(AckermannReplace(concept, pgp_premise, BottomConcept.getInstance()));
					}
				}
				if (!positive_Leq_premises.isEmpty()) {
					for (Formula plp_premise : positive_Leq_premises) {
						output_list.add(AckermannReplace(concept, plp_premise, BottomConcept.getInstance()));
					}
				}
				
				
			} else {
				
				List<Formula> and_list = new ArrayList<>();
				List<Formula> or_list = new ArrayList<>();
				
				for (Formula ns_premise : negative_star_premises) {
					
					Formula ns_def = null;
					Formula ps_def = null;
					
					List<Formula> def_disjunct_list = new ArrayList<>(ns_premise.getSubFormulas());
					//remove ~A from (D or ~A), leaving D alone
					def_disjunct_list.remove(new Negation(concept));
					//if D is not a disjunction
					if (def_disjunct_list.size() == 1) {
						ns_def = def_disjunct_list.get(0);
				    //if D is a disjunction
					} else {
						ns_def = new Or(def_disjunct_list);
					}
					
					if (def_disjunct_list.size() == 1) {
						if (def_disjunct_list.get(0) instanceof Negation) {
							ps_def = def_disjunct_list.get(0).getSubFormulas().get(0);
						} else {
						    ps_def = new Negation(def_disjunct_list.get(0));
						}
					} else {
						ps_def = new Negation(new Or(def_disjunct_list));
					}

					and_list.add(ns_def);
					or_list.add(ps_def);
					
					if (!positive_star_premises.isEmpty()) {
						for (Formula ps_premise : positive_star_premises) {
							output_list.add(AckermannReplace(concept, ps_premise, ns_def));
						}
					}
					
				}
				
				Formula ns_def_and = null;
				Formula ns_def_or = null;
				
				if (and_list.size() == 1) {
				    ns_def_and = and_list.get(0);
				} else {
					ns_def_and = new And(and_list);
				}
				
				if (or_list.size() == 1) {
					ns_def_or = or_list.get(0);
				} else {
					ns_def_or = new Or(or_list);
				}
				
				if (!positive_Geq_premises.isEmpty()) {
									
					for (Formula pe_premise : positive_Geq_premises) {
						output_list.add(AckermannReplace(concept, pe_premise, ns_def_and));
					}
				}	
				if (!positive_Leq_premises.isEmpty()) {
					
					for (Formula ped_premise : positive_Leq_premises) {
						output_list.add(AckermannReplace(concept, ped_premise, new Negation(ns_def_or)));
					}
				}	
			}
		}

		//[1]
		if (!positive_star_premises.isEmpty()) {
						
			if (positive_star_premises.contains(concept)) {
								
				if (!negative_Geq_premises.isEmpty()) {
					for (Formula ne_premise : negative_Geq_premises) {
						output_list.add(AckermannReplace(concept, ne_premise, TopConcept.getInstance()));
					}
				}
				if (!negative_Leq_premises.isEmpty()) {
					for (Formula ned_premise : negative_Leq_premises) {
						output_list.add(AckermannReplace(concept, ned_premise, TopConcept.getInstance()));
					}
				}
				
				
			} else {
				
				List<Formula> and_list = new ArrayList<>();
				List<Formula> or_list = new ArrayList<>();
				
				for (Formula ps_premise : positive_star_premises) {
					
					Formula ps_def = null;
					Formula ns_def = null;
					
					List<Formula> def_disjunct_list = new ArrayList<>(ps_premise.getSubFormulas());
					//remove A from C or A, leaving C alone
					def_disjunct_list.remove(concept);
					
					if (def_disjunct_list.size() == 1) {
						ns_def = def_disjunct_list.get(0);
				    
					} else {
						ns_def = new Or(def_disjunct_list);
					}
					
					if (def_disjunct_list.size() == 1) {
						if (def_disjunct_list.get(0) instanceof Negation) {
							ps_def = def_disjunct_list.get(0).getSubFormulas().get(0);
						} else {
						    ps_def = new Negation(def_disjunct_list.get(0));
						}
					} else {
						ps_def = new Negation(new Or(def_disjunct_list));
					}
					
					or_list.add(ps_def);
					and_list.add(ns_def);
				}
				
				Formula ps_def_or = null;
				Formula ps_def_and = null;
				
				if (and_list.size() == 1) {
				    ps_def_and = and_list.get(0);
				} else {
					ps_def_and = new And(and_list);
				}
				
				if (or_list.size() == 1) {
				    ps_def_or = or_list.get(0);
				} else {
					ps_def_or = new Or(or_list);
				}	
				
				if (!negative_Geq_premises.isEmpty()) {			
					for (Formula ne_premise : negative_Geq_premises) {
						output_list.add(AckermannReplace(concept, ne_premise, new Negation(ps_def_and)));
					}
				}
				if (!negative_Leq_premises.isEmpty()) {			
					for (Formula ned_premise : negative_Leq_premises) {
						output_list.add(AckermannReplace(concept, ned_premise, ps_def_or));
					}
				}
				
			}
		}
		
		if (!negative_Geq_premises.isEmpty()) {
			
			if(!positive_Geq_premises.isEmpty()) {
				
				if(negative_star_premises.isEmpty() || positive_star_premises.isEmpty()) {
					for (Formula pgp_premise : positive_Geq_premises) {
						List<Formula> pgp_frac_list = new ArrayList<>();
						Geq pgp_geq = null;
						
						if (pgp_premise instanceof Geq) {
							pgp_frac_list.add(BottomConcept.getInstance());
							pgp_geq = (Geq) pgp_premise.clone();
						} else {
							List<Formula> pgp_disjunct_list = new ArrayList<>(pgp_premise.getSubFormulas());
							for (Formula pgp_disjunct : pgp_disjunct_list) {
								if (ec.isPresent(concept, pgp_disjunct)) {
									pgp_geq = (Geq) pgp_disjunct.clone();
									
								} else {
									pgp_frac_list.add(pgp_disjunct);
								}
							}
						}
						
						
						for (Formula ngp_premise : negative_Geq_premises) {
							List<Formula> ngp_frac_list = new ArrayList<>();
							Geq ngp_geq = null;
							
							if (ngp_premise instanceof Geq) {
								ngp_frac_list.add(BottomConcept.getInstance());
								ngp_geq = (Geq) ngp_premise.clone();
							} else {
								List<Formula> ngp_disjunct_list = new ArrayList<>(ngp_premise.getSubFormulas());
								for (Formula ngp_disjunct : ngp_disjunct_list) {
									if (ec.isPresent(concept, ngp_disjunct)) {
										ngp_geq = (Geq) ngp_disjunct.clone();
										
									} else {
										ngp_frac_list.add(ngp_disjunct);
									}
									
								}
							}
							
							
							//****************************************
							//if (ngp_geq.getSubFormulas().get(0).equals(pgp_geq.getSubFormulas().get(0))) {
							OWLAxiom oa1 = getRoleSubAxiom(ngp_geq.getSubFormulas().get(0), 
									pgp_geq.getSubFormulas().get(0));
							OWLAxiom oa2 = getRoleSubAxiom(pgp_geq.getSubFormulas().get(0), 
									ngp_geq.getSubFormulas().get(0));
							Boolean combine_flag = false;
							Formula com_r = null;
							if(Converter.reasoner.isEntailed(oa1)) {
								combine_flag = true;
								com_r = pgp_geq.getSubFormulas().get(0);
							} else if (Converter.reasoner.isEntailed(oa2)) {
								combine_flag = true;
								com_r = ngp_geq.getSubFormulas().get(0);
							}
							
							
							if (combine_flag) {
								Integer n1 = Math.max(ngp_geq.get_num(),pgp_geq.get_num());
								Integer n2 = Math.min(ngp_geq.get_num(), pgp_geq.get_num());
								List<Formula> or_list = new ArrayList<>();
								Formula k12 = null;
								if (ngp_geq.getSubFormulas().get(1) instanceof Or) {
									or_list.addAll(ngp_geq.getSubFormulas().get(1).getSubFormulas());
									or_list.remove(new Negation(concept));
								}
								if (pgp_geq.getSubFormulas().get(1) instanceof Or) {
									or_list.addAll(pgp_geq.getSubFormulas().get(1).getSubFormulas());
									or_list.remove(concept);
								}
								if (or_list.isEmpty()) {
									k12 = BottomConcept.getInstance();
								} else if (or_list.size() == 1) {
									k12 = or_list.get(0);
								} else {
									k12 = new Or(or_list);
								}
								
								
								for(int i = 1;i <= n2;i++) {
									List<Formula> res_list = new ArrayList<>();
									res_list.addAll(pgp_frac_list);
									res_list.addAll(ngp_frac_list);
									res_list.add(new Geq(n1+i,com_r,TopConcept.getInstance()));
									res_list.add(new Geq(n2+1-i,com_r,k12));
									output_list.add(new Or(res_list));
								}
							}
						}
					}
				}
				
				if (negative_star_premises.isEmpty()) {
					for(Formula pgp_premise : positive_Geq_premises) {
						output_list.add(AckermannReplace(concept,pgp_premise,TopConcept.getInstance()));
					}
				}
				
				if (positive_star_premises.isEmpty()) {
					for(Formula ngp_premise : negative_Geq_premises) {
						output_list.add(AckermannReplace(concept,ngp_premise,BottomConcept.getInstance()));
						
					}
				}
			}
			
			if(!positive_Leq_premises.isEmpty()) {
				
				if(positive_star_premises.isEmpty() || negative_star_premises.isEmpty()) {
					
					for(Formula ngp_premise : negative_Geq_premises) {
						for(Formula plp_premise : positive_Leq_premises) {
							List<Formula> ngp_frac_list = new ArrayList<>();
							Geq ngp_geq = null;
							
							if (ngp_premise instanceof Geq) {
								ngp_frac_list.add(BottomConcept.getInstance());
								ngp_geq = (Geq) ngp_premise.clone();
							} else {
								List<Formula> ngp_disjunct_list = new ArrayList<>(ngp_premise.getSubFormulas());
								for (Formula ngp_disjunct : ngp_disjunct_list) {
									if (ec.isPresent(concept, ngp_disjunct)) {
										ngp_geq = (Geq) ngp_disjunct.clone();
										
									} else {
										ngp_frac_list.add(ngp_disjunct);
									}
									
								}
							}
							
							List<Formula> plp_frac_list = new ArrayList<>();
							Leq plp_leq = null;
							
							if (plp_premise instanceof Leq) {
								plp_frac_list.add(BottomConcept.getInstance());
								plp_leq = (Leq) plp_premise.clone();
							} else {
								List<Formula> plp_disjunct_list = new ArrayList<>(plp_premise.getSubFormulas());
								for (Formula plp_disjunct : plp_disjunct_list) {
									if (ec.isPresent(concept, plp_disjunct)) {
										plp_leq = (Leq) plp_disjunct.clone();
										
									} else {
										plp_frac_list.add(plp_disjunct);
									}
									
								}
							}
							
							//****************************************
							//if (plp_leq.getSubFormulas().get(0).equals(ngp_geq.getSubFormulas().get(0)) &&
							//		ngp_geq.get_num() >= plp_leq.get_num()) {
							
							
							OWLAxiom oa1 = getRoleSubAxiom(ngp_geq.getSubFormulas().get(0), 
									plp_leq.getSubFormulas().get(0));
							Boolean combine_flag = false;
							Formula com_r = null;
							if(Converter.reasoner.isEntailed(oa1)) {
								combine_flag = true;
								com_r = ngp_geq.getSubFormulas().get(0);
							} 
							
							if (combine_flag&&(ngp_geq.get_num() >= plp_leq.get_num())){
								List<Formula> and_list = new ArrayList<>();
								List<Formula> tmp_list = new ArrayList<>();
								List<Formula> res_list = new ArrayList<>();
 								Formula k23 = null;
								Formula k2 = null;
								Formula k3 = null;
								if (plp_leq.getSubFormulas().get(1).equals(new Negation(concept))) {
									k3 = BottomConcept.getInstance();
								} else {
									tmp_list = plp_leq.getSubFormulas().get(1).getSubFormulas();
									tmp_list.remove(new Negation(concept));
									if (tmp_list.size() == 1) {
										k3 = tmp_list.get(0);
									} else {
										k3 = new Or(tmp_list);
									}
								}
								
								if (ngp_geq.getSubFormulas().get(1).equals(new Negation(concept))) {
									k2 = BottomConcept.getInstance();
								} else {
									tmp_list = ngp_geq.getSubFormulas().get(1).getSubFormulas();
									tmp_list.remove(new Negation(concept));
									if (tmp_list.size() == 1) {
										k2 = tmp_list.get(0);
									} else {
										k2 = new Or(tmp_list);
									}
								}
								and_list.add(k2);
								and_list.add(new Negation(k3));
								res_list.addAll(plp_frac_list);
								res_list.addAll(ngp_frac_list);
								res_list.add(new Geq(ngp_geq.get_num()-plp_leq.get_num(),com_r,new And(and_list)));
								output_list.add(new Or(res_list));
							}
							
							
						}
					}
				}
				
				if (positive_star_premises.isEmpty() && positive_Geq_premises.isEmpty()) {
					for(Formula ngp_premise : negative_Geq_premises) {
						output_list.add(AckermannReplace(concept,ngp_premise,BottomConcept.getInstance()));
						
					}
				}
				
				if (negative_star_premises.isEmpty()) {
					for(Formula plp_premise : positive_Leq_premises) {
						output_list.add(AckermannReplace(concept,plp_premise,TopConcept.getInstance()));
					}
				}
				
			}
		}
		
		if (!positive_Geq_premises.isEmpty()) {
			
			if (!negative_Leq_premises.isEmpty()) {
				if (positive_star_premises.isEmpty() || (negative_star_premises.isEmpty() && negative_Geq_premises.isEmpty())) {
					for(Formula nlp_premise : negative_Leq_premises) {
						for(Formula pgp_premise : positive_Geq_premises) {
							List<Formula> pgp_frac_list = new ArrayList<>();
							Geq pgp_geq = null;
							
							if (pgp_premise instanceof Geq) {
								pgp_frac_list.add(BottomConcept.getInstance());
								pgp_geq = (Geq) pgp_premise.clone();
							} else {
								List<Formula> pgp_disjunct_list = new ArrayList<>(pgp_premise.getSubFormulas());
								for (Formula pgp_disjunct : pgp_disjunct_list) {

									if (ec.isPresent(concept, pgp_disjunct)) {
										pgp_geq = (Geq) pgp_disjunct.clone();
										
									} else {
										pgp_frac_list.add(pgp_disjunct);
									}
									
								}
							}
							
							List<Formula> nlp_frac_list = new ArrayList<>();
							Leq nlp_leq = null;
							
							if (nlp_premise instanceof Leq) {
								nlp_frac_list.add(BottomConcept.getInstance());
								nlp_leq = (Leq) nlp_premise.clone();
							} else {
								List<Formula> nlp_disjunct_list = new ArrayList<>(nlp_premise.getSubFormulas());
								for (Formula nlp_disjunct : nlp_disjunct_list) {
									if (ec.isPresent(concept, nlp_disjunct)) {
										nlp_leq = (Leq) nlp_disjunct.clone();
										
									} else {
										nlp_frac_list.add(nlp_disjunct);
									}
									
								}
							}
							
							//******************************
							//if (nlp_leq.getSubFormulas().get(0).equals(pgp_geq.getSubFormulas().get(0)) &&
							//		pgp_geq.get_num() >= nlp_leq.get_num()) {
							

							OWLAxiom oa1 = getRoleSubAxiom(pgp_geq.getSubFormulas().get(0), 
									nlp_leq.getSubFormulas().get(0));
							Boolean combine_flag = false;
							Formula com_r = null;
							if(Converter.reasoner.isEntailed(oa1)) {
								combine_flag = true;
								com_r = pgp_geq.getSubFormulas().get(0);
							}
							

							if ((combine_flag) && (pgp_geq.get_num() >= nlp_leq.get_num())){
								List<Formula> and_list = new ArrayList<>();
								List<Formula> tmp_list = new ArrayList<>();
								List<Formula> res_list = new ArrayList<>();
 								Formula k14 = null;
								Formula k1 = null;
								Formula k4 = null;
								if (nlp_leq.getSubFormulas().get(1).equals(concept)) {
									k4 = BottomConcept.getInstance();
								} else {
									tmp_list = nlp_leq.getSubFormulas().get(1).getSubFormulas();
									tmp_list.remove(concept);
									if (tmp_list.size() == 1) {
										k4 = tmp_list.get(0);
									} else {
										k4 = new Or(tmp_list);
									}
								}
								
								if (pgp_geq.getSubFormulas().get(1).equals(concept)) {
									k1 = BottomConcept.getInstance();
								} else {
									tmp_list = pgp_geq.getSubFormulas().get(1).getSubFormulas();
									tmp_list.remove(concept);
									if (tmp_list.size() == 1) {
										k1 = tmp_list.get(0);
									} else {
										k1 = new Or(tmp_list);
									}
								}
								and_list.add(k1);
								and_list.add(new Negation(k4));
								res_list.addAll(nlp_frac_list);
								res_list.addAll(pgp_frac_list);
								res_list.add(new Geq(pgp_geq.get_num()-nlp_leq.get_num(),com_r,new And(and_list)));
								output_list.add(new Or(res_list));
							}
						}
					}
				}
				
				if (negative_star_premises.isEmpty() && negative_Geq_premises.isEmpty()) {
					for(Formula pgp_premise : positive_Geq_premises) {
						output_list.add(AckermannReplace(concept,pgp_premise,TopConcept.getInstance()));
					}
				}
				
				if (positive_star_premises.isEmpty()) {
					for(Formula nlp_premise : negative_Leq_premises) {
						output_list.add(AckermannReplace(concept,nlp_premise,BottomConcept.getInstance()));
					}
				}
			}
			
		}
		
		if (!negative_Leq_premises.isEmpty()) {
			if (!positive_Leq_premises.isEmpty()) {
				if((positive_star_premises.isEmpty() && positive_Geq_premises.isEmpty()) ||
						(negative_star_premises.isEmpty() && negative_Geq_premises.isEmpty())) {
					for(Formula plp_premise : positive_Leq_premises) {
						for(Formula nlp_premise : negative_Leq_premises) {
							List<Formula> nlp_frac_list = new ArrayList<>();
							Leq nlp_leq = null;
							
							if (nlp_premise instanceof Leq) {
								nlp_frac_list.add(BottomConcept.getInstance());
								nlp_leq = (Leq) nlp_premise.clone();
							} else {
								List<Formula> nlp_disjunct_list = new ArrayList<>(nlp_premise.getSubFormulas());
								for (Formula nlp_disjunct : nlp_disjunct_list) {
									if (ec.isPresent(concept, nlp_disjunct)) {
										nlp_leq = (Leq) nlp_disjunct.clone();
										
									} else {
										nlp_frac_list.add(nlp_disjunct);
									}
									
								}
							}
							
							List<Formula> plp_frac_list = new ArrayList<>();
							Leq plp_leq = null;
							
							if (plp_premise instanceof Leq) {
								plp_frac_list.add(BottomConcept.getInstance());
								plp_leq = (Leq) plp_premise.clone();
							} else {
								List<Formula> plp_disjunct_list = new ArrayList<>(plp_premise.getSubFormulas());
								for (Formula plp_disjunct : plp_disjunct_list) {
									if (ec.isPresent(concept, plp_disjunct)) {
										plp_leq = (Leq) plp_disjunct.clone();
										
									} else {
										plp_frac_list.add(plp_disjunct);
									}
									
								}
							}
							
							//if (plp_leq.getSubFormulas().get(0).equals(nlp_leq.getSubFormulas().get(0))) {
							OWLAxiom oa1 = getRoleSubAxiom(plp_leq.getSubFormulas().get(0), 
									nlp_leq.getSubFormulas().get(0));
							OWLAxiom oa2 = getRoleSubAxiom(nlp_leq.getSubFormulas().get(0), 
									plp_leq.getSubFormulas().get(0));
							Boolean combine_flag = false;
							Formula com_r = null;
							if(Converter.reasoner.isEntailed(oa1)) {
								combine_flag = true;
								com_r = nlp_leq.getSubFormulas().get(0);
							} else if (Converter.reasoner.isEntailed(oa2)) {
								combine_flag = true;
								com_r = plp_leq.getSubFormulas().get(0);
							}
							
							if (combine_flag) {
								List<Formula> res_list = new ArrayList<>();
								res_list.addAll(plp_frac_list);
								res_list.addAll(nlp_frac_list);
								res_list.add(new Leq(plp_leq.get_num()+nlp_leq.get_num(),com_r,TopConcept.getInstance()));
								output_list.add(new Or(res_list));
							}
							
						}
					}
				}
				
				if (positive_star_premises.isEmpty() && positive_Geq_premises.isEmpty()) {
					for(Formula nlp_premise : negative_Leq_premises) {
						output_list.add(AckermannReplace(concept,nlp_premise,BottomConcept.getInstance()));
					}
				}
				
				if (negative_star_premises.isEmpty() && negative_Geq_premises.isEmpty()) {
					for(Formula plp_premise : positive_Leq_premises) {
						output_list.add(AckermannReplace(concept,plp_premise,TopConcept.getInstance()));
					}
				}
			}
		}


		return output_list;
	}
	

	
		
	/*public List<Formula> Ackermann_R(AtomicRole role, List<Formula> formula_list)
			throws CloneNotSupportedException {

		List<Formula> output_list = new ArrayList<>();

		List<Formula> positive_TBox_premises = new ArrayList<>();
		List<Formula> negative_TBox_premises = new ArrayList<>();

		EChecker ec = new EChecker();

		for (Formula formula : formula_list) {
			if (!ec.isPresent(role, formula)) {
				output_list.add(formula);

			} else if (formula instanceof Exists) {
				positive_TBox_premises.add(formula);

			} else if (formula instanceof Forall) {
				negative_TBox_premises.add(formula);

			} else if (formula instanceof Or) {
				List<Formula> disjunct_list = formula.getSubFormulas();

				for (Formula disjunct : disjunct_list) {
					if (disjunct instanceof Exists && disjunct.getSubFormulas().get(0).equals(role)) {
						positive_TBox_premises.add(formula);
						break;
					} else if (disjunct instanceof Forall && disjunct.getSubFormulas().get(0).equals(role)) {
						negative_TBox_premises.add(formula);
						break;
					}
				}

			}
		}
		
		System.out.println("positive_TBox_premises = " + positive_TBox_premises);
		System.out.println("negative_TBox_premises = " + negative_TBox_premises);

		if (positive_TBox_premises.isEmpty() || negative_TBox_premises.isEmpty()) {
			return output_list;
		
		} else {
			
			Simplifier pp = new Simplifier();
			
			List<List<Formula>> combination_list = getCombinations(negative_TBox_premises);
			//System.out.println("combination_list = " + combination_list);
			
			for (Formula pt_premise : positive_TBox_premises) {
				List<Formula> pt_C_list = new ArrayList<>();
				List<Formula> pt_D_list = new ArrayList<>();

				if (pt_premise instanceof Exists) {
					if (pt_premise.getSubFormulas().get(1) instanceof And) {
						pt_D_list.addAll(pt_premise.getSubFormulas().get(1).getSubFormulas());
					} else {
						pt_D_list.add(pt_premise.getSubFormulas().get(1));
					}

				} else {
					List<Formula> pt_disjunct_list = pt_premise.getSubFormulas();

					for (Formula pt_disjunct : pt_disjunct_list) {
						if (pt_disjunct instanceof Exists && pt_disjunct.getSubFormulas().get(0).equals(role)) {
							if (pt_disjunct.getSubFormulas().get(1) instanceof And) {
								pt_D_list.addAll(pt_disjunct.getSubFormulas().get(1).getSubFormulas());
							} else {
								pt_D_list.add(pt_disjunct.getSubFormulas().get(1));
							}
						} else {
							pt_C_list.add(pt_disjunct);
						}
					}

				}

				for (List<Formula> combination : combination_list) {
					List<Formula> CE_list = new ArrayList<>(pt_C_list);
					List<Formula> DF_list = new ArrayList<>(pt_D_list);

					for (Formula nt_premise : combination) {
						if (nt_premise instanceof Forall) {
							if (nt_premise.getSubFormulas().get(1) instanceof And) {
								DF_list.addAll(nt_premise.getSubFormulas().get(1).getSubFormulas());
							} else {
								DF_list.add(nt_premise.getSubFormulas().get(1));
							}

						} else {
							List<Formula> nt_disjunct_list = nt_premise.getSubFormulas();
							for (Formula nt_disjunct : nt_disjunct_list) {
								if (nt_disjunct instanceof Forall
										&& nt_disjunct.getSubFormulas().get(0).equals(role)) {
									if (nt_disjunct.getSubFormulas().get(1) instanceof And) {
										DF_list.addAll(nt_disjunct.getSubFormulas().get(1).getSubFormulas());
									} else {
										DF_list.add(nt_disjunct.getSubFormulas().get(1));
									}

								} else {
									CE_list.add(nt_disjunct);
								}
							}
						}
					}

					Formula DF = null;

					if (DF_list.size() == 1) {
						DF = DF_list.get(0);
					} else {
						DF = new And(DF_list);
					}
					
					//System.out.println("DF = " + DF);
					//System.out.println("pp.getSimplifiedForm(DF) = " + pp.getSimplifiedForm(DF));

					if (pp.getSimplifiedForm(DF.clone()) == BottomConcept.getInstance()) {

						if (CE_list.isEmpty()) {
							continue;
						} else if (CE_list.size() == 1) {
							output_list.add(CE_list.get(0));
							continue;
						} else {
							output_list.add(new Or(CE_list));
							continue;
						}
					}
				}
			}			
		}
		
		return output_list;
	}*/

			
	public List<Formula> Ackermann_R(AtomicRole role, List<Formula> formula_list)
			throws CloneNotSupportedException {

		// System.out.println("role = " + role);
		// System.out.println("formula_list = " + formula_list);

		List<Formula> output_list = new ArrayList<>();

		List<Formula> positive_RBox_premises = new ArrayList<>();
		List<Formula> negative_RBox_premises = new ArrayList<>();
		List<Formula> positive_TBox_premises = new ArrayList<>();
		List<Formula> negative_TBox_premises = new ArrayList<>();

		EChecker ec = new EChecker();
		FChecker fc = new FChecker();

		for (Formula formula : formula_list) {
			if (fc.positive(role, formula) + fc.negative(role, formula) > 1) {
				return formula_list;
						
			} else if (!ec.isPresent(role, formula)) {
				output_list.add(formula);

			} else if (formula.equals(role)) {
				positive_RBox_premises.add(formula);

			} else if (formula.equals(new Negation(role))) {
				negative_RBox_premises.add(formula);

			} else if (formula instanceof Geq && formula.getSubFormulas().get(0).equals(role)) {
				positive_TBox_premises.add(formula);

			} else if (formula instanceof Leq && formula.getSubFormulas().get(0).equals(role)) {
				negative_TBox_premises.add(formula);

			} else if (formula instanceof Or) {
				List<Formula> disjunct_list = formula.getSubFormulas();

				if (disjunct_list.contains(role)) {
					positive_RBox_premises.add(formula);

				} else if (disjunct_list.contains(new Negation(role))) {
					negative_RBox_premises.add(formula);

				} else {
					for (Formula disjunct : disjunct_list) {
						if (disjunct instanceof Geq && disjunct.getSubFormulas().get(0).equals(role)) {
							positive_TBox_premises.add(formula);
							break;
						} else if (disjunct instanceof Leq && disjunct.getSubFormulas().get(0).equals(role)) {
							negative_TBox_premises.add(formula);
							break;
						}
					}
				}
			}
		}

		if (negative_TBox_premises.isEmpty() && negative_RBox_premises.isEmpty()) {
			return output_list;
		}

		if (positive_TBox_premises.isEmpty() && positive_RBox_premises.isEmpty()) {
			return output_list;
		}

		//
		if (!negative_RBox_premises.isEmpty()) {

			if (negative_RBox_premises.contains(new Negation(role))) {
				if (!positive_RBox_premises.isEmpty()) {
					for (Formula pr_premise : positive_RBox_premises) {
						output_list.add(AckermannReplace(role, pr_premise, BottomRole.getInstance()));
					}
				}
				if (!positive_TBox_premises.isEmpty()) {
					for (Formula pt_premise : positive_TBox_premises) {
						output_list.add(AckermannReplace(role, pt_premise, BottomRole.getInstance()));
					}
				}

			} else {

				for (Formula nr_premise : negative_RBox_premises) {

					Formula nr_def = null;
					List<Formula> nr_def_list = new ArrayList<>(nr_premise.getSubFormulas());
					nr_def_list.remove(new Negation(role));
					if (nr_def_list.size() == 1) {
						nr_def = nr_def_list.get(0);
					} else {
						nr_def = new Or(nr_def_list);
					}

					if (!positive_RBox_premises.isEmpty()) {
						for (Formula pr_premise : positive_RBox_premises) {
							output_list.add(AckermannReplace(role, pr_premise, nr_def));
						}
					}
					if (!positive_TBox_premises.isEmpty()) {
						for (Formula pt_premise : positive_TBox_premises) {
							output_list.add(AckermannReplace(role, pt_premise, nr_def));
						}
					}
				}
			}
		}

		//
		if (!positive_RBox_premises.isEmpty()) {

			if (positive_RBox_premises.contains(role)) {
				if (!negative_TBox_premises.isEmpty()) {
					for (Formula nt_premise : negative_TBox_premises) {
						output_list.add(AckermannReplace(role, nt_premise, TopRole.getInstance()));
					}
				}

			} else {

				for (Formula pr_premise : positive_RBox_premises) {

					Formula pr_def = null;
					List<Formula> pr_def_list = new ArrayList<>(pr_premise.getSubFormulas());
					pr_def_list.remove(role);
					if (pr_def_list.size() == 1) {
						pr_def = new Negation(pr_def_list.get(0));
					} else {
						pr_def = new Negation(new Or(pr_def_list));
					}
					if (!negative_TBox_premises.isEmpty()) {
						for (Formula nt_premise : negative_TBox_premises) {
							output_list.add(AckermannReplace(role, nt_premise, pr_def));
						}
					}
				}
			}
		}

		//
		if (!positive_TBox_premises.isEmpty() && !negative_TBox_premises.isEmpty()) {
			
			Simplifier simp = new Simplifier();

			if (positive_RBox_premises.isEmpty() || negative_RBox_premises.isEmpty()) {
				BackConverter bc = new BackConverter();
				for(Formula pt_premise : positive_TBox_premises) {
					
					for(Formula nt_premise : negative_TBox_premises) {
						List<Formula> frac_list = new ArrayList<>();
						Leq nt_leq = null;
						
						if (nt_premise instanceof Leq) {
							frac_list.add(BottomConcept.getInstance());
							nt_leq = (Leq) nt_premise;
						} else {
							List<Formula> nt_disjunct_list = nt_premise.getSubFormulas();
							for (Formula nt_disjunct : nt_disjunct_list) {
								if (ec.isPresent(role, nt_disjunct)) {
									nt_leq = (Leq) nt_disjunct;
									
								} else {
									frac_list.add(nt_disjunct);
								}
								
							}
						}
						Geq pt_geq = null;
						if (pt_premise instanceof Geq) {
							frac_list.add(BottomConcept.getInstance());
							pt_geq = (Geq) pt_premise;
						} else {
							List<Formula> pt_disjunct_list = pt_premise.getSubFormulas();
							for (Formula pt_disjunct : pt_disjunct_list) {
								if (ec.isPresent(role, pt_disjunct)) {
									pt_geq = (Geq) pt_disjunct;
									
								} else {
									frac_list.add(pt_disjunct);
								}
								
							}
						}
						List<Formula> or_list = new ArrayList<>();
						or_list.add(new Negation(pt_geq.getSubFormulas().get(1)));
						or_list.add(nt_leq.getSubFormulas().get(1));
						Formula flag = simp.getSimplifiedForm(new Or(or_list));
						if (pt_geq.get_num() > nt_leq.get_num() && flag.equals(TopConcept.getInstance())) {
							if(frac_list.size() == 1) {
								output_list.add(frac_list.get(0));
							} else {
								output_list.add(new Or(frac_list));
							}
						}
					}
				}
			}
		} 
		
		return output_list;
	}
	
	
	
	public List<Formula> AckermannPositive(AtomicConcept concept, List<Formula> input_list) throws CloneNotSupportedException {

		List<Formula> output_list = new ArrayList<>();
		List<Formula> toBeReplaced_list = new ArrayList<>();
		List<Formula> toReplace_list = new ArrayList<>();

		FChecker cf = new FChecker();

		for (Formula formula : input_list) {
			if (cf.positive(concept, formula) == 0) {
				toBeReplaced_list.add(formula);

			} else {
				toReplace_list.add(formula);
			}
		}

		Formula definition = null;
		List<Formula> disjunct_list = new ArrayList<>();

		for (Formula toReplace : toReplace_list) {
			if (toReplace.equals(concept)) {
				definition = TopConcept.getInstance();
				break;
				
			} else {
				List<Formula> other_list = new ArrayList<>(toReplace.getSubFormulas());
				other_list.remove(concept);
				if (other_list.size() == 1) {
					disjunct_list.add(new Negation(other_list.get(0)));
					continue;
				} else {
					disjunct_list.add(new Negation(new Or(other_list)));
					continue;
				}
			}
		}

		if (definition != TopConcept.getInstance()) {
			if (disjunct_list.size() == 1) {
				definition = disjunct_list.get(0);
			} else {
				definition = new Or(disjunct_list);
			}
		}

		for (Formula toBeReplaced : toBeReplaced_list) {
			output_list.add(AckermannReplace(concept, toBeReplaced, definition));
		}

		return output_list;
	}
	
	public List<Formula> AckermannNegative(AtomicConcept concept, List<Formula> input_list)
			throws CloneNotSupportedException {
		
		List<Formula> output_list = new ArrayList<>();
		List<Formula> toBeReplaced_list = new ArrayList<>();
		List<Formula> toReplace_list = new ArrayList<>();

		FChecker cf = new FChecker();

		for (Formula formula : input_list) {
			if (cf.negative(concept, formula) == 0) {
				toBeReplaced_list.add(formula);

			} else {
				toReplace_list.add(formula);
			}
		}

		Formula definition = null;
		List<Formula> disjunct_list = new ArrayList<>();

		for (Formula toReplace : toReplace_list) {
			if (toReplace.equals(new Negation(concept))) {
				definition = BottomConcept.getInstance();
				break;
				
			} else {
				List<Formula> other_list = new ArrayList<>(toReplace.getSubFormulas());
				other_list.remove(new Negation(concept));
				if (other_list.size() == 1) {
					disjunct_list.add(other_list.get(0));
					continue;
				} else {
					disjunct_list.add(new Or(other_list));
					continue;
				}
			}
		}

		if (definition != BottomConcept.getInstance()) {
			if (disjunct_list.size() == 1) {
				definition = disjunct_list.get(0);
			} else {
				definition = new And(disjunct_list);
			}
		}

		for (Formula toBeReplaced : toBeReplaced_list) {
			output_list.add(AckermannReplace(concept, toBeReplaced, definition));
		}

		return output_list;
	}

	public List<Formula> PurifyPositive(AtomicRole role, List<Formula> input_list)
			throws CloneNotSupportedException {

		FChecker cf = new FChecker();

		List<Formula> output_list = new ArrayList<>();

		for (Formula formula : input_list) {
			if (cf.positive(role, formula) == 0) {
				output_list.add(formula);
			} else {
				output_list.add(PurifyPositive(role, formula));
			}
		}

		return output_list;
	}

	public List<Formula> PurifyPositive(AtomicConcept concept, List<Formula> input_list)
			throws CloneNotSupportedException {

		FChecker cf = new FChecker();

		List<Formula> output_list = new ArrayList<>();

		for (Formula formula : input_list) {
			if (cf.positive(concept, formula) == 0) {
				output_list.add(formula);
			} else {
				output_list.add(PurifyPositive(concept, formula));
			}
		}

		return output_list;
	}

	public List<Formula> PurifyNegative(AtomicRole role, List<Formula> input_list)
			throws CloneNotSupportedException {

		FChecker cf = new FChecker();

		List<Formula> output_list = new ArrayList<>();

		for (Formula formula : input_list) {
			if (cf.negative(role, formula) == 0) {
				output_list.add(formula);
			} else {
				output_list.add(PurifyNegative(role, formula));
			}
		}

		return output_list;
	}

	public List<Formula> PurifyNegative(AtomicConcept concept, List<Formula> inputList)
			throws CloneNotSupportedException {

		FChecker cf = new FChecker();

		List<Formula> outputList = new ArrayList<>();

		for (Formula formula : inputList) {
			if (cf.negative(concept, formula) == 0) {
				outputList.add(formula);
			} else {
				outputList.add(PurifyNegative(concept, formula));
			}
		}

		return outputList;
	}

	public Formula AckermannReplace(AtomicRole role, Formula toBeReplaced, Formula definition) {

		if (toBeReplaced instanceof AtomicConcept) {
			return new AtomicConcept(toBeReplaced.getText());

		} else if (toBeReplaced instanceof AtomicRole) {
			return toBeReplaced.equals(role) ? definition : new AtomicRole(toBeReplaced.getText());

		} else if (toBeReplaced instanceof Individual) {
			return new Individual(toBeReplaced.getText());
		
		} else if (toBeReplaced instanceof Negation) {
			return new Negation(AckermannReplace(role, toBeReplaced.getSubFormulas().get(0), definition));

		} else if (toBeReplaced instanceof Exists) {
			return new Exists(AckermannReplace(role, toBeReplaced.getSubFormulas().get(0), definition),
					AckermannReplace(role, toBeReplaced.getSubFormulas().get(1), definition));

		} else if (toBeReplaced instanceof Forall) {
			return new Forall(AckermannReplace(role, toBeReplaced.getSubFormulas().get(0), definition),
					AckermannReplace(role, toBeReplaced.getSubFormulas().get(1), definition));

		} else if (toBeReplaced instanceof Leq) {
			Leq tmp = (Leq) toBeReplaced;
			return new Leq(tmp.get_num(),AckermannReplace(role,tmp.getSubFormulas().get(0),definition),
					AckermannReplace(role,tmp.getSubFormulas().get(1),definition));
			
		} else if (toBeReplaced instanceof Geq) {
			Geq tmp = (Geq) toBeReplaced;
			return new Geq(tmp.get_num(),AckermannReplace(role,tmp.getSubFormulas().get(0),definition),
					AckermannReplace(role,tmp.getSubFormulas().get(1),definition));
			
		} else if (toBeReplaced instanceof And) {
			List<Formula> conjunct_list = toBeReplaced.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(AckermannReplace(role, conjunct, definition));
			}
			return new And(new_conjunct_list);

		} else if (toBeReplaced instanceof Or) {
			List<Formula> disjunct_list = toBeReplaced.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(AckermannReplace(role, disjunct, definition));
			}
			return new Or(new_disjunct_list);

		}

		return toBeReplaced;
	}
	
	public Formula AckermannReplace(AtomicConcept concept, Formula toBeReplaced, Formula definition) {

		if (toBeReplaced instanceof AtomicConcept) {
			return toBeReplaced.equals(concept) ? definition : new AtomicConcept(toBeReplaced.getText());
			
		} else if (toBeReplaced instanceof AtomicRole) {
			return new AtomicRole(toBeReplaced.getText());

		} else if (toBeReplaced instanceof Individual) {
			return new Individual(toBeReplaced.getText());
		
		} else if (toBeReplaced instanceof Negation) {
			return new Negation(AckermannReplace(concept, toBeReplaced.getSubFormulas().get(0), definition));
			
		} else if (toBeReplaced instanceof Exists) {
			return new Exists(AckermannReplace(concept, toBeReplaced.getSubFormulas().get(0), definition),
					AckermannReplace(concept, toBeReplaced.getSubFormulas().get(1), definition));

		} else if (toBeReplaced instanceof Forall) {
			return new Forall(AckermannReplace(concept, toBeReplaced.getSubFormulas().get(0), definition),
					AckermannReplace(concept, toBeReplaced.getSubFormulas().get(1), definition));
			
		} else if (toBeReplaced instanceof Leq) {
			Leq tmp = (Leq) toBeReplaced;
			return new Leq(tmp.get_num(),AckermannReplace(concept,tmp.getSubFormulas().get(0),definition),
					AckermannReplace(concept,tmp.getSubFormulas().get(1),definition));
			
		} else if (toBeReplaced instanceof Geq) {
			Geq tmp = (Geq) toBeReplaced;
			return new Geq(tmp.get_num(),AckermannReplace(concept,tmp.getSubFormulas().get(0),definition),
					AckermannReplace(concept,tmp.getSubFormulas().get(1),definition));
			
		} else if (toBeReplaced instanceof And) {
			List<Formula> conjunct_list = toBeReplaced.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(AckermannReplace(concept, conjunct, definition));
			}
			return new And(new_conjunct_list);
			
		} else if (toBeReplaced instanceof Or) {
			List<Formula> disjunct_list = toBeReplaced.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(AckermannReplace(concept, disjunct, definition));
			}
			return new Or(new_disjunct_list);
			
		}
		
		return toBeReplaced;
	}
	
	public Formula PurifyPositive(AtomicRole role, Formula formula) {
		
		if (formula instanceof AtomicConcept) {
			return new AtomicConcept(formula.getText());
		
		} else if (formula instanceof AtomicRole) {
			return formula.equals(role) ? TopRole.getInstance() : new AtomicRole(formula.getText());
		
		} else if (formula instanceof Individual) {
			return new Individual(formula.getText());
		
		} else if (formula instanceof Negation) {
			return new Negation(PurifyPositive(role, formula.getSubFormulas().get(0)));
			
		} else if (formula instanceof Leq) {
			Leq tmp = (Leq) formula;
			return new Leq(tmp.get_num(),PurifyPositive(role, tmp.getSubFormulas().get(0)),
					PurifyPositive(role, formula.getSubFormulas().get(1)));
		
		} else if (formula instanceof Geq) {
			Geq tmp = (Geq) formula;
			return new Geq(tmp.get_num(),PurifyPositive(role, tmp.getSubFormulas().get(0)),
					PurifyPositive(role, formula.getSubFormulas().get(1)));
			
		} else if (formula instanceof And) {
			List<Formula> conjunct_list = formula.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(PurifyPositive(role, conjunct));
			}
			return new And(new_conjunct_list);
			
		} else if (formula instanceof Or) {
			List<Formula> disjunct_list = formula.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(PurifyPositive(role, disjunct));
			}
			return new Or(new_disjunct_list);
		}

		return formula;
	}
	
	public Formula PurifyNegative(AtomicRole role, Formula formula) {
		
		if (formula instanceof AtomicConcept) {
			return new AtomicConcept(formula.getText());
		
		} else if (formula instanceof AtomicRole) {
			return formula.equals(role) ? BottomRole.getInstance() : new AtomicRole(formula.getText());
		
		} else if (formula instanceof Individual) {
			return new Individual(formula.getText());
		
		} else if (formula instanceof Negation) {
			return new Negation(PurifyNegative(role, formula.getSubFormulas().get(0)));
			
		} else if (formula instanceof Leq) {
			Leq tmp = (Leq) formula;
			return new Leq(tmp.get_num(),PurifyNegative(role, tmp.getSubFormulas().get(0)),
					PurifyNegative(role, formula.getSubFormulas().get(1)));
		
		} else if (formula instanceof Geq) {
			Geq tmp = (Geq) formula;
			return new Geq(tmp.get_num(),PurifyNegative(role, tmp.getSubFormulas().get(0)),
					PurifyNegative(role, formula.getSubFormulas().get(1)));
			
		} else if (formula instanceof And) {
			List<Formula> conjunct_list = formula.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(PurifyNegative(role, conjunct));
			}
			return new And(new_conjunct_list);
			
		} else if (formula instanceof Or) {
			List<Formula> disjunct_list = formula.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(PurifyNegative(role, disjunct));
			}
			return new Or(new_disjunct_list);
		}

		return formula;
	}
	
	public Formula PurifyPositive(AtomicConcept concept, Formula formula) {

		if (formula instanceof AtomicConcept) {
			return formula.equals(concept) ? TopConcept.getInstance() : new AtomicConcept(formula.getText());
			
		} else if (formula instanceof AtomicRole) {
			return new AtomicRole(formula.getText());

		} else if (formula instanceof Individual) {
			return new Individual(formula.getText());
		
		} else if (formula instanceof Negation) {
			return new Negation(PurifyPositive(concept, formula.getSubFormulas().get(0)));
			
		} else if (formula instanceof Leq) {
			Leq tmp = (Leq) formula;
			return new Leq(tmp.get_num(),PurifyPositive(concept, tmp.getSubFormulas().get(0)),
					PurifyPositive(concept, formula.getSubFormulas().get(1)));
		
		} else if (formula instanceof Geq) {
			Geq tmp = (Geq) formula;
			return new Geq(tmp.get_num(),PurifyPositive(concept, tmp.getSubFormulas().get(0)),
					PurifyPositive(concept, formula.getSubFormulas().get(1)));
			
		} else if (formula instanceof And) {
			List<Formula> conjunct_list = formula.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(PurifyPositive(concept, conjunct));
			}
			return new And(new_conjunct_list);
			
		} else if (formula instanceof Or) {
			List<Formula> disjunct_list = formula.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(PurifyPositive(concept, disjunct));
			}
			return new Or(new_disjunct_list);
		}

		return formula;
	}
			
	public Formula PurifyNegative(AtomicConcept concept, Formula formula) {

		if (formula instanceof AtomicConcept) {
			return formula.equals(concept) ? BottomConcept.getInstance() : new AtomicConcept(formula.getText());

		} else if (formula instanceof AtomicRole) {
			return new AtomicRole(formula.getText());

		} else if (formula instanceof Individual) {
			return new Individual(formula.getText());
		
		} else if (formula instanceof Negation) {
			return new Negation(PurifyNegative(concept, formula.getSubFormulas().get(0)));
			
		} else if (formula instanceof Leq) {
			Leq tmp = (Leq) formula;
			return new Leq(tmp.get_num(),PurifyNegative(concept, tmp.getSubFormulas().get(0)),
					PurifyNegative(concept, tmp.getSubFormulas().get(1)));
		
		} else if (formula instanceof Geq) {
			Geq tmp = (Geq) formula;
			return new Geq(tmp.get_num(),PurifyNegative(concept, tmp.getSubFormulas().get(0)),
					PurifyNegative(concept, tmp.getSubFormulas().get(1)));
			
		} else if (formula instanceof And) {
			List<Formula> conjunct_list = formula.getSubFormulas();
			List<Formula> new_conjunct_list = new ArrayList<>();
			for (Formula conjunct : conjunct_list) {
				new_conjunct_list.add(PurifyNegative(concept, conjunct));
			}
			return new And(new_conjunct_list);

		} else if (formula instanceof Or) {
			List<Formula> disjunct_list = formula.getSubFormulas();
			List<Formula> new_disjunct_list = new ArrayList<>();
			for (Formula disjunct : disjunct_list) {
				new_disjunct_list.add(PurifyNegative(concept, disjunct));
			}
			return new Or(new_disjunct_list);
		}

		return formula;	
	}
	
	public List<Formula> transitive_rule(Set<AtomicConcept> cig, List<Formula> input_list){
		List<Formula> output_list = new ArrayList<>();
		for (AtomicConcept concept : cig) {
			input_list = transitive_rule(concept, input_list);
		}
		return input_list;
	}
	
	@SuppressWarnings("static-access")
	public List<Formula> transitive_rule(AtomicConcept concept, List<Formula> input_list){
		Converter cv = new Converter();
		EChecker ec = new EChecker();
		List<Formula> output_list = new ArrayList<>();
		for (Formula formula : input_list) {
			if(ec.isPresent(concept, formula)) {
				if(formula instanceof Geq) {
					Geq geq = (Geq) formula;
					if (cv.TransitiveRole_Set.contains(geq.getSubFormulas().get(0)) 
							&& cv.IrreflexiveRole_Set.contains(geq.getSubFormulas().get(0))
							&& ec.isTopLevel(concept, geq)){
						int num = geq.get_num();
						List<Formula> or_list = new ArrayList<>();
						or_list.add(new Leq(0,geq.getSubFormulas().get(0),new Geq(num,geq.getSubFormulas().get(0),geq.getSubFormulas().get(1))));
						or_list.add(new Geq(num+1,geq.getSubFormulas().get(0),TopConcept.getInstance()));
						output_list.add(new Or(or_list));
					}	
				} else if (formula instanceof Or) {
					Or or = (Or) formula;
					boolean flag = false;
					List<Formula> disjunct_list = or.getSubFormulas();
					List<Formula> or_list = new ArrayList<>();
					for (Formula disjunct:disjunct_list) {
						if(disjunct instanceof Geq) {
							Geq geq = (Geq) disjunct;
							if (cv.TransitiveRole_Set.contains(geq.getSubFormulas().get(0))
									&& ec.isTopLevel(concept, geq)) {
								int num = geq.get_num();
								or_list.add(new Leq(0,geq.getSubFormulas().get(0),new Geq(num,geq.getSubFormulas().get(0),geq.getSubFormulas().get(1))));
								or_list.add(new Geq(num+1,geq.getSubFormulas().get(0),TopConcept.getInstance()));
								flag = true;
							} else {
								or_list.add(geq);
							}
						} else {
							or_list.add(disjunct);
						}
					}
					if(flag) {
						output_list.add(new Or(or_list));
					}
				}
			} 
		}
		input_list.addAll(output_list);
		return input_list;
		
	}
	
	public static OWLAxiom getRoleSubAxiom(String pre, String r, String s) {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		PrefixManager prefix = new DefaultPrefixManager(
				pre);
		OWLDataFactory df = man.getOWLDataFactory();
		OWLObjectProperty rr = df.getOWLObjectProperty(r, prefix);
		OWLObjectProperty ss = df.getOWLObjectProperty(s, prefix);
		OWLAxiom OSP =df.getOWLSubObjectPropertyOfAxiom(rr, ss);
		return OSP;
	}
	
	public static OWLAxiom getRoleSubAxiom(Formula role1, Formula role2) {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		OWLDataFactory df = man.getOWLDataFactory();
		OWLObjectProperty rr = Converter.map1.get(role1);
		OWLObjectProperty ss = Converter.map1.get(role2);
		OWLAxiom OSP =df.getOWLSubObjectPropertyOfAxiom(rr, ss);
		return OSP;
	}
	
}
