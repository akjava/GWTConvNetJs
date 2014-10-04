package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.akjava.gwt.comvnetjs.client.StageControler.LearningInfo;
import com.akjava.gwt.comvnetjs.client.StageControler.PhaseData;
import com.akjava.gwt.comvnetjs.client.StageControler.Score;
import com.akjava.gwt.comvnetjs.client.StageControler.ScoreGroup;
import com.akjava.gwt.comvnetjs.client.StageControler.StageResult;
import com.akjava.gwt.comvnetjs.client.worker.CropRectParam;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam1;
import com.akjava.gwt.comvnetjs.client.worker.DetectParam2;
import com.akjava.gwt.comvnetjs.client.worker.HaarRect;
import com.akjava.gwt.comvnetjs.client.worker.MakeRectParam;
import com.akjava.gwt.comvnetjs.client.worker.NegativeResult;
import com.akjava.gwt.html5.client.download.HTML5Download;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataArrayListener;
import com.akjava.gwt.html5.client.file.FileUtils.DataURLListener;
import com.akjava.gwt.html5.client.file.Uint8Array;
import com.akjava.gwt.jszip.client.JSFile;
import com.akjava.gwt.jszip.client.JSZip;
import com.akjava.gwt.lib.client.Base64Utils;
import com.akjava.gwt.lib.client.BrowserUtils;
import com.akjava.gwt.lib.client.BrowserUtils.LoadBinaryListener;
import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.JavaScriptUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ExecuteButton;
import com.akjava.gwt.lib.client.experimental.ImageDataUtils;
import com.akjava.gwt.lib.client.experimental.RectCanvasUtils;
import com.akjava.gwt.lib.client.experimental.ResizeUtils;
import com.akjava.gwt.lib.client.experimental.ToStringValueListBox;
import com.akjava.gwt.lib.client.experimental.lbp.ByteImageDataIntConverter;
import com.akjava.gwt.lib.client.experimental.lbp.ByteImageDataIntConverter.ImageDataToByteFunction;
import com.akjava.gwt.lib.client.experimental.lbp.SimpleLBP;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageData;
import com.akjava.gwt.lib.client.game.PointXY;
import com.akjava.gwt.lib.client.widget.PanelUtils;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.akjava.gwt.webworker.client.Worker2;
import com.akjava.gwt.webworker.client.WorkerPool;
import com.akjava.gwt.webworker.client.WorkerPool.HashedParameterData;
import com.akjava.gwt.webworker.client.WorkerPool.Uint8WorkerPoolData;
import com.akjava.gwt.webworker.client.WorkerPool.WorkerPoolData;
import com.akjava.gwt.webworker.client.WorkerPoolMultiCaller;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.FileNames;
import com.akjava.lib.common.utils.ListUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.webworker.client.MessageEvent;

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
	
	private Canvas imageShowingCanvas;
	
	private List<CascadeNet> cascades=Lists.newArrayList();
	
	private CVImageZip negativesZip;
	private CVImageZip positivesZip;
	final int classNumber=2;
	
	private String lastJson;
	
	private CascadeNet dummyCascadeForKeepNegativeTrained;

	private IntegerBox detectStepSize;

	private DoubleBox detectInitialScale;

	private DoubleBox detectScaleFactor;
	private void doUndo(){
		fromJson(lastJson);
	}
	
	private  Canvas lbpCanvas=Canvas.createIfSupported();//
	
	private String imageDataUrl;

	private CheckBox use45AngleCheck;

	private CheckBox use90AngleCheck;

	private CheckBox createHorizontalVolCheck;
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
	
	private VerticalPanel createImageDetectionPanel(){
		VerticalPanel root=new VerticalPanel();
		//create up-photo controling area
				imageShowingCanvas=Canvas.createIfSupported();
				HorizontalPanel topImageControlPanel=new HorizontalPanel();
				topImageControlPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
				
				/*
				final ExecuteButton detectBt=new ExecuteButton("Detect") {
					
					@Override
					public void executeOnClick() {
						ImageElementUtils.copytoCanvas(imageDataUrl, imageShowingCanvas);
					//	getLastCascade().setMinRate(minRateBox.getValue());
						detectImage(imageShowingCanvas,detectStepSize.getValue(),detectScaleFactor.getValue());
						//detectImageWithWorker(imageShowingCanvas,detectStepSize.getValue(),detectScaleFactor.getValue());
					}
				};
				detectBt.setEnabled(false);
				*/
				
detectWorkerBt = new ExecuteButton("Detect Worker",false) {
					
					@Override
					public void executeOnClick() {
						ImageElementUtils.copytoCanvas(imageDataUrl, imageShowingCanvas);//clear rects
					//	getLastCascade().setMinRate(minRateBox.getValue());
						//detectImage(imageShowingCanvas,detectStepSize.getValue(),detectScaleFactor.getValue());
						detectImageWithWorker(imageShowingCanvas,detectStepSize.getValue(),detectScaleFactor.getValue());
					}
				};
				detectWorkerBt.setEnabled(false);//
				
				
				FileUploadForm imageUpload= FileUtils.createSingleFileUploadForm(new DataURLListener() {
					
					@Override
					public void uploaded(File file, String text) {
						imageDataUrl=text;
						//ImageElementUtils.copytoCanvas(text, imageShowingCanvas);
						
						//detectBt.setEnabled(true);//this is not smart should i separete?
						//detectWorkerBt.startExecute(false);
						
						//
						ImageElementUtils.copytoCanvas(imageDataUrl, imageShowingCanvas);//clear rects
						detectWorkerBt.setEnabled(true);
					}
				}, true);
				
				
				imageUpload.setAccept("image/*");
				HorizontalPanel hpanel=new HorizontalPanel();
				hpanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
				root.add(hpanel);
				
				hpanel.add(new Label("Detect-canvas"));
				hpanel.add(new Label("detect-width:"));
				detectWidth = new IntegerBox();
				detectWidth.setValue(32);
				detectWidth.setWidth("50px");
				hpanel.add(detectWidth);
				
				hpanel.add(new Label("detect-height:"));
				detectHeight = new IntegerBox();
				detectHeight.setValue(32);
				detectHeight.setWidth("50px");
				hpanel.add(detectHeight);
				
				Button resetDetectSizeBt=new Button("Reset",new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						detectWidth.setValue(positiveSize.x);
						detectHeight.setValue(positiveSize.y);
					}
				});
				hpanel.add(resetDetectSizeBt);
				
				
				hpanel.add(new Label("step-size:"));
				detectStepSize = new IntegerBox();
				detectStepSize.setValue(6);
				detectStepSize.setWidth("50px");
				hpanel.add(detectStepSize);
				
				hpanel.add(new Label("initial-scale:"));
				detectInitialScale = new DoubleBox();
				detectInitialScale.setValue(1.0);//now 32 based
				detectInitialScale.setWidth("50px");
				hpanel.add(detectInitialScale);
				
				hpanel.add(new Label("scale-factor:"));
				detectScaleFactor = new DoubleBox();
				detectScaleFactor.setValue(1.8);
				detectScaleFactor.setWidth("50px");
				hpanel.add(detectScaleFactor);
				
				
				root.add(imageShowingCanvas);
				root.add(topImageControlPanel);
				
				topImageControlPanel.add(detectWorkerBt);
				//topImageControlPanel.add(detectBt);
				topImageControlPanel.add(new Label("select image(after that detection start automatic) recommend around 250x250.otherwise take too mauch time"));
				topImageControlPanel.add(imageUpload);
				
				
				useHorizontalFlipCheck = new CheckBox("horizontal flip");
				topImageControlPanel.add(useHorizontalFlipCheck);
				
				use45AngleCheck = new CheckBox("turn(45,135,225,315)");
				topImageControlPanel.add(use45AngleCheck);
				use90AngleCheck = new CheckBox("turn(90,180,270)");
				topImageControlPanel.add(use90AngleCheck);
				
				return root;
	}
	
	public void onModuleLoad() {
	
		sharedCanvas = Canvas.createIfSupported();//shared for multiple-task
		
		
		DockLayoutPanel dockRoot=new DockLayoutPanel(Unit.PX);
		RootLayoutPanel.get().add(dockRoot);
		
		
		//create bottom-south panel
		ScrollPanel scroll=new ScrollPanel();
		dockRoot.addSouth(scroll, 250);
		VerticalPanel bottomPanel=createBottomPanel();
		scroll.add(bottomPanel);
		scroll.setSize("100%", "100%");
		//for some simple test
		bottomPanel.add(cerateTestButtons());
				
				
		
		
		dummyCascadeForKeepNegativeTrained=new CascadeNet(null, createNewNet());
		
		//initial cascade start with empty
		cascades.add(new CascadeNet(null, createNewNet()));
		
		
		mainPanel = PanelUtils.createScrolledVerticalPanel(dockRoot);
		mainPanel.add(createImageDetectionPanel());
		
		
		
		//I'm not sure still need undo?
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
		mainPanel.add(undo);
		
		
		ExecuteButton addBt = new ExecuteButton("Add New Cascade"){
			@Override
			public void executeOnClick() {
				addNewCascade();
			}
		};
		mainPanel.add(addBt);
		
		cascadeLabel = new Label();
		mainPanel.add(cascadeLabel);
		
		mainPanel.add(new Label("load last-saved cascades(sometime compatible problem happen)"));
		FileUploadForm cascadeUploadBt = FileUtils.createSingleTextFileUploadForm(new DataURLListener() {
			@Override
			public void uploaded(File file, String text) {
				fromJson(text);
				if(FileNames.getRemovedExtensionName(file.getFileName()).endsWith("_release")){
					boolean result=Window.confirm("this json is released version,to continue creating to add new cascade?");
					if(result){
						addNewCascade();
					}
				}
			}
		}, true);
		mainPanel.add(cascadeUploadBt);
		cascadeUploadBt.setAccept(Lists.newArrayList(".json",".js"));
		
		
		
		//loadInitialJson(); //right now no need because setting so changed.
		
		
	//	createMinRateBox();
		//root.add(minRateBox); //right now stop using this.because now 1pos & 4 neg classes

		Button bt2b=new Button("Do Test Last Cascade first 100 item Passed parent filters Vol",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				TestResult result=doTestCascadeReal(getLastCascade(),true);
				
				
				LogUtils.log("mutchresult:"+result.successMatch+","+result.faildMatch);
				LogUtils.log("missresult:"+result.successMiss+","+result.missMatch);
				
				LogUtils.log("finish test:"+result.getTestTime()+"s");
			}
		});
		mainPanel.add(bt2b);
		
		
		Button bt3=new Button("Do Test Positives All",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				TestResult result=doTestLastPositiveTrainedAll();
				
				LogUtils.log("finish Test all trained-positive datas:"+result.getTestTime() +"s" + " successMatch="+result.successMatch+",faildMatch="+result.faildMatch);
			}
		});
		mainPanel.add(bt3);
		
		
		HorizontalPanel saveButtons=new HorizontalPanel();
		mainPanel.add(saveButtons);
		Button saveAllBt=new Button("Save All cascades",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String json=toJson();
				
				Anchor downloadJson=HTML5Download.get().generateTextDownloadLink(json, "cascades.json", "download cascades",true);
				mainPanel.add(downloadJson);
			}
		});
		saveButtons.add(saveAllBt);
		
		Button saveReleaseBt=new Button("Save Release cascades(remove last one)",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				List<CascadeNet> nets=Lists.newArrayList(cascades);
				if(nets.size()>0){
				nets.remove(nets.size()-1);
				}
				String json=toJson(nets);
				
				Anchor downloadJson=HTML5Download.get().generateTextDownloadLink(json, "cascades_release.json", "download cascades",true);
				mainPanel.add(downloadJson);
			}
		});
		saveButtons.add(saveReleaseBt);
		
		HorizontalPanel volOptions=new HorizontalPanel();
		volOptions.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		volOptions.add(new Label("Negative Vol options"));
		mainPanel.add(volOptions);
		
		createRandomVolCheck = new CheckBox("use random image");
		createRandomVolCheck.setValue(true);
		createRandomVolCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				useRandomOnCreateVol=event.getValue();
			}
			
		});
		volOptions.add(createRandomVolCheck);
		
		createHorizontalVolCheck = new CheckBox("use horizontal");
	
		createHorizontalVolCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				useHorizontalFlip=event.getValue();
			}
			
		});
		volOptions.add(createHorizontalVolCheck);
		List<Integer> searchWorkersValues=Lists.newArrayList(0,2,4,6,8);
		searchVolWorkerSizeBox = new ToStringValueListBox<Integer>(searchWorkersValues);
		searchVolWorkerSizeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {

			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				searchPassedImageWorkerSize=event.getValue();
			}
			
		});
		searchVolWorkerSizeBox.setValue(2,true);
		
		volOptions.add(searchVolWorkerSizeBox);
		
		
		mainPanel.add(createRepeatControls());
		
		
		mainPanel.add(createTrainingButtons());
		
		
		
		//final String positiveImageName="opencv-pos-images_all.zip";
		
		//maybe it's better to use lbp-cropped when export images.
		
		loadPositiveZip();
		//loadNegativeZip();
		
		
		
		FileUploadForm negativeUpload=FileUtils.createSingleFileUploadForm(new DataArrayListener() {
			
			@Override
			public void uploaded(File file, Uint8Array array) {
				negativeZipLabel.setText(file.getFileName());
				final Stopwatch watch=Stopwatch.createStarted();
				
				negativesZip=new CVImageZip(array);
				negativesZip.setUseCache(true);
				negativesZip.setName(file.getFileName());
				negativesZip.shuffle();//negative file need shuffle?
				checkState(negativesZip.size()>0,"some how empty zip or index/bg");
				
				LogUtils.log("pre-extract-time:"+watch.elapsed(TimeUnit.SECONDS)+"s");
				watch.reset();watch.start();
				final List<CVImageData> datas=Lists.newArrayList(negativesZip.getDatas());
				
				
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
		});
		HorizontalPanel negativePanel=new HorizontalPanel();
		mainPanel.add(negativePanel);
		negativePanel.add(new Label("Negative-Zip:"));
		
		negativeZipLabel=new Label("NOT SELECTED NEED SELECT TO LEARN");
		negativePanel.add(negativeZipLabel);
		negativePanel.add(negativeUpload);
		
		
		mainPanel.add(createFullAutoControls());
		
		
		
		
		updateCascadeLabel();
		testCanvas = CanvasUtils.createCanvas(200, 200);;
		mainPanel.add(testCanvas);
	}
	
	 interface Driver extends SimpleBeanEditorDriver< StageResult,  StageResultEditor> {}
	 Driver stageResultEditorDriver = GWT.create(Driver.class);
	 
		public class StageResultEditor extends VerticalPanel implements Editor<StageResult>,ValueAwareEditor<StageResult>{
			  IntegerBox stageEditor;
			
			  IntegerBox consumeSecondEditor;
			  SimpleEditor<List<PhaseData>> phaseDatasEditor;
			  SimpleEditor<ScoreGroup> scoreGroupEditor;
			  SimpleEditor<String> jsonEditor;
			  SimpleEditor<Score> scoreEditor;

			  
			private Label scoreLabel;

		

			private CopyJsonButton scoreBt;

			private Label bestMatchScoreLabel;

			private Label bestFalseScoreLabel;
			public StageResultEditor(){
				HorizontalPanel top=new HorizontalPanel();
				top.setVerticalAlignment(ALIGN_MIDDLE);
				stageEditor=new IntegerBox();
				stageEditor.setEnabled(false);
				top.add(new Label("Stage:"));
				top.add(stageEditor);
				
				consumeSecondEditor=new IntegerBox();
				consumeSecondEditor.setEnabled(false);
				top.add(new Label("time:"));
				top.add(consumeSecondEditor);
				
				phaseDatasEditor=SimpleEditor.of();
				scoreGroupEditor=SimpleEditor.of();
				jsonEditor=SimpleEditor.of();
				scoreEditor=SimpleEditor.of();
				
				HorizontalPanel scorePanel = new HorizontalPanel();
				scorePanel.setVerticalAlignment(ALIGN_MIDDLE);
				add(scorePanel);
				
				scorePanel.add(new Label("Score:"));
				scoreLabel = new Label();
				scorePanel.add(scoreLabel);
				
				scoreBt = new CopyJsonButton("copy");
				scorePanel.add(scoreBt);
				//other score
				
				HorizontalPanel otherScorePanel = new HorizontalPanel();
				otherScorePanel.setVerticalAlignment(ALIGN_MIDDLE);
				add(otherScorePanel);
				
				otherScorePanel.add(new Label("BestMatch:"));
				bestMatchScoreLabel = new Label();
				otherScorePanel.add(bestMatchScoreLabel);
				//TODO add button
				
				otherScorePanel.add(new Label("BestFalse:"));
				bestFalseScoreLabel = new Label();
				otherScorePanel.add(bestFalseScoreLabel);
			}

			@Override
			public void setDelegate(EditorDelegate<StageResult> delegate) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void flush() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPropertyChange(String... paths) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setValue(StageResult value) {
				
				//do by manual
				scoreLabel.setText(value.getScore().toString());
				scoreBt.setJson(value.getJson());
				
				
				bestMatchScoreLabel.setText(value.getScoreGroup().getBestMatchRateScore().toString());
				bestFalseScoreLabel.setText(value.getScoreGroup().getBestFalseRateScore().toString());
			}
		}
		
		private class CopyJsonButton extends Button{
			private String json;
			public String getJson() {
				return json;
			}
			public void setJson(String json) {
				this.json = json;
			}
			public CopyJsonButton(String label){
				super(label);
				addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						doClick();
					}
				});
			}
			public void doClick(){
				if(json==null){
					return;
				}
				fromJson(json);	
			}
		}
		
	NumberFormat numberFormat=NumberFormat.getFormat("0.000");

	private Label statusLabel;
	private Panel createFullAutoControls() {
		final StageResultEditor resultEditor=new StageResultEditor();
		stageResultEditorDriver.initialize(resultEditor);
		
		VerticalPanel panel=new VerticalPanel();
		
		panel.add(new Label("Auto"));
		//public LearningInfo(int maxStage, double minRate, double falseRate, double firstMatchRate,double firstFalseRate, int minRatio, int maxRatio, int maxVariation, int maxLearning) {
		
		HorizontalPanel firstPanel=new HorizontalPanel();
		firstPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.add(firstPanel);
		
		firstPanel.add(new Label("MaxStage:"));
		List<Integer> maxStageValue=Lists.newArrayList();
		for(int i=1;i<=20;i++){
			maxStageValue.add(i);
		}
		final ToStringValueListBox<Integer> maxStageBox=new ToStringValueListBox<Integer>(maxStageValue);
		firstPanel.add(maxStageBox);
		maxStageBox.setValue(10);
		
		firstPanel.add(new Label("MatchRate:"));
		final DoubleBox matchRateBox=new DoubleBox();
		matchRateBox.setWidth("40px");
		firstPanel.add(matchRateBox);
		matchRateBox.setValue(0.98);
		
		firstPanel.add(new Label("FalseRate:"));
		final DoubleBox falseRateBox=new DoubleBox();
		falseRateBox.setWidth("40px");
		falseRateBox.setValue(0.5);
		firstPanel.add(falseRateBox);
		
		List<Integer> ratioValue=Lists.newArrayList();
		for(int i=1;i<=10;i++){
			ratioValue.add(i);
		}
		firstPanel.add(new Label("maxPositiveRatio:"));
		final ToStringValueListBox<Integer> maxPositiveBox=new ToStringValueListBox<Integer>(ratioValue);
		maxPositiveBox.setValue(10);
		firstPanel.add(maxPositiveBox);
		
		firstPanel.add(new Label("minPositiveRatio:"));
		final ToStringValueListBox<Integer> minPositiveBox=new ToStringValueListBox<Integer>(ratioValue);
		minPositiveBox.setValue(2);
		firstPanel.add(minPositiveBox);
		
		//TODO make min LerningBox to better result
		
		firstPanel.add(new Label("maxLerning:"));
		final IntegerBox maxLearningBox=new IntegerBox();
		maxLearningBox.setValue(2000);
		maxLearningBox.setWidth("80px");
		firstPanel.add(maxLearningBox);
		
		HorizontalPanel secondPanel=new HorizontalPanel();
		secondPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.add(secondPanel);
		
		
		
		
		final DoubleBox firstMatchRateBox=new DoubleBox();
		firstMatchRateBox.setWidth("40px");
		firstMatchRateBox.setValue(0.995);
		
		
		
		final DoubleBox firstFalseRateBox=new DoubleBox();
		firstFalseRateBox.setWidth("40px");
		firstFalseRateBox.setValue(0.2);
		
		List<Integer> offValues=Lists.newArrayList();
		for(int i=2;i<=20;i++){
			offValues.add(i);
		}
		
		
		
		
		
		//for debug initial values
				matchRateBox.setValue(0.99);
				falseRateBox.setValue(0.4);
				firstMatchRateBox.setValue(1.0);
				firstFalseRateBox.setValue(0.05);
				
		
		
		final CheckBox useFirstSpecialValue=new CheckBox();
		secondPanel.add(new Label("use first stage special value"));
		useFirstSpecialValue.setValue(true);
		useFirstSpecialValue.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				firstMatchRateBox.setEnabled(event.getValue());
				firstFalseRateBox.setEnabled(event.getValue());
			}
		});
		secondPanel.add(useFirstSpecialValue);
		secondPanel.add(new Label("FirstMatchRate:"));
		secondPanel.add(firstMatchRateBox);
		secondPanel.add(new Label("FirstFalseRate:"));
		secondPanel.add(firstFalseRateBox);
		
		
		secondPanel.add(new Label("auto off random on create-cascade level at "));
		autoRandomOffStage = new ToStringValueListBox<Integer>(offValues);
		autoRandomOffStage.setValue(6);
		secondPanel.add(autoRandomOffStage);
		

		HorizontalPanel buttons=new HorizontalPanel();
		panel.add(buttons);
		ExecuteButton startBt=new ExecuteButton("Start",false) {
			Stopwatch watch=Stopwatch.createUnstarted();
			@Override
			public void executeOnClick() {
				recycledNegatives.clear();
				usedNegatives.clear();
				
				stageControler = new StageControler(){

					@Override
					public void onEndTraining(boolean reachedGoal) {
						LogUtils.log("finished:"+getStageResults().size()+"goaled="+reachedGoal);
						stageResultObjects.setDatas(getStageResults());
						stageResultObjects.update();
						setEnabled(true);
					}

					@Override
					public void createNewStage() {
						LogUtils.log("clear-stage:"+getStageResults().size());
						stageResultObjects.setDatas(getStageResults());
						stageResultObjects.update();
						addNewCascade();
					}

					@Override
					public Score doTraining(int positiveRatio, boolean initial) {
						
						doTrain(positiveRatio,initial);
						
						TestResult result=doTestLastPositiveTrainedAll();
						TestResult missHit=doTestCascadeReal(getLastCascade(),false);
						
						return new Score(result.getMatchRate(),missHit.getFalseAlarmRate());
					}

					@Override
					public Score repeating() {
						if(learningTime==0){
							messageLabel.setText("start-repeat-learning");
						}
						doRepeat(false);
						TestResult result=doTestLastPositiveTrainedAll();
						TestResult missHit=doTestCascadeReal(getLastCascade(),false);
						
						
						
						Score score= new Score(result.getMatchRate(),missHit.getFalseAlarmRate());
						if(learningTime%100==0){
							messageLabel.setText("learning:"+learningTime+",score:"+score+",item="+lastLeaningList.size());
						if(learningTime%500==0){
							if(learningTime!=0){
								LogUtils.log("learning:"+learningTime+",score:"+score+",item="+lastLeaningList.size()+",time="+watch.elapsed(TimeUnit.SECONDS)+"s");
								
							}
							
							watch.reset();watch.start();
						}
						}
						
						
						return score;
					}

					@Override
					public String makeJson() {
						return toJson();
					}

					@Override
					public int searchPassedImages(boolean isInitial,int needPassedImageSize,long searched) {
						return GWTConvNetJs.this.searchPassedImage(isInitial,needPassedImageSize,searched);
					}

					@Override
					public int getPositiveCount() {
						return positivesZip.getDatas().size()-droppedList.size();
					}

					@Override
					public int getNegativeCount() {
						return negativeTestSize;
					}

					@Override
					public void sendInfo(String message) {
						statusLabel.setText(message);
					}
					
				};
				
				//TODO make variation box;
				double startMatch=useFirstSpecialValue.getValue()?firstMatchRateBox.getValue():matchRateBox.getValue();
				double startFalse=useFirstSpecialValue.getValue()?firstFalseRateBox.getValue():falseRateBox.getValue();
				stageControler.start(new LearningInfo(maxStageBox.getValue(), matchRateBox.getValue(), falseRateBox.getValue(),startMatch, startFalse, 
						minPositiveBox.getValue(), maxPositiveBox.getValue(), 6, maxLearningBox.getValue()));
				
			}
		};
		buttons.add(startBt);
		
		Button test1Cancel=new Button("Cancel",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doCancelAuto();
			}
		});
		buttons.add(test1Cancel);
		
		HorizontalPanel infoPanel=new HorizontalPanel();
		panel.add(infoPanel);
		statusLabel = new Label();
		statusLabel.setWidth("200px");
		infoPanel.add(statusLabel);
		messageLabel = new Label();
		infoPanel.add(messageLabel);
		
		
		HorizontalPanel h=new HorizontalPanel();
		panel.add(h);
		SimpleCellTable<StageResult> table=new SimpleCellTable<StageResult>() {
			@Override
			public void addColumns(CellTable<StageResult> table) {
				TextColumn<StageResult> nameColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						return String.valueOf(object.getStage());
					}
				};
				table.addColumn(nameColumn);
				
				TextColumn<StageResult> matchRateColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						if(object.getScore()==null){
							return "";
						}
						return numberFormat.format(object.getScore().getMatchRate());
					}
				};
				table.addColumn(matchRateColumn,"MatchRate");
				
				TextColumn<StageResult> falseRateColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						if(object.getScore()==null){
							return "";
						}
						return numberFormat.format(object.getScore().getFalseRate());
					}
				};
				table.addColumn(falseRateColumn,"FalseRate");
				
				TextColumn<StageResult> ratioColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						if(object.getPhaseDatas()==null || object.getPhaseDatas().size()==0){
							return "";
						}
						PhaseData phase=object.getPhaseDatas().get(object.getPhaseDatas().size()-1);
						return String.valueOf(phase.getPhaseInfo().getPositiveRatio());
					}
				};
				table.addColumn(ratioColumn,"FinalRatio");
				
				TextColumn<StageResult> variationColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						if(object.getPhaseDatas()==null || object.getPhaseDatas().size()==0){
							return "";
						}
						PhaseData phase=object.getPhaseDatas().get(object.getPhaseDatas().size()-1);
						return String.valueOf(phase.getPhaseInfo().getVariationSize());
					}
				};
				table.addColumn(variationColumn,"FinalVariation");
				
				TextColumn<StageResult> learningColumn=new TextColumn<StageResult>() {
					@Override
					public String getValue(StageResult object) {
						if(object.getPhaseDatas()==null || object.getPhaseDatas().size()==0){
							return "";
						}
						PhaseData phase=object.getPhaseDatas().get(object.getPhaseDatas().size()-1);
						return String.valueOf(phase.getLearningCount());
					}
				};
				table.addColumn(learningColumn,"FinalLearning");
			}
		};
		
		
		
		stageResultObjects = new EasyCellTableObjects<StageResult>(table){
			@Override
			public void onSelect(StageResult selection) {
				resultEditor.setVisible(selection!=null);
				if(selection!=null){
					stageResultEditorDriver.edit(selection);
				}
			}};
		h.add(table);
		
		panel.add(resultEditor);
		
		
		
		
		
		
		return panel;
	}

	protected void doCancelAuto() {
		stageControler.setCanceled(true);
		stageResultObjects.update();//for manual update
		
		
		//cancel searching worker,TODO method?
		if(searchPassedImageWorker!=null){
			searchPassedImageWorker.setCancelled(true);
			}
		searchPassedImageWorkerRunning=false;
		searchPassedImageWorker=null;
		
		//expire
		usedNegatives.clear();
		recycledNegatives.clear();
	}

	private VerticalPanel createRepeatControls() {
		VerticalPanel mainPanel=new VerticalPanel();
		final Label statusLabel=new Label("-");
		mainPanel.add(statusLabel);
		
		HorizontalPanel repeatBts=new HorizontalPanel();
		repeatBts.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(repeatBts);
		
		repeatBts.add(new Label("MinHitRate"));
		final DoubleBox matchRateBox=new DoubleBox();
		matchRateBox.setValue(0.98);
		matchRateBox.setWidth("100px");
		repeatBts.add(matchRateBox);
		
		repeatBts.add(new Label("FalseAlarmRate"));
		final DoubleBox falseAlarmBox=new DoubleBox();
		falseAlarmBox.setValue(0.3);
		falseAlarmBox.setWidth("100px");
		repeatBts.add(falseAlarmBox);
		
		Button cancel=new Button("Cancel",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				autoRepeatCancel=true;
				
			}
		});
		repeatBts.add(cancel);
		
		ExecuteButton autoRepeatBt=new ExecuteButton("Repeat Last Learning(Auto)",false){

			@Override
			public void executeOnClick() {
			//	getLastCascade().setMinRate(minRateBox.getValue());
				autoRepeatCancel=false;
				
				final double targetRate=matchRateBox.getValue();
				final double falseAlarm=falseAlarmBox.getValue();
				
				final int maxIteration=10000;
				Timer timer=new Timer(){
					int iteration=0;
					boolean doing;
					@Override
					public void run() {
						if(doing){
							return;
						}
						
						if(autoRepeatCancel){
							LogUtils.log("cancelled by user");
							cancel();
							doing=false;
							setEnabled(true);
							return;
						}
						
						doing=true;
						doRepeat(false);
						TestResult result=doTestLastPositiveTrainedAll();
						TestResult missHit=doTestCascadeReal(getLastCascade(),false);
						
						if(result.getMatchRate()>targetRate && missHit.getFalseAlarmRate()<falseAlarm){
							statusLabel.setText(iteration+":reach target-muth-rate.stopped "+result.getMatchRate()+">"+targetRate +" falseAlarm="+missHit.getFalseAlarmRate()+"<"+falseAlarm);
							cancel();
							doing=false;
							setEnabled(true);
							return;
						}else{
							
							statusLabel.setText(iteration+":minHitRate="+result.getMatchRate()+"<"+targetRate +" falseAlarm="+missHit.getFalseAlarmRate()+">"+falseAlarm);
							
						}
						
						iteration++;
						if(iteration==maxIteration){
							LogUtils.log("max iteration reached "+maxIteration+".canceled");
							cancel();
							doing=false;
							setEnabled(true);
							return;
							
						}
						
						doing=false;
					}};
				
				timer.scheduleRepeating(200);
			}
			
		};
			
		repeatBts.add(autoRepeatBt);
		
		
		ExecuteButton repeatBt=new ExecuteButton("Repeat Last Learning(continue)"){

			@Override
			public void executeOnClick() {
			//	getLastCascade().setMinRate(minRateBox.getValue());
				
				//re-train last trained for improve
				doRepeat(false);
				doTestLastPositiveTrainedAll();
			}
			
		};
			
		repeatBts.add(repeatBt);
		
		ExecuteButton repeatBt2=new ExecuteButton("Repeat Last Learning(Initial)"){

			@Override
			public void executeOnClick() {
			//	getLastCascade().setMinRate(minRateBox.getValue());
				
				//re-train last trained for improve
				doRepeat(true);
				doTestLastPositiveTrainedAll();
			}
			
		};
			
		repeatBts.add(repeatBt2);
		
		return mainPanel;
	}

	private VerticalPanel createTrainingButtons() {
		VerticalPanel mainPanel=new VerticalPanel();

		HorizontalPanel negativeRatePanel=new HorizontalPanel();
		negativeRatePanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(negativeRatePanel);
		negativeRatePanel.add(new Label("Negative ratio 1 : positive ratio "));
		/**
		 * besically this called after load initial-jsons this means at least you train twice same positives
		 */
		final ListBox list=new ListBox();
		for(int i=1;i<=30;i++){
			list.addItem(String.valueOf(i));
		}
		list.setSelectedIndex(1);
		negativeRatePanel.add(list);
		
		
		ExecuteButton trainingBothButton=new ExecuteButton("Train Positive & Negative Last cascade from initial"){

			@Override
			public void executeOnClick() {
				//getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,true);
				doTestLastPositiveTrainedAll();
			}
			
		};
		mainPanel.add(trainingBothButton);
		
		ExecuteButton trainingBothHorizontalButton=new ExecuteButton("Train Positive & Negative Last cascade with Horizontal"){

			@Override
			public void executeOnClick() {
				//getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,false,true);
				doTestLastPositiveTrainedAll();
			}
			
		};
			
		mainPanel.add(trainingBothHorizontalButton);

		HorizontalPanel variationLevelPanel=new HorizontalPanel();
		mainPanel.add(variationLevelPanel);
		variationLevelPanel.add(new Label("variationLevel:"));
		variationLevelLabel = new Label("0");
		variationLevelPanel.add(variationLevelLabel);
		
		ExecuteButton trainingBoth6Button=new ExecuteButton("Train Positive & Negative Last cascade x 6 differenct"){

			@Override
			public void executeOnClick() {
				//getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,true);//offsetRectContinueIndex initialized here
				updateVariationLevelLabel();
				
				for(int i=0;i<6;i++){
				doTrain(v,false);
				}
				doTestLastPositiveTrainedAll();
				offsetRectContinueIndex++;
				updateVariationLevelLabel();
			}
			
		};
			
		mainPanel.add(trainingBoth6Button);
		
		
		ExecuteButton trainingBoth12Button=new ExecuteButton("Train Positive & Negative Last cascade x 12 differenct"){

			@Override
			public void executeOnClick() {
				//getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,true);//offsetRectContinueIndex initialized here
				updateVariationLevelLabel();
				
				for(int i=0;i<12;i++){//maybe 12 is max
				doTrain(v,false);
				}
				doTestLastPositiveTrainedAll();
				offsetRectContinueIndex++;
				updateVariationLevelLabel();
			}
			
		};
			
		mainPanel.add(trainingBoth12Button);
		
		
		ExecuteButton trainingBothContinueButton=new ExecuteButton("Train Positive & Negative Last cascade continue(make differenct positives"){

			@Override
			public void executeOnClick() {
				//getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,false);
				doTestLastPositiveTrainedAll();
				offsetRectContinueIndex++;
				updateVariationLevelLabel();
			}
			
		};
			
		mainPanel.add(trainingBothContinueButton);
		
		return mainPanel;
		
	}

	protected void updateVariationLevelLabel() {
		variationLevelLabel.setText(String.valueOf(offsetRectContinueIndex));
	}

	private VerticalPanel createBottomPanel(){
		final VerticalPanel bottomPanel=new VerticalPanel();
		bottomPanel.add(new Label("success-positive"));
		successPosPanel = new HorizontalPanel();
		bottomPanel.add(successPosPanel);
		
		HorizontalPanel fpPanel=new HorizontalPanel();fpPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		bottomPanel.add(fpPanel);
		fpPanel.add(new Label("Faild-positive"));
		ExecuteButton retrainFaildPos=new ExecuteButton("Retrain") {
			@Override
			public void executeOnClick() {
				retrainFaildPositives();
				doTestLastPositiveTrainedAll();
			}
		};
		fpPanel.add(retrainFaildPos);
		
		faildPosPanel = new HorizontalPanel();
		bottomPanel.add(faildPosPanel);
		
		bottomPanel.add(new Label("success-negative"));
		successNegativesPanel = new HorizontalPanel();
		bottomPanel.add(successNegativesPanel);
		bottomPanel.add(new Label("Faild-Negative"));
		faildNegativePanel = new HorizontalPanel();
		bottomPanel.add(faildNegativePanel);
		return bottomPanel;
	}
	
	WorkerPool testWorkerPool;
	
	public static  Vol convertToHorizontalVol(Vol vol){
		double[] values=new double[vol.getLength()];
		for(int i=0;i<values.length;i++){
			values[i]=vol.getW(i);
		}
		
		Vol convertedVol=createNewVol();
		double[] converted=SimpleLBP.flipHorizontal(values, lbpDataSplit, lbpDataSplit);
		for(int i=0;i<values.length;i++){
			convertedVol.setW(i, converted[i]);
		}
		return convertedVol;
	}
	
	private void doTest1(){
	//	dtime=0;dtime1=0;dtime2=0;dtime3=0;
		/*
		Optional<Vol> optional=createPassedRandomVol(getLastCascade());
		if(optional.isPresent()){
			Vol vol=optional.get();
			LogUtils.log("normal:"+Joiner.on(",").join(vol.getWAsList()));
			LogUtils.log("horizontal:"+Joiner.on(",").join(convertToHorizontalVol(vol).getWAsList()));
		}*/
		
		long time2=0;
		JsArray<Net> nets=toNetList();
		
		int passed=0;
		int horizontal=0;
		CascadeNet cascadeNet=getLastCascade();
		Stopwatch test1Watch=Stopwatch.createStarted();//share 1 worker pool
		for(int i=0;i<1000;i++){
			
			Optional<Vol> optional=createRandomVol();
			for(Vol vol:optional.asSet()){
				//
				if(passAll(nets,vol)!=-1){
					passed++;
				}
				//dtime2+=watch2.elapsed(TimeUnit.MILLISECONDS);
				if(useHorizontalFlip){
					Vol converted=convertToHorizontalVol(vol);
					if(passAll(nets,converted)!=-1){
						horizontal++;
					}
				}
				/*
				
				*/
				
				Optional<Vol> passedOption=cascadeNet.filterParents(vol);
				if(passedOption.isPresent()){
					passed++;
				}
				Vol converted=convertToHorizontalVol(vol);
				Optional<Vol> horizontalPassed=cascadeNet.filterParents(converted);
				if(horizontalPassed.isPresent()){
					horizontal++;
				}
				
				
			}
		}
	//	LogUtils.log("time:"+test1Watch.elapsed(TimeUnit.SECONDS)+"s"+" passed="+passed+" horizontal="+horizontal+",time2="+time2+"ms");
	//	LogUtils.log("core:"+dtime+"ms"+",dtime1="+dtime1+",dtime2="+dtime2+",dtime3="+dtime3);
		
	}
	public  JsArray<Net> toNetList(){
		JsArray<Net> nets=JavaScriptObject.createArray().cast();
		for(int i=0;i<cascades.size();i++){
			nets.push(cascades.get(i).getNet());
		}
		return nets;
	}
	
	   public static double passAll(JsArray<Net> nets,Vol vol){
		   double r=0;
		   
		   for(int i=0;i<nets.length();i++){
			   Vol result=nets.get(i).forward(vol);
			   if(!CascadeNet.isZeroIndexMostMatched(result)){
				   return -1;
			   }
			   r=result.getW(0);
		   }
		   
		   return r;
	   }
	private HorizontalPanel cerateTestButtons() {
		HorizontalPanel panel=new HorizontalPanel();
		

		Button test1Bt=new Button("Test1",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doTest1();
			}
		});
		panel.add(test1Bt);
		
		
		ExecuteButton execute=new ExecuteButton("Test1",false) {
			Stopwatch test1Watch=Stopwatch.createStarted();//share 1 worker pool
			@Override
			public void executeOnClick() {
				test1Watch.reset();test1Watch.start();
				ImageElement image=negativesZip.getImageElement(negativesZip.get(0)).get();
				final Canvas canvas=CanvasUtils.createCanvas(image);
				final ImageData imageData=ImageDataUtils.copyFrom(canvas);
				final int w=imageData.getWidth()/2;
				final int h=imageData.getHeight()/2;
				
				
				
				final int totalSize=1;
				String option="?"+System.currentTimeMillis();//TODO default
				
				
				if(testWorkerPool==null){
					testWorkerPool=new WorkerPool(totalSize,"/workers/cropgrayscale.js"+option) {
					int extracted;
					@Override
					public void doInitialize(){
						extracted=0;
					}
					@Override
					public void extractData(WorkerPoolData data, MessageEvent event) {
						JsArray<Uint8ArrayNative> arrays=Worker2.getDataAsJavaScriptObject(event).cast();
						
						extracted++;
						
						
						
						if(extracted==totalSize){
							LogUtils.log("crop-time="+test1Watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
							 
								for(int i=0;i<arrays.length();i++){
								ImageData imageData=ImageDataUtils.createNoCopyWith(canvas,w, h);
								ImageDataUtils.setGrayscale(imageData, arrays.get(i), true);
								CanvasUtils.copyTo(imageData, canvas);
								
								mainPanel.add(new Image(canvas.toDataUrl()));
								}
							}
						setEnabled(true);//button
					}
				};
				}else{
					testWorkerPool.doInitialize();
				}
				
				JsArray<HaarRect> rects=JsArray.createArray().cast();
				for(int x=0;x<2;x++){
					for(int y=0;y<2;y++){
						HaarRect rect=HaarRect.create(x*w, y*h, w, h);
						rects.push(rect);
					}
				}
				
				List<JsArray<HaarRect>> rectsList=Lists.newArrayList();
				rectsList.add(rects);
				
				WorkerPoolMultiCaller<JsArray<HaarRect>> multiCaller=new WorkerPoolMultiCaller<JsArray<HaarRect>>(testWorkerPool,rectsList) {
					
					@Override
					public WorkerPoolData convertToData(JsArray<HaarRect> rects) {
						
						final CropRectParam param=CropRectParam.create(imageData,rects);
						
						WorkerPoolData poolData=new WorkerPoolData(){

							//maybe no need
							@Override
							public String getParameterString(String key) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public void postData(Worker2 worker) {
								
								worker.postMessage(param);
							}
						};
						
						return poolData;
					}
					
				};
				multiCaller.start(10);
				
				
				
				/*
				Stopwatch watch=Stopwatch.createStarted();
				List<Rect> rects=RectGenerator.generateRect(2000,2000, 4, 1.4,24,14,1.2);
				LogUtils.log("rect-size:"+rects.size());
				LogUtils.log("rect-time:"+watch.elapsed(TimeUnit.MILLISECONDS));
				for(Rect rect:rects){
					RectCanvasUtils.stroke(rect, testCanvas, "#000");
				}*/
				
//				Canvas canvas=
				
				//calcurate generation vol-time
				/*
				final Stopwatch watch=Stopwatch.createStarted();
				Timer timer=new Timer(){
					int total=0;
					int max=10;
					@Override
					public void run() {
						Optional<Vol> optional=createRandomVol(getLastCascade());
						if(optional.isPresent()){
							total++;
							LogUtils.log("negatives:"+negativeCreated);
						}
						
						if(total==max){
							cancel();
							LogUtils.log("find-time:"+watch.elapsed(TimeUnit.SECONDS)+","+negativeCreated);
							setEnabled(true);
							negativeCreated=0;
						}
					}};
				timer.scheduleRepeating(20);
				*/
			}
		};
		
		//panel.add(execute);
		
		Button test1Cancel=new Button("Test1 Cancel",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				stageControler.setCanceled(true);
				stageResultObjects.update();//for manual update
			}
		});
		//panel.add(test1Cancel);
		
		Button test2=new Button("Test2",new ClickHandler() {
			Net testNet=ConvnetJs.createRawDepathNet(1, 1, 2, 4, 2);
			
			Trainer testTrainer=ConvnetJs.createTrainer(testNet, 1);
			int trained=0;
			List<Vol> pos=Lists.newArrayList();
			List<Vol> neg=Lists.newArrayList();
			@Override
			public void onClick(ClickEvent event) {
				int max=100;
				if(pos.isEmpty()){
				
				for(int i=0;i<max;i++){
					int x=getRandom(0, 10);
					int y=getRandom(0, 10);
					
					Vol v=ConvnetJs.createVol(1, 1, 2, 0);
					v.set(0, 0, new int[]{x,y});
					
					//Stats s=testTrainer.train(v, 0);
					//LogUtils.log(s);
					pos.add(v);
				}
				}else{
					for(Vol v:pos){
						testTrainer.train(v, 0);
					}
				}
				if(neg.isEmpty()){
				for(int i=0;i<max;i++){
					int x=getRandom(100, 200);
					int y=getRandom(100, 200);
					
					Vol v=ConvnetJs.createVol(1, 1, 2, 0);
					v.set(0, 0, new int[]{x,y});
					
					Stats s=testTrainer.train(v, 1);
					//LogUtils.log(s);
					neg.add(v);
				}
				}else{
					for(Vol v:neg){
						testTrainer.train(v, 1);
					}
				}
				
				trained+=max*2;
				
				int fine=0;
				for(int i=0;i<10;i++){
					for(int j=0;j<10;j++){
					int x=getRandom(0, 10);
					int y=getRandom(0, 10);
					
					
					Vol randomVol=ConvnetJs.createVol(1, 1, 2, 0);
					
					randomVol.set(0, 0, new double[]{i,j});
					//LogUtils.log(randomVol);
					
					//Vol randomVol=testb(new double[]{x,y});
					
					//LogUtils.log(randomVol.get(0,0,0)+","+randomVol.get(0,0,1));
					Vol r=testNet.forward(randomVol);
					//LogUtils.log(randomVol);
					//LogUtils.log(randomVol);
					String status="";
					if(r.getW(0)>r.getW(1)){
						status="fine";
						fine++;
					}else{
						status="ng";
					}
					//LogUtils.log(status+","+i+":"+r.getW(0)+","+r.getW(1));
				}
				}
				LogUtils.log("fine:"+fine+",trained="+trained);
				//LogUtils.log(testb(new double[]{0.1,0.2}));
				
			}
		});
		//panel.add(test2);
		return panel;
	}

	protected void doTestx() {

		
		/*
		StageControler controler=new StageControler(){
			int stage=1;
			double testBaseRatio=0.5;
			double testBaseFalse=1.0;
			
			double testRatio;
			double testFalse;
			@Override
			public void onEndTraining(boolean reachedGoal) {
				LogUtils.log("finished:"+getStageResults().size());
				stageResultObjects.setDatas(getStageResults());
				stageResultObjects.update();
			}

			@Override
			public void createNewStage() {
				//update cell-list here.
				stageResultObjects.setDatas(getStageResults());
				stageResultObjects.update();
				
				//check last stage logs.
				StageResult result=getStageResults().get(getStageResults().size()-1);
				LogUtils.log(result.getStage()+","+result.getScore());
				
				stage++;
				LogUtils.log("create-stage"+stage);
			}

			@Override
			public Score doTraining(int positiveRatio, boolean initial) {
				LogUtils.log("initi-training ratio="+positiveRatio+",initial="+initial);
				
				testRatio=testBaseRatio;
				testFalse=testBaseFalse;
				
				Score score=new Score(testRatio, testFalse);
				testBaseRatio+=0.05;
				testBaseFalse-=0.05;
				return score;
			}

			@Override
			public Score repeating() {
				int random=(int) (Math.random()*2);
				if(random==0){
					testRatio+=0.01;
				}else{
					testFalse-=0.01;
				}
				return new Score(testRatio,testFalse);
			}

			@Override
			public String makeJson() {
				return "";
			}
			
		};
		*/
		
	}
	
	public String getNegativeInfo(){
		Stopwatch watch=Stopwatch.createUnstarted();
		int totalRect=0;
		if(negativesZip==null){
			return "";//possible call before negative zip loaded;
		}
		for(CVImageData data:negativesZip.getDatas()){
			for(ImageElement image:negativesZip.getImageElement(data).asSet()){
				watch.start();
			List<Rect> rects=loadRect(image, data.getFileName());
			watch.stop();
			totalRect+=rects.size();
			}
		}
		//LogUtils.log();

		return "negative-info:remain "+totalRect+" rects of "+negativesZip.getDatas().size()+" images"+" rect-generate-time:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms";
	}

	private void loadNegativeZip() {
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
	
	//some how this is slow
	private void createNegativeRectangles(){
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
		
		final int minW=detectWidth.getValue();
		final int minH=detectHeight.getValue();
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
	public static class MakeRectResult extends JavaScriptObject{
		protected MakeRectResult(){
			
		}

		public final  native JsArrayString getRects()/*-{
		return this.rects;
		}-*/;
		
		public final  native String getName()/*-{
		return this.name;
		}-*/;
		
	}

	PointXY positiveSize;//middle of ratio create when load positives,use for reset
	
	private void loadPositiveZip() {
		final String positiveImageName="pos_eye_closed_clip.zip";//for test
		//final String positiveImageName="pos_eye_front_clip.zip";
		//final String positiveImageName="posimages.zip";
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
				for(CVImageData data:positivesZip.getDatas()){
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
				List<Rect> rects=Lists.newArrayList();
				//calculare ratio
				for(CVImageData data:positivesZip.getDatas()){
					for(Rect rect:data.getRects()){
						rects.add(rect);
					}
				}
				
				List<Double> ratios=Lists.newArrayList(FluentIterable.from(rects).transform(new Function<Rect, Double>() {

					@Override
					public Double apply(Rect input) {
						
						return (double)input.getWidth()/input.getHeight();
					}
				}));
				
				double average=Statics.average(ratios);
				double middle=Statics.middle(ratios);
				
				int w=32;
				int h=(int) (w/middle);
				if(h%2==1){
					if(h>w){
						h--;
					}else{
						h++;
					}
				}
				
				positiveSize=new PointXY(w, h);
				detectWidth.setValue(w);
				detectHeight.setValue(h);
				
				LogUtils.log("Ratio:average="+average+",middle="+middle);
				
				LogUtils.log("posimages from "+positivesZip.getName()+" lines="+positivesZip.size()+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
				
			}
			
			@Override
			public void onFaild(int states, String statesText) {
				LogUtils.log(states+","+statesText);
			}
		});
	}

	/*
	private void createMinRateBox() {

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
		
	}
	*/

	private void loadInitialJson() {
		try {
			new RequestBuilder(RequestBuilder.GET,"first-cascade.json").sendRequest(null, new RequestCallback() {
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					cascades.clear();//maybe no need;
					//same upload net;
					Net net=Net.fromJsonString(response.getText());
					Trainer trainer = ConvnetJs.createTrainer(net,1);//need recreate
					
					cascades.add(new CascadeNet(null, net,trainer));//root
					
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
	}

	protected void addNewCascade() {
		//handle min-rate first
		//double minRate=minRateBox.getValue();
		//minrateValues.add(minRate);
		
		//getLastCascade().setMinRate(minRate);
		
		//minRateBox.setValue(0.5);
		
		//expired
		usedNegatives.clear();
		recycledNegatives.clear();
				
		//test is recycled,i'm not sure really good?
		passedNegativeVolsForTest=Lists.newArrayList(FluentIterable.from(passedNegativeVolsForTest).filter(new Predicate<PassedData>() {
			@Override
			public boolean apply(PassedData data) {
				return getLastCascade().filter(data.getVol()).isPresent();
			}
		}));
		
		LogUtils.log("recycled test-negative:"+passedNegativeVolsForTest.size());
		
		CascadeNet next=new CascadeNet(cascades.get(cascades.size()-1),createNewNet(),null);
		cascades.add(next);
		
		
		
		
		//trainPositiveOnly();
		droppedList.addAll(temporalyIgnoreList);
		temporalyIgnoreList.clear();
		
		updateCascadeLabel();
		
		LogUtils.log(getNegativeInfo());
		
		
		
		//for auto
		if(cascades.size()>=autoRandomOffStage.getValue()){
			createRandomVolCheck.setValue(false,true);//not fire event
		}
	}

	public  static  final native Vol testb(double[] values) /*-{
	var vol=new $wnd.convnetjs.Vol(1,1,values.length,0);
	
	for(var i=0;i<values.length;i++){
		vol.set(0,0,i,values[i]);
	}
	
	return vol;
	 }-*/;
	
	private Set<String> droppedList=Sets.newHashSet();
	private Set<String> temporalyIgnoreList=Sets.newHashSet();
	
	
	public static final int lbpDataSplit=3;//somewhere bug? TODO separate  horizontal & vertical
	
	Net createNewNet(){
		
		//create positive 2 negatives
		int depth=8*lbpDataSplit*lbpDataSplit;
		int neutron=8*3;
		int classes=5; //1pos and 4 neg(lbp details)
		return ConvnetJs.createRawDepathNet2(1,1,depth,neutron , classes);
	}
	
	
	//for studyin conitue change similar image to different
	Rect zeroRect=new Rect(0,0,0,0);
	List<Rect> continuRect=Lists.newArrayList(
			new Rect(1,0,-1,0),new Rect(0,1,0,-1),new Rect(0,0,-1,0),new Rect(0,0,0,-1),new Rect(1,0,-2,0),new Rect(0,1,0,-2),
			new Rect(2,0,-2,0),new Rect(0,2,0,-2),new Rect(0,0,-2,0),new Rect(0,0,0,-2),new Rect(2,0,-4,0),new Rect(0,2,0,-4)
			);
	int offsetRectContinueIndex;
	
	List<PassedData> lastLeaningList=Lists.newArrayList();
protected void doRepeat(boolean initial) {
	//learning same value is not bad idea.
		int trained=0;
		
		if(initial){
			
			offsetRectContinueIndex=0;	
			Net net = createNewNet();
			Trainer trainer = ConvnetJs.createTrainer(net,1);//batch no so effect on speed up
			
			getLastCascade().setNet(net);
			getLastCascade().setTrainer(trainer);
			
			}
		
		Stopwatch watch=Stopwatch.createStarted();
		for(PassedData passedData:lastLeaningList){
			Vol origin=passedData.getVol();
			
			getLastCascadesTrainer().train(origin, passedData.getClassIndex());
			trained++;
		}
		

		//LogUtils.log("trained-both:"+trained+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
	}

/**
 * 
 * usedNegatives

clear on newcascade
add & clear on initialize train

add used as negative train.

charged searchpassedImage
 */
	private List<PassedData> usedNegatives=Lists.newArrayList();
	
	private List<PassedData> recycledNegatives=Lists.newArrayList();

	//int negIndex=1;
	int maxNegIndex=2;
	protected void doTrain(int rate,boolean initial) {
		doTrain(rate,initial,false);
	}
	
	boolean recyleWhenVariation=false;
	
	private int detectNegativeIndex(Vol neg){
		
		double total=0;
		for(int i=0;i<neg.getLength();i++){
			total+=neg.getW(i);
		}
		//try separate flat & active
		int negIndex=1;
		if(total>0){
			if(total<0.5){
				negIndex=3;
			}else{
				negIndex=4;
			}
		}else{
			if(total>-0.5){
				negIndex=2;
			}
			
			
		}
		return negIndex;
	}
	protected void doTrain(int rate,boolean initial,boolean horizontal) {

		if(initial){
			lastLeaningList.clear();
			}
		
		if(initial || recyleWhenVariation){
			recycledNegatives.addAll(usedNegatives);
			usedNegatives.clear();
			ListUtils.shuffle(recycledNegatives);
			if(recycledNegatives.size()!=0){//not initialize
			LogUtils.log("recycledNegatives:"+recycledNegatives.size());
			}
		}
		
		trainedPositiveDatas.clear();
		Stopwatch watch=Stopwatch.createStarted();
		int trained=0;
		int negative=0;
		
		int ignored=0;
		Rect offsetRect=null;
		
		if(initial){
		offsetRectContinueIndex=0;	
		Net net = createNewNet();
		Trainer trainer = ConvnetJs.createTrainer(net,1);//batch no so effect on speed up
		
		getLastCascade().setNet(net);
		getLastCascade().setTrainer(trainer);
		offsetRect=zeroRect;
		}else{
			if(!horizontal){
				offsetRect=continuRect.get(offsetRectContinueIndex);
			}else{
				//right now horizontal only do origin for test
				offsetRect=zeroRect;
			}
		}
		
		Stopwatch watch2=Stopwatch.createStarted();
		
		Rect changedRect=new Rect();
		for(CVImageData pdata:positivesZip.getDatas()){
			
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
				
			
				if(horizontal){
					CanvasUtils.drawToFlipHorizontal(sharedCanvas, resizedCanvas);
				}else{
					CanvasUtils.drawToWithDestSameSize(sharedCanvas, resizedCanvas);
				}
				
				//resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				
				Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				getLastCascade().getTrainer().train(vol, 0);
				trained++;
				trainedPositiveDatas.add(new PassedData(vol, path));//re-use when test
				lastLeaningList.add(new PassedData(vol, path));
				if(trained%500==0){//need progress
					LogUtils.log("trained-positive:"+trained+" time="+watch2.elapsed(TimeUnit.SECONDS));
					watch2.reset();watch2.start();
				}
			
				int m=trained%rate;
				if(m==0){
					
					PassedData negativeData=null;
					if(recycledNegatives.size()>0){
						negativeData=recycledNegatives.remove(0);
					}else{
					
				//	LogUtils.log(trained+","+rate);
					Optional<Vol> optional=createPassedRandomVol(getLastCascade());
					if(!optional.isPresent()){
						Window.alert("creating random vol faild maybe image problems");
						return;
					}
					
				Vol neg= optional.get();
				
				double total=0;
				for(int i=0;i<neg.getLength();i++){
					total+=neg.getW(i);
				}
				//try separate flat & active
				int negIndex=1;
				if(total>0){
					if(total<0.5){
						negIndex=3;
					}else{
						negIndex=4;
					}
				}else{
					if(total>-0.5){
						negIndex=2;
					}
					
					
				}
				negativeData=new PassedData(neg, negIndex);
					}
				
				getLastCascade().getTrainer().train(negativeData.getVol(), negativeData.getClassIndex());
				
				lastLeaningList.add(negativeData);//for repeat
				
				usedNegatives.add(negativeData);//for recyle
				negative++;
			
					
				}
				
				
			}
		}
		LogUtils.log("trained-positive:"+trained+ " negative="+negative+" ignored="+ignored+" time="+watch.elapsed(TimeUnit.SECONDS)+"s"+" remain-negative-images:"+negativesZip.getDatas().size());
	}

	
	public static class DetectionData{
		private List<CascadeNet> cascades=Lists.newArrayList();
		public List<CascadeNet> getCascades() {
			return cascades;
		}
		public DetectionData(List<CascadeNet> cascades, Set<String> droppedList) {
			super();
			this.cascades = cascades;
			this.droppedList = droppedList;
		}
		public Set<String> getDroppedList() {
			return droppedList;
		}
		private Set<String> droppedList=Sets.newHashSet();//for future study
		
	}
	
	public static Optional<DetectionData> createDetectionDataFromJson(String text){
		if(text==null || text.isEmpty()){
			return Optional.absent();
		}
		//this happend in webworker Uncaught com.google.gwt.json.client.JSONException: com.google.gwt.core.client.JavaScriptException: (TypeError) : Expecting a function in instanceof check, but got undefined 
		JSONValue value=JSONParser.parseStrict(text);//don't work on WebWorker(prj.gwtwwlinker) both parseLenient
		//JSONValue value=JSONParser.parseLenient(text);
		JSONObject object=value.isObject();
		
		if(object==null){
			return Optional.absent();
		}
		
		//parse nets
		JSONValue netsValue=object.get("nets");
		
		JSONArray array=netsValue.isArray();
		if(array==null){
			LogUtils.log("seems invalid cascade-json type:no nets array");
			//return Optional.absent();
		}
		
		
		List<CascadeNet> cascades=Lists.newArrayList();
		for(int i=0;i<array.size();i++){
			JSONValue arrayValue=array.get(i);
			String jsonText=arrayValue.toString();
			
			CascadeNet parent=null;
			if(cascades.size()>0){
				parent=cascades.get(cascades.size()-1);
			}
			cascades.add(new CascadeNet(parent, jsonText));
		}
		
		
		 Set<String> droppedList=Sets.newHashSet();
		 JSONValue droppedValue=object.get("dropped");
			
			JSONArray droppedArray=droppedValue.isArray();
			if(droppedArray==null){
				LogUtils.log("seems invalid cascade-json type:no dropped array");
				//return Optional.absent();
			}
			for(int i=0;i<droppedArray.size();i++){
				JSONValue arrayValue=droppedArray.get(i);
				JSONString string=arrayValue.isString();
				String jsonText=string.stringValue();
				
				droppedList.add(jsonText);
			}
	
		return Optional.of(new DetectionData(cascades,droppedList));
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
		
		/*
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
		*/
		
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
				
				
		passedNegativeVolsForTest.clear();
		temporalyIgnoreList.clear();
		trainedPositiveDatas.clear();		
				
		
		updateCascadeLabel();
	}

	private void updateCascadeLabel() {
		cascadeLabel.setText("cascade-size:"+cascades.size());
	}

	//List<Double> minrateValues=Lists.newArrayList();
	
	protected String toJson() {
		return toJson(cascades);
	}
	protected String toJson(List<CascadeNet> cascades) {
		//TODO switch to json-version
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
		
		//List<Double> fvalues=Lists.newArrayList(minrateValues);
		//fvalues.add(minRateBox.getValue());
		
		//objects.add("\"minrates\":["+joiner.join(fvalues)+"]");
		
		//TODO check is value valid
		objects.add("\"size\":\""+detectWidth.getValue()+"x"+detectHeight.getValue()+"\"");
		
		objects.add("\"cascadeconvnetFormat\":"+1.0);//
		
		return "{"+joiner.join(objects)+"}";
	}
	
	

	

	protected void trainPositiveOnly() {
		Stopwatch watch=Stopwatch.createStarted();
		int trained=0;
		
		Net net = createNewNet();
		Trainer trainer = ConvnetJs.createTrainer(net,1);//batch no so effect on speed up
		
		getLastCascade().setNet(net);
		getLastCascade().setTrainer(trainer);
		
		for(CVImageData pdata:positivesZip.getDatas()){
			
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
		for(String dataUrl:faildPositivedDatasForRetrainCommand){
			
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
	
	 static final SimpleLBP lbpConverter=new SimpleLBP(true, 2);//
	
	int detectCount=0;
	List<ConfidenceRect> mutchRect=Lists.newArrayList();
	
	static ImageDataToByteFunction imageDataToByteFunctionGrayscale=new ImageDataToByteFunction(false);
	static ImageDataToByteFunction imageDataToByteFunction=new ImageDataToByteFunction(true);
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
	
	
	private List<ImageData> createImageDatas(Canvas imageCanvas,List<Rect> rects){
		List<ImageData> result=Lists.newArrayList();
		for(Rect rect:rects){
		RectCanvasUtils.crop(imageCanvas, rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight(), sharedCanvas);
		
		CanvasUtils.clear(resizedCanvas);//for transparent image
		resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
		
		result.add(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
		
		}
		
		return result;
		
	}
	
	private static Stopwatch bench1=Stopwatch.createUnstarted();
	private static  Stopwatch bench2=Stopwatch.createUnstarted();
	private static  Stopwatch bench3=Stopwatch.createUnstarted();
	private static  Stopwatch bench4=Stopwatch.createUnstarted();
	private static  Stopwatch bench5=Stopwatch.createUnstarted();
	
	
	private void createGrayScaleImageDatas(final Canvas canvasToDrawResult,List<Rect> rects){
		final Stopwatch watch=Stopwatch.createStarted();
		final Stopwatch watch2=Stopwatch.createUnstarted();
		final int totalSize=2;
		int eachSize=rects.size()/totalSize*40;
		
		if(rects.size()%totalSize>0){
			eachSize++;
		}
		
		final List<List<Rect>> partitioned=Lists.partition(rects, eachSize);
		final Map<String,List<Rect>> tmpMap=new HashMap<String, List<Rect>>();
		
		
		WorkerPool cropImageWorkerPool=new WorkerPool(totalSize,"/workers/cropgrayscale.js") {
			int extracted;
			
			//List<Uint8ArrayNative> uintArray=Lists.newArrayList();
			List<Rect> rectArray=Lists.newArrayList();
			List<JsArrayNumber> imageDatas=Lists.newArrayList();
			@Override
			public void doInitialize(){
				extracted=0;
			}
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				
				List<Rect> rects=tmpMap.get(data.getParameterString(null));
				rectArray.addAll(rects);
				
				JsArray<Uint8ArrayNative> arrays=Worker2.getDataAsJavaScriptObject(event).cast();
				
				
				
				List<Uint8ArrayNative> uints=JavaScriptUtils.toList(arrays);
				
				for(int i=0;i<uints.size();i++){
					sharedImageData=ImageDataUtils.createNoCopyWith(canvasToDrawResult,rectArray.get(i).getWidth(), rectArray.get(i).getHeight());
					ImageDataUtils.setGrayscale(sharedImageData, uints.get(i), false);
					

					//somehow do resize need alpha,TODO move to worker
					for(int x=0;x<sharedImageData.getWidth();x++){
						for(int y=0;y<sharedImageData.getHeight();y++){
							sharedImageData.setAlphaAt(255, x, y);
						}
					}
					
					
					
					CanvasUtils.copyTo(sharedImageData, sharedCanvas);
					
					CanvasUtils.clear(resizedCanvas);//for transparent image
					resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
					
					successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
					
					
					sharedImageData=ImageDataUtils.copyFrom(resizedCanvas);//is reduce data?
					
					int[] ints=createLBPDepthFromImageData(sharedImageData,false);
					sharedImageData=null;
					
					JsArrayNumber number=JavaScriptUtils.toArray(ints);
					
					
					imageDatas.add(number);
					
					}
				
				
				//uintArray.addAll(uints);
				
				extracted++;
				
				
				
				if(extracted==partitioned.size()){
					LogUtils.log("crop-image-worker-end:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					watch.reset();watch.start();
					
					
					 	terminateAll();
						
						onEndCreateGrayScaleImageDatas(canvasToDrawResult,rectArray,imageDatas);
						LogUtils.log("end-cropped-imagedata-time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms"+",rect="+rectArray.size());
						LogUtils.log("copy-rect-time:"+watch2.elapsed(TimeUnit.MILLISECONDS)+"ms");
					}
				
			}
		};
		
		
		
		final ImageData imageData=ImageDataUtils.copyFrom(canvasToDrawResult);
		ImageDataUtils.convertToGrayScale(imageData, true);//1 channel grayscale
		
		
		
		WorkerPoolMultiCaller<List<Rect>> multiCaller=new WorkerPoolMultiCaller<List<Rect>>(cropImageWorkerPool,partitioned) {
			
			@Override
			public WorkerPoolData convertToData(List<Rect> rects) {
				watch2.start();
				JsArray<HaarRect> array=JsArray.createArray().cast();
				for(Rect rect:rects){
					array.push(HaarRect.create(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
				}
				watch2.stop();
				
				final String keyName=""+System.currentTimeMillis()+","+Math.random();
				tmpMap.put(keyName, rects);
				final CropRectParam param=CropRectParam.create(imageData,array);
				
				WorkerPoolData poolData=new WorkerPoolData(){

					//maybe no need
					@Override
					public String getParameterString(String key) {
						// TODO Auto-generated method stub
						return keyName;
					}

					@Override
					public void postData(Worker2 worker) {
						
						worker.postMessage(param,WorkerPool.createMessagePorts(imageData));//for single case
					}
				};
				
				return poolData;
			}
			
		};
		multiCaller.start(10);
		
		
		
		
	}
	
	private class RectAndImageData{
		
		
		Rect rect;
		JsArrayNumber number;
		//ImageData imageData;
		public RectAndImageData(Rect rect, JsArrayNumber number) {
		//public RectAndImageData(Rect rect, ImageData imageData) {
			super();
			this.rect = rect;
			this.number = number;
		}
	}
	
	/*
	 * even this style crash on memory-over
	 */
	private ImageData sharedImageData;
	
	
	protected void detectImageWithWorker(final Canvas canvas,final List<Rect> rects) {
		final int totalSize=32;
		
		
		
		int eachSize=rects.size()/totalSize;//split small piece for keep safe-crash
		
		
		
		
		
		final List<List<Rect>> partitioned=Lists.partition(rects, eachSize);
		
		
		final Stopwatch watch=Stopwatch.createStarted();
		final Stopwatch watch2=Stopwatch.createUnstarted();
		
		final List<JsArray<HaarRect>> rectArrayList=Lists.newArrayList();
		final StringBuffer createImageDataLogBuffer=new StringBuffer();
		final String json=toJson();
		
		
		WorkerPool workerPool=new WorkerPool(totalSize,"/detect/worker.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				JsArray<HaarRect> rects=Worker2.getDataAsJavaScriptObject(event).cast();
				rectArrayList.add(rects);
				
				extracted++;
				
				if(extracted==partitioned.size()){
					int match=0;
					LogUtils.log("detect-time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					 canvas.getContext2d().setStrokeStyle("#888");
					
					//TODO should sort?
					for(JsArray<HaarRect> array:rectArrayList){
					 for(int i=0;i<array.length();i++){
						 HaarRect r=array.get(i);
						 	
						 	//LogUtils.log(r);
				        	RectCanvasUtils.strokeCircle(canvas,r.getX(),r.getY(),r.getWidth(),r.getHeight(), true);
				        	match++;
				        }
					}
					LogUtils.log("match-count:"+match);
					LogUtils.log("create-image-data-time:"+watch2.elapsed(TimeUnit.MILLISECONDS)+" "+createImageDataLogBuffer.toString());
					LogUtils.log("total-detect-time:"+detectStopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					terminateAll();
					detectWorkerBt.setEnabled(true);
				}
				
				
			}
		};
		
		
		
		
		
				
		
		
		//
		
		
		createImageDataLogBuffer.append("rects="+rects.size()+",image="+canvas.getCoordinateSpaceWidth()+"x"+canvas.getCoordinateSpaceHeight());
		
		
		
		
		
		 Canvas grayscale=CanvasUtils.convertToGrayScale(canvas, null);
		 final ImageData grayScaleImageData=ImageDataUtils.copyFrom(grayscale);
		
		
				
		WorkerPoolMultiCaller<List<Rect>> multiCaller=new WorkerPoolMultiCaller<List<Rect>>(workerPool,partitioned) {
			
			@Override
			public WorkerPoolData convertToData(List<Rect> data) {
				//LogUtils.log("data-converted:"+data.size());
				watch2.start();
				
				JsArray<HaarRect> rectArray= JsArray.createArray().cast();
				
				for(int i=0;i<data.size();i++){
					Rect rect=data.get(i);
					rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
				}
				watch2.stop();
				
				
				final DetectParam param=DetectParam.create(json, grayScaleImageData, rectArray);
				param.setUseHorizontalFlip(useHorizontalFlipCheck.getValue());
				JsArrayNumber turns=JavaScriptObject.createArray().cast();
				if(use45AngleCheck.getValue()){
					turns.push(45);
					turns.push(135);
					turns.push(225);
					turns.push(315);
				}
				if(use90AngleCheck.getValue()){
					turns.push(90);
					turns.push(180);
					turns.push(270);
				}
				param.setTurnAngles(turns);
			//	final JavaScriptObject transfers=WorkerPool.createMessagePorts(grayScaleImageData);
				
				WorkerPoolData poolData=new WorkerPoolData(){

					@Override
					public String getParameterString(String key) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void postData(Worker2 worker) {
						
						//JsArray<MessagePort> ports=transfers.cast();
						worker.postMessage(param);
						//param.setImageDatas(null);
						//param.setRects(null);
						//worker.postMessage(param,ports);
						
						
						
						//LogUtils.log("data-length:"+param.getImageDatas().get(0).getData().getLength());
						//
					}
				};
				
				return poolData;
			}
			
		};
		
		multiCaller.start(10);
		
	}
	
	protected void onEndCreateGrayScaleImageDatas(final Canvas canvas,final List<Rect> rectList, List<JsArrayNumber> imageDatas) {
		if(rectList.size()!=imageDatas.size()){
			LogUtils.log("Invalid result datas.quit. rects="+rectList.size()+",imageDatas="+imageDatas.size());
			return;
		}
		
		
		int eachSize=rectList.size()/10;//split small piece for keep safe-crash
		
		
		
		List<RectAndImageData> datas=Lists.newArrayList();
		for(int i=0;i<rectList.size();i++){
			datas.add(new RectAndImageData(rectList.get(i),imageDatas.get(i)));
		}
		
		
		final List<List<RectAndImageData>> partitioned=Lists.partition(datas, eachSize);
		
		
		final Stopwatch watch=Stopwatch.createStarted();
		final Stopwatch watch2=Stopwatch.createUnstarted();
		final int totalSize=2;
		final List<JsArray<HaarRect>> rectArrayList=Lists.newArrayList();
		final StringBuffer createImageDataLogBuffer=new StringBuffer();
		final String json=toJson();
		
		
		WorkerPool workerPool=new WorkerPool(totalSize,"/detect/worker.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				JsArray<HaarRect> rects=Worker2.getDataAsJavaScriptObject(event).cast();
				rectArrayList.add(rects);
				
				extracted++;
				
				if(extracted==partitioned.size()){
					int match=0;
					LogUtils.log("detect-time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					 canvas.getContext2d().setStrokeStyle("#888");
					
					//TODO should sort?
					for(JsArray<HaarRect> array:rectArrayList){
					 for(int i=0;i<array.length();i++){
						 HaarRect r=array.get(i);
						 	
						 	//LogUtils.log(r);
				        	RectCanvasUtils.strokeCircle(canvas,r.getX(),r.getY(),r.getWidth(),r.getHeight(), true);
				        	match++;
				        }
					}
					LogUtils.log("match-count:"+match);
					LogUtils.log("create-image-data-time:"+watch2.elapsed(TimeUnit.MILLISECONDS)+" "+createImageDataLogBuffer.toString());
					LogUtils.log("total-detect-time:"+detectStopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					terminateAll();
				}
				
				
			}
		};
		
		
		
		
		
				
		
		
		//
		
		
		createImageDataLogBuffer.append("rects="+rectList.size()+",image="+canvas.getCoordinateSpaceWidth()+"x"+canvas.getCoordinateSpaceHeight());
		
		
		
		
		
		
		
				
		WorkerPoolMultiCaller<List<RectAndImageData>> multiCaller=new WorkerPoolMultiCaller<List<RectAndImageData>>(workerPool,partitioned) {
			
			@Override
			public WorkerPoolData convertToData(List<RectAndImageData> data) {
				//LogUtils.log("data-converted:"+data.size());
				watch2.start();
				
				JsArray<HaarRect> rectArray= JsArray.createArray().cast();
				JsArray<JsArrayNumber> array= JsArray.createArray().cast();
				for(int i=0;i<data.size();i++){
					RectAndImageData rdata=data.get(i);
					Rect rect=rdata.rect;
					rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
					
					array.push(rdata.number);
				}
				watch2.stop();
				
				
				final DetectParam2 param=DetectParam2.create(json, array, rectArray);
				//final JavaScriptObject transfers=DetectParam2.toTransfer(array);
				
				WorkerPoolData poolData=new WorkerPoolData(){

					@Override
					public String getParameterString(String key) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void postData(Worker2 worker) {
						
						//JsArray<MessagePort> ports=transfers.cast();
						worker.postMessage(param);
						//param.setImageDatas(null);
						//param.setRects(null);
						//worker.postMessage(param,ports);
						
						
						
						//LogUtils.log("data-length:"+param.getImageDatas().get(0).getData().getLength());
						//
					}
				};
				
				return poolData;
			}
			
		};
		LogUtils.log("call");
		multiCaller.start(10);
		
	}
	
	/*
	 * sadly this way really consume memory over 100,000
	 */
	/**
	 * @deprecated
	 * @param canvas
	 * @param rectList
	 * @param imageDatas
	 */
	
	protected void onEndCreateGrayScaleImageDatasWithImageData(final Canvas canvas,final List<Rect> rectList, List<ImageData> imageDatas) {
		if(rectList.size()!=imageDatas.size()){
			LogUtils.log("Invalid result datas.quit. rects="+rectList.size()+",imageDatas="+imageDatas.size());
			return;
		}
		final Stopwatch watch=Stopwatch.createStarted();
		final Stopwatch watch2=Stopwatch.createUnstarted();
		final int totalSize=2;
		final List<JsArray<HaarRect>> result=Lists.newArrayList();
		final StringBuffer createImageDataLogBuffer=new StringBuffer();
		final String json=toJson();
		
		
		WorkerPool workerPool=new WorkerPool(totalSize,"/detect/worker.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				JsArray<HaarRect> rects=Worker2.getDataAsJavaScriptObject(event).cast();
				result.add(rects);
				extracted++;
				
				
				
				if(result.size()==rectList.size()){
					int match=0;
					LogUtils.log("detect-time="+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
					 canvas.getContext2d().setStrokeStyle("#888");
					
					//TODO should sort?
					for(JsArray<HaarRect> array:result){
					 for(int i=0;i<array.length();i++){
						 HaarRect r=array.get(i);
						 	
						 	//LogUtils.log(r);
				        	RectCanvasUtils.strokeCircle(canvas,r.getX(),r.getY(),r.getWidth(),r.getHeight(), true);
				        	match++;
				        }
					}
					LogUtils.log("match-count:"+match);
					LogUtils.log("create-image-data-time:"+watch2.elapsed(TimeUnit.MILLISECONDS)+" "+createImageDataLogBuffer.toString());
					LogUtils.log("total-detect-time:"+detectStopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms");
				}
				
				
			}
		};
		
		
		
		
		
				
		
		
		//
		
		
		createImageDataLogBuffer.append("rects="+rectList.size()+",image="+canvas.getCoordinateSpaceWidth()+"x"+canvas.getCoordinateSpaceHeight());
		
		
		
		int eachSize=rectList.size()/20;//split small piece for keep safe-crash
		
		
		
		List<RectAndImageData> datas=Lists.newArrayList();
		for(int i=0;i<rectList.size();i++){
			datas.add(new RectAndImageData(rectList.get(i),null));
			//datas.add(new RectAndImageData(rectList.get(i),imageDatas.get(i)));
		}
		
		
		List<List<RectAndImageData>> partitioned=Lists.partition(datas, eachSize);
		
		
		
				
		WorkerPoolMultiCaller<List<RectAndImageData>> multiCaller=new WorkerPoolMultiCaller<List<RectAndImageData>>(workerPool,partitioned) {
			
			@Override
			public WorkerPoolData convertToData(List<RectAndImageData> data) {
				watch2.start();
				
				JsArray<HaarRect> rectArray= JsArray.createArray().cast();
				JsArray<ImageData> array= JsArray.createArray().cast();
				for(RectAndImageData rdata:data){
					
					
					/*
					//debug to show
					for(int x=0;x<rdata.imageData.getWidth();x++){
						for(int y=0;y<rdata.imageData.getHeight();y++){
							rdata.imageData.setAlphaAt(255, x, y);
						}
					}
					
					
					*/
					//deprecated
					//CanvasUtils.copyTo(rdata.imageData, sharedCanvas);
					
					CanvasUtils.clear(resizedCanvas);//for transparent image
					resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
					
					//successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
					
					array.push(ImageDataUtils.copyFrom(resizedCanvas));
					Rect rect=rdata.rect;
					rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
				}
				watch2.stop();
				
				
				final DetectParam1 param=DetectParam1.create(json, array, rectArray);
				//final JavaScriptObject transfers=DetectParam.toTransfer(array);
				
				WorkerPoolData poolData=new WorkerPoolData(){

					@Override
					public String getParameterString(String key) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void postData(Worker2 worker) {
						
						//JsArray<MessagePort> ports=transfers.cast();
						worker.postMessage(param);
						//worker.postMessage(param,ports);
						
						
						
						//LogUtils.log("data-length:"+param.getImageDatas().get(0).getData().getLength());
						//
					}
				};
				
				return poolData;
			}
			
		};
		multiCaller.start(10);
		
	}

	 Stopwatch detectStopwatch;
	private void detectImageWithWorker(final Canvas canvas,int stepScale,double scale_factor){
		detectStopwatch=Stopwatch.createStarted();
		successPosPanel.clear();
		
		int minW=detectWidth.getValue();
		int minH=detectHeight.getValue();
		double min_scale=detectInitialScale.getValue();
		
		checkState(minW>0 && minH>0,"invaid detect-size:"+minW+"x"+minH);
		List<Rect> rects=RectGenerator.generateRect(canvas.getCoordinateSpaceWidth(),canvas.getCoordinateSpaceHeight(), stepScale, scale_factor,minW,minH,min_scale);
		
		
		detectImageWithWorker(canvas,rects);
		//createGrayScaleImageDatas(canvas, rects);//on end-worker call onEndCreateGrayScaleImageDatas
		
		
	}
	
	
	protected void detectImage(Canvas canvas,int stepScale,double scale_factor) {
		bench1.reset();
		bench2.reset();
		bench3.reset();
		bench4.reset();
		bench5.reset();
		
		detectCount=0;
		Stopwatch watch=Stopwatch.createStarted();
		if(cascades.size()<1){
			Window.alert("loas positive and make cascadeNets!exit.");
			return;
		}
		mutchRect.clear();
		successPosPanel.clear();
		int minW=detectWidth.getValue();
		int minH=detectHeight.getValue();
		
		checkState(minW>0 && minH>0,"invaid detect-size:"+minW+"x"+minH);
		
		
		
		double min_scale=detectInitialScale.getValue();//usually this is 1.0 but skip small item
		Canvas grayscaleCanvasForDetector=Canvas.createIfSupported();
		
		
		
		
		//make method to use same Convert way
		
		//PROPOSE ImageDataUtils.createFrom(Canvas);
		//int[][] bytes=byteDataIntConverter.convert(canvas.getContext2d().getImageData(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight()));
		
		//ImageData imageData=byteDataIntConverter.reverse().convert(lbpConverter.convert(bytes));
		//CanvasUtils.createCanvas(grayscaleCanvasForDetector, imageData);
		
		//i think no need convert grayscale
		
		
		CanvasUtils.convertToGrayScale(canvas, grayscaleCanvasForDetector);
		
		
		//CanvasUtils.copyTo(canvas, grayscaleCanvasForDetector);//just copy no grayscale
		
		
		//grayscaleCanvas=CanvasUtils.copyTo(canvas, grayscaleCanvas, true);//test image is LBP just copy here
		//CanvasUtils.convertToGrayScale(canvas,grayscaleCanvas);//TODO convert LBP here
		
		while(minW*min_scale<canvas.getCoordinateSpaceWidth() && minH*min_scale<canvas.getCoordinateSpaceHeight()){
			detectImage(minW,minH,stepScale,min_scale,grayscaleCanvasForDetector);
			min_scale*=scale_factor;
		}
		LogUtils.log("try:"+detectCount+",mutch:"+mutchRect.size()+","+watch.elapsed(TimeUnit.SECONDS)+"s");
		
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
		 
		 LogUtils.log("crop:"+bench1.elapsed(TimeUnit.MILLISECONDS)+",resize:"+bench2.elapsed(TimeUnit.MILLISECONDS)+",create-vol:"+bench3.elapsed(TimeUnit.MILLISECONDS)+",net.forwards:"+bench4.elapsed(TimeUnit.MILLISECONDS)+",none:"+bench5.elapsed(TimeUnit.MILLISECONDS));
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
	

	
	private String toDataUrl(Uint8ArrayNative array,int w,int h){
		ImageData data=ImageDataUtils.createNoCopyWith(sharedCanvas, w, h);//they must have rgba
		ResizeUtils.setUint8Array(data, array);
		
		CanvasUtils.copyTo(data, resizedCanvas);
		return resizedCanvas.toDataUrl();
		
	}

	
	protected void detectImage(int minW,int minH,int stepScale,double scale,Canvas imageCanvas) {
		ImageData imageData=ImageDataUtils.copyFrom(imageCanvas);//this make slow
		int lastCascadeIndex=cascades.size()-1;
		
		int winW=(int) (minW*scale);
		int winH=(int)(minH*scale);
        double step_x = (0.5 * scale + 1.5)*stepScale;
        double step_y = step_x;
        
        Rect sharedRect=new Rect(0,0,winW,winH);
        int endX=imageCanvas.getCoordinateSpaceWidth()-winW;
        int endY=imageCanvas.getCoordinateSpaceHeight()-winH;
        
        
        for(double x=0;x<endX;x+=step_x){
        	for(double y=0;y<endY;y+=step_y){
        		int dx=(int)x;
        		int dy=(int)y;
        		sharedRect.setX(dx);
        		sharedRect.setY(dy);
        		
        		bench1.start();
        		Uint8ArrayNative cropped=ImageDataUtils.cropRedOnlyPacked(imageData, sharedRect.getX(), sharedRect.getY(), sharedRect.getWidth(), sharedRect.getHeight());
        		bench1.stop();
        		
        		bench2.start();
        		Uint8ArrayNative resized=ResizeUtils.resizeBilinearRedOnlyPacked(cropped, sharedRect.getWidth(), sharedRect.getHeight(), 36,36);
        		bench2.stop();
    			
        		bench3.start();
    			Vol vol=GWTConvNetJs.createVolFromIndexes(GWTConvNetJs.createLBPDepthFromUint8ArrayPacked(resized, false),GWTConvNetJs.parseMaxLBPValue());
    			bench3.stop();
    			
    			//successPosPanel.add(new Image(toDataUrl(cropped,sharedRect.getWidth(),sharedRect.getHeight())));
    			
        		
        		
        		//now cropped image into sharedCanvas
        		
        		/*
        		RectCanvasUtils.crop(imageCanvas, sharedRect, sharedCanvas);
				CanvasUtils.clear(resizedCanvas);//for transparent image
				
				resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
				
				//#RectCanvasUtils.cropAndResize(imageCanvas,rect,w,h,sharedCanvas,clearBeforedraw);//if use transparent or something
			
				//this must extreamly slow
				
				Vol vol=createVolFromGrayscaleImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				*/
        		
				//Vol vol=ConvnetJs.createGrayVolFromGrayScaleImage(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
				
    			bench4.start();
				for(Vol passAll:cascades.get(lastCascadeIndex).filter(vol).asSet()){
					
					
					
					Vol passMyself=cascades.get(lastCascadeIndex).getNet().forward(passAll);
					//successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
        			ConfidenceRect conf=new ConfidenceRect(sharedRect);
        			conf.setConfidence(passMyself.getW(0));
        			mutchRect.add(conf);
				}
				bench4.stop();
				
				detectCount++;
        		//detect
        		
        	}
        }
        
       
	}


	
	
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
	for(CVImageData pdata:positivesZip.getDatas()){
		
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
			
			Optional<Vol> neg= createPassedRandomVol(cascadeNet);//finding data is really take time
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
		negativeCreated++;
		//LogUtils.log(vol);
		
		Optional<Vol> optional=cascadeNet.filterParents(vol);
		if(optional.isPresent()){
			passedParentFilter=optional.get();
			}
		else{
			if(useHorizontalFlip){
				Vol converted=convertToHorizontalVol(vol);
				Optional<Vol> horizontalPassed=cascadeNet.filterParents(converted);
				if(horizontalPassed.isPresent()){
					passedParentFilter=horizontalPassed.get();
				}
			}
		}
		if(negativesZip.getDatas().size()==0){
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
	
	private void startTraining(int trained,CascadeNet cascadeNet){
		LogUtils.log("start-training");
		Stopwatch stopWatch=Stopwatch.createStarted();
		Canvas resizedCanvas=CanvasUtils.createCanvas(24, 24);
		
		int negatives=0;
		int w=24;
		int h=24;
		ImageElement baseImage=null;
		CVImageData data=null;
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
	boolean autoRepeatCancel;
	
	/**@deprecated*/
	
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
			Optional<Vol> neg=createPassedRandomVol(getLastCascade());
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
	
	//double lastTestMutchRate;
	public TestResult  doTestLastPositiveTrainedAll(){//train 1200 = 24s
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
		
		
		//LogUtils.log("test-size:"+trainedPositiveDatas.size());
		
		//trainedPositiveDatas.add(new PassedData(vol, pdata.getFileName()+"#"+rect.toString()));
		
		for(PassedData trainedData:trainedPositiveDatas){
			
			Vol vol=trainedData.getVol();
			Vol result=getLastCascadesNet().forward(vol,true);
			
			if(CascadeNet.isZeroIndexMostMatched(result)){
			//if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				//successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}else{
				if(debugThumbImage){
					temporalyIgnoreList.add(trainedData.getDataUrl());
				}
				faildMatch++;
				//faildPosPanel.add(new Image(resizedCanvas.toDataUrl()));
			}
			
		}
		
		//
		
		
		//LogUtils.log("missresult:"+successMiss+","+missMatch);
		TestResult result=new TestResult(successMatch,faildMatch,0,0);
		result.setTestTime(stopWatch.elapsed(TimeUnit.SECONDS));
		return result;
		
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
		faildPositivedDatasForRetrainCommand.clear();
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
			
			
			CVImageData pdata=positivesZip.get(i);
			
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
				faildPositivedDatasForRetrainCommand.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
			}
			
		}
		
		
		
		LogUtils.log("mutchresult:"+successMatch+","+faildMatch);
		LogUtils.log("missresult:"+successMiss+","+missMatch);
		
		LogUtils.log("finish test:"+ stopWatch.elapsed(TimeUnit.SECONDS)+"s");
	}
	
	
	List<String> faildPositivedDatasForRetrainCommand=Lists.newArrayList();
	
	/*
	public void doTest(){
		faildPositivedDatasForRetrainCommand.clear();
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
			CVImageData pdata=positivesZip.get(i);
			
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
				faildPositivedDatasForRetrainCommand.add(newDataUrl);//reuse when retrain
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
	*/
	boolean trainSecond;
	
	List<PassedData> passedNegativeVolsForTest=Lists.newArrayList();
	
	
	

	
	
	public class PassedData{
		private Vol vol;
		private String dataUrl;
		
		private int classIndex;
	
		public int getClassIndex() {
			return classIndex;
		}
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
		public PassedData(Vol vol,int classIndex){
			this.vol=vol;
			this.classIndex=classIndex;
		}
	}
	
	
	public class TestResult{
		int successMatch=0;
		int faildMatch=0;
		long testTime;
		
		public long getTestTime() {
			return testTime;
		}
		public void setTestTime(long testTime) {
			this.testTime = testTime;
		}
		public TestResult(int successMatch, int faildMatch, int successMiss, int missMatch) {
			super();
			this.successMatch = successMatch;
			this.faildMatch = faildMatch;
			this.successMiss = successMiss;
			this.missMatch = missMatch;
		}
		int successMiss=0;
		int missMatch=0;
		public int getSuccessMatch() {
			return successMatch;
		}
		public void setSuccessMatch(int successMatch) {
			this.successMatch = successMatch;
		}
		public int getFaildMatch() {
			return faildMatch;
		}
		public void setFaildMatch(int faildMatch) {
			this.faildMatch = faildMatch;
		}
		public int getSuccessMiss() {
			return successMiss;
		}
		public void setSuccessMiss(int successMiss) {
			this.successMiss = successMiss;
		}
		public int getMissMatch() {
			return missMatch;
		}
		public void setMissMatch(int missMatch) {
			this.missMatch = missMatch;
		}
		
		public double getMatchRate(){
			return (double)successMatch/(successMatch+faildMatch);
		}
		
		//how many
		public double getFalseAlarmRate(){
			if(missMatch==0){
				return 0;
			}
			return (double)missMatch/(successMiss+missMatch);
		}
	}
	private boolean debugThumbImage;
public TestResult doTestCascadeReal(CascadeNet cascade,boolean testPositives){
		
	
		
		if(testPositives){
			faildPositivedDatasForRetrainCommand.clear();
			successPosPanel.clear();
			faildPosPanel.clear();
		}
		
		successNegativesPanel.clear();
		faildNegativePanel.clear();
		
	
		
		
		Stopwatch stopWatch=Stopwatch.createStarted();
		int successMatch=0;
		int faildMatch=0;
		
		int successMiss=0;
		int missMatch=0;
		
		int testNumber=100;
		
		
		//double minPositiveRate=minRateBox.getValue();
		if(testPositives){
		for(int i=0;i<testNumber;i++){
			CVImageData pdata=positivesZip.get(i);
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
			
			
			Vol result=cascade.getNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(CascadeNet.isZeroIndexMostMatched(result)){
			//if(vol2.getW(0)>vol2.getW(1)){
				successMatch++;
				if(debugThumbImage){
				successPosPanel.add(new Image(resizedCanvas.toDataUrl()));
				}
			}else{
				faildMatch++;
				String newDataUrl=resizedCanvas.toDataUrl();
				if(debugThumbImage){
				faildPositivedDatasForRetrainCommand.add(newDataUrl);//reuse when retrain
				faildPosPanel.add(new Image(newDataUrl));
				}
			}
			
		}
		}
		
		//create new negative datas
		if(passedNegativeVolsForTest.size()==0){
			stopWatch.stop();
			Stopwatch stopWatch2=Stopwatch.createStarted();
			for(int i=0;i<testNumber;i++){
				Optional<Vol> optionalR=createPassedRandomVol(cascade);
				if(!optionalR.isPresent()){
					Window.alert("invalid test data");
					throw new RuntimeException("invalid data");
				}
				Vol vol=optionalR.get();
				passedNegativeVolsForTest.add(new PassedData(vol,lastDataUrl));
				dummyCascadeForKeepNegativeTrained.getTrainer().train(vol, 1);//dummy train.maybe generate inner.
			}
			LogUtils.log("generate-parent-filter passed vol:"+ stopWatch2.elapsed(TimeUnit.SECONDS)+"s");
			stopWatch.start();
			
		}
		
		for(int i=0;i<testNumber;i++){
			
			Vol vol=passedNegativeVolsForTest.get(i).getVol();
			Vol result= cascade.getNet().forward(vol,true);//i'm not sure trained true no effect on result,i guess this mean use just cached inside
			if(i==0){
			//LogUtils.log(vol);
			//LogUtils.log(value);
			}
			if(!CascadeNet.isZeroIndexMostMatched(result)){
			//if(value.getW(1)>value.getW(0)){
				if(debugThumbImage){
				String imageDataUrl=passedNegativeVolsForTest.get(i).getDataUrl();
				successNegativesPanel.add(new Image(imageDataUrl));
				}
				successMiss++;
			}else{
				if(debugThumbImage){
				String imageDataUrl=passedNegativeVolsForTest.get(i).getDataUrl();
				faildNegativePanel.add(new Image(imageDataUrl));
				}
				missMatch++;
			}
		}
		
		
		
		TestResult result=new TestResult(successMatch,faildMatch,successMiss,missMatch);
		result.setTestTime(stopWatch.elapsed(TimeUnit.SECONDS));
		
		return result;
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
				Vol flipped=convertToHorizontalVol(vol);
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

public int negativeTestSize=100;

private int searchPassedImageWorkerSize;
/*
 * new search passed image action
 */

Stopwatch searchWatch=Stopwatch.createUnstarted();
//TODO add need image size
	public int searchPassedImage(boolean isInitial,int needSize,long searched){
		if(searched==0){
			searchWatch.reset();
			if(!searchWatch.isRunning()){
				searchWatch.start();
			}
			messageLabel.setText("start-search:"+isInitial+" needSize="+needSize);
		}
		
		
		//TODO support out of image;
		if(negativesZip.getDatas().size()==0){
			doCancelAuto();
			Window.alert("empty negative images");
		}
		
		//just search don't care doesn't find or faild pass 
		boolean useWorker=false;
		if(!stageControler.isCanceled()){//possible cancelled
			if(searchPassedImageWorkerSize==0 || useRandomOnCreateVol){//no worker,random image not suite worker
				CascadeNet cascadeNet=getLastCascade();
				Optional<Vol> passedVol=getPossiblePassedRandomVol(cascadeNet);
				
				for(Vol vol:passedVol.asSet()){
						if(passedNegativeVolsForTest.size()<negativeTestSize){//make test first
							//this PassedData no need index,just test and must be not 0
							passedNegativeVolsForTest.add(new PassedData(vol,lastDataUrl));
							dummyCascadeForKeepNegativeTrained.getTrainer().train(vol, 1);//dummy train.maybe generate inner.
						}else{
							recycledNegatives.add(new PassedData(vol,detectNegativeIndex(vol)));
					}
				}
				}else{
					useWorker=true;
					if(searched==0){
						searchPassedImageWithWorker(isInitial,needSize);
					}
				}
		}
		
		
		
		//running worker?
		
		
		
		
		
		int numFind=0;
		if(isInitial){
			numFind =passedNegativeVolsForTest.size()+usedNegatives.size()+recycledNegatives.size();	
		}else{
			numFind =recycledNegatives.size();
		}
		String option="";
		if(searched!=0 && searched%1000==0){
			
			if(!useWorker){
				option="searched="+searched;//on non-worker search ,searched is important
			}//TODO count how many searched on worker
			messageLabel.setText("search:"+isInitial+",needSize="+needSize+",finished="+numFind+",time="+searchWatch.elapsed(TimeUnit.SECONDS)+"s "+option);
		}
		
		if(hasEnoughSearchPassedImage(isInitial,needSize)){
			LogUtils.log("search-finished:"+isInitial+",needSize="+needSize+","+",time="+searchWatch.elapsed(TimeUnit.SECONDS)+"s "+option);
			
		}
		
		return numFind;
	}
	


	private boolean searchPassedImageWorkerRunning;
	
	private boolean hasEnoughSearchPassedImage(boolean isInitial,int size){
		if(isInitial){
			return passedNegativeVolsForTest.size()+usedNegatives.size()+recycledNegatives.size()>=size;	
			}else{
			return recycledNegatives.size()>=size;
			}
	}
	
		//this method called every 1-10 ms
	private synchronized void searchPassedImageWithWorker(final boolean isInitial,final int size) {
	
	
			if(searchPassedImageWorkerRunning || stageControler.isCanceled()){
				return;//do nothing
			}
			
			if(hasEnoughSearchPassedImage(isInitial,size)){
				LogUtils.log("no need to running worker");
				return;
			}
			
			LogUtils.log("searchPassedImageWithWorker-called:"+isInitial+",size:"+size);
			searchPassedImageWorkerRunning=true;
		
		
		List<CascadeNet> parents=Lists.newArrayList(cascades);
		parents.remove(parents.size()-1);//
		
		final String json=toJson(parents);
		
		final List<CVImageData> negatives=Lists.newArrayList(negativesZip.getDatas());
		
		searchPassedImageWorker = new WorkerPool(searchPassedImageWorkerSize,"/negative/worker.js") {
			int extracted;
			@Override
			public void extractData(WorkerPoolData data, MessageEvent event) {
				extracted++;
				
				if(extracted==negatives.size()){
					LogUtils.log("reach end of all images.maybe canceled by auto controler");//TODO throw empty error
				}
				
				String name=data.getParameterString("name");
				//LogUtils.log("searchPassedImageWithWorker-extract:"+name);
				CVImageData negativeData=null;
				for(CVImageData negative:negativesZip.getDatas()){
					if(negative.getFileName().equals(name)){
						negativeData=negative;
						break;
					}
				}
				if(negativeData==null){
					LogUtils.log("data not found in zip:"+name);
					negativesZip.getDatas().remove(negativeData);//consumed;
					return;
				}
				Optional<ImageElement> optional=negativesZip.getImageElement(negativeData);
				if(!optional.isPresent()){
					LogUtils.log("image not found in zip:"+name);
					negativesZip.getDatas().remove(negativeData);//consumed;
					return;
				}
				
				JsArray<NegativeResult> rects=Worker2.getDataAsJavaScriptObject(event).cast();
				//LogUtils.log("searchPassedImageWithWorker-extract:"+name+",rects="+rects.length());
				if(rects.length()==0){
					negativesZip.getDatas().remove(negativeData);//consumed;
					return;
				}
				synchronized(this){//possible return same time
					if(hasEnoughSearchPassedImage(isInitial,size)){
						return;//if others done
					}
			
				for(int i=0;i<rects.length();i++){
					NegativeResult negative=rects.get(i);
					
						addSearchingNegativeVol(negative.getVol());
					
					}
				
				
				
					negativesZip.getDatas().remove(negativeData);//consumed;
				
					if(hasEnoughSearchPassedImage(isInitial,size)){
					LogUtils.log("searchPassedImageWithWorker-hasEnoughSearchPassedImage:");
					setCancelled(true);
					
					searchPassedImageWorkerRunning=false;
					searchPassedImageWorker=null;
					}
				}
			}
			};
		
		
		
		
				
		WorkerPoolMultiCaller<CVImageData> multiCaller=new WorkerPoolMultiCaller<CVImageData>(searchPassedImageWorker,negatives) {
			
			@Override
			public WorkerPoolData convertToData(CVImageData data) {
				Optional<ImageElement> element=negativesZip.getImageElement(data);
				if(!element.isPresent()){
					//no need anymore
					negativesZip.getDatas().remove(data);
					return null;//TODO support error
				}
				ImageElementUtils.copytoCanvas(element.get(), sharedCanvas);
				
				Canvas grayscale=CanvasUtils.convertToGrayScale(sharedCanvas, null);
				final ImageData grayScaleImageData=ImageDataUtils.copyFrom(grayscale);
				
				List<Rect> rects=loadRect(element.get(), data.getFileName());
				
				//convert to rects TODO method? #HaarRect.from(List<Rect)
				JsArray<HaarRect> rectArray= JsArray.createArray().cast();
				for(int i=0;i<rects.size();i++){
					Rect rect=rects.get(i);
					rectArray.push(HaarRect.create(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()));
				}
				
				final DetectParam param=DetectParam.create(json, grayScaleImageData, rectArray);
				param.setUseHorizontalFlip(useHorizontalFlip);
				param.setImageData(grayScaleImageData);
				param.setJson(json);
				
				HashedParameterData poolData=new HashedParameterData() {
					
					@Override
					public void postData(Worker2 worker) {
						
						worker.postMessage(param);
					}
					
				};
				poolData.setParameterString("name", data.getFileName());
				
				//LogUtils.log("searchPassedImageWithWorker-multicaller-called(alway 1 more called):"+data.getFileName());
				
				return poolData;
			}
			
		};
		
		
		multiCaller.start(10);
	}

	protected void addSearchingNegativeVol(Vol vol) {
		//List<Double> debug=vol.getWAsList();
		//LogUtils.log("negative-vol:size="+debug.size()+","+Joiner.on(",").join(debug));
		
		if(passedNegativeVolsForTest.size()<negativeTestSize){//make test first
			//this PassedData no need index,just test and must be not 0
			passedNegativeVolsForTest.add(new PassedData(vol,lastDataUrl));
			dummyCascadeForKeepNegativeTrained.getTrainer().train(vol, 1);//dummy train
		}else{
			recycledNegatives.add(new PassedData(vol,detectNegativeIndex(vol)));
		}
	}

	public void doTestCascade(CascadeNet cascade){
		
		faildPositivedDatasForRetrainCommand.clear();
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
			
			CVImageData pdata=positivesZip.get(i);
			
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
				faildPositivedDatasForRetrainCommand.add(newDataUrl);//reuse when retrain
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
	
	public static final int netWidth=24;
	public static final  int netHeight=24;
	
	//expand net because of edge.
	public static  final int edgeSize=4;//must be can divided 2,neibors x 2
	private Canvas resizedCanvas=CanvasUtils.createCanvas(netWidth+edgeSize, netHeight+edgeSize);//fixed size //can i change?
	
	private HorizontalPanel successPosPanel;
	//private ExecuteButton analyzeBt;
	private FileUploadForm netUploadBt;
	
	private String lastDataUrl;//create if random vol 
	
	private Map<String,ImageElement> imageElementMap=new HashMap<String, ImageElement>();
	
	private Map<String,List<Rect>> rectsMap=new HashMap<String, List<Rect>>();
	
	private boolean useRandomOnCreateVol=true;//on first stage it's better use random
	private boolean useHorizontalFlip=false;//on first stage it's better use random
	//private ImageElement lastImageElement;
	private ImageData lastImageData;
	private CVImageData lastData;
	
	/*
	long dtime=0;
	long dtime1;
	long dtime2;
	long dtime3;
	long dtime4;
	*/
	
	public Optional<Vol> createRandomVol(){
		if(negativesZip.size()==0){
			LogUtils.log("negativeZip is empty");
			return Optional.absent();
		}
		
		for(int j=0;j<20;j++){ //just check image exist
	//		Stopwatch watch1=Stopwatch.createStarted();
		int index=useRandomOnCreateVol?getRandom(0, negativesZip.size()):0;
		
		CVImageData pdata=negativesZip.get(index);
		
		
		
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
		List<Rect> rects=loadRect(null,pdata.getFileName());
		Rect rect=rects.remove(0);
		if(rects.size()==0){
				negativesZip.getDatas().remove(pdata);//remove permanently,if neee use,do refrash page
				LogUtils.log("rect is empty removed:"+pdata.getFileName()+","+getNegativeInfo());
		
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
	
	boolean isAllImageGrayscale=true;

	//private ValueListBox<Double> minRateBox;

	private Label variationLevelLabel;

	private IntegerBox detectWidth;

	private IntegerBox detectHeight;

	private Label cascadeLabel;

	private EasyCellTableObjects<StageResult> stageResultObjects;

	private StageControler stageControler;

	private Canvas testCanvas;

	private VerticalPanel mainPanel;

	private CheckBox createRandomVolCheck;

	private Label negativeZipLabel;

	private ToStringValueListBox<Integer> autoRandomOffStage;

	private ExecuteButton detectWorkerBt;

	private CheckBox useHorizontalFlipCheck;

	private Label messageLabel;

	private ToStringValueListBox<Integer> searchVolWorkerSizeBox;

	private WorkerPool searchPassedImageWorker;
	public static Vol createVolFromImageData(ImageData imageData){
		//return createGrayscaleImageVolFromImageData(imageData);
		return createLBPDepthVolFromImageData(imageData,true);
	}
	public static Vol createVolFromGrayscaleImageData(ImageData imageData){
		//return createGrayscaleImageVolFromImageData(imageData);
		return createLBPDepthVolFromImageData(imageData,false);
	}
	public static  Vol createNewVol(){
		return ConvnetJs.createVol(1, 1, 8*lbpDataSplit*lbpDataSplit, 0);
	}
	
	
	/**
	 * 
	 * @param array must be 36x36
	 * @param color
	 * @return
	 */
	public static int[] createLBPDepthFromUint8Array(Uint8ArrayNative array,boolean color){
		int[][] ints=null;
		if(color){
			//TODO
		}else{
			ints=new int[netHeight+edgeSize][netWidth+edgeSize];
			for(int x=0;x<ints[0].length;x++){
				for(int y=0;y<ints.length;y++){
					int index=(y*ints[0].length+x)*4;
					ints[y][x]=array.get(index);//red
				}
			}
		}
		
		//int[][] ints=byteDataIntConverter.convert(imageData); //convert color image to grayscale data
		//int[][] data=lbpConverter.convert(ints);
		int[] retInt=lbpConverter.dataToBinaryPattern(ints, edgeSize, edgeSize);
		return retInt;
	}
	
	public static int[] createLBPDepthFromUint8ArrayPacked(Uint8ArrayNative array,boolean color){
		int[][] ints=null;
		if(color){
			//TODO
		}else{
			ints=new int[netHeight+edgeSize][netWidth+edgeSize];
			for(int x=0;x<ints[0].length;x++){
				for(int y=0;y<ints.length;y++){
					int index=(y*ints[0].length+x);
					ints[y][x]=array.get(index);//red
				}
			}
		}
		
		//int[][] ints=byteDataIntConverter.convert(imageData); //convert color image to grayscale data
		//int[][] data=lbpConverter.convert(ints);
		int[] retInt=lbpConverter.dataToBinaryPattern(ints,lbpDataSplit,lbpDataSplit, edgeSize, edgeSize);
		return retInt;
	}
	
	private static int[] createLBPDepthFromImageData(ImageData imageData,boolean color){
		int[][] ints=null;
		if(color){
			ints=imageDataToByteFunction.apply(imageData); 
		}else{
			ints=imageDataToByteFunctionGrayscale.apply(imageData); 
		}
		
		//int[][] ints=byteDataIntConverter.convert(imageData); //convert color image to grayscale data
		//int[][] data=lbpConverter.convert(ints);
		int[] retInt=lbpConverter.dataToBinaryPattern(ints,lbpDataSplit,lbpDataSplit, edgeSize, edgeSize);
		return retInt;
	}
	
	/*
	 * this is fixed max value 256 16x16 block
	 */
	public static Vol createVolFromIndexes(int[] indexes){
		
		Vol vol=createNewVol();
		//set vol
		for(int i=0;i<indexes.length;i++){
			double v=(double)indexes[i]/128-1; //max valu is 16x16(256) to range(-1 - 1)
			if(v>1 || v<-1){
				LogUtils.log("invalid createVolFromIndexes:"+v);
			}
			vol.set(0, 0,i,v);
		}
		
		return vol;
	}
	
	public static final int parseMaxLBPValue(){
		return ((netWidth)/lbpDataSplit)*((netHeight)/lbpDataSplit);
	}
	
	/**
	 * 
	 * @param indexes
	 * @param maxValue if pixel is 16x16 max=256
	 * @return
	 */
	public static Vol createVolFromIndexes(int[] indexes,int maxValue){
		int half=maxValue/2;
		Vol vol=createNewVol();
		//set vol
		for(int i=0;i<indexes.length;i++){
			double v=(double)indexes[i]/half-1; //max valu is 16x16(256) to range(-1 - 1)
			if(v>1 || v<-1){
				LogUtils.log("invalid:"+v+",maxValue="+maxValue);
			}
			vol.set(0, 0,i,v);
		}
		
		return vol;
	}
	
	private static Vol createLBPDepthVolFromImageData(ImageData imageData,boolean color){
		if(imageData.getWidth()!=netWidth+edgeSize || imageData.getHeight()!=netHeight+edgeSize){ 
			Window.alert("invalid size:"+imageData.getWidth()+","+imageData.getHeight()+" expected "+(netWidth+edgeSize)+"x"+(netHeight+edgeSize));
			return null;
		}
		
		
		//simple lbp return 8 data
		
		//LBP here,now no effect on but edge problem possible happen
		
		
		int[] retInt=createLBPDepthFromImageData(imageData,color);
		//debug
		for(int v:retInt){
			if(v>parseMaxLBPValue()){
				LogUtils.log("retInt is over "+parseMaxLBPValue());
			}
		}
		return createVolFromIndexes(retInt,parseMaxLBPValue());// 24x24 pixel split 3x3 is 8x8
		
		
		//Vol vol=createNewVol();
		
		
		
		//bench4.start();
		//int[] retInt=BinaryPattern.dataToBinaryPattern(data,lbpDataSplit,edgeSize,edgeSize);
		//bench4.stop();
		
		
		//set vol
		/*
		for(int i=0;i<retInt.length;i++){
			double v=(double)retInt[i]/128-1; //max valu is 16x16(256) to range(-1 - 1)
			//double v=(double)retInt[i]/72-1;//maybe range(-1 - 1) //split must be 2 for 12x12(144) block <-- for 24x24
			//double v=(double)retInt[i]/18-1;//maybe -1 - 1 //split must be 4 for 6x6(36) block  <-- for 24x24
			
			if(v>1 || v<-1){
				LogUtils.log("invalid");
			}
			vol.set(0, 0,i,v);
			
			//vol.set(0, 0,i,retInt[i]);
			
			//
			//vol.set(0, 0,i,retInt[i]);//no normalize
		}
		*/
		//int r=getRandom(0, 100);
		//if(r==50){
			//LogUtils.log(Ints.join(",", retInt));
		//}
		/*
		
		for(int[][] values:splitArray(ints,2)){
			int[] histogram=lbpConverter.count(ints);
			//should i normalize myself?
			for(int i=0;i<8;i++){
				vol.set(1, 1, i+8*offset, histogram[i]);
				offset++;
			}
		}
		*/
		//return vol;
		
		/*
		int min=ints.length*ints[0].length;
		for(int v:histogram){
			if(v!=0 && v<min){
				min=v;
			}
		}
		
		//max 576
		
		int[] normalized=new int[8];
		for(int i=0;i<8;i++){
			if(histogram[i]>0){
				normalized[i]=Math.min(16,(int)(histogram[i]/36));//0-16
			}
		}	
		*/
	}
	

	
	
	//not tested
	private List<int[][]> splitArray(int[][] arrays,int splitsize){
		List<int[][]> values=Lists.newArrayList();
		int w=arrays.length/2;
		int h=arrays[0].length/2;
		for(int i=0;i<splitsize*2;i++){
			int[][] result=new int[arrays.length/2][arrays[0].length/2];
			int x=i%w;
			int y=i/w;
			for(int ix=0;ix<w;ix++){
				for(int iy=0;iy<h;iy++){
					result[ix][iy]=arrays[ix+x*w][iy+y*h];
				}
			}
		}
		
		
		return values;
	}
	
	/**
	 * @deprecated
	 * @param imageData
	 * @return
	 */
	private Vol createGrayscaleImageVolFromImageData(ImageData imageData){
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
			int minW=detectWidth.getValue();
			int minH=detectHeight.getValue();
			double min_scale=1.2;//no need really small pixel
			
			rects=RectGenerator.generateRect(image.getWidth(),image.getHeight(), 4, 1.4,minW,minH,min_scale);
			//rects=generateRect(image, 2, 1.2);//this is too much make rects
			rectsMap.put(fileName, ListUtils.shuffle(rects));
		}
		return rects;
	}

	public Vol createRandomVolFromPositiveStyle(){
		for(int j=0;j<10;j++){
		int index=getRandom(0, positivesZip.size());
		CVImageData pdata=positivesZip.get(index);
		
		
		
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
	
	
	

}
