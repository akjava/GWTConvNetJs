package com.akjava.gwt.comvnetjs.client;

import com.google.gwt.canvas.dom.client.ImageData;


public class ConvnetJs {
	private ConvnetJs(){}
	
	public static  final native Net xcreateImageNet(int w,int h,int depth,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:w, out_sy:h, out_depth:depth});
	layer_defs.push({type:'conv', sx:5, filters:16, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'conv', sx:5, filters:20, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'conv', sx:5, filters:20, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'softmax', num_classes:classes});

	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	return net;
    }-*/;
	
	public static  final native Net createGrayImageNet(int w,int h,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:w, out_sy:h, out_depth:1});
	layer_defs.push({type:'conv', sx:5, filters:8, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'conv', sx:5, filters:16, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:3, stride:3});
	layer_defs.push({type:'softmax', num_classes:classes});

	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	return net;
    }-*/;
	
	public static  final native Net createGrayImageNet2(int w,int h,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:w, out_sy:h, out_depth:1});
	layer_defs.push({type:'conv', sx:2, filters:8, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:2, stride:2});
	layer_defs.push({type:'conv', sx:2, filters:16, stride:1, pad:2, activation:'relu'});
	layer_defs.push({type:'pool', sx:3, stride:3});
	//layer_defs.push({type:'softmax', num_classes:classes});
	layer_defs.push({type:'svm', num_classes:classes});

	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	return net;
    }-*/;
	
	
	public static  final native Net createTwoLayerNetworkDemo(int x,int y,int depth,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:x, out_sy:y, out_depth:depth});
	layer_defs.push({type:'fc',num_neurons:6});
	layer_defs.push({type:'fc',num_neurons:2});
	layer_defs.push({type:'softmax', num_classes:classes});
	
	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	
	return net;
    }-*/;
	
	/* input value better -1.0 - 1.0 */
	public static  final native Net createRawDepathNet(int x,int y,int depth,int neurons,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:x, out_sy:y, out_depth:depth});
	layer_defs.push({type:'fc',num_neurons:neurons});
	
	layer_defs.push({type:'softmax', num_classes:classes});
	
	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	
	return net;
    }-*/;
	
	
	
	
	/* input value better -1 - 1 */
	public static  final native Net createRawDepathNet2(int x,int y,int depth,int neurons,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:x, out_sy:y, out_depth:depth});
	layer_defs.push({type:'fc',num_neurons:neurons});//here is raw
	//layer_defs.push({type:'fc',num_neurons:neurons,activation: 'tanh'});//here is raw
	//layer_defs.push({type:'fc', num_neurons:classes,activation: 'sigmoid'});
	//layer_defs.push({type:'fc', num_neurons:classes,activation: 'relu'});
	layer_defs.push({type:'fc', num_neurons:classes,activation: 'tanh'});
	layer_defs.push({type:'softmax', num_classes:classes});
	
	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	
	return net;
    }-*/;
	
	
	public static  final native Net createDepathNet(int x,int y,int depth,int tanh,int classes) /*-{
	var layer_defs = [];
	layer_defs.push({type:'input', out_sx:x, out_sy:y, out_depth:depth});
	layer_defs.push({type:'fc',num_neurons:tanh,activation: 'tanh'});
	layer_defs.push({type:'fc', num_neurons:classes,activation: 'tanh'});
	
	layer_defs.push({type:'softmax', num_classes:classes});
	//layer_defs.push({type:'svm', num_classes:classes});

	var net = new $wnd.convnetjs.Net();
	net.makeLayers(layer_defs);
	
	return net;
    }-*/;
	
	

	//NOT TEST YET
	public static  final native Vol createDepthOnlyVol(double[] values) /*-{
	var vol= new $wnd.convnetjs.Vol(1,1,values.length,0);
	
	for(var i=0;i<values.legth;i++){
		vol.set(0,0,i,values[i]);
		}
		
	return vol;
	 }-*/;
	
	
	public static  final native Vol createVol(int w,int h,int depth,double defaultValue) /*-{
	return new $wnd.convnetjs.Vol(w,h,depth,defaultValue);
	 }-*/;
	
	public static Vol xcreateVol(ImageData imageData){
		Vol vol=createVol(imageData.getWidth(),imageData.getHeight(),3,0);
		for(int y=0;y<imageData.getHeight();y++){
			for(int x=0;x<imageData.getWidth();x++){
				vol.set(x, y, 0, imageData.getRedAt(x, y));
				vol.set(x, y, 1, imageData.getGreenAt(x, y));
				vol.set(x, y, 2, imageData.getBlueAt(x, y));
			}
		}
		return vol;
		
	}
	
	public static Vol createGrayVolFromRGBImage(ImageData imageData){
		Vol vol=createVol(imageData.getWidth(),imageData.getHeight(),3,0);
		for(int y=0;y<imageData.getHeight();y++){
			for(int x=0;x<imageData.getWidth();x++){
				int value=(int) (0.299*imageData.getRedAt(x, y) + 0.587*imageData.getGreenAt(x, y) + 0.114*imageData.getBlueAt(x, y));
				vol.set(x, y, 0, value);
			}
		}
		return vol;
	}
	public static Vol createGrayVolFromGrayScaleImage(ImageData imageData){
		Vol vol=createVol(imageData.getWidth(),imageData.getHeight(),3,0);
		for(int y=0;y<imageData.getHeight();y++){
			for(int x=0;x<imageData.getWidth();x++){
				int value=imageData.getRedAt(x, y);
				vol.set(x, y, 0, value);
			}
		}
		return vol;
	}

	
	public static  final native Trainer createTrainer(Net net,int batchsize) /*-{
	return new $wnd.convnetjs.SGDTrainer(net, {method:'adadelta', batch_size:batchsize, l2_decay:0.0001});
	 }-*/;
	

	public static  final native Trainer createTrainer2(Net net) /*-{
	return new $wnd.convnetjs.SGDTrainer(net, {learning_rate:0.01, momentum:0.1, batch_size:10, l2_decay:0.001});
	 }-*/;

}
