package com.akjava.gwt.comvnetjs.client.worker;

import com.akjava.gwt.comvnetjs.client.Vol;
import com.google.gwt.core.client.JavaScriptObject;

public class NegativeResult extends JavaScriptObject{
protected NegativeResult(){}

public final  native Vol getVol()/*-{
return this.vol;
}-*/;
public final  native void setVol(Vol  param)/*-{
this.vol=param;
}-*/;

public final  native boolean isHorizontalFlipped()/*-{
return this.horizontalFlipped;
}-*/;
public final  native void setHorizontalFlipped(boolean  param)/*-{
this.horizontalFlipped=param;
}-*/;

public final  native int getAngle()/*-{
return this.angle;
}-*/;
public final  native void setAngle(int  param)/*-{
this.angle=param;
}-*/;

}
