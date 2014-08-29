package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.akjava.gwt.html5.client.download.HTML5Download;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataURLListener;
import com.akjava.gwt.jszip.client.JSFile;
import com.akjava.gwt.jszip.client.JSZip;
import com.akjava.gwt.lib.client.Base64Utils;
import com.akjava.gwt.lib.client.BrowserUtils;
import com.akjava.gwt.lib.client.BrowserUtils.LoadBinaryListener;
import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ExecuteButton;
import com.akjava.gwt.lib.client.experimental.RectCanvasUtils;
import com.akjava.gwt.lib.client.experimental.lbp.ByteImageDataIntConverter;
import com.akjava.gwt.lib.client.experimental.lbp.SimpleLBP;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageeData;
import com.akjava.gwt.lib.client.widget.PanelUtils;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.ColorUtils;
import com.akjava.lib.common.utils.FileNames;
import com.akjava.lib.common.utils.ListUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GWTConvNetJs implements EntryPoint {
	//private List<CVImageeData> positiveDatas;
	//private List<CVImageeData> positiveImagesData;//posimages.zip (clipped)
	private JSZip loadedZip;
	
	//private JSZip posImageZip;
	private int trained;
	//private Trainer trainer;
	private Canvas sharedCanvas;
	//private Net net;
	
	
	private HorizontalPanel successNegativesPanel;
	private HorizontalPanel faildNegativePanel;
	
	//private List<CVImageeData> testDatas=new ArrayList<CVImageeData>();
	//private List<Vol> trainedPositiveDatas=new ArrayList<Vol>();
	private List<PassedData> trainedPositiveDatas=new ArrayList<GWTConvNetJs.PassedData>();
	private HorizontalPanel faildPosPanel;
	
	private Canvas anotherCanvas;
	
	private List<CascadeNet> cascades=Lists.newArrayList();
	private ExecuteButton trainSecondBt;
	
	private CVImageZip negativesZip;
	private CVImageZip positivesZip;
	final int classNumber=2;
	
	private String lastJson;
	
	private CascadeNet dummyCascade;
	private void doUndo(){
		fromJson(lastJson);
	}
	
	private static Canvas lbpCanvas=Canvas.createIfSupported();//
	
	public class LBPImageZip extends CVImageZip{
		private ByteImageDataIntConverter converter=new ByteImageDataIntConverter(lbpCanvas.getContext2d(),true);
	
		
		public LBPImageZip(ArrayBuffer buffer) {
			super(buffer);
			
		}
		
		protected ImageElement doFilter(ImageElement image) {
			ImageElementUtils.copytoCanvas(image, lbpCanvas);
			
			//PROPOSE ImageDataUtils.createFrom(Canvas);
			int[][] bytes=converter.convert(lbpCanvas.getContext2d().getImageData(0, 0, lbpCanvas.getCoordinateSpaceWidth(), lbpCanvas.getCoordinateSpaceHeight()));
			
			ImageData imageData=converter.reverse().convert(lbpConverter.convert(bytes));
			CanvasUtils.createCanvas(lbpCanvas, imageData);
			
			//PROPOSE ImageElementUtils.createPngFrom(Canvas);
			return ImageElementUtils.create(lbpCanvas.toDataUrl());
		}
		
	}
	
	public void onModuleLoad() {
		
		DockLayoutPanel dockRoot=new DockLayoutPanel(Unit.PX);
		RootLayoutPanel.get().add(dockRoot);
		
		
		
		ScrollPanel scroll=new ScrollPanel();
		dockRoot.addSouth(scroll, 250);
		final VerticalPanel result=new VerticalPanel();
		scroll.add(result);
		scroll.setSize("100%", "100%");
		
		dummyCascade=new CascadeNet(null, createNewNet());
		
		
		final VerticalPanel root=PanelUtils.createScrolledVerticalPanel(dockRoot);
		
		
		
		anotherCanvas=Canvas.createIfSupported();
		HorizontalPanel h=new HorizontalPanel();
		h.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		
		FileUploadForm imageUpload= FileUtils.createSingleFileUploadForm(new DataURLListener() {
			
			@Override
			public void uploaded(File file, String text) {
				
				ImageElementUtils.copytoCanvas(text, anotherCanvas);
				Timer timer=new Timer(){
					@Override
					public void run() {
						//detectImage(anotherCanvas,8,2.4);//1.2 is default (1-16,1.2 - 3.6)
						//detectImage(anotherCanvas,4,1.6);
						getLastCascade().setMinRate(minRateBox.getValue());
						detectImage(anotherCanvas,6,1.8);
						
						
					}};
				timer.schedule(100);
				//
			}
		}, true);
		imageUpload.setAccept("image/*");
		root.add(new Label("detect-canvas"));
		root.add(anotherCanvas);
		root.add(h);
		h.add(new Label("select image(after that detection start automatic) recommend around 250x250.otherwise take too mauch time"));
		h.add(imageUpload);
		
		
		
	
		
		sharedCanvas = Canvas.createIfSupported();
		
		
		Button undo=new Button("Undo",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(lastJson==null){
					Window.alert("nothing to undo");
					return;
				}
				doUndo();
			}
		});
		root.add(undo);
		
		analyzeBt = new ExecuteButton("Train Negative at last-cascades 10"){

			@Override
			public void executeOnClick() {
				lastJson=toJson();
				startTraining(10);
			}
			
			
		};
		root.add(analyzeBt);
		
		ExecuteButton bt5 = new ExecuteButton("Train Negative at last-cascades 5"){

			@Override
			public void executeOnClick() {
				lastJson=toJson();
				startTraining(5);
			}
			
			
		};
		root.add(bt5);
		
		ExecuteButton bt1 = new ExecuteButton("Train Negative at last-cascades 1"){

			@Override
			public void executeOnClick() {
				lastJson=toJson();
				startTraining(1);
			}
			
			
		};
		root.add(bt1);
		
		trainSecondBt = new ExecuteButton("Add New Cascade & Train Positive & Negative(step by step it'take extreme time to train)"){

			@Override
			public void executeOnClick() {
				CascadeNet next=new CascadeNet(cascades.get(cascades.size()-1),createNewNet(),null);
				cascades.add(next);
				
				//train last one
				trainBoth(cascades.get(cascades.size()-1));
				passedVols.clear();
			}
			
			
		};
		//root.add(trainSecondBt);
		
		ExecuteButton trainPositiveBt = new ExecuteButton("Add New Cascade & Train Positive"){

			@Override
			public void executeOnClick() {
				CascadeNet next=new CascadeNet(cascades.get(cascades.size()-1),createNewNet(),null);
				cascades.add(next);
				
				trainPositiveOnly();
				passedVols.clear();
			}
			
			
		};
		//root.add(trainPositiveBt);
		
		ExecuteButton addBt = new ExecuteButton("Add New Cascade"){

			@Override
			public void executeOnClick() {
				//handle min-rate first
				double minRate=minRateBox.getValue();
				minrateValues.add(minRate);
				
				getLastCascade().setMinRate(minRate);
				
				minRateBox.setValue(0.5);
				
				
				CascadeNet next=new CascadeNet(cascades.get(cascades.size()-1),createNewNet(),null);
				cascades.add(next);
				
				passedVols.clear();
				//trainPositiveOnly();
				droppedList.addAll(temporalyIgnoreList);
				temporalyIgnoreList.clear();
				
				
			}
			
			
		};
		root.add(addBt);
		
		trainSecondBt.setEnabled(false);
		//analyzeBt.setEnabled(false);
		
		/*
		root.add(new Label("upload net(@deprecated) use upload cascades"));
		netUploadBt = FileUtils.createSingleTextFileUploadForm(new DataURLListener() {
			
			@Override
			public void uploaded(File file, String text) {
				cascades.clear();
				net=Net.fromJsonString(text);
				trainer = ConvnetJs.createTrainer(net,1);//need recreate
				
				cascades.add(new CascadeNet(null, net,trainer));//root
				
				analyzeBt.setEnabled(true);
				
				//create new because can't create that way.
				
				trainSecondBt.setEnabled(true);
			}
		}, true);
		
		//root.add(netUploadBt);
		netUploadBt.setAccept(Lists.newArrayList(".json",".js"));
		netUploadBt.setEnabled(false);
		*/
		
		root.add(new Label("load cascades(initially first-cascade.json: loades)"));
		FileUploadForm cascadeUploadBt = FileUtils.createSingleTextFileUploadForm(new DataURLListener() {
			
			@Override
			public void uploaded(File file, String text) {
				fromJson(text);
				
				
			}
		}, true);
		root.add(cascadeUploadBt);
		cascadeUploadBt.setAccept(Lists.newArrayList(".json",".js"));
		
		
		//maybe you can replace cascades4 or something already cascaded
		try {
			new RequestBuilder(RequestBuilder.GET,"first-cascade.json").sendRequest(null, new RequestCallback() {
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					//same upload net;
					Net net=Net.fromJsonString(response.getText());
					Trainer trainer = ConvnetJs.createTrainer(net,1);//need recreate
					
					cascades.add(new CascadeNet(null, net,trainer));//root
					
					analyzeBt.setEnabled(true);
					
					
					trainSecondBt.setEnabled(true);
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					LogUtils.log(exception.getMessage());
				}
			});
		} catch (RequestException e) {
			LogUtils.log(e.getMessage());
			e.printStackTrace();
		}
		
		
		Button bt=new Button("Do Test pos & neg(First cascade) maybe deprecated?",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doTest();
			}
		});
		//root.add(bt);
		
		//test second cascade pass normally
