package com.akjava.gwt.comvnetjs.client;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;

public class Vol extends JavaScriptObject{
protected Vol(){}

public final void set(int x,int y,int[] depthValues){
	for(int i=0;i<depthValues.length;i++){
		set(x,y,i,depthValues[i]);
	}
}

public final void set(int x,int y,double[] depthValues){
	for(int i=0;i<depthValues.length;i++){
		set(x,y,i,depthValues[i]);
	}
}


public   final native double get(int x,int y,int d) /*-{
return this.get(x,y,d);
 }-*/;


public   final native void set(int x,int y,int d,double value) /*-{
this.set(x,y,d,value);
 }-*/;


public   final native double getW(int index) /*-{
return this.w[index];
 }-*/;

public   final native double setW(int index,double value) /*-{
return this.w[index]=value;
 }-*/;

public   final native int getLength() /*-{
return this.w.length;
 }-*/;

public final List<Double> getWAsList(){
	List<Double> doubles=Lists.newArrayList();
	for(int i=0;i<getLength();i++){
		doubles.add(getW(i));
	}
	return doubles;
}
}
