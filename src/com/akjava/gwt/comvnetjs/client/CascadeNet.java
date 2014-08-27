package com.akjava.gwt.comvnetjs.client;
import java.util.List;

import javax.annotation.Nullable;

import com.akjava.gwt.lib.client.LogUtils;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;


public class CascadeNet {
private CascadeNet parent;
private Trainer trainer;
public Trainer getTrainer() {
	return trainer;
}

private Net net;
public void setTrainer(Trainer trainer) {
	this.trainer = trainer;
}

public void setNet(Net net) {
	this.net = net;
}

public Net getNet() {
	return net;
}

public CascadeNet(CascadeNet parent,String netJson){
	this(parent,Net.fromJsonString(netJson),null);
}


public CascadeNet(CascadeNet parent,Net net){
	this(parent,net,null);
}

public CascadeNet(CascadeNet parent,Net net,@Nullable Trainer trainer){
	this.parent=parent;
	this.net=net;
	if(trainer!=null){
		this.trainer = trainer;//need recreates
	}else{
		this.trainer=ConvnetJs.createTrainer(net,1);
	}
}

public String toJsonString(){
	return net.toJsonString();
}

List<VolFilter> filters;
public void createFilters(){
	if(filters!=null){
		return;
	}
	filters=Lists.newArrayList();
	CascadeNet target=this;
	while(target.getParent()!=null){
		target=target.getParent();
		if(target!=null){
		filters.add(0,new VolFilter(target,0));
		}
	}
	
}

List<VolFilter> allFilters;
public void createAllFilters(){
	if(allFilters!=null){
		return;
	}
	allFilters=Lists.newArrayList();
	CascadeNet target=this;
	while(target.getParent()!=null){
		target=target.getParent();
		if(target!=null){
			allFilters.add(0,new VolFilter(target,0));
		}
	}
	allFilters.add(new VolFilter(this, 0));
}

public Optional<Vol> filterParents(Vol vol){
	
	createFilters();
	boolean passParents=Predicates.and(filters).apply(vol);
	if(passParents){
		return Optional.of(vol);
	}else{
		return Optional.absent();
	}
}

public Optional<Vol> filter(Vol vol){
	
	createAllFilters();
	
	boolean passParents=Predicates.and(allFilters).apply(vol);
	if(passParents){
		return Optional.of(vol);
	}else{
		return Optional.absent();
	}
}

//do filter by your self
public void train(Vol vol,int classIndex){
	trainer.train(vol, classIndex);
}


public class VolFilter implements Predicate<Vol>{
CascadeNet cnet;
int index;
double minConfidence=0.55;
public VolFilter(CascadeNet cnet,int index){
	this.cnet=cnet;
	this.index=index;
}
	@Override
	public boolean apply(Vol input) {
		Vol result=cnet.getNet().forward(input);
		return result.getW(0)>result.getW(1) && result.getW(0)>minConfidence;
	}
	
}




public CascadeNet getParent(){
	return parent;
}

}
