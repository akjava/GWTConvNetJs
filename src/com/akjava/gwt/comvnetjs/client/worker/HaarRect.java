package com.akjava.gwt.comvnetjs.client.worker;

import com.akjava.lib.common.graphics.Rect;
import com.google.gwt.core.client.JavaScriptObject;

public class HaarRect extends JavaScriptObject{
protected HaarRect(){}

public   final native int getX() /*-{
	return this.x;
}-*/;


public   final native int getY() /*-{
	return this.y;
}-*/;


public   final native double getConfidence() /*-{
return this.confidence;
}-*/;

public   final native void setConfidence(double value) /*-{
 this.confidence=value;
}-*/;

public   final native int getWidth() /*-{
	return this.width;
}-*/;


public   final native int getHeight() /*-{
	return this.height;
}-*/;

public final Rect toRect(){
	return new Rect(getX(),getY(),getWidth(),getHeight());
}


public static final native HaarRect create(int x,int y,int w,int h) /*-{
return {x:x,y:y,width:w,height:h};
}-*/;



}
