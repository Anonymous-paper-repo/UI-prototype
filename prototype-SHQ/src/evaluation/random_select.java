package evaluation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class random_select {
	public random_select() {
		
	}
	
	public <T> Set<T> getrandomList(List<T> inputlist,int percent){
		int num=inputlist.size()*percent/100;
		//int num=percent;
		Collections.shuffle(inputlist);
		Set<T> output=new HashSet<>();
		for(int i=0;i<num;i++) {
			output.add(inputlist.get(i));
		}
		return output;
	}
	
	public <T> Set<T> getrandomSet(Set<T> inputset,int percent){
		int num = inputset.size()*percent/100;
		@SuppressWarnings("unchecked")
		List<T> inputlist = (List<T>) inputset;
		Collections.shuffle(inputlist);
		Set<T> output=new HashSet<>();
		for(int i=0;i<num;i++) {
			output.add(inputlist.get(i));
		}
		return output;
	}
	
}
