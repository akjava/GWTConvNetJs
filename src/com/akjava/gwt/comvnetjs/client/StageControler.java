package com.akjava.gwt.comvnetjs.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ResizeUtils;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageData;
import com.akjava.lib.common.graphics.IntRect;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.user.client.Timer;

public abstract class StageControler {

	private boolean doing;
	public static final int MODE_INITIAL=0;
	public static final int MODE_VARIATION=1;
	public static final int MODE_REPEAT=2;
	public static final int MODE_SEARCH_PASSED_IMAGES=3;
	private int mode;
	
	private int ratio;
	protected int learningTime;
	protected List<StageResult> stageResults;
	public List<StageResult> getStageResults() {
		return stageResults;
	}


	private int stage;
	private boolean canceled;
	
	private List<PhaseData> phaseDatas;
	
	private PhaseData phaseData;
	
	private ScoreGroup stageScoreGroup;
	private ScoreGroup phrageScoreGroup;
	
	private int variationSize=0;
	private int needPassedImageSize;
	private boolean initialImageSearch;
	private long searched;
	public boolean isCanceled() {
		return canceled;
	}
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
	
	private void setInitialSearchPassedImageMode(boolean initial){
		mode=MODE_SEARCH_PASSED_IMAGES;
		
		initialImageSearch=initial;
		if(initial){
			needPassedImageSize=getNegativeCount()+(int) (getPositiveCount()*(1.0/ratio));
			LogUtils.log("start-searching passed images:"+needPassedImageSize);
		}else{
			needPassedImageSize=(int) (getPositiveCount()*(1.0/ratio));
			LogUtils.log("start-searching passed images-variation:"+(needPassedImageSize));
		}
		
		searched=0;
		sendInfo("search-negative:");
	}
	
