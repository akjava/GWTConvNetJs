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

import com.akjava.gwt.comvnetjs.client.StageControler.LearningInfo;
import com.akjava.gwt.comvnetjs.client.StageControler.PhaseData;
import com.akjava.gwt.comvnetjs.client.StageControler.Score;
import com.akjava.gwt.comvnetjs.client.StageControler.ScoreGroup;
import com.akjava.gwt.comvnetjs.client.StageControler.StageResult;
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
import com.akjava.gwt.lib.client.experimental.opencv.CVImageData;
import com.akjava.gwt.lib.client.widget.PanelUtils;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.FileNames;
import com.akjava.lib.common.utils.ListUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
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
	
	private static Canvas lbpCanvas=Canvas.createIfSupported();//
	
	private String imageDataUrl;
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
				
final ExecuteButton detectBt=new ExecuteButton("Detect") {
					
					@Override
					public void executeOnClick() {
						ImageElementUtils.copytoCanvas(imageDataUrl, imageShowingCanvas);
						getLastCascade().setMinRate(minRateBox.getValue());
						detectImage(imageShowingCanvas,detectStepSize.getValue(),detectScaleFactor.getValue());
					}
				};
				detectBt.setEnabled(false);
				
				FileUploadForm imageUpload= FileUtils.createSingleFileUploadForm(new DataURLListener() {
					
					@Override
					public void uploaded(File file, String text) {
						imageDataUrl=text;
						//ImageElementUtils.copytoCanvas(text, imageShowingCanvas);
						
						detectBt.setEnabled(true);//this is not smart should i separete?
						detectBt.startExecute(true);
						
						//
					}
				}, true);
				
				
				imageUpload.setAccept("image/*");
				HorizontalPanel hpanel=new HorizontalPanel();
				hpanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
				root.add(hpanel);
				
				hpanel.add(new Label("Detect-canvas"));
				hpanel.add(new Label("detect-width:"));
				detectWidth = new IntegerBox();
				detectWidth.setValue(24);
				detectWidth.setWidth("50px");
				hpanel.add(detectWidth);
				
				hpanel.add(new Label("detect-height:"));
				detectHeight = new IntegerBox();
				detectHeight.setValue(14);
				detectHeight.setWidth("50px");
				hpanel.add(detectHeight);
				
				
				
				hpanel.add(new Label("step-size:"));
				detectStepSize = new IntegerBox();
				detectStepSize.setValue(6);
				detectStepSize.setWidth("50px");
				hpanel.add(detectStepSize);
				
				hpanel.add(new Label("initial-scale:"));
				detectInitialScale = new DoubleBox();
				detectInitialScale.setValue(1.4);
				detectInitialScale.setWidth("50px");
				hpanel.add(detectInitialScale);
				
				hpanel.add(new Label("scale-factor:"));
				detectScaleFactor = new DoubleBox();
				detectScaleFactor.setValue(1.8);
				detectScaleFactor.setWidth("50px");
				hpanel.add(detectScaleFactor);
				
				
				root.add(imageShowingCanvas);
				root.add(topImageControlPanel);
				
				
				topImageControlPanel.add(detectBt);
				topImageControlPanel.add(new Label("select image(after that detection start automatic) recommend around 250x250.otherwise take too mauch time"));
				topImageControlPanel.add(imageUpload);
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
		
		
		final VerticalPanel mainPanel=PanelUtils.createScrolledVerticalPanel(dockRoot);
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
			}
		}, true);
		mainPanel.add(cascadeUploadBt);
		cascadeUploadBt.setAccept(Lists.newArrayList(".json",".js"));
		
		
		
		//loadInitialJson(); //right now no need because setting so changed.
		
		
		createMinRateBox();
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
		
		
		Button saveAllBt=new Button("Save All cascades",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String json=toJson();
				
				Anchor downloadJson=HTML5Download.get().generateTextDownloadLink(json, "cascades.json", "download cascades",true);
				mainPanel.add(downloadJson);
			}
		});
		mainPanel.add(saveAllBt);
		
		
		
		mainPanel.add(createRepeatControls());
		
		
		mainPanel.add(createTrainingButtons());
		
		
		
		//final String positiveImageName="opencv-pos-images_all.zip";
		
		//maybe it's better to use lbp-cropped when export images.
		
		loadPositiveZip();
		loadNegativeZip();
		
		

		
		mainPanel.add(createFullAutoControls());
		
		updateCascadeLabel();
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
	private Panel createFullAutoControls() {
		final StageResultEditor resultEditor=new StageResultEditor();
		stageResultEditorDriver.initialize(resultEditor);
		
		VerticalPanel panel=new VerticalPanel();
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
				getLastCascade().setMinRate(minRateBox.getValue());
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
				getLastCascade().setMinRate(minRateBox.getValue());
				
				//re-train last trained for improve
				doRepeat(false);
				doTestLastPositiveTrainedAll();
			}
			
		};
			
		repeatBts.add(repeatBt);
		
		ExecuteButton repeatBt2=new ExecuteButton("Repeat Last Learning(Initial)"){

			@Override
			public void executeOnClick() {
				getLastCascade().setMinRate(minRateBox.getValue());
				
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
				getLastCascade().setMinRate(minRateBox.getValue());
				
				int v=Integer.parseInt(list.getValue(list.getSelectedIndex()));
				doTrain(v,true);
				doTestLastPositiveTrainedAll();
			}
			
		};
		mainPanel.add(trainingBothButton);
		
		ExecuteButton trainingBothHorizontalButton=new ExecuteButton("Train Positive & Negative Last cascade with Horizontal"){

			@Override
			public void executeOnClick() {
				getLastCascade().setMinRate(minRateBox.getValue());
				
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
				getLastCascade().setMinRate(minRateBox.getValue());
				
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
				getLastCascade().setMinRate(minRateBox.getValue());
				
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
				getLastCascade().setMinRate(minRateBox.getValue());
				
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
	
	private HorizontalPanel cerateTestButtons() {
		HorizontalPanel panel=new HorizontalPanel();

Button test=new Button("Test",new ClickHandler() {
	
			@Override
			public void onClick(ClickEvent event) {
				doTest1();
				
				
				/*
				//calculate time
				for(int i=0;i<10;i++){
					Vol v=createRandomVol().get();
					Vol result=getLastCascadesNet().forward(v);
					LogUtils.log(result.getW(0)+","+result.getW(1));
					
				}
				*/
				
			}
		});
		panel.add(test);
		
		Button test1Cancel=new Button("Test1 Cancel",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				stageControler.setCanceled(true);
				stageResultObjects.update();//for manual update
			}
		});
		panel.add(test1Cancel);
		
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

	protected void doTest1() {

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
		
		stageControler = new StageControler(){

			@Override
			public void onEndTraining(boolean reachedGoal) {
				LogUtils.log("finished:"+getStageResults().size()+"goaled="+reachedGoal);
				stageResultObjects.setDatas(getStageResults());
				stageResultObjects.update();
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
				doRepeat(false);
				TestResult result=doTestLastPositiveTrainedAll();
				TestResult missHit=doTestCascadeReal(getLastCascade(),false);
				
				return new Score(result.getMatchRate(),missHit.getFalseAlarmRate());
			}

			@Override
			public String makeJson() {
				return toJson();
			}
			
		};
		
		stageControler.start(new LearningInfo(10, 0.98, 0.5, 0.2, 1, 10, 6, 3000));
	}
	
	public String getNegativeInfo(){
		int totalRect=0;
		for(CVImageData data:negativesZip.getDatas()){
			for(ImageElement image:negativesZip.getImageElement(data).asSet()){
			List<Rect> rects=loadRect(image, data.getFileName());
			totalRect+=rects.size();
			}
		}

		return "negative-info:image="+negativesZip.getDatas().size()+" images,total-rect="+totalRect;
	}

	private void loadNegativeZip() {
		final String negativeImageName="neg_eye_paint_face.zip";//bg2 is face up
		//final String negativeImageName="neg_eye_clip.zip";//bg2 is face up
//final String negativeImageName="bg2.zip";//bg2 is face up
//final String negativeImageName="clipbg.zip";//"nose-inpainted-faces.zip"
BrowserUtils.loadBinaryFile(negativeImageName,new LoadBinaryListener() {
	
	




	@Override
	public void onLoadBinaryFile(ArrayBuffer buffer) {
		
		Stopwatch watch=Stopwatch.createStarted();
	
		negativesZip=new CVImageZip(buffer);
		negativesZip.setUseCache(true);
		negativesZip.setName(negativeImageName);
		negativesZip.shuffle();//negative file need shuffle?
		checkState(negativesZip.size()>0,"some how empty zip or index/bg");
		
		LogUtils.log(getNegativeInfo());
		
		LogUtils.log("load negatives from "+negativesZip.getName()+" items="+negativesZip.size()+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
		
	}
	
	@Override
	public void onFaild(int states, String statesText) {
		LogUtils.log(states+","+statesText);
	}
});
		
	}

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
				
				LogUtils.log("Ratio:average="+average+",middle="+middle);
				
				LogUtils.log("posimages from "+positivesZip.getName()+" lines="+positivesZip.size()+" time="+watch.elapsed(TimeUnit.SECONDS)+"s");
				
			}
			
			@Override
			public void onFaild(int states, String statesText) {
				LogUtils.log(states+","+statesText);
			}
		});
	}

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
		
		updateCascadeLabel();
		
		LogUtils.log(getNegativeInfo());
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
	
	
	int lbpDataSplit=2;//somewhere bug?
	
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

	//int negIndex=1;
	int maxNegIndex=2;
	protected void doTrain(int rate,boolean initial) {
		doTrain(rate,initial,false);
	}
	protected void doTrain(int rate,boolean initial,boolean horizontal) {

		if(initial){
			lastLeaningList.clear();
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
					CanvasUtils.drawTo(sharedCanvas, resizedCanvas);
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
				//	LogUtils.log(trained+","+rate);
					Optional<Vol> optional=createRandomVol(getLastCascade());
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
				
				getLastCascade().getTrainer().train(neg, negIndex);
				lastLeaningList.add(new PassedData(neg, negIndex));
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
				
		
		updateCascadeLabel();
	}

	private void updateCascadeLabel() {
		cascadeLabel.setText("cascade-size:"+cascades.size());
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
	
	 final SimpleLBP lbpConverter=new SimpleLBP(true, 2);//
	
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
		int minW=detectWidth.getValue();
		int minH=detectHeight.getValue();
		double min_scale=1.2;//no need really small pixel
		
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
				
				count++;
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
	
	boolean trainSecond;
	
	List<PassedData> passedVols=Lists.newArrayList();
	
	
	

	
	
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
		
		
		double minPositiveRate=minRateBox.getValue();
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
			
			
			Vol vol2=cascade.getNet().forward(vol);
			if(i==0){
			//	LogUtils.log("v:"+vol2.getW(0)+","+vol2.getW(1));
			}
			if(vol2.getW(0)>minPositiveRate){
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
		if(passedVols.size()==0){
			stopWatch.stop();
			Stopwatch stopWatch2=Stopwatch.createStarted();
			for(int i=0;i<testNumber;i++){
				Optional<Vol> optionalR=createRandomVol(cascade);
				if(!optionalR.isPresent()){
					Window.alert("invalid test data");
					throw new RuntimeException("invalid data");
				}
				Vol vol=optionalR.get();
				passedVols.add(new PassedData(vol,lastDataUrl));
				dummyCascadeForKeepNegativeTrained.getTrainer().train(vol, 1);//dummy train.maybe generate inner.
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
				if(debugThumbImage){
				successNegativesPanel.add(new Image(imageDataUrl));
				}
				successMiss++;
			}else{
				if(debugThumbImage){
				faildNegativePanel.add(new Image(imageDataUrl));
				}
				missMatch++;
			}
		}
		
		
		
		TestResult result=new TestResult(successMatch,faildMatch,successMiss,missMatch);
		result.setTestTime(stopWatch.elapsed(TimeUnit.SECONDS));
		
		return result;
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
	
	private int netWidth=32;
	private int netHeight=32;
	
	//expand net because of edge.
	private int edgeSize=4;//must be can divided 2
	private Canvas resizedCanvas=CanvasUtils.createCanvas(netWidth+edgeSize, netHeight+edgeSize);//fixed size //can i change?
	
	private HorizontalPanel successPosPanel;
	//private ExecuteButton analyzeBt;
	private FileUploadForm netUploadBt;
	
	private String lastDataUrl;
	
	private Map<String,ImageElement> imageElementMap=new HashMap<String, ImageElement>();
	
	private Map<String,List<Rect>> rectsMap=new HashMap<String, List<Rect>>();
	
	public Optional<Vol> createRandomVol(){
		Stopwatch watch=Stopwatch.createStarted();
		for(int j=0;j<20;j++){
		int index=getRandom(0, negativesZip.size());
		CVImageData pdata=negativesZip.get(index);
		//String extension=FileNames.getExtension(pdata.getFileName());
		//FileType type=FileType.getFileTypeByExtension(extension);//need to know jpeg or png
		//byte[] bt=imgFile.asUint8Array().toByteArray();
		//this action kill 
		Optional<ImageElement> optional=negativesZip.getImageElement(pdata);
		if(!optional.isPresent()){
			LogUtils.log("skip.image not found in zip(or filter faild):"+pdata.getFileName()+" of "+negativesZip.getName());
			continue;
		}
		
		ImageElement negativeImage=optional.get();
		//LogUtils.log("load-image:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
		watch.reset();watch.start();
		
		List<Rect> rects=loadRect(negativeImage,pdata.getFileName());
		//LogUtils.log("generate-rect:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
		watch.reset();watch.start();
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
			
			//rects.add(r);///stop looping
			if(rects.size()==0){
				negativesZip.getDatas().remove(pdata);//remove permanently,if neee use,do refrash page
			}
			//LogUtils.log(r);
			
			//LogUtils.log("train:"+data.getFileName()+","+r.toString());
			RectCanvasUtils.crop(negativeImage, r, sharedCanvas);
			CanvasUtils.clear(resizedCanvas);
			resizedCanvas.getContext2d().drawImage(sharedCanvas.getCanvasElement(), 0, 0,sharedCanvas.getCoordinateSpaceWidth(),sharedCanvas.getCoordinateSpaceHeight(),0,0,resizedCanvas.getCoordinateSpaceWidth(),resizedCanvas.getCoordinateSpaceHeight());
			Vol vol=createVolFromImageData(resizedCanvas.getContext2d().getImageData(0, 0, resizedCanvas.getCoordinateSpaceWidth(), resizedCanvas.getCoordinateSpaceHeight()));
			lastDataUrl=resizedCanvas.toDataUrl();
			//LogUtils.log("generate-image:"+watch.elapsed(TimeUnit.MILLISECONDS)+"ms");
			watch.reset();watch.start();
			//LogUtils.log("randomVol:"+pdata.getFileName()+":"+r.toKanmaString());
			return Optional.of(vol);
		
		}
		return Optional.absent();
	}
	
	boolean isAllImageGrayscale=true;

	private ValueListBox<Double> minRateBox;

	private Label variationLevelLabel;

	private IntegerBox detectWidth;

	private IntegerBox detectHeight;

	private Label cascadeLabel;

	private EasyCellTableObjects<StageResult> stageResultObjects;

	private StageControler stageControler;
	private Vol createVolFromImageData(ImageData imageData){
		//return createGrayscaleImageVolFromImageData(imageData);
		return createLBPDepthVolFromImageData(imageData);
	}
	private Vol createNewVol(){
		return ConvnetJs.createVol(1, 1, 8*lbpDataSplit*lbpDataSplit, 0);
	}
	private Vol createLBPDepthVolFromImageData(ImageData imageData){
		if(imageData.getWidth()!=netWidth+edgeSize || imageData.getHeight()!=netHeight+edgeSize){ //somehow still 26x26 don't care!
			Window.alert("invalid size:"+imageData.getWidth()+","+imageData.getHeight()+" expected "+(netWidth+edgeSize)+"x"+(netHeight+edgeSize));
			return null;
		}
		
		
		//simple lbp return 8 data
		
		//LBP here,now no effect on but edge problem possible happen
		int[][] ints=byteDataIntConverter.convert(imageData); //convert color image to grayscale data
		int[][] data=lbpConverter.convert(ints);
		
		Vol vol=createNewVol();
		
		int[] retInt=BinaryPattern.dataToBinaryPattern(data,lbpDataSplit,edgeSize,edgeSize);
		//set vol
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
		
		int r=getRandom(0, 100);
		if(r==50){
			//LogUtils.log(Ints.join(",", retInt));
		}
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
		return vol;
		
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
			rects=generateRect(image, 4, 1.4);
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