Button bt2=new Button("Do Test Last Cascade first 100 item(quick but not real vol)",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				LogUtils.log("cascade:"+cascades.size());
				doTestCascade(cascades.get(cascades.size()-1));
			}
		});
		//root.add(bt2);

	List<Double> doubles=Lists.newArrayList();
	for(double i=750;i>=250;i-=25){
		doubles.add(i/1000);
	}
	minRateBox = new ValueListBox<Double>(new  Renderer<Double>() {

		@Override
		public String render(Double object) {
			String v=object.toString();
			return v.substring(0,Math.min(v.length(), 4));
		}

		@Override
		public void render(Double object, Appendable appendable) throws IOException {
			// TODO Auto-generated method stub
			
		}
	});
	minRateBox.setValue(0.5);
	minRateBox.setAcceptableValues(doubles);
	root.add(minRateBox);

Button bt2b=new Button("Do Test Last Cascade first 100 item Passed parent filters Vol",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				LogUtils.log("cascade:"+cascades.size());
				doTestCascadeReal(getLastCascade());
			}
		});
		root.add(bt2b);
		
		
Button btSecond=new Button("Do Test2(all positive datas) at Last-Cascade",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doTestAllPositivesAtLastCascade();
			}
		});
		//root.add(btSecond);
		
Button bt3=new Button("Do Test All(can only train all)",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doTest3();
			}
		});
		root.add(bt3);
		
		/*
Button saveBt=new Button("Save",new ClickHandler() {//deprecated for first 
			
			@Override
			public void onClick(ClickEvent event) {
				String json=net.toJsonString();
				Anchor downloadJson=HTML5Download.get().generateTextDownloadLink(json, "trained.json", "download net",true);
				root.add(downloadJson);
			}
		});
		*/
		//root.add(saveBt);
		
Button saveAllBt=new Button("Save All cascades",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String json=toJson();
				
				Anchor downloadJson=HTML5Download.get().generateTextDownloadLink(json, "cascades.json", "download cascades",true);
				root.add(downloadJson);
			}
		});
		root.add(saveAllBt);
		
		/**
		 * besically this called after load initial-jsons this means at least you train twice same positives
		 */
		final ListBox list=new ListBox();
		for(int i=1;i<=30;i++){
			list.addItem(String.valueOf(i));
		}
		list.setSelectedIndex(1);
		root.add(list);
		
		ExecuteButton trainPositiveRoot=new ExecuteButton("Train Positive & Negative Last cascade from initial"){

			@Override
			public void executeOnClick() {
				getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,true);
				doTest3();
			}
			
		};
			
		root.add(trainPositiveRoot);
		
		ExecuteButton trainPositiveRoot2=new ExecuteButton("Train Positive & Negative Last cascade continue"){

			@Override
			public void executeOnClick() {
				getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,false);
				doTest3();
				continueIndex++;
			}
			
		};
			
		root.add(trainPositiveRoot2);
		
		
		ExecuteButton trainPositive=new ExecuteButton("Train last-cascade Positive Only"){//maybe not useful

			@Override
			public void executeOnClick() {
				trainPositiveOnly();
			}
			
		};
			
		//root.add(trainPositive);
		
		//final String positiveImageName="opencv-pos-images_all.zip";
		
		//maybe it's better to use lbp-cropped when export images.
		
		final String positiveImageName="posimages.zip";
		final boolean isCroppedImage=true;
		//posimages.zip is cropped positive images around 1200-2500
BrowserUtils.loadBinaryFile(positiveImageName,new LoadBinaryListener() {
	
			

			@Override
			public void onLoadBinaryFile(ArrayBuffer buffer) {
				
				Stopwatch watch=Stopwatch.createStarted();
				positivesZip=new CVImageZip(buffer);
				positivesZip.setName(positiveImageName);
				
				checkState(positivesZip.size()>0,"info.txt or info.dat is empty");
				
				positivesZip.shuffle();
				
				//cropped image need slash edges.
				if(isCroppedImage){
				for(CVImageeData data:positivesZip.getDatas()){
					for(Rect rect:data.getRects()){
					//	LogUtils.log("before:"+rect.toString());
						
						//now stop convert lbp here
						//rect.expand(-2, -2).copyTo(rect);//because LBP 1 neibors has problem
						
						//this already too much lost image info.
						
					//	LogUtils.log("after:"+rect.toString());
					}
				}
				}
				
				//loadZip();//need this
				
				
				LogUtils.log("posimages load times:"+positivesZip.size()+" items,"+watch.elapsed(TimeUnit.SECONDS)+"s");
				
			}
			
			@Override
			public void onFaild(int states, String statesText) {
				LogUtils.log(states+","+statesText);
			}
		});