	int called;
	int skipRate=10;
	public void start(final LearningInfo learningInfo){
		LogUtils.log("stage controler-started");
		checkState(doing==false,"error somehow other thread runnning.");
		//initialize;
		stageResults=Lists.newArrayList();
		stage=0;
		ratio=learningInfo.maxRatio;
		learningTime=0;
		
		phaseDatas=Lists.newArrayList();
		
		variationSize=learningInfo.minVariation;
		
		phaseData=new PhaseData(new PhaseInfo(variationSize,ratio,learningInfo.matchRate,learningInfo.falseRate));
		phaseDatas.add(phaseData);
		
		phrageScoreGroup=new ScoreGroup();
		stageScoreGroup=new ScoreGroup();
		
		final Score targetScore=new Score(learningInfo.firstMatchRate,learningInfo.firstFalseRate);
		final Stopwatch totalWatch=Stopwatch.createStarted();
		
		
		setInitialSearchPassedImageMode(true);
		
		Timer timer=new Timer(){
			@Override
			public void run() {
				if(canceled){
					sendInfo("cancelled:by user");
					LogUtils.log("cancelling(but can't stop until initial training)");
					doing=false;
					cancel();
					onEndTraining(false);
					return;
				}
				
				if(doing){
					return;
				}
				
				//skip check
				called++;
				if(called==skipRate){
					called=0;
					return;//skip for gc
				}
				
				//ratio check
				if(mode==MODE_INITIAL && ratio<learningInfo.minRatio){
					sendInfo("cancelled:reach ratio");
					onEndTraining(false);
					cancel();//timer
					doing=false;
					return;
				}
				
				
				
				doing=true;
				
				boolean needFinalyzePhrase=false;
				
				Score score=null;
				if(mode==MODE_INITIAL){
					LogUtils.log("initial-training:ratio="+ratio);
					doTraining(ratio, true);//this trainedScore not have result,because do test search-image first
					
					variationSize=0;//not learningInfo.minVariation,only do step by step
					learningTime=0;
					mode=MODE_REPEAT;
					sendInfo("repeat-learning:");
					//how many need? test(100) + pos x ratio
				}else if(mode==MODE_REPEAT){
					score=repeating();
					learningTime++;
					
					//skip reapeat lower variation
					if(learningInfo.minVariation>variationSize || learningTime==learningInfo.maxLearning){
						
						boolean initial=false;
						
						
						//ratio has already incresed;
						if(variationSize==learningInfo.maxVariation){
							ratio--;
							//mode=MODE_INITIAL;//TODO switch to MODE_SEARCH_PASSED_IMAGES
							initial=true;
						}
						
						setInitialSearchPassedImageMode(initial);
						
						needFinalyzePhrase=true;
					}
				}else if(mode==MODE_VARIATION){
					
					variationSize++;
					LogUtils.log("variation-training:ratio="+ratio+",variation="+(variationSize));
					doTraining(ratio, false);
					
					
					
					learningTime=0;
					mode=MODE_REPEAT;
					
					sendInfo("repeat-learning:");
					
					
				}else if(mode==MODE_SEARCH_PASSED_IMAGES){
					
					int imageSize=searchPassedImages(initialImageSearch,needPassedImageSize,searched);//just call
					if(imageSize>=needPassedImageSize){
						LogUtils.log("finish-searching passed images:");
						sendInfo("training:");
						if(initialImageSearch){
							mode=MODE_INITIAL;
						}else{
							mode=MODE_VARIATION;
						}
						
						}
					searched++;
					
				}
				
				//score check
				if(score!=null){//basically null score never happen
					
					//compare each score
					phrageScoreGroup.bestMatchRateScore=getBetterMatchRate(phrageScoreGroup.bestMatchRateScore, score);
					if(phrageScoreGroup.bestMatchRateScore==score){
						stageScoreGroup.bestMatchRateScore=getBetterMatchRate(phrageScoreGroup.bestMatchRateScore, stageScoreGroup.bestMatchRateScore);
						stageScoreGroup.bestFalseRateJson=makeJson();
					}
					
					phrageScoreGroup.bestFalseRateScore=getBetterFalseRate(phrageScoreGroup.bestFalseRateScore, score);
					if(phrageScoreGroup.bestFalseRateScore==score){
						stageScoreGroup.bestFalseRateScore=getBetterFalseRate(phrageScoreGroup.bestFalseRateScore, stageScoreGroup.bestFalseRateScore);
						stageScoreGroup.bestFalseRateJson=makeJson();
					}
					
					Optional<Score> rateOptional=getBetterMatchRateOnClearFalseRate(learningInfo.matchRate, phrageScoreGroup.bestMatchRateScoreOnClearFalseRate, score);
					for(Score sc:rateOptional.asSet()){
						phrageScoreGroup.bestMatchRateScoreOnClearFalseRate=sc;
						if(phrageScoreGroup.bestMatchRateScoreOnClearFalseRate==score){
							//this optional must present.
							stageScoreGroup.bestMatchRateScoreOnClearFalseRate=getBetterMatchRateOnClearFalseRate(learningInfo.matchRate,phrageScoreGroup.bestMatchRateScoreOnClearFalseRate, stageScoreGroup.bestMatchRateScoreOnClearFalseRate).get();
							stageScoreGroup.bestMatchRateScoreOnClearFalseRateJson=makeJson();
						}
					}
					
					Optional<Score> falseOptional=getBetterFalseRateOnClearMatchRate(learningInfo.falseRate, phrageScoreGroup.bestFalseRateScoreOnClearMatchRate, score);
					for(Score sc:falseOptional.asSet()){
						phrageScoreGroup.bestFalseRateScoreOnClearMatchRate=sc;
						if(phrageScoreGroup.bestFalseRateScoreOnClearMatchRate==score){
							//this optional must present.
							stageScoreGroup.bestFalseRateScoreOnClearMatchRate=getBetterFalseRateOnClearMatchRate(learningInfo.falseRate,phrageScoreGroup.bestFalseRateScoreOnClearMatchRate, stageScoreGroup.bestFalseRateScoreOnClearMatchRate).get();
							stageScoreGroup.bestFalseRateScoreOnClearMatchRateJson=makeJson();
						}
					}
					
					
					
					
					if(clearScore(targetScore,score)){
						//finalyze stage action
						needFinalyzePhrase=true;
						
						
						StageResult stageResult=new StageResult();
						
						stageResult.stage=stage+1;//value start 0,but label start 1
						stageResult.phaseDatas=phaseDatas;
						//stage group is phaseed on phase action
						stageResult.scoreGroup=stageScoreGroup;
						stageScoreGroup=new ScoreGroup();
						
						int t=0;
						for(PhaseData data:phaseDatas){
							t+=data.consumeSecond;
						}
						stageResult.consumeSecond=t;
						
						
						phaseDatas=Lists.newArrayList();//clear phrase data
						
						stageResult.score=score;
						stageResult.json=makeJson();
						
						stageResults.add(stageResult);
						if(stageResults.size()==learningInfo.maxStage){
							
							sendInfo("finished:");
							onEndTraining(true);
							cancel();//timer
							
						}else{
							stage++;
							createNewStage();
							
							//ratio=learningInfo.maxRatio; //stop reset ratio when stage clear
							
							targetScore.falseRate=learningInfo.falseRate;//switch first special value to natural value
							targetScore.matchRate=learningInfo.matchRate;
							setInitialSearchPassedImageMode(true);
							
						}
					}
					
				}
				
				if(needFinalyzePhrase){
					//score
					phaseData.scoreGroup=phrageScoreGroup;
					phrageScoreGroup=new ScoreGroup();
					//learning
					phaseData.learningCount=learningTime;
					learningTime=0;
					//time
					long second=totalWatch.elapsed(TimeUnit.SECONDS);
					totalWatch.reset();totalWatch.start();
					phaseData.consumeSecond=(int)second;
					phaseData.phaseInfo.variationSize=variationSize;//update late
					//TODO more info
					LogUtils.log("phase-end:time="+phaseData.consumeSecond+"s,learning="+phaseData.learningCount);
					
					phaseData=new PhaseData(new PhaseInfo(variationSize,ratio,learningInfo.matchRate,learningInfo.falseRate));
					phaseDatas.add(phaseData);
				}
				
				doing=false;
			}
		};
		
		timer.scheduleRepeating(10);
		
		
		
		
	}
	
