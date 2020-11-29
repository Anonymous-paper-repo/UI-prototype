package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

public class random_select {
	public random_select() {
		
	}
	
	public <T> Set<T> getrandomList(List<T> inputlist,int percent){
		//int num=inputlist.size()*percent/100;
		int num=percent;
		Collections.shuffle(inputlist);
		Set<T> output=new HashSet<>();
		for(int i=0;i<num;i++) {
			output.add(inputlist.get(i));
		}
		return output;
	}
	
	public  Set<OWLEntity> getrandomSet(Set<OWLEntity> inputset,int percent){
		int num = inputset.size()*percent/100;

		List<OWLEntity> inputlist = new ArrayList<>() ;
		for (OWLEntity aa : inputset) {
			inputlist.add(aa);
		}
		Collections.shuffle(inputlist);
		Set<OWLEntity> output=new HashSet<>();
		for(int i=0;i<num;i++) {
			output.add(inputlist.get(i));
		}
		return output;
	}
}