final String negativeImageName="bg2.zip";//bg2 is face up
//final String negativeImageName="clipbg.zip";//"nose-inpainted-faces.zip"
BrowserUtils.loadBinaryFile(negativeImageName,new LoadBinaryListener() {
	
	




	@Override
	public void onLoadBinaryFile(ArrayBuffer buffer) {
		
		Stopwatch watch=Stopwatch.createStarted();
	
		negativesZip=new CVImageZip(buffer);
		negativesZip.setName(negativeImageName);
		negativesZip.shuffle();//negative file need shuffle?
		checkState(negativesZip.size()>0,"some how empty zip or index/bg");
		
		LogUtils.log("load negatives datas items="+negativesZip.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
	}
	
	@Override
	public void onFaild(int states, String statesText) {
		LogUtils.log(states+","+statesText);
	}
});
		
		
		
		

		
		
		/*
		result.setSize("100%", "100%");
		ScrollPanel scroll=new ScrollPanel(result);
		scroll.setSize("800px", "400px");
		root.add(scroll);
		*/
		
		result.add(new Label("success-positive"));
		successPosPanel = new HorizontalPanel();
		result.add(successPosPanel);
		
		HorizontalPanel fpPanel=new HorizontalPanel();fpPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		result.add(fpPanel);
		fpPanel.add(new Label("Faild-positive"));
		ExecuteButton retrainFaildPos=new ExecuteButton("Retrain") {
			@Override
			public void executeOnClick() {
				retrainFaildPositives();
				doTest3();
			}
		};
		fpPanel.add(retrainFaildPos);
		
		faildPosPanel = new HorizontalPanel();
		result.add(faildPosPanel);
		
		result.add(new Label("success-negative"));
		successNegativesPanel = new HorizontalPanel();
		result.add(successNegativesPanel);
		result.add(new Label("Faild-Negative"));
		faildNegativePanel = new HorizontalPanel();
		result.add(faildNegativePanel);
		
		/*
		FileUploadForm cifar10Upload=FileUtils.createSingleFileUploadForm(new DataArrayListener() {
			
			@Override
			public void uploaded(File file, Uint8Array array) {
				
			}
		}, true, false);
		
		root.add(cifar10Upload);
		*/
		
		Button test=new Button("Test",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				for(int i=0;i<10;i++){
					Vol v=createRandomVol().get();
					Vol result=getLastCascadesNet().forward(v);
					LogUtils.log(result.getW(0)+","+result.getW(1));
					
				}
				/*
				for(int i=0;i<negativesZip.size();i++){
				CVImageeData pdata=negativesZip.getDatas().get(i);
				
				Optional<ImageElement> negativeImage=negativesZip.getImageElement(pdata);
				if(!negativeImage.isPresent()){
					LogUtils.log("accidently image not contain:"+pdata.getFileName());
					continue;
				}
				
				ImageElement testImage=negativeImage.get();
				LogUtils.log(testImage.getWidth()+"x"+testImage.getHeight());
				generateRect(testImage, 1, 1.2);
				}
				*/
				
			}
		});
		result.add(test);
	}
	
	private Set<String> droppedList=Sets.newHashSet();
	private Set<String> temporalyIgnoreList=Sets.newHashSet();
	
	
	Net createNewNet(){
		
		//return ConvnetJs.createDepathNet(1,1,2, 10, 2);//i tried int[] to softmax,but faild
		
		return ConvnetJs.createGrayImageNet(24, 24, classNumber);
		
	}
	
	
	Rect zeroRect=new Rect(0,0,0,0);
	List<Rect> continuRect=Lists.newArrayList(new Rect(1,0,-1,0),new Rect(0,1,0,-1),new Rect(0,0,-1,0),new Rect(0,0,0,-1),new Rect(1,0,-2,0),new Rect(0,1,0,-2));
	int continueIndex;
	protected void doTrain(int rate,boolean initial) {

		
		
		trainedPositiveDatas.clear();
		Stopwatch watch=Stopwatch.createStarted();
		int trained=0;
		int negative=0;
		
		int ignored=0;
		Rect offsetRect=null;
		
		if(initial){
		continueIndex=0;	
		Net net = createNewNet();
		Trainer trainer = ConvnetJs.createTrainer(net,1);//batch no so effect on speed up
		
		getLastCascade().setNet(net);
		getLastCascade().setTrainer(trainer);
		offsetRect=zeroRect;
		}else{
			offsetRect=continuRect.get(continueIndex);
		}
		
		Rect changedRect=new Rect();
		for(CVImageeData pdata:positivesZip.getDatas()){
			
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				ignored++;
				//LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement baseImage=imageElementOptional.get();
			//generate canvas for image
			
			for(Rect rect:pdata.getRects()){
				String path=pdata.getFileName()+"#"+rect.toKanmaString().replace(",", "-");
				if(droppedList.contains(path)){
					//LogUtils.log("ignore-train:"+path);
					ignored++;
					continue;//already dropped data skip
				}
				
				//when continue rect a bit changed
				changedRect.set(rect.getX()+offsetRect.getX(), rect.getY()+offsetRect.getY(), rect.getWidth()+offsetRect.getWidth(), rect.getHeight()+offsetRect.getHeight());
				
				
				RectCanvasUtils.crop(baseImage, rect, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);
			
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				getLastCascade().getTrainer().train(vol, 0);
				trained++;
				trainedPositiveDatas.add(new PassedData(vol, path));//re-use when test
				
				if(trained%100==0){//need progress
					LogUtils.log("trained-positive:"+trained);
				}
			
				int m=trained%rate;
				if(m==0){
				//	LogUtils.log(trained+","+rate);
					Optional<Vol> optional=createRandomVol(getLastCascade());
					if(!optional.isPresent()){
						Window.alert("creating random vol faild maybe image problems");
						return;
					}
					
				Vol neg= optional.get();
				getLastCascade().getTrainer().train(neg, 1);
				negative++;
				
					
				}
				
				
			}
		}
		LogUtils.log("trained-positive:"+trained+ " negative="+negative+" ignored="+ignored+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
	}

	protected void fromJson(String text) {
		cascades.clear();
		
		JSONValue value=JSONParser.parseStrict(text);
		
		
		
		JSONObject object=value.isObject();
		
		//parse nets
		JSONValue netsValue=object.get("nets");
		
		JSONArray array=netsValue.isArray();
		if(array==null){
			Window.alert("seems invalid cascade-json type");
			return;
		}
		for(int i=0;i<array.size();i++){
			JSONValue arrayValue=array.get(i);
			String jsonText=arrayValue.toString();
			//Net net=Net.fromJsonString(jsonText);
			CascadeNet parent=null;
			if(cascades.size()>0){
				parent=cascades.get(cascades.size()-1);
			}
			cascades.add(new CascadeNet(parent, jsonText));
		}
		LogUtils.log("nets:"+cascades.size()+" cascades");
		//parse minrates
		minrateValues.clear();
		JSONValue minratesJSValue=object.get("minrates");
		
		JSONArray minratesArray=minratesJSValue.isArray();
		if(minratesArray==null){
			Window.alert("seems invalid cascade-json type");
			return;
		}
		for(int i=0;i<minratesArray.size();i++){
			JSONValue arrayValue=minratesArray.get(i);
			JSONNumber number=arrayValue.isNumber();
			if(i!=minratesArray.size()-1){
				minrateValues.add(number.doubleValue());
			}else{
				minRateBox.setValue(number.doubleValue());
			}
		}
		
		LogUtils.log("minrates:");
		for(double minrate:minrateValues){
			LogUtils.log(minrate);
		}
		LogUtils.log("current:"+minRateBox.getValue());
		
		//parse dropped
		droppedList.clear();
				JSONValue droppedValue=object.get("dropped");
				
				JSONArray droppedArray=droppedValue.isArray();
				if(droppedArray==null){
					Window.alert("seems invalid cascade-json type");
					return;
				}
				for(int i=0;i<droppedArray.size();i++){
					JSONValue arrayValue=droppedArray.get(i);
					JSONString string=arrayValue.isString();
					String jsonText=string.stringValue();
					
					droppedList.add(jsonText);
				}
		
				LogUtils.log("dropped:");
				for(String dropped:droppedList){
					LogUtils.log(dropped);
				}	
				
				
		passedVols.clear();
		temporalyIgnoreList.clear();
		trainedPositiveDatas.clear();		
				
	}

	List<Double> minrateValues=Lists.newArrayList();
	
	protected String toJson() {
		Joiner joiner=Joiner.on(",");
		List<String> objects=Lists.newArrayList();
		
		List<String> jsons=Lists.newArrayList();
		for(CascadeNet cascade:cascades){
			jsons.add(cascade.getNet().toJsonString());
		}
		
		String nets="\"nets\":["+Joiner.on(",").join(jsons)+"]";
		objects.add(nets);
		
		//dropped files
		
		List<String> files=Lists.newArrayList();
		for(String file:droppedList){
			files.add("\""+file+"\"");
		}
		objects.add("\"dropped\":["+joiner.join(files)+"]");
		
		List<Double> fvalues=Lists.newArrayList(minrateValues);
		fvalues.add(minRateBox.getValue());
		
		objects.add("\"minrates\":["+joiner.join(fvalues)+"]");
		
		//
		
		
		return "{"+joiner.join(objects)+"}";
	}

	

	protected void trainPositiveOnly() {
		Stopwatch watch=Stopwatch.createStarted();
		int trained=0;
		
		Net net = createNewNet();
		Trainer trainer = ConvnetJs.createTrainer(net,1);//batch no so effect on speed up
		
		getLastCascade().setNet(net);
		getLastCascade().setTrainer(trainer);
		
		for(CVImageeData pdata:positivesZip.getDatas()){
			
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement baseImage=imageElementOptional.get();
			
			//generate canvas for image
			for(Rect rect:pdata.getRects()){
				RectCanvasUtils.crop(baseImage, rect, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);
				
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				getLastCascadesTrainer().train(vol, 0);
				trained++;
			}
		}
		LogUtils.log("trained-positive:"+trained+ " "+watch.elapsed(TimeUnit.SECONDS)+"s");
	}

	//for last cascade
	protected void retrainFaildPositives() {
		Stopwatch watch=Stopwatch.createStarted();
		int trained=0;
		for(String dataUrl:faildPositivedDatas){
			
			//ImageElement baseImage=ImageElementUtils.create(dataUrl);
			
			ImageElementUtils.copytoCanvas(dataUrl, sharedCanvas);
				
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				cascades.get(cascades.size()-1).getTrainer().train(vol, 0);
				trained++;
		}
		LogUtils.log("retrained-positive:"+trained+ " "+watch.elapsed(TimeUnit.SECONDS)+"s");
	}

	//private JSZip negativeImagesZip;

	//private ArrayList<CVImageeData> negativeImagesData;
	
	 final SimpleLBP lbpConverter=new SimpleLBP(true, false);//share
	
	int count=0;
	List<ConfidenceRect> mutchRect=Lists.newArrayList();
	ByteImageDataIntConverter byteDataIntConverter=new ByteImageDataIntConverter(Canvas.createIfSupported().getContext2d(),true);
	/**
	 * 
	 * @param canvas
	 * @param stepScale original is 1 but it too take time i use large value,however detection quolity really down
	 * @param scale_factor how image size upgrade step by step
	 * 
	 * for example detection 250x250 with 1,1.2(finest) try 47861 items and take 421s
	 * 
	 * 4,1.6(so so) try:1404,mutch:38,12s 
	 */
	
	
	protected void detectImage(Canvas canvas,int stepScale,double scale_factor) {
		Stopwatch watch=Stopwatch.createStarted();
		if(cascades.size()<1){
			Window.alert("loas positive and make cascadeNets!exit.");
			return;
		}
		mutchRect.clear();
		successPosPanel.clear();
		int minW=26;//TODO change based clip
		int minH=24;
		double min_scale=1.6;//no need really small pixel
		Canvas grayscaleCanvasForDetector=Canvas.createIfSupported();
		
		
		
		
		//make method to use same Convert way
		
		//PROPOSE ImageDataUtils.createFrom(Canvas);
		//int[][] bytes=byteDataIntConverter.convert(canvas.getContext2d().getImageData(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight()));
		
		//ImageData imageData=byteDataIntConverter.reverse().convert(lbpConverter.convert(bytes));
		//CanvasUtils.createCanvas(grayscaleCanvasForDetector, imageData);
		CanvasUtils.convertToGrayScale(canvas, grayscaleCanvasForDetector);
		
		
		//grayscaleCanvas=CanvasUtils.copyTo(canvas, grayscaleCanvas, true);//test image is LBP just copy here
		//CanvasUtils.convertToGrayScale(canvas,grayscaleCanvas);//TODO convert LBP here
		
		while(minW*min_scale<canvas.getCoordinateSpaceWidth() && minH*min_scale<canvas.getCoordinateSpaceHeight()){
			detectImage(minW,minH,stepScale,min_scale,grayscaleCanvasForDetector);
			min_scale*=scale_factor;
		}
		LogUtils.log("try:"+count+",mutch:"+mutchRect.size()+","+watch.elapsed(TimeUnit.SECONDS)+"s");
		
		Collections.sort(mutchRect);
		canvas.getContext2d().setStrokeStyle("rgba(255,0,0,1)");
		
		
		//TODO group rectangles.
		
		 for(int i=0;i<mutchRect.size();i++){
			 	ConfidenceRect r=mutchRect.get(i);
			 	if(i<5){
			 		LogUtils.log(r.toKanmaString()+"="+r.getConfidence());
			 	}else{
			 		canvas.getContext2d().setStrokeStyle("#888");
			 	}
	        	RectCanvasUtils.strokeCircle(r, canvas, true);;
	        }
	}
	
	protected List<Rect> generateRect(ImageElement imageElement,int stepScale,double scale_factor) {
		Stopwatch watch=Stopwatch.createStarted();
		List<Rect> rects=Lists.newArrayList();
		int minW=26;//TODO change based clip
		int minH=24;
		double min_scale=1.6;//no need really small pixel
		
		int imageW=imageElement.getWidth();
		int imageH=imageElement.getHeight();
		
		
		while(minW*min_scale<imageW && minH*min_scale<imageH){
			generateRect(minW,minH,stepScale,min_scale,rects,imageW,imageH);
			min_scale*=scale_factor;
		}
		
		//LogUtils.log("genrate-rect-time:"+rects.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
		return rects;
	}
	
	
	
	
	
	private class ConfidenceRect extends Rect implements Comparable<ConfidenceRect>{
		private double confidence;
		public ConfidenceRect(Rect rect){
			super(rect);
		}

		public double getConfidence() {
			return confidence;
		}

		public void setConfidence(double confidence) {
			this.confidence = confidence;
		}

		@Override
		public int compareTo(ConfidenceRect o) {//large first
			return Doubles.compare( o.confidence,this.confidence);
		}
	}
	

	protected void generateRect(int minW,int minH,int stepScale,double scale,List<Rect> rects,int imageW,int imageH) {
		int clipWidth=(int) (minW*scale);
		int clipHeight=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;//usually stepScale use for reduce detect rect size.js is really slow than C-lang
        double step_y = step_x;
        
        Rect sharedRect=new Rect(0,0,clipWidth,clipHeight);
        int endX=imageW-clipWidth;
        int endY=imageH-clipHeight;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int) x;
        		int dy=(int)y;
        		sharedRect.setX(dx);
        		sharedRect.setY(dy);
        		rects.add(sharedRect.copy());
        	}
        }
        
       
	}
	
	protected void detectImage(int minW,int minH,int stepScale,double scale,Canvas imageCanvas) {
		
		int lastCascadeIndex=cascades.size()-1;
		
		int winW=(int) (minW*scale);
		int winH=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;
        double step_y = step_x;
        
        Rect sharedRect=new Rect(0,0,winW,winH);
        int endX=imageCanvas.getCoordinateSpaceWidth()-minW;
        int endY=imageCanvas.getCoordinateSpaceHeight()-minH;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int) x;
        		int dy=(int)y;
        		sharedRect.setX(dx);
        		sharedRect.setY(dy);
        		
        		RectCanvasUtils.crop(imageCanvas, sharedRect, sharedCanvas);
        		
        		//now cropped image into sharedCanvas
        		
				CanvasUtils.clear(resizedCanvas);//for transparent image
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				
				//this must extreamly slow
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				
				//Vol vol=ConvnetJs.createGrayVolFromGrayScaleImage(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				
				
				for(Vol passAll:cascades.get(lastCascadeIndex).filter(vol).asSet()){
					
					
					
					Vol passMyself=cascades.get(lastCascadeIndex).getNet().forward(passAll);
					successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
        			ConfidenceRect conf=new ConfidenceRect(sharedRect);
        			conf.setConfidence(passMyself.getW(0));
        			mutchRect.add(conf);
				}
				/*
				Vol result=net.forward(vol);
        		if(result.getW(0)>result.getW(1)){
        			successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
        			ConfidenceRect conf=new ConfidenceRect(sharedRect);
        			conf.setConfidence(result.getW(0));
        			mutchRect.add(conf);
        		}
        		*/
				count++;
        		//detect
        		
        	}
        }
        
       
	}


	/*
	private void loadZip(){
		BrowserUtils.loadBinaryFile("allposimages.zip",new LoadBinaryListener() {
			
			
			

			@Override
			public void onLoadBinaryFile(ArrayBuffer buffer) {
				Uint8Array array=Uint8Array.createUint8(buffer);
			    loadedZip = JSZip.loadFromArray(array);
				
				JSFile indexFile=loadedZip.getFile("info.txt");
				if(indexFile==null){
					indexFile=loadedZip.getFile("info.dat");//i'm windows-os user and .dat extensin used other case
				}
				
				if(indexFile==null){
					Window.alert("info.txt or info.dat name not found here.or in folder");
					return;
				}
				
				
				String text=indexFile.asText();
				
				List<String> lines=CSVUtils.splitLinesWithGuava(text);
				
				checkState(lines.size()>0,"info.txt or info.dat is empty");
				positiveDatas=FluentIterable.from(lines).transform(new CVImageDataConverter()).toList();
				//LogUtils.log("p-size:"+positiveDatas.size());
				//analyzeBt.setEnabled(true);
				
				netUploadBt.setEnabled(true);
			}
			
			@Override
			public void onFaild(int states, String statesText) {
				LogUtils.log(states+","+statesText);
			}
		});
	}
	*/
	
	
	public int getRandom(int min,int max){
		return (int) (min+(Math.random()*(max-min)));
	}
	

	int negativeCreated;
	private void trainBoth(CascadeNet cascadeNet){
		trainedPositiveDatas.clear();
		negativeCreated=0;
		Stopwatch watch=Stopwatch.createStarted();
	int trained=0;
	LogUtils.log("start last cascades:"+cascades.size()+" size="+ positivesZip.size());
	for(CVImageeData pdata:positivesZip.getDatas()){
		
		Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
		
		if(!imageElementOptional.isPresent()){
			LogUtils.log(pdata.getFileName()+" is not exist.skip");
			continue;
		}
		
		ImageElement baseImage=imageElementOptional.get();
		//generate canvas for image
		
		for(Rect rect:pdata.getRects()){
			RectCanvasUtils.crop(baseImage, rect, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
		
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			cascadeNet.getTrainer().train(vol, 0);
			trained++;
			trainedPositiveDatas.add(new PassedData(vol, pdata.getFileName()+"#"+rect.toKanmaString().replace(",", "-")));
			//LogUtils.log("trained-positive:"+trained);
			
			Optional<Vol> neg= createRandomVol(cascadeNet);//finding data is really take time
			if(!neg.isPresent()){
				Window.alert("faild training");
				return;
			}
			cascadeNet.getTrainer().train(neg.get(), 1);
			if(trained%10==0){
				LogUtils.log("trained:"+trained);
			}
			
		}
		
		if(trained==10){
		//	break; //for test break;
		}
		
	}
	LogUtils.log("trained-both:"+trained+ " "+watch.elapsed(TimeUnit.SECONDS)+"s negativeCreated="+negativeCreated);
	trainSecond=true;
	}
	
	public Optional<Vol> createRandomVol(CascadeNet cascadeNet){
		
		Vol filterd=null;
		int maxError=10000;
		int error=0;
		while(filterd==null){
			Optional<Vol> optional2=createRandomVol();
			if(!optional2.isPresent()){//usually never faild creating randon vol
				return Optional.absent();
			}
			Vol vol=optional2.get();
		negativeCreated++;
		//LogUtils.log(vol);
		
		Optional<Vol> optional=cascadeNet.filterParents(vol);
		if(optional.isPresent()){
			filterd=optional.get();
			}
		error++;
		if(error>maxError){
			break;
		}
		
		}
		return Optional.of(filterd);
	}
	
	private void startTraining(int trained,CascadeNet cascadeNet){
		LogUtils.log("start-training");
		Stopwatch stopWatch=Stopwatch.createStarted();
		Canvas resizedCanvas=CanvasUtils.createCanvas(24, 24);
		
		int negatives=0;
		int w=24;
		int h=24;
		ImageElement baseImage=null;
		CVImageeData data=null;
		int dummy=0;
		int tryingSameImage=0;
		while(negatives<trained){
			if(data==null){
				tryingSameImage=0;
			int target=(int)(Math.random()*positivesZip.size());
			data=positivesZip.get(target);
			JSFile imgFile=loadedZip.getFile(data.getFileName());
			if(imgFile==null){
				LogUtils.log("img not found:"+data.getFileName());
				continue;
			}
			
			String extension=FileNames.getExtension(data.getFileName());
			FileType type=FileType.getFileTypeByExtension(extension);
			//byte[] bt=imgFile.asUint8Array().toByteArray();
			String dataUrl=Base64Utils.toDataUrl(type.getMimeType(),imgFile.asUint8Array().toByteArray());
			baseImage=ImageElementUtils.create(dataUrl);
			}
			
			int width=baseImage.getWidth();
			int height=baseImage.getHeight();
			
			int randomWidth=getRandom(w,Math.min(24*20,width-w));
			int randomHeight=getRandom(h,Math.min(24*20,height-h));
			
			int x=getRandom(0,width-randomWidth);
			int y=getRandom(0,height-randomHeight);
			Rect r=new Rect(x,y,randomWidth,randomHeight);//more varaerty of rect
			//LogUtils.log(r);
			
			boolean safe=true;
			for(Rect rect:data.getRects()){
				//LogUtils.log(r.toString()+" vs "+rect.toString());
				if(rect.collision(r)){
					safe=false;
					break;
				}
			}
			if(safe){
				//LogUtils.log("train:"+data.getFileName()+","+r.toString());
				RectCanvasUtils.crop(baseImage, r, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				
				Optional<Vol> optional=cascadeNet.filterParents(vol);
				if(optional.isPresent()){
					Vol filterdVol=optional.get();
				//for(Vol filterdVol:cascadeNet.filterParents(vol).asSet()){
					getLastCascadesTrainer().train(filterdVol, 1);//train as negative
					negatives++;
					data=null;//change another image
				}else{
					LogUtils.log("blocked filter:"+r.toKanmaString());
				}
				
				
				//successNegativesPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				tryingSameImage++;
				if(tryingSameImage>10){
					data=null;//re select another image
				}
			}
			
			dummy++;
			
		}
		LogUtils.log("finish negatives:"+negatives+" items "+ stopWatch.elapsed(TimeUnit.SECONDS)+"s clipped="+dummy);
		stopWatch.reset();stopWatch.start();
		
		
		

		
		
	}
	
	private void startTraining(int trained){
		LogUtils.log("start-training");
		Stopwatch stopWatch=Stopwatch.createStarted();
		/*
		for(int i=0;i<max;i++){
			if(i>=positiveDatas.size()){
				break;
			}
			
			PositiveData data=positiveDatas.get(i);
			JSFile imgFile=loadedZip.getFile(data.getFileName());
			
			
			String dataUrl=null;
			if(imgFile==null){
				//dataUrl=createColorImageDataUrl(sharedCanvas,64,64,"#800");//TODO change not found
				continue;
			}else{
				//actually consume too much memory,but image is inside zip,maybe this is only way
				
				String extension=FileNames.getExtension(data.getFileName());
				FileType type=FileType.getFileTypeByExtension(extension);
				//byte[] bt=imgFile.asUint8Array().toByteArray();
				dataUrl=Base64Utils.toDataUrl(type.getMimeType(),imgFile.asUint8Array().toByteArray());//should use cache 300MB etc
				//dataUrl=Base64Utils.toDataUrl(type.getMimeType(),imgFile.asUint8Array().toByteArray());
			}
			
			ImageElement baseImage=ImageElementUtils.create(dataUrl);
			//generate canvas for image
			for(Rect rect:data.getRects()){
				RectCanvasUtils.crop(baseImage, rect, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				for(int k=0;k<1;k++){
				
				Vol vol=ConvnetJs.createGrayVol(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				trainer.train(vol, 0);
				}
				trained++;
				positivesPanel.add(new Image(resizedCanvas.toDataUrl()));
			}
			
		}
		LogUtils.log("finish positives:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
		stopWatch.reset();stopWatch.start();
		*/
		
		
		
		int negatives=0;
		while(negatives<trained){
			Optional<Vol> neg=createRandomVol(getLastCascade());
			if(!neg.isPresent()){
				Window.alert("somethin happend on creating random data");
			}
			Vol passedData=neg.get();
			getLastCascadesTrainer().train(passedData, 1);//train as negative
			negatives++;
		}
		LogUtils.log("finish negatives:"+negatives+"items "+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
		stopWatch.reset();stopWatch.start();
		
		
		

		
		
	}
	
	public void doTest3(){//train 1200 = 24s
		temporalyIgnoreList.clear();
		
		successPosPanel.clear();
		faildPosPanel.clear();
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		
		LogUtils.log("test-size:"+trainedPositiveDatas.size());
		
		//trainedPositiveDatas.add(new PassedData(vol, pdata.getFileName()+"#"+rect.toString()));
		
		for(PassedData trainedData:trainedPositiveDatas){
			
			Vol vol=trainedData.getVol();
			Vol vol2=getLastCascadesNet().forward(vol,true);
			
			if(vol2.getW(0)>minRateBox.getValue()){
			//if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				//successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				temporalyIgnoreList.add(trainedData.getDataUrl());
				faildMatch++;
				//faildPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}
			
		}
		
		
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test3:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}
	
	public CascadeNet getLastCascade(){
		return cascades.get(cascades.size()-1);
	}
	
	public Net getLastCascadesNet(){
		return cascades.get(cascades.size()-1).getNet();
	}
	public Trainer getLastCascadesTrainer(){
		return cascades.get(cascades.size()-1).getTrainer();
	}
	
	public void doTestAllPositivesAtLastCascade(){//train 1200 = 60s
		faildPositivedDatas.clear();
		successPosPanel.clear();
		faildPosPanel.clear();
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		
		LogUtils.log("test-size:"+positivesZip.size()+" at "+(cascades.size()));
		
		for(int i=0;i<positivesZip.size();i++){
			
			
			CVImageeData pdata=positivesZip.get(i);
			
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement testImage=imageElementOptional.get();
			//generate canvas for image
			Rect rect=pdata.getRects().get(0);
			RectCanvasUtils.crop(testImage, rect, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			
			Vol vol2=getLastCascadesNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				faildMatch++;
				String newDataUrl=resizedCanvas.toDataUrl();
				faildPositivedDatas.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
			}
			
		}
		
		
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}
	
	
	List<String> faildPositivedDatas=Lists.newArrayList();
	
	public void doTest(){
		faildPositivedDatas.clear();
		successPosPanel.clear();
		faildPosPanel.clear();
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		int testNumber=100;
		LogUtils.log("start-test:"+testNumber+" items");
		for(int i=0;i<testNumber;i++){
			
			if(i>=positivesZip.size()){
				break;
			}
			CVImageeData pdata=positivesZip.get(i);
			
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement testImage=imageElementOptional.get();
			
			//generate canvas for image
			Rect rect=pdata.getRects().get(0);
			RectCanvasUtils.crop(testImage, rect, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			
			Vol vol2=getLastCascadesNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				faildMatch++;
				String newDataUrl=resizedCanvas.toDataUrl();
				faildPositivedDatas.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
			}
			
		}
		
		
		
		for(int i=0;i<testNumber;i++){
			Optional<Vol> optional=createRandomVol();
			if(!optional.isPresent()){
				Window.alert("create random vol faild maybe image problems");
				return;
			}
			Vol vol=optional.get();
			Vol value= getLastCascadesNet().forward(vol);//possible null?
			if(i==0){
			//LogUtils.log(vol);
			//LogUtils.log(value);
			}
			if(value.getW(1)>value.getW(0)){
				successNegativesPanel.add(new Image(lastDataUrl));
				successMiss++;
			}else{
				faildNegativePanel.add(new Image(lastDataUrl));
				missMatch++;
			}
		}
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}
	
	boolean trainSecond;
	
	List<PassedData> passedVols=Lists.newArrayList();
	
	
	

	
	
	public class PassedData{
		private Vol vol;
		private String dataUrl;
		public Vol getVol() {
			return vol;
		}
		public void setVol(Vol vol) {
			this.vol = vol;
		}
		public String getDataUrl() {
			return dataUrl;
		}
		public void setDataUrl(String dataUrl) {
			this.dataUrl = dataUrl;
		}
		public PassedData(Vol vol,String dataUrl){
			this.vol=vol;
			this.dataUrl=dataUrl;
		}
	}
public void doTestCascadeReal(CascadeNet cascade){
		
		faildPositivedDatas.clear();
		successPosPanel.clear();
		faildPosPanel.clear();
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
	
		
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		int testNumber=100;
		
		
		double minPositiveRate=minRateBox.getValue();
		
		for(int i=0;i<testNumber;i++){
			CVImageeData pdata=positivesZip.get(i);
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement testImage=imageElementOptional.get();
			//generate canvas for image
			Rect rect=pdata.getRects().get(0);
			RectCanvasUtils.crop(testImage, rect, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			
			Vol vol2=cascade.getNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(vol2.getW(0)>minPositiveRate){
			//if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				faildMatch++;
				String newDataUrl=resizedCanvas.toDataUrl();
				faildPositivedDatas.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
			}
			
		}
		
		if(passedVols.size()==0){
			stopWatch.stop();
			Stopwatch stopWatch2=Stopwatch.createStarted();
			for(int i=0;i<testNumber;i++){
				Optional<Vol> optionalR=createRandomVol(cascade);
				if(!optionalR.isPresent()){
					Window.alert("invalid test data");
					return;
				}
				Vol vol=optionalR.get();
				passedVols.add(new PassedData(vol,lastDataUrl));
				dummyCascade.getTrainer().train(vol, 1);//dummy train.maybe generate inner.
			}
			LogUtils.log("generate-parent-filter passed vol:"+ stopWatch2.elapsed(TimeUnit.SECONDS)+"s");
			stopWatch.start();
			
		}
		
		for(int i=0;i<testNumber;i++){
			String imageDataUrl=passedVols.get(i).getDataUrl();
			Vol vol=passedVols.get(i).getVol();
			Vol value= cascade.getNet().forward(vol,true);//i'm not sure trained true no effect on result,i guess this mean use just cached inside
			if(i==0){
			//LogUtils.log(vol);
			//LogUtils.log(value);
			}
			if(value.getW(1)>(1.0-minPositiveRate)){
			//if(value.getW(1)>value.getW(0)){
				successNegativesPanel.add(new Image(imageDataUrl));
				successMiss++;
			}else{
				faildNegativePanel.add(new Image(imageDataUrl));
				missMatch++;
			}
		}
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}

	public void doTestCascade(CascadeNet cascade){
		
		faildPositivedDatas.clear();
		successPosPanel.clear();
		faildPosPanel.clear();
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		int testNumber=100;
		for(int i=0;i<testNumber;i++){
			
			CVImageeData pdata=positivesZip.get(i);
			
			Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
			
			if(!imageElementOptional.isPresent()){
				LogUtils.log(pdata.getFileName()+" is not exist.skip");
				continue;
			}
			
			ImageElement testImage=imageElementOptional.get();
			
			
			
			//generate canvas for image
			Rect rect=pdata.getRects().get(0);
			RectCanvasUtils.crop(testImage, rect, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			
			
			Vol vol2=cascade.getNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				faildMatch++;
				String newDataUrl=resizedCanvas.toDataUrl();
				faildPositivedDatas.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
			}
			
		}
		
		for(int i=0;i<testNumber;i++){
			Optional<Vol> optional=createRandomVol();
			if(!optional.isPresent()){
				Window.alert("create random vol faild maybe image problems");
				return;
			}
			Vol vol=optional.get();
			Vol value= cascade.getNet().forward(vol);//possible null?
			if(i==0){
			//LogUtils.log(vol);
			//LogUtils.log(value);
			}
			if(value.getW(1)>value.getW(0)){
				successNegativesPanel.add(new Image(lastDataUrl));
				successMiss++;
			}else{
				faildNegativePanel.add(new Image(lastDataUrl));
				missMatch++;
			}
		}
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}
	
	private int netWidth=24;
	private int netHeight=24;
	
	//expand net because of edge.
	private int edgeSize=2;//must be can divided 2
	private Canvas resizedCanvas=CanvasUtils.createCanvas(netWidth+edgeSize, netHeight+edgeSize);//fixed size //can i change?
	
	private HorizontalPanel successPosPanel;
	private ExecuteButton analyzeBt;
	private FileUploadForm netUploadBt;
	
	private String lastDataUrl;
	
	private Map<String,ImageElement> imageElementMap=new HashMap<String, ImageElement>();
	
	private Map<String,List<Rect>> rectsMap=new HashMap<String, List<Rect>>();
	
	public Optional<Vol> createRandomVol(){
		for(int j=0;j<20;j++){
		int index=getRandom(0, negativesZip.size());
		CVImageeData pdata=negativesZip.get(index);
		//String extension=FileNames.getExtension(pdata.getFileName());
		//FileType type=FileType.getFileTypeByExtension(extension);//need to know jpeg or png
		//byte[] bt=imgFile.asUint8Array().toByteArray();
		//this action kill 
		Optional<ImageElement> optional=negativesZip.getImageElement(pdata);
		if(!optional.isPresent()){
			LogUtils.log("skip.image not found in zip(or filter faild):"+pdata.getFileName()+" of "+negativesZip.getName());
			continue;
		}
		
		ImageElement testImage=optional.get();
		
		List<Rect> rects=loadRect(testImage,pdata.getFileName());
		/*
		int width=testImage.getWidth();
		int height=testImage.getHeight();
		
		int w=24;
		int h=24;
		*/
		//should i keep ratio?
		
		/*
			int randomWidth=getRandom(w,Math.min(24*20,width-w));
			int randomHeight=getRandom(h,Math.min(24*20,height-h));
			
			int x=getRandom(0,width-randomWidth);
			int y=getRandom(0,height-randomHeight);
		*/
			
			
			//Rect r=new Rect(x,y,randomWidth,randomHeight);//more varaerty of rect
			Rect r=rects.remove(0);
			rects.add(r);//for loop;
			//LogUtils.log(r);
			
			//LogUtils.log("train:"+data.getFileName()+","+r.toString());
			RectCanvasUtils.crop(testImage, r, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			lastDataUrl=resizedCanvas.toDataUrl();
			//LogUtils.log("randomVol:"+pdata.getFileName()+":"+r.toKanmaString());
			return Optional.of(vol);
		
		}
		return Optional.absent();
	}
	
	boolean isAllImageGrayscale=true;

	private ValueListBox<Double> minRateBox;
	private Vol createVolFromImageData(ImageData imageData){
		if(imageData.getWidth()!=netWidth+edgeSize || imageData.getHeight()!=netHeight+edgeSize){ //somehow still 26x26 don't care!
			Window.alert("invalid size:"+imageData.getWidth()+","+imageData.getHeight());
			return null;
		}
		if(isAllImageGrayscale){
			
			//LBP here,now no effect on but edge problem possible happen
			int[][] ints=byteDataIntConverter.convert(imageData); //convert color image to grayscale data
			int[][] data=lbpConverter.convert(ints);
			int[][] cropped=new int[netWidth][netHeight];
			for(int x=0;x<netWidth;x++){
				for(int y=0;y<netHeight;y++){
					cropped[x][y]=data[x+edgeSize/2][y+edgeSize/2];
				}
			}
			
			return ConvnetJs.createGrayVolFromGrayScaleImage(byteDataIntConverter.reverse().convert(cropped));
		}else{
			throw new RuntimeException("not support. now use 1x1x8 network");
			//return ConvnetJs.createGrayVolFromRGBImage(imageData);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Rect> loadRect(ImageElement image, String fileName) {
		List<Rect> rects=rectsMap.get(fileName);
		if(rects==null){
			rects=generateRect(image, 2, 1.2);
			rectsMap.put(fileName, ListUtils.shuffle(rects));
		}
		return rects;
	}

	public Vol createRandomVolFromPositiveStyle(){
		for(int j=0;j<10;j++){
		int index=getRandom(0, positivesZip.size());
		CVImageeData pdata=positivesZip.get(index);
		
		
		
		Optional<ImageElement> imageElementOptional=positivesZip.getImageElement(pdata);
		
		if(!imageElementOptional.isPresent()){
			LogUtils.log(pdata.getFileName()+" is not exist.skip");
			continue;
		}
		
		ImageElement testImage=imageElementOptional.get();
		
		
	
		int width=testImage.getWidth();
		int height=testImage.getHeight();
		
		int w=24;
		int h=24;
		for(int i=0;i<100;i++){//anyway try 100 times
			//LogUtils.log(j+","+i);
			
			
			int randomWidth=getRandom(w,Math.min(24*20,width-w));
			int randomHeight=getRandom(h,Math.min(24*20,height-h));
			
			int x=getRandom(0,width-randomWidth);
			int y=getRandom(0,height-randomHeight);
			Rect r=new Rect(x,y,randomWidth,randomHeight);//more varaerty of rect
			//LogUtils.log(r);
			
			boolean safe=true;
			for(Rect rect:pdata.getRects()){
				//LogUtils.log(r.toString()+" vs "+rect.toString());
				if(rect.collision(r)){
					safe=false;
					break;
				}
			}
			
			if(safe){
				//LogUtils.log("train:"+data.getFileName()+","+r.toString());
				RectCanvasUtils.crop(testImage, r, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				lastDataUrl=resizedCanvas.toDataUrl();
				//LogUtils.log("randomVol:"+pdata.getFileName()+":"+r.toKanmaString());
				return vol;
			}
		}
		}
		return null;
	}
	
	
	//TODO method?
	public String createColorImageDataUrl(Canvas canvas,int w,int h,String color){
		if(canvas==null){
			canvas=Canvas.createIfSupported();
		}
		CanvasUtils.createCanvas(canvas, w, h);
		CanvasUtils.fillRect(canvas, color);
		return canvas.toDataUrl();
	}
	
	/*
	public void onModuleLoad() {
	
		final Net net=ConvnetJs.createImageNet(32, 32, 3, 10);
		
		final Trainer trainer=ConvnetJs.createTrainer(net,10);//batch not so effect
		
		
		VerticalPanel root=new VerticalPanel();
		RootLayoutPanel.get().add(root);
		
		FileUploadForm cifar10Upload=FileUtils.createSingleFileUploadForm(new DataArrayListener() {
			
			private List<Cifar10Data> datas;

			@Override
			public void uploaded(File file, Uint8Array array) {
				Stopwatch watch=Stopwatch.createStarted();
				int max=1000;
				
				datas = new Cifar10Parser().parse(array.toByteArray(), "file");
				LogUtils.log("cifar10 parsed"+watch.elapsed(TimeUnit.SECONDS)+"s");
				
				watch.reset();watch.start();
				List<Vol> vols=Lists.newArrayList();
				
				
				
				for(int j=0;j<5;j++){ //train x *
				for(int i=0;i<max;i++){
					Vol vol=ConvnetJs.createGrayVol(datas.get(i).getImageData());
					trainer.train(vol, datas.get(i).getClassNumber());
					vols.add(vol);
				}
				}
				LogUtils.log("cifar10 trained:"+watch.elapsed(TimeUnit.SECONDS)+"s");
				watch.reset();watch.start();
				int matched=0;
				//test same
				for(int i=0;i<max;i++){
					
					int maxIndex=0;
					double maxValue=0;
					Vol test=net.forward(ConvnetJs.createGrayVol(datas.get(i).getImageData()));
					for(int c=0;c<10;c++){
						double v=test.getW(c);
						if(v>maxValue){
							maxValue=v;
							maxIndex=c;
						}
					}
					
					if(maxIndex==datas.get(i).getClassNumber()){
						matched++;
					}
				}
				LogUtils.log("cifar10 analyzed:"+watch.elapsed(TimeUnit.SECONDS)+"s");
				
				LogUtils.log(matched+"/"+max);
				
				//x 10 177/1000 
			}
		}, true, false);
		
		root.add(cifar10Upload);
	}*/
	
	
	//0-1
	/*
	public void onModuleLoad() {
		final int classNumber=2;
		LogUtils.log("2-class");
		final Net net=ConvnetJs.createImageNet(32, 32, 3, classNumber);
		
		final Trainer trainer=ConvnetJs.createTrainer(net,4);//batch not so effect
		
		final Canvas sharedCanvas=Canvas.createIfSupported();
		
		final VerticalPanel root=new VerticalPanel();
		RootLayoutPanel.get().add(root);
		
		FileUploadForm cifar10Upload=FileUtils.createSingleFileUploadForm(new DataArrayListener() {
			
			private List<Cifar10Data> datas;

			@Override
			public void uploaded(File file, Uint8Array array) {
				Stopwatch watch=Stopwatch.createStarted();
				int max=1000;
				
				datas = new Cifar10Parser().parse(array.toByteArray(), "file");
				LogUtils.log("cifar10 parsed"+watch.elapsed(TimeUnit.SECONDS)+"s");
				
				watch.reset();watch.start();
				List<Vol> vols=Lists.newArrayList();
				
				
				
				int zeroAdded=0;
				for(int i=0;i<max;i++){
					Vol vol=ConvnetJs.createGrayVol(datas.get(i).getImageData());
					
					int id=datas.get(i).getClassNumber();
					if(id!=0){
						id=1;
					}
					
					if(id==0){
						trainer.train(vol, id);
						
						
						
						CanvasUtils.createCanvas(sharedCanvas, datas.get(i).getImageData());
						root.add(new Image(sharedCanvas.toDataUrl()));
						vols.add(vol);
						zeroAdded++;
					}else{
						
					}
					
					
				}
				int otherAdded=0;
				for(int i=0;i<max;i++){
					Vol vol=ConvnetJs.createGrayVol(datas.get(i).getImageData());
					
					int id=datas.get(i).getClassNumber();
					if(id!=0){
						id=1;
					}
					
					if(id==0){
						
						
					}else{
					//	trainer.train(vol, id);
						//vols.add(vol);
						otherAdded++;
						if(otherAdded==zeroAdded){
							break;
						}
					}
					
					
				}
				
				
				
				LogUtils.log("cifar10 trained:"+watch.elapsed(TimeUnit.SECONDS)+"s");
				watch.reset();watch.start();
				int matched=0;
				int matched2=0;
				int ignoreMatch=0;
				int missMatch=0;
				//test same
				for(int i=0;i<max;i++){
					
					int maxIndex=0;
					double maxValue=0;
					Vol test=net.forward(ConvnetJs.createGrayVol(datas.get(i).getImageData()));
					//Vol test=net.forward(vols.get(i));
					if(datas.get(i).getClassNumber()==0){
				//		LogUtils.log(test);
					}
					
					for(int c=0;c<classNumber;c++){
						double v=test.getW(c);
						if(v>maxValue){
							maxValue=v;
							maxIndex=c;
						}
					}
					
					if(maxIndex==0){
						if(datas.get(i).getClassNumber()==0){
							matched++;
						}else{
							missMatch++;
						}
					}else{
						if(datas.get(i).getClassNumber()==0){
							ignoreMatch++;
						}else{
							matched2++;
						}
					}
					
					
					
					
				}
				LogUtils.log("cifar10 analyzed:"+watch.elapsed(TimeUnit.SECONDS)+"s");
				
				LogUtils.log(matched+":"+matched2+"/"+max+" ignored="+ignoreMatch+",miss="+missMatch);
				
				//x 10 177/1000 
			}
		}, true, false);
		
		root.add(cifar10Upload);
	}*/
	
	public static  final native void test() /*-{
var layer_defs = [];
layer_defs.push({type:'input', out_sx:1, out_sy:1, out_depth:2});
layer_defs.push({type:'fc', num_neurons:20, activation:'relu'});
layer_defs.push({type:'fc', num_neurons:20, activation:'maxout', group_size: 4});
layer_defs.push({type:'fc', num_neurons:10, activation:'sigmoid'});
layer_defs.push({type:'softmax', num_classes:2});
 
// create a net out of it
var net = new $wnd.convnetjs.Net();
net.makeLayers(layer_defs);

 
// the network always works on Vol() elements. These are essentially
// simple wrappers around lists, but also contain gradients and dimensions
// line below will create a 1x1x2 volume and fill it with 0.5 and -1.3
var x = new $wnd.convnetjs.Vol([0.5, -1.3]);
 
var probability_volume = net.forward(x);
console.log('probability that x is class 0: ' + probability_volume.w[0]);


var trainer = new $wnd.convnetjs.SGDTrainer(net, 
              {learning_rate:0.01, momentum:0.0, batch_size:1, l2_decay:0.001});

trainer.train(x, 0);

 
var probability_volume2 = net.forward(x);
console.log('probability that x is class 0: ' + probability_volume2.w[0]);
    }-*/;
	
	/*
	public void test2(){
		Net net=ConvnetJs.createImageNet(32, 32, 3, 2);
		Vol tmp=ConvnetJs.createVol(32, 32,3,  0);
		LogUtils.log(net.forward(tmp));
		
		Trainer trainer=ConvnetJs.createTrainer(net,1);//batch not so effect
		
		Stopwatch watch=Stopwatch.createStarted();
		
		for(int i=0;i<100;i++){
		trainer.train(tmp, 0);
		}
		watch.stop();
		LogUtils.log(watch.elapsed(TimeUnit.MILLISECONDS));
		
		Vol tmp2=ConvnetJs.createGrayVol(32, 32,3,  255);
		
		LogUtils.log("probably:"+net.forward(tmp).getW(0));
		LogUtils.log("probably:"+net.forward(tmp2).getW(0));
	}
	
	public void test3(){
		Net net=ConvnetJs.createImageNet(32, 32, 3, 2);
		Vol tmp=ConvnetJs.createGrayVol(32, 32,3,  0);
		LogUtils.log(net.forward(tmp));
		
		Trainer trainer=ConvnetJs.createTrainer(net,1);//batch not so effect
		
		Stopwatch watch=Stopwatch.createStarted();
		
		for(int i=0;i<100;i++){
		trainer.train(tmp, 0);
		}
		watch.stop();
		LogUtils.log(watch.elapsed(TimeUnit.MILLISECONDS));
		
		Vol tmp2=ConvnetJs.createGrayVol(32, 32,3,  255);
		
		LogUtils.log("probably:"+net.forward(tmp).getW(0));
		LogUtils.log("probably:"+net.forward(tmp2).getW(0));
	}
	*/
	

}