	public static Score createMinScore(){
		return new Score(0,1);
	}
	
	public static  Score getBetterFalseRate(Score scoreA,Score scoreB){
		
		if(scoreA.falseRate==scoreB.falseRate){
			if(scoreA.matchRate>scoreB.matchRate){
				return scoreA;
			}else{
				return scoreB;
			}
		}
		else if(scoreA.falseRate<scoreB.falseRate){
			return scoreA;
		}else{
			return scoreB;
		}
	}
	
	public static Optional<Score> getBetterFalseRateOnClearMatchRate(double matchRate,Score scoreA,Score scoreB){
		if(scoreA.matchRate<matchRate && scoreB.matchRate<matchRate){
			return Optional.absent();
		}
		
		if(scoreA.matchRate>=matchRate && scoreB.matchRate>=matchRate){
			return Optional.of(getBetterFalseRate(scoreA,scoreB));
		}else{
			if(scoreA.matchRate>=matchRate){
				return Optional.of(scoreA);
			}else{
			return Optional.of(scoreB);
			}
			
		}
	}
	
	public static  Score getBetterMatchRate(Score scoreA,Score scoreB){
		if(scoreA.matchRate==scoreB.matchRate){
			 if(scoreA.falseRate<scoreB.falseRate){
					return scoreA;
				}else{
					return scoreB;
				}
		}
		else if(scoreA.matchRate>scoreB.matchRate){
			return scoreA;
		}else{
			return scoreB;
		}
	}
	public static Optional<Score> getBetterMatchRateOnClearFalseRate(double falseRate,Score scoreA,Score scoreB){
		if(scoreA.falseRate>falseRate && scoreB.falseRate>falseRate){
			return Optional.absent();
		}
		
		if(scoreA.falseRate<=falseRate && scoreB.falseRate<=falseRate){
			return Optional.of(getBetterMatchRate(scoreA,scoreB));
		}else{
			if(scoreA.falseRate<=falseRate){
				return Optional.of(scoreA);
			}else{
			return Optional.of(scoreB);
			}
			
		}
	}
	
	public boolean clearScore(Score targetScore,Score score){
		return score.matchRate>=targetScore.matchRate && score.falseRate<=targetScore.falseRate;
	}
	public abstract void onEndTraining(boolean reachedGoal);
	
	public abstract void createNewStage();
	
