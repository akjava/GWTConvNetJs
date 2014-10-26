package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.akjava.gwt.comvnetjs.client.GWTConvNetJs.MakeRectResult;
import com.akjava.gwt.comvnetjs.client.worker.MakeRectParam;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.Uint8Array;
import com.akjava.gwt.jszip.client.JSFile;
import com.akjava.gwt.lib.client.Base64Utils;
import com.akjava.gwt.lib.client.BrowserUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.BrowserUtils.LoadBinaryListener;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ResizeUtils;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageData;
import com.akjava.gwt.webworker.client.Worker2;
import com.akjava.gwt.webworker.client.WorkerPool;
import com.akjava.gwt.webworker.client.WorkerPool.Uint8WorkerPoolData;
import com.akjava.gwt.webworker.client.WorkerPool.WorkerPoolData;
import com.akjava.gwt.webworker.client.WorkerPoolMultiCaller;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.ListUtils;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.Window;
import com.google.gwt.webworker.client.MessageEvent;

public class NegativeControler {
	private boolean useRandomNegative;
	private boolean useHorizontalFlip;//only work on non-random mode ,should better use later stage
	public boolean isUseHorizontalFlip() {
		return useHorizontalFlip;
	}

	public void setUseHorizontalFlip(boolean useHorizontalFlip) {
		this.useHorizontalFlip = useHorizontalFlip;
	}

	public boolean isUseRandomNegative() {
		return useRandomNegative;
	}

	public void setUseRandomNegative(boolean useRandomNegative) {
		this.useRandomNegative = useRandomNegative;
	}

	public static interface HasDetectSize{
		public int getDetectWidth();
		public int getDetectHeight();
	}
	
	private HasDetectSize hasDetectSize;
	public NegativeControler(HasDetectSize hasDetectSize){
		this.hasDetectSize=hasDetectSize;
	}
	private CVImageZip negativesZip;
	
	public CVImageZip getNegativesZip() {
		return negativesZip;
	}
	private Map<String,List<Rect>> rectsMap=new HashMap<String, List<Rect>>();
	
