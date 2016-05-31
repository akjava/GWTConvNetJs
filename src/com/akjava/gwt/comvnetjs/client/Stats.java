package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Stats extends JavaScriptObject{
protected Stats(){}

public   final native double getLoss() /*-{
return this.loss;
 }-*/;
}
