package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.List;

public class Statics {

	public static double average(List<Double> values){
		double total=0;
		for(Double v:values){
			total+=v;
		}
		return total/values.size();
	}
	
	//value would sort
	public static double middle(List<Double> values){
		checkNotNull(values, "value is null");
		checkState(values.size()>0,"empty value");
		
		if(values.size()<2){
			return values.get(0);
		}
		
		Collections.sort(values);
		
		int center=values.size()/2;
		int mod=values.size()%2;
		if(mod==1){
			return values.get(center);
		}else{
			return (values.get(center)+values.get(center-1))/2;
		}
	}
}