	/**
	 * @deprecated
	 */
	public void loadNegativeZip(final int minW,final int minH) {
		final String negativeImageName="neg_eye_paint_face.zip";//bg2 is face up
		//final String negativeImageName="neg_eye_clip.zip";//bg2 is face up
//final String negativeImageName="bg2.zip";//bg2 is face up
//final String negativeImageName="clipbg.zip";//"nose-inpainted-faces.zip"
BrowserUtils.loadBinaryFile(negativeImageName,new LoadBinaryListener() {
	
	





	@Override
	public void onLoadBinaryFile(ArrayBuffer buffer) {
		
		final Stopwatch watch=Stopwatch.createStarted();
	
		negativesZip=new CVImageZip(buffer);
		negativesZip.setUseCache(true);
		negativesZip.setName(negativeImageName);
		negativesZip.shuffle();//negative file need shuffle?
		checkState(negativesZip.size()>0,"some how empty zip or index/bg");
		
		LogUtils.log("pre-extract-time:"+watch.elapsed(TimeUnit.SECONDS)+"s");
		watch.reset();watch.start();
		final List<CVImageData> datas=Lists.newArrayList(negativesZip.getDatas());
		
		
		if(negativesZip.getType()==CVImageZip.POSITIVES){
			List<CVImageData> invalidEmptyDatas=Lists.newArrayList();
			//has rect
			for(CVImageData data:negativesZip.getDatas()){
				if(data.getRects()!=null && data.getRects().size()>0){
					List<Rect> rects=Lists.newArrayList(data.getRects());
					rectsMap.put(data.getFileName(), ListUtils.shuffle(rects));//put rect directly
				}else{
					LogUtils.log("empty rects on negative zip removed:"+data.getFileName());
					invalidEmptyDatas.add(data);
				}
			}
			negativesZip.getDatas().removeAll(invalidEmptyDatas);
			LogUtils.log(getNegativeInfo());
			LogUtils.log("load negatives with rects from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
			
		}else{
		
		WorkerPool workerPool=new WorkerPool(24,"/workers/uint8tobase64.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				String base64=event.getDataAsString();
				extracted++;
				
				
				
				for(FileType type:FileType.getFileTypeFromFileName(data.getParameterString("name")).asSet()){
					String dataUrl=Base64Utils.toDataUrl(type.getMimeType(),base64);
					negativesZip.putCache(data.getParameterString("name"), dataUrl);
					
					//how to catch all data extracted.
					if(extracted==negativesZip.getDatas().size()){
						//createNegativeRectangles();//this way slow,but less freeze time
						LogUtils.log(getNegativeInfo());
						LogUtils.log("load negatives-image-only from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					}
					
					return;
				}
				
				LogUtils.log("invalid name for convert base64 imagee:"+data.getParameterString("name"));
				
			}
		};
		
		WorkerPoolMultiCaller<CVImageData> multiCaller=new WorkerPoolMultiCaller<CVImageData>(workerPool,datas) {

			@Override
			public WorkerPoolData convertToData(CVImageData data) {
				JSFile file=negativesZip.getZip().getFile(data.getFileName());
				Uint8Array array=file.asUint8Array();
				Uint8WorkerPoolData poolData= new Uint8WorkerPoolData(array.convertToNative(),false);
				poolData.setParameterString("name", data.getFileName());
				return poolData;
			}
			
		};
		multiCaller.start(10);
		}
		
		/*
		AsyncMultiCaller<CVImageData> preloader=new AsyncMultiCaller<CVImageData>(datas) {

			@Override
			public void doFinally(boolean cancelled) {
				LogUtils.log(getNegativeInfo());
				
				LogUtils.log("load negatives from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
				
			}

			@Override
			public void execAsync(CVImageData data) {
				negativesZip.getImageElement(data);
				done(data,true);
			}
		};
		preloader.startCall(1);
		*/
		
	}
	
	@Override
	public void onFaild(int states, String statesText) {
		LogUtils.log(states+","+statesText);
	}
});
		
	}
	
	/*
	 * load all image-element if cache
	 * create all rect if not exist
	 */
	public String getNegativeInfo(){
		Stopwatch watch=Stopwatch.createUnstarted();
		int totalRect=0;
		if(negativesZip==null){
			return "";//possible call before negative zip loaded;
		}
		int minW=hasDetectSize.getDetectWidth();
		int minH=hasDetectSize.getDetectHeight();
		for(CVImageData data:negativesZip.getDatas()){
			for(ImageElement image:negativesZip.getImageElement(data).asSet()){
				watch.start();
			List<Rect> rects=loadRect(image, data.getFileName(),minW, minH);
			watch.stop();
			totalRect+=rects.size();
			}
		}
		//LogUtils.log();

		return "negative-info:remain "+totalRect+" rects of "+negativesZip.getDatas().size()+" images"+" rect-generate-time:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms";
	}
	
	/*
	 * int minW=detectWidth.getValue();
			int minH=detectHeight.getValue();
	 */
	@SuppressWarnings("unchecked")
	public List<Rect> loadRect(ImageElement image, String fileName,int minW,int minH) {
		
		
		List<Rect> rects=rectsMap.get(fileName);
		if(rects==null){
			
			double min_scale=1.2;//no need really small pixel
			
			rects=RectGenerator.generateRect(image.getWidth(),image.getHeight(), 4, 1.4,minW,minH,min_scale);
			//rects=generateRect(image, 2, 1.2);//this is too much make rects
			rectsMap.put(fileName, ListUtils.shuffle(rects));
		}
		return rects;
	}
	

	//some how this is slow
	public void createNegativeRectangles(final int minW,final int minH){
		final Stopwatch watch=Stopwatch.createStarted();
		WorkerPool workerPool=new WorkerPool(4,"/makerect/worker.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				MakeRectResult result=Worker2.getDataAsJavaScriptObject(event).cast();
				extracted++;
				
				List<Rect> rects=Lists.newArrayList();
				for(int i=0;i<result.getRects().length();i++){
					Rect rect=Rect.fromString(result.getRects().get(i));
					rects.add(rect);
				}
				
				
				rectsMap.put(result.getName(), ListUtils.shuffle(rects));
				
				if(extracted==negativesZip.getDatas().size()){
					LogUtils.log(getNegativeInfo());
					LogUtils.log("preload rectangles "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
				}
				
				
			}
		};
		
		final List<CVImageData> datas=Lists.newArrayList(negativesZip.getDatas());
		
		final double min_scale=1.2;//no need really small pixel
		
		WorkerPoolMultiCaller<CVImageData> multiCaller=new WorkerPoolMultiCaller<CVImageData>(workerPool,datas) {

			@Override
			public WorkerPoolData convertToData(final CVImageData data) {
				Optional<ImageElement> optional=negativesZip.getImageElement(data);
				for(final ImageElement image:optional.asSet()){
					WorkerPoolData wdata=new WorkerPoolData(){

						@Override
						public String getParameterString(String key) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public void postData(Worker2 worker) {
							worker.postMessage(MakeRectParam.create(data.getFileName(), image.getWidth(), image.getHeight(), 4, 1.4,minW,minH,min_scale));
						}
						
					};
					return wdata;
				}
				//never happen todo support something
				return null;
			}
			
		};
		multiCaller.start(1);
		
	
	}
	
	
	public void loadNegativeZip(File file, Uint8Array array,final int minW,final int minH) {
		rectsMap.clear();//zip replaced;
		//negativeZipLabel.setText(file.getFileName());
		final Stopwatch watch=Stopwatch.createStarted();
		
		negativesZip=new CVImageZip(array);
		negativesZip.setUseCache(true);
		negativesZip.setName(file.getFileName());
		negativesZip.shuffle();//negative file need shuffle?
		checkState(negativesZip.size()>0,"some how empty zip or index/bg");
		
		LogUtils.log("pre-extract-time:"+watch.elapsed(TimeUnit.SECONDS)+"s");
		watch.reset();watch.start();
		
		if(negativesZip.getType()==CVImageZip.POSITIVES){
			List<CVImageData> invalidEmptyDatas=Lists.newArrayList();
			//has rect
			for(CVImageData data:negativesZip.getDatas()){
				if(data.getRects()!=null && data.getRects().size()>0){
					List<Rect> rects=Lists.newArrayList(data.getRects());
					rectsMap.put(data.getFileName(), ListUtils.shuffle(rects));//put rect directly
				}else{
					LogUtils.log("empty rects on negative zip removed:"+data.getFileName());
					invalidEmptyDatas.add(data);
				}
			}
			negativesZip.getDatas().removeAll(invalidEmptyDatas);
			LogUtils.log(getNegativeInfo());
			LogUtils.log("load negatives with rects from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
			
		}else{
		
		
		
		final List<CVImageData> datas=Lists.newArrayList(negativesZip.getDatas());
		
		
		WorkerPool workerPool=new WorkerPool(4,"/workers/uint8tobase64.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				String base64=event.getDataAsString();
				extracted++;
				
				
				
				for(FileType type:FileType.getFileTypeFromFileName(data.getParameterString("name")).asSet()){
					String dataUrl=Base64Utils.toDataUrl(type.getMimeType(),base64);
					negativesZip.putCache(data.getParameterString("name"), dataUrl);
					
					//how to catch all data extracted.
					if(extracted==negativesZip.getDatas().size()){
						//createNegativeRectangles();//this way slow,but less freeze time
						LogUtils.log(getNegativeInfo());
						LogUtils.log("load negatives-image-only from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					}
					
					return;
				}
				
				LogUtils.log("invalid name for convert base64 imagee:"+data.getParameterString("name"));
				
			}
		};
		
		WorkerPoolMultiCaller<CVImageData> multiCaller=new WorkerPoolMultiCaller<CVImageData>(workerPool,datas) {

			@Override
			public WorkerPoolData convertToData(CVImageData data) {
				JSFile file=negativesZip.getZip().getFile(data.getFileName());
				Uint8Array array=file.asUint8Array();
				Uint8WorkerPoolData poolData= new Uint8WorkerPoolData(array.convertToNative(),false);
				poolData.setParameterString("name", data.getFileName());
				return poolData;
			}
			
		};
		multiCaller.start(10);
		}
		
	}
	public CVImageData getCVImageData(int index){
		return negativesZip.get(index);
	}
	
	public int size(){
		return negativesZip.size();
	}
	

	Canvas sharedCanvas = Canvas.createIfSupported();
	
		public Optional<Vol> createRandomVol(){
			if(size()==0){
				LogUtils.log("negativeZip is empty");
				return Optional.absent();
			}
			final CVImageZip negativesZip=getNegativesZip();
			int detectWidth=hasDetectSize.getDetectWidth();
			int detectHeight=hasDetectSize.getDetectHeight();
			for(int j=0;j<20;j++){ //just check image exist
		//		Stopwatch watch1=Stopwatch.createStarted();
			int index=isUseRandomNegative()?getRandom(0, size()):0;
			
			CVImageData pdata=getCVImageData(index);
			
			
			
			//String extension=FileNames.getExtension(pdata.getFileName());
			//FileType type=FileType.getFileTypeByExtension(extension);//need to know jpeg or png
			//byte[] bt=imgFile.asUint8Array().toByteArray();
			//this action kill 
			
			ImageData imageData=null;
			//ImageElement negativeImage=null;
		//	dtime1+=watch1.elapsed(TimeUnit.MILLISECONDS);
			//Stopwatch watch2=Stopwatch.createStarted();
			if(pdata!=lastData){
			Optional<ImageElement> optional=negativesZip.getImageElement(pdata);
			if(!optional.isPresent()){
				LogUtils.log("skip.image not found in zip(or filter faild):"+pdata.getFileName()+" of "+negativesZip.getName());
				continue;
			}
			ImageElement image=optional.get();
			ImageElementUtils.copytoCanvas(image, sharedCanvas);
			imageData=sharedCanvas.getContext2d().getImageData(0, 0, sharedCanvas.getCoordinateSpaceWidth(), sharedCanvas.getCoordinateSpaceHeight());
			
			}else{
				imageData=lastImageData;
			}
			
			lastData=pdata;
			lastImageData=imageData;
		//	dtime2+=watch2.elapsed(TimeUnit.MILLISECONDS);
			//Stopwatch watch3=Stopwatch.createStarted();
			//rect must be initialized when load
			List<Rect> rects=loadRect(null,pdata.getFileName(),detectWidth,detectHeight);
			Rect rect=rects.remove(0);
			if(rects.size()==0){
					negativesZip.getDatas().remove(pdata);//remove permanently,if neee use,do refrash page
					if(negativesZip.getType()!=CVImageZip.POSITIVES){
						LogUtils.log("rect is empty removed:"+pdata.getFileName()+","+getNegativeInfo());
					}
			
			}
			//dtime3+=watch3.elapsed(TimeUnit.MILLISECONDS);
			
			//Stopwatch watch=Stopwatch.createStarted();
			Uint8ArrayNative resized=ResizeUtils.resizeBilinearRedOnly(imageData,rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight(), GWTConvNetJs.netWidth+GWTConvNetJs.edgeSize,GWTConvNetJs.netHeight+GWTConvNetJs.edgeSize);
			
			
			int[] binaryPattern=GWTConvNetJs.createLBPDepthFromUint8ArrayPacked(resized, false);
			
			Vol vol=GWTConvNetJs.createVolFromIndexes(binaryPattern,GWTConvNetJs.parseMaxLBPValue());
			
			//dtime+=watch.elapsed(TimeUnit.MILLISECONDS);
			
				return Optional.of(vol);
			
			}
			return Optional.absent();
		}
		private ImageData lastImageData;
		private CVImageData lastData;
	
		private int getRandom(int min,int max){
			return (int) (min+(Math.random()*(max-min)));
		}
		
		/**
		 * do single action
		 * @param cascadeNet
		 * @return
		 */
		public Optional<Vol> getPossiblePassedRandomVol(CascadeNet cascadeNet){
			Optional<Vol> randomVol=createRandomVol();//random or orderd
			for(Vol vol:randomVol.asSet()){
				Optional<Vol> optional=cascadeNet.filterParents(vol);
				if(optional.isPresent()){
					return optional;
				}else{
					if(useHorizontalFlip){
						Vol flipped=GWTConvNetJs.convertToHorizontalVol(vol);
						Optional<Vol> flippedOptional=cascadeNet.filterParents(flipped);
						if(flippedOptional.isPresent()){
							//LogUtils.log("flipped-find");
							return flippedOptional;
						}
					}
					//TODO support truning
				}
			}
			return Optional.absent();
		}
		
		public Optional<Vol> createPassedRandomVol(CascadeNet cascadeNet){
			
			Vol passedParentFilter=null;
			
			//int maxError=10000000;
			//int error=0;
			
			while(passedParentFilter==null){
				Optional<Vol> optional2=createRandomVol();
				if(!optional2.isPresent()){//usually never faild creating randon vol
					return Optional.absent();
				}
				Vol vol=optional2.get();
			
			//LogUtils.log(vol);
			
			Optional<Vol> optional=cascadeNet.filterParents(vol);
			if(optional.isPresent()){
				passedParentFilter=optional.get();
				}
			else{
				if(useHorizontalFlip){
					Vol converted=GWTConvNetJs.convertToHorizontalVol(vol);
					Optional<Vol> horizontalPassed=cascadeNet.filterParents(converted);
					if(horizontalPassed.isPresent()){
						passedParentFilter=horizontalPassed.get();
					}
				}
			}
			if(size()==0){
				Window.alert("No more Negative Images return null");
				break;
			}
			/*
			 * stop using max limi,because 
			error++;
			if(error>maxError){
				Window.alert("critical error-not found image");
				break;
			}
			*/
			
			}
			return Optional.of(passedParentFilter);
		}
	
}