	public abstract Score doTraining(int positiveRatio, boolean initial);
	public abstract Score repeating();
	public abstract String makeJson();
	public abstract int searchPassedImages(boolean hasNoVariety,int needPassedImageSize,long searched);
	
	public abstract int getPositiveCount();
	public abstract int getNegativeCount();
	public abstract void sendInfo(String message);
	
	public static class StageResult{
	private int stage;
	public int getStage() {
		return stage;
	}
	private int consumeSecond;
	private List<PhaseData> phaseDatas;
	private ScoreGroup scoreGroup;
	private String json;
	private Score score;
	public int getConsumeSecond() {
		return consumeSecond;
	}
	public List<PhaseData> getPhaseDatas() {
		return phaseDatas;
	}
	public ScoreGroup getScoreGroup() {
		return scoreGroup;
	}
	public String getJson() {
		return json;
	}
	public Score getScore() {
		return score;
	}

	}
	
	public static class Score{
		
		private double matchRate;
		public double getMatchRate() {
			return matchRate;
		}

		public double getFalseRate() {
			return falseRate;
		}
		public Score(double matchRate, double falseRate) {
			this.matchRate = matchRate;
			this.falseRate = falseRate;
		}

		
		private double falseRate;
		
		public String toString(){
			return "matchRate="+matchRate+",falseRate="+falseRate;
		}
	}
	
	public static class PhaseInfo{
		public PhaseInfo(int variationSize, int positiveRatio, double matchRate, double falseRate) {
			super();
			this.variationSize = variationSize;
			this.positiveRatio = positiveRatio;
			this.matchRate = matchRate;
			this.falseRate = falseRate;
		}
		private int variationSize;
		public int getVariationSize() {
			return variationSize;
		}
		private int positiveRatio;
		public int getPositiveRatio() {
			return positiveRatio;
		}
		private double matchRate;
		private double falseRate;
	}
	
	public static class ScoreGroup{
		public ScoreGroup(){
			bestMatchRateScore=createMinScore();
			bestFalseRateScore=createMinScore();
			
			bestFalseRateScoreOnClearMatchRate=createMinScore();
			bestMatchRateScoreOnClearFalseRate=createMinScore();
		}
		private Score bestMatchRateScore;
		public Score getBestMatchRateScore() {
			return bestMatchRateScore;
		}
		public Score getBestFalseRateScore() {
			return bestFalseRateScore;
		}
		private Score bestFalseRateScore;
		
		private Score bestFalseRateScoreOnClearMatchRate;
		private Score bestMatchRateScoreOnClearFalseRate;
		
		private String bestMatchRateJson;
		private String bestFalseRateJson;
		
		private String bestFalseRateScoreOnClearMatchRateJson;
		private String bestMatchRateScoreOnClearFalseRateJson;
	}
	
	public static class PhaseData{
		private int consumeSecond;
		public int getConsumeSecond() {
			return consumeSecond;
		}
		private PhaseInfo phaseInfo;
		public PhaseInfo getPhaseInfo() {
			return phaseInfo;
		}
		public PhaseData(PhaseInfo phaseInfo) {
			super();
			this.phaseInfo = phaseInfo;
		}
		private ScoreGroup scoreGroup;
		private int learningCount;
		public int getLearningCount() {
			return learningCount;
		}
		private boolean cleared;
	}
	
	
	
	public static class LearningInfo{
		public LearningInfo(int maxStage, double matchRate, double falseRate, double firstMatchRate,double firstFalseRate, int minRatio, int maxRatio, int minVariation,int maxVariation, int maxLearning) {
			super();
			this.maxStage = maxStage;
			this.matchRate = matchRate;
			this.firstMatchRate = firstMatchRate;
			this.falseRate = falseRate;
			this.firstFalseRate = firstFalseRate;
			this.minRatio = minRatio;
			this.maxRatio = maxRatio;
			this.minVariation=minVariation;
			this.maxVariation = maxVariation;
			this.maxLearning = maxLearning;
		}
		private int minVariation;
		private int maxStage;
		private double firstMatchRate;
		private double matchRate;
		private double falseRate;
		private double firstFalseRate;//
		
		private int minRatio;
		private int maxRatio;
		private int maxVariation;
		private int maxLearning;
	}
	
	
}
