package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Trainer extends JavaScriptObject{
protected Trainer(){}

public   final native Stats train(Vol vol,int classIndex) /*-{
return this.train(vol,classIndex);
 }-*/;
}
