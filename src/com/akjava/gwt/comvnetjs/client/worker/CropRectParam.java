package com.akjava.gwt.comvnetjs.client.worker;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public  class CropRectParam extends JavaScriptObject{
	
	protected CropRectParam(){}

	
	public static CropRectParam create(ImageData imageData,JsArray<HaarRect> rects){
		CropRectParam param=createObject().cast();
		
		param.setRects(rects);
		param.setImageData(imageData);
	
		return param;
	}
	
	
	


	public final  native JsArray<HaarRect> getRects()/*-{
	return this.rects;
	}-*/;
	public final  native void setRects(JavaScriptObject  param)/*-{
	this.rects=param;
	}-*/;	
	


	public final  native ImageData getImageData()/*-{
	return this.imageData;
	}-*/;
	public final  native void setImageData(JavaScriptObject  param)/*-{
	this.imageData=param;
	}-*/;
	

}