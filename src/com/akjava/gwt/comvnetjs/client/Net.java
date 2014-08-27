package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONValue;

public class Net extends JavaScriptObject{
protected Net(){}


public   final native Vol forward(Vol vol) /*-{
return this.forward(vol);
 }-*/;

public   final native Vol forward(Vol vol,boolean isTraining) /*-{
return this.forward(vol,isTraining);
 }-*/;

public   final native String toJsonString() /*-{
var json = this.toJSON();
return JSON.stringify(json);
 }-*/;




public  static  final native Net fromJsonString(String str) /*-{

var json = $wnd.JSON.parse(str); // creates json object out of a string

var net2 = new $wnd.convnetjs.Net(); // create an empty network

net2.fromJSON(json); // load all parameters from JSON
return net2;
 }-*/;



}
