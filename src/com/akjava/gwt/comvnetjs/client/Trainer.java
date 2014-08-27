package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Trainer extends JavaScriptObject{
protected Trainer(){}

public   final native void train(Vol vol,int classIndex) /*-{
this.train(vol,classIndex);
 }-*/;
}
