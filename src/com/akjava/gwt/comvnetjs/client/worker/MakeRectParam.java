package com.akjava.gwt.comvnetjs.client.worker;

import com.google.gwt.core.client.JavaScriptObject;

public class MakeRectParam extends JavaScriptObject{

	protected MakeRectParam(){}

	public static MakeRectParam create(String name,int imageW,int imageH,int stepScale,double scale_factor,int minW,int minH,double min_scale){
		
		MakeRectParam param=MakeRectParam.createObject().cast();
		param.setImageW(imageW);
		param.setImageH(imageH);
		
		param.setStepScale(stepScale);
		param.setScale_factor(scale_factor);
		param.setMinW(minW);
		param.setMinH(minH);
		param.setMin_scale(min_scale);
		return param;
	}
	
	public final  native String getName()/*-{
	return this.name;
	}-*/;
	public final  native void setName(String  param)/*-{
	this.name=param;
	}-*/;

	public final  native int getImageW()/*-{
	return this.imageW;
	}-*/;
	public final  native void setImageW(int  param)/*-{
	this.imageW=param;
	}-*/;
	

	public final  native int getImageH()/*-{
	return this.imageH;
	}-*/;
	public final  native void setImageH(int  param)/*-{
	this.imageH=param;
	}-*/;
	
	
	public final  native int getStepScale()/*-{
	return this.stepScale;
	}-*/;
	public final  native void setStepScale(int  param)/*-{
	this.stepScale=param;
	}-*/;


	public final  native double getScale_factor()/*-{
	return this.scale_factor;
	}-*/;
	public final  native void setScale_factor(double  param)/*-{
	this.scale_factor=param;
	}-*/;


	public final  native int getMinW()/*-{
	return this.minW;
	}-*/;
	public final  native void setMinW(int  param)/*-{
	this.minW=param;
	}-*/;


	public final  native int getMinH()/*-{
	return this.minH;
	}-*/;
	public final  native void setMinH(int  param)/*-{
	this.minH=param;
	}-*/;


	public final  native double getMin_scale()/*-{
	return this.min_scale;
	}-*/;
	public final  native void setMin_scale(double  param)/*-{
	this.min_scale=param;
	}-*/;

}
