package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Vol extends JavaScriptObject{
protected Vol(){}

public   final native void set(int x,int y,int d,double value) /*-{
this.set(x,y,d,value);
 }-*/;


public   final native double getW(int index) /*-{
return this.w[index];
 }-*/;

}
