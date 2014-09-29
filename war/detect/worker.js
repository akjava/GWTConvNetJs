(function(){
var $wnd;var $doc;var $workergwtbridge;var $moduleName, $moduleBase;
if(typeof(window) != 'undefined'){ $wnd = window;  $doc = $wnd.document; }
else{ $wnd = {JSON: JSON}; }
var $gwt_version = "2.5.1";var _, seedTable = {}, Q$Object = 0, Q$String = 1, Q$Throwable = 2, CM$ = {};
function newSeed(id){
  return new seedTable[id];
}

function defineSeed(id, superSeed, castableTypeMap){
  var seed = seedTable[id];
  if (seed && !seed.___clazz$) {
    _ = seed.prototype;
  }
   else {
    !seed && (seed = seedTable[id] = function(){
    }
    );
    _ = seed.prototype = superSeed < 0?{}:newSeed(superSeed);
    _.castableTypeMap$ = castableTypeMap;
  }
  for (var i_0 = 3; i_0 < arguments.length; ++i_0) {
    arguments[i_0].prototype = _;
  }
  if (seed.___clazz$) {
    _.___clazz$ = seed.___clazz$;
    seed.___clazz$ = null;
  }
}

function makeCastMap(a){
  var result = {};
  for (var i_0 = 0, c = a.length; i_0 < c; ++i_0) {
    result[a[i_0]] = 1;
  }
  return result;
}

function nullMethod(){
}

defineSeed(1, -1, CM$);
_.typeMarker$ = nullMethod;
function $exportBridge(){
  window = self;
  $wnd = self;
  self.importScripts('/js/convnet.js');
  $workergwtbridge = function(str){
    detect(str);
  }
  ;
}

function detect(param){
  var binaryPattern, flipped, flippedPattern, i_0, imageData, j, minH, minW, min_scale, nets, numbers, rect, resized, result, resultRect, scale_factor, stepScale, turnAngles, turned, turnedVol, useHorizontalFlip, vol;
  resultRect = [];
  nets = jsonToNet(param.json);
  imageData = param.imageData;
  useHorizontalFlip = param.useHorizontalFlip;
  turnAngles = param.turnAngles;
  !turnAngles && (turnAngles = []);
  if (!param.rects) {
    numbers = param.detectOption;
    stepScale = 4;
    scale_factor = 1.6;
    minW = 24;
    minH = 24;
    min_scale = 1.2;
    if (numbers) {
      numbers.length > 0 && (stepScale = round_int(numbers[0]));
      numbers.length > 1 && (scale_factor = numbers[1]);
      numbers.length > 2 && (minW = round_int(numbers[2]));
      numbers.length > 3 && (minH = round_int(numbers[3]));
      numbers.length > 4 && (min_scale = numbers[4]);
    }
    $setRects(param, generateHaarRect(imageData.width, imageData.height, stepScale, scale_factor, minW, minH, min_scale));
  }
  for (i_0 = 0; i_0 < param.rects.length; ++i_0) {
    rect = param.rects[i_0];
    resized = resizeBilinearRedOnly(imageData, rect.x, rect.y, rect.width, rect.height);
    binaryPattern = createLBPDepthFromUint8ArrayPacked(resized);
    vol = createVolFromIndexes(binaryPattern, ($clinit_GWTConvNetJs() , 64));
    result = passAll(nets, vol);
    if (result != -1) {
      rect.confidence = result;
      $push(resultRect, rect);
    }
    if (result == -1) {
      for (j = 0; j < turnAngles.length; ++j) {
        turned = turn3x3(binaryPattern, round_int(turnAngles[j]));
        turnedVol = createVolFromIndexes(turned, 64);
        result = passAll(nets, turnedVol);
        if (result != -1) {
          rect.confidence = result;
          $push(resultRect, rect);
          break;
        }
      }
    }
    if (result == -1 && useHorizontalFlip) {
      flippedPattern = flipHorizontal(binaryPattern);
      flipped = createVolFromIndexes(flippedPattern, 64);
      result = passAll(nets, flipped);
      if (result != -1) {
        rect.confidence = result;
        $push(resultRect, rect);
      }
       else {
        for (j = 0; j < turnAngles.length; ++j) {
          turned = turn3x3(flippedPattern, round_int(turnAngles[j]));
          turnedVol = createVolFromIndexes(turned, 64);
          result = passAll(nets, turnedVol);
          if (result != -1) {
            rect.confidence = result;
            $push(resultRect, rect);
            break;
          }
        }
      }
    }
  }
  self.postMessage(resultRect);
}

function jsonToNet(json){
  var jsonObject = JSON.parse(json);
  var netsJson = jsonObject.nets;
  var result = [];
  for (i = 0; i < netsJson.length; i++) {
    var net2 = new convnetjs.Net;
    net2.fromJSON(netsJson[i]);
    result.push(net2);
  }
  return result;
}

function passAll(nets, vol){
  var i_0, r, result;
  r = 0;
  for (i_0 = 0; i_0 < nets.length; ++i_0) {
    result = nets[i_0].forward(vol);
    if (!isZeroIndexMostMatched(result)) {
      return -1;
    }
    r = result.w[0];
  }
  return r;
}

function isZeroIndexMostMatched(result){
  var first, i_0;
  first = result.w[0];
  for (i_0 = 1; i_0 < result.w.length; ++i_0) {
    if (first < result.w[i_0]) {
      return false;
    }
  }
  return true;
}

function $clinit_GWTConvNetJs(){
  $clinit_GWTConvNetJs = nullMethod;
  lbpConverter = new SimpleLBP_0;
  new Stopwatch_0;
  new Stopwatch_0;
  new Stopwatch_0;
  new Stopwatch_0;
  new Stopwatch_0;
}

function createLBPDepthFromUint8ArrayPacked(array){
  $clinit_GWTConvNetJs();
  var index, ints, retInt, x, y;
  ints = initDims([_3_3I_classLit, _3I_classLit], [CM$, CM$], [Q$Object, -1], [28, 28], 2, 1);
  for (x = 0; x < ints[0].length; ++x) {
    for (y = 0; y < ints.length; ++y) {
      index = y * ints[0].length + x;
      ints[y][x] = array[index];
    }
  }
  retInt = $dataToBinaryPattern(lbpConverter, ints);
  return retInt;
}

function createVolFromIndexes(indexes, maxValue){
  $clinit_GWTConvNetJs();
  var half, i_0, v, vol;
  half = ~~(maxValue / 2);
  vol = new $wnd.convnetjs.Vol(1, 1, 72, 0);
  for (i_0 = 0; i_0 < indexes.length; ++i_0) {
    v = indexes[i_0] / half - 1;
    (v > 1 || v < -1) && log('invalid:' + v + ',maxValue=' + maxValue);
    vol.set(0, 0, i_0, v);
  }
  return vol;
}

var lbpConverter;
function generateHaarRect(imageW, imageH, stepScale, scale_factor, minW, minH, min_scale){
  var rects;
  rects = [];
  while (minW * min_scale < imageW && minH * min_scale < imageH) {
    generateHaarRect_0(minW, minH, stepScale, min_scale, rects, imageW, imageH);
    min_scale *= scale_factor;
  }
  return rects;
}

function generateHaarRect_0(minW, minH, stepScale, scale, rects, imageW, imageH){
  var clipHeight, clipWidth, dx, dy, endX, endY, sharedRect, step_x, x, y;
  clipWidth = round_int(minW * scale);
  clipHeight = round_int(minH * scale);
  step_x = (0.5 * scale + 1.5) * stepScale;
  sharedRect = new Rect_0(clipWidth, clipHeight);
  endX = imageW - clipWidth;
  endY = imageH - clipHeight;
  for (x = 0; x < endX; x += step_x) {
    for (y = 0; y < endY; y += step_x) {
      dx = round_int(x);
      dy = round_int(y);
      sharedRect.x_0 = dx;
      sharedRect.y_0 = dy;
      $push(rects, {x:sharedRect.x_0, y:sharedRect.y_0, width:sharedRect.width_0, height:sharedRect.height_0});
    }
  }
}

function $setRects(this$static, param){
  this$static.rects = param;
}

function log(object){
  if (navigator.appName == 'Microsoft Internet Explorer') {
    return;
  }
  console && console.log(object);
}

function resizeBilinearRedOnly(imageData, ax, ay, w, h){
  var a, array, b, c, d, i_0, index, iw, j, offset, result, value, x, x_diff, x_ratio, y, y_diff, y_ratio;
  array = imageData.data;
  result = new Uint8Array(784);
  x_ratio = (w - 1) / 28;
  y_ratio = (h - 1) / 28;
  offset = 0;
  iw = imageData.width;
  for (i_0 = 0; i_0 < 28; ++i_0) {
    for (j = 0; j < 28; ++j) {
      x = round_int(x_ratio * j);
      y = round_int(y_ratio * i_0);
      x_diff = x_ratio * j - x;
      y_diff = y_ratio * i_0 - y;
      index = (ay + y) * iw + x + ax;
      a = array[index * 4];
      b = array[(index + 1) * 4];
      c = array[(index + iw) * 4];
      d = array[(index + iw + 1) * 4];
      value = (a & 255) * (1 - x_diff) * (1 - y_diff) + (b & 255) * x_diff * (1 - y_diff) + (c & 255) * y_diff * (1 - x_diff) + (d & 255) * x_diff * y_diff;
      result[offset] = round_int(value);
      ++offset;
    }
  }
  return result;
}

function $clinit_SimpleLBP(){
  $clinit_SimpleLBP = nullMethod;
  atx = initValues(_3I_classLit, CM$, -1, [-1, 0, 1, 1, 1, 0, -1, -1]);
  aty = initValues(_3I_classLit, CM$, -1, [-1, -1, -1, 0, 1, 1, 1, 0]);
  turnoffsets = initDims([_3_3_3Ljava_lang_Integer_2_classLit, _3_3Ljava_lang_Integer_2_classLit, _3Ljava_lang_Integer_2_classLit], [CM$, CM$, CM$], [Q$Object, Q$Object, Q$Object], [3, 3, 8], 3, 0);
}

function $dataToBinaryPattern(this$static, arrays){
  var center, centers, h, i_0, offx, offy, otherValue, resultH, resultW, retIndexOffset, retInt, retX, retY, w, x, y;
  w = arrays[0].length;
  h = arrays.length;
  resultW = ~~((w - 4) / 3);
  resultH = ~~((h - 4) / 3);
  retInt = initDim(_3I_classLit, CM$, -1, 72, 1);
  for (x = 2; x < w - 2; ++x) {
    for (y = 2; y < h - 2; ++y) {
      centers = $getAroundValues(this$static, x, y, arrays);
      center = $getCenterValue(this$static, centers);
      retX = ~~((x - 2) / resultW);
      retY = ~~((y - 2) / resultH);
      retIndexOffset = 8 * (retY * 3 + retX);
      for (i_0 = 0; i_0 < atx.length; ++i_0) {
        offx = 1 + atx[i_0];
        offy = 1 + aty[i_0];
        this$static.neighbor == 1?(otherValue = centers[offy][offx]):(otherValue = $getOtherValue(arrays, x + atx[i_0] * this$static.neighbor, y + aty[i_0] * this$static.neighbor));
        otherValue > center && ++retInt[i_0 + retIndexOffset];
      }
    }
  }
  return retInt;
}

function $getAroundValues(this$static, tx, ty, arrays){
  var i_0, offx, offy;
  for (i_0 = 0; i_0 < atx.length; ++i_0) {
    this$static.container[1][1] = arrays[ty][tx];
    offx = tx + atx[i_0];
    offy = ty + aty[i_0];
    offx >= 0 && offy >= 0 && offx < arrays[0].length && offy < arrays.length?(this$static.container[1 + aty[i_0]][1 + atx[i_0]] = arrays[offy][offx]):(this$static.container[1 + aty[i_0]][1 + atx[i_0]] = -1);
  }
  return this$static.container;
}

function $getCenterValue(this$static, around){
  var exists, total, x, y;
  if (this$static.useImprovedLBP) {
    total = 0;
    exists = 0;
    for (x = 0; x < 3; ++x) {
      for (y = 0; y < 3; ++y) {
        if (around[y][x] >= 0) {
          total += around[y][x];
          ++exists;
        }
      }
    }
    return ~~(total / exists);
  }
   else {
    return around[1][1];
  }
}

function $getOtherValue(arrays, offx, offy){
  if (offx < 0 || offx >= arrays.length || offy < 0 || offy >= arrays[0].length) {
    return -1;
  }
  return arrays[offy][offx];
}

function SimpleLBP_0(){
  $clinit_SimpleLBP();
  this.useImprovedLBP = true;
  this.neighbor = 2;
  this.container = initDims([_3_3I_classLit, _3I_classLit], [CM$, CM$], [Q$Object, -1], [3, 3], 2, 1);
}

function findNewTurnOffset(sx, sy, move){
  var i_0, v, x, y;
  if (turnoffsets[sx][sy][move]) {
    return turnoffsets[sx][sy][move].value;
  }
  x = sx;
  y = sy;
  if (sx != 1 || sy != 1) {
    for (i_0 = 0; i_0 < move; ++i_0) {
      x == 0?y > 0?--y:++x:x == 1?y == 0?++x:--x:x == 2 && (y < 2?++y:--x);
    }
  }
  v = (y * 3 + x) * 8;
  turnoffsets[sx][sy][move] = valueOf(v);
  return v;
}

function flipHorizontal(binaryPattern){
  $clinit_SimpleLBP();
  var converted, destOffset, i_0, nindex, srcOffset, x, y;
  checkState(binaryPattern.length == 72);
  converted = initDim(_3I_classLit, CM$, -1, binaryPattern.length, 1);
  for (y = 0; y < 3; ++y) {
    for (x = 0; x < 3; ++x) {
      srcOffset = (y * 3 + x) * 8;
      destOffset = (y * 3 + (2 - x)) * 8;
      for (i_0 = 0; i_0 < 8; ++i_0) {
        nindex = 0;
        switch (i_0) {
          case 0:
            nindex = 2;
            break;
          case 1:
            nindex = 1;
            break;
          case 2:
            nindex = 0;
            break;
          case 3:
            nindex = 7;
            break;
          case 4:
            nindex = 6;
            break;
          case 5:
            nindex = 5;
            break;
          case 6:
            nindex = 4;
            break;
          case 7:
            nindex = 3;
        }
        converted[destOffset + nindex] = binaryPattern[srcOffset + i_0];
      }
    }
  }
  return converted;
}

function turn3x3(binaryPattern, angle){
  $clinit_SimpleLBP();
  var destOffset, i_0, move, newIndex, result, srcOffset, x, y;
  if (binaryPattern.length != 72) {
    return null;
  }
  result = initDim(_3I_classLit, CM$, -1, 72, 1);
  angle < 0 && (angle = 360 + angle);
  move = ~~(angle / 45);
  for (y = 0; y < 3; ++y) {
    for (x = 0; x < 3; ++x) {
      srcOffset = (y * 3 + x) * 8;
      destOffset = findNewTurnOffset(x, y, move);
      for (i_0 = 0; i_0 < 8; ++i_0) {
        newIndex = i_0 + move;
        newIndex >= 8 && (newIndex -= 8);
        result[destOffset + newIndex] = binaryPattern[srcOffset + i_0];
      }
    }
  }
  return result;
}

defineSeed(20, 1, {}, SimpleLBP_0);
_.container = null;
_.neighbor = 0;
_.useImprovedLBP = true;
var atx, aty, turnoffsets;
function Rect_0(width, height){
  this.x_0 = 0;
  this.y_0 = 0;
  this.width_0 = width;
  this.height_0 = height;
}

defineSeed(21, 1, {}, Rect_0);
_.height_0 = 0;
_.width_0 = 0;
_.x_0 = 0;
_.y_0 = 0;
function checkNotNull(reference){
  if (!reference) {
    throw new NullPointerException_1;
  }
  return reference;
}

function checkState(expression){
  if (!expression) {
    throw new IllegalStateException_0;
  }
}

function Stopwatch_0(){
  Stopwatch_1.call(this, ($clinit_Ticker() , $clinit_Ticker() , SYSTEM_TICKER));
}

function Stopwatch_1(ticker){
  checkNotNull(ticker);
}

defineSeed(23, 1, {}, Stopwatch_0);
function $clinit_Ticker(){
  $clinit_Ticker = nullMethod;
  SYSTEM_TICKER = new Ticker$1_0;
}

defineSeed(24, 1, {});
var SYSTEM_TICKER;
function Ticker$1_0(){
}

defineSeed(25, 24, {}, Ticker$1_0);
function $setStackTrace(stackTrace){
  var c, copy, i_0;
  copy = initDim(_3Ljava_lang_StackTraceElement_2_classLit, CM$, Q$Object, stackTrace.length, 0);
  for (i_0 = 0 , c = stackTrace.length; i_0 < c; ++i_0) {
    if (!stackTrace[i_0]) {
      throw new NullPointerException_0;
    }
    copy[i_0] = stackTrace[i_0];
  }
}

defineSeed(32, 1, makeCastMap([Q$Throwable]));
defineSeed(31, 32, makeCastMap([Q$Throwable]));
defineSeed(30, 31, makeCastMap([Q$Throwable]));
function JavaScriptException_0(e){
  $fillInStackTrace();
  this.e = e;
  $createStackTrace(this);
}

defineSeed(29, 30, makeCastMap([Q$Throwable]), JavaScriptException_0);
_.e = null;
function $push(this$static, value){
  this$static[this$static.length] = value;
}

defineSeed(36, 1, {});
function apply(jsFunction, thisObj, args){
  return jsFunction.apply(thisObj, args);
  var __0;
}

function enter(){
  var now;
  if (entryDepth != 0) {
    now = (new Date).getTime();
    if (now - watchdogEntryDepthLastScheduled > 2000) {
      watchdogEntryDepthLastScheduled = now;
      watchdogEntryDepthTimerId = watchdogEntryDepthSchedule();
    }
  }
  if (entryDepth++ == 0) {
    $flushEntryCommands(($clinit_SchedulerImpl() , INSTANCE));
    return true;
  }
  return false;
}

function entry(jsFunction){
  return function(){
    try {
      return entry0(jsFunction, this, arguments);
    }
     catch (e) {
      throw e;
    }
  }
  ;
}

function entry0(jsFunction, thisObj, args){
  var initialEntry;
  initialEntry = enter();
  try {
    return apply(jsFunction, thisObj, args);
  }
   finally {
    exit(initialEntry);
  }
}

function exit(initialEntry){
  initialEntry && $flushFinallyCommands(($clinit_SchedulerImpl() , INSTANCE));
  --entryDepth;
  if (initialEntry) {
    if (watchdogEntryDepthTimerId != -1) {
      watchdogEntryDepthCancel(watchdogEntryDepthTimerId);
      watchdogEntryDepthTimerId = -1;
    }
  }
}

function watchdogEntryDepthCancel(timerId){
  $wnd.clearTimeout(timerId);
}

function watchdogEntryDepthSchedule(){
  return $wnd.setTimeout(function(){
    entryDepth != 0 && (entryDepth = 0);
    watchdogEntryDepthTimerId = -1;
  }
  , 10);
}

var entryDepth = 0, watchdogEntryDepthLastScheduled = 0, watchdogEntryDepthTimerId = -1;
function $clinit_SchedulerImpl(){
  $clinit_SchedulerImpl = nullMethod;
  INSTANCE = new SchedulerImpl_0;
}

function $flushEntryCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.entryCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.entryCommands;
      this$static.entryCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.entryCommands);
    this$static.entryCommands = rescheduled;
  }
}

function $flushFinallyCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.finallyCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.finallyCommands;
      this$static.finallyCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.finallyCommands);
    this$static.finallyCommands = rescheduled;
  }
}

function SchedulerImpl_0(){
}

function push(queue, task){
  !queue && (queue = []);
  $push(queue, task);
  return queue;
}

function runScheduledTasks(tasks, rescheduled){
  var i_0, j, t;
  for (i_0 = 0 , j = tasks.length; i_0 < j; ++i_0) {
    t = tasks[i_0];
    try {
      t[1]?t[0].nullMethod() && (rescheduled = push(rescheduled, t)):t[0].nullMethod();
    }
     catch ($e0) {
      $e0 = caught($e0);
      if (!instanceOf($e0, Q$Throwable))
        throw $e0;
    }
  }
  return rescheduled;
}

defineSeed(38, 36, {}, SchedulerImpl_0);
_.entryCommands = null;
_.finallyCommands = null;
var INSTANCE;
function extractNameFromToString(fnToString){
  var index, start, toReturn;
  toReturn = '';
  fnToString = $trim(fnToString);
  index = fnToString.indexOf('(');
  start = fnToString.indexOf('function') == 0?8:0;
  if (index == -1) {
    index = $indexOf(fnToString, String.fromCharCode(64));
    start = fnToString.indexOf('function ') == 0?9:0;
  }
  index != -1 && (toReturn = $trim(fnToString.substr(start, index - start)));
  return toReturn.length > 0?toReturn:'anonymous';
}

function splice(arr, length_0){
  arr.length >= length_0 && arr.splice(0, length_0);
  return arr;
}

function $createStackTrace(e){
  var i_0, j, stack, stackTrace;
  stack = $inferFrom(instanceOfJso(e.e)?dynamicCastJso(e.e):null);
  stackTrace = initDim(_3Ljava_lang_StackTraceElement_2_classLit, CM$, Q$Object, stack.length, 0);
  for (i_0 = 0 , j = stackTrace.length; i_0 < j; ++i_0) {
    stackTrace[i_0] = new StackTraceElement_0(stack[i_0]);
  }
  $setStackTrace(stackTrace);
}

function $fillInStackTrace(){
  var i_0, j, stack, stackTrace;
  stack = splice($inferFrom($makeException()), 2);
  stackTrace = initDim(_3Ljava_lang_StackTraceElement_2_classLit, CM$, Q$Object, stack.length, 0);
  for (i_0 = 0 , j = stackTrace.length; i_0 < j; ++i_0) {
    stackTrace[i_0] = new StackTraceElement_0(stack[i_0]);
  }
  $setStackTrace(stackTrace);
}

function $makeException(){
  try {
    null.a();
  }
   catch (e) {
    return e;
  }
}

function $inferFrom(e){
  var i_0, j, stack;
  stack = e && e.stack?e.stack.split('\n'):[];
  for (i_0 = 0 , j = stack.length; i_0 < j; ++i_0) {
    stack[i_0] = extractNameFromToString(stack[i_0]);
  }
  return stack;
}

function Array_0(){
}

function createFromSeed(seedType, length_0){
  var array = new Array(length_0);
  if (seedType == 3) {
    for (var i_0 = 0; i_0 < length_0; ++i_0) {
      var value = new Object;
      value.l = value.m = value.h = 0;
      array[i_0] = value;
    }
  }
   else if (seedType > 0) {
    var value = [null, 0, false][seedType];
    for (var i_0 = 0; i_0 < length_0; ++i_0) {
      array[i_0] = value;
    }
  }
  return array;
}

function initDim(arrayClass, castableTypeMap, queryId, length_0, seedType){
  var result;
  result = createFromSeed(seedType, length_0);
  initValues(arrayClass, castableTypeMap, queryId, result);
  return result;
}

function initDims(arrayClasses, castableTypeMapExprs, queryIdExprs, dimExprs, count, seedType){
  return initDims_0(arrayClasses, castableTypeMapExprs, queryIdExprs, dimExprs, 0, count, seedType);
}

function initDims_0(arrayClasses, castableTypeMapExprs, queryIdExprs, dimExprs, index, count, seedType){
  var i_0, isLastDim, length_0, result;
  length_0 = dimExprs[index];
  isLastDim = index == count - 1;
  result = createFromSeed(isLastDim?seedType:0, length_0);
  initValues(arrayClasses[index], castableTypeMapExprs[index], queryIdExprs[index], result);
  if (!isLastDim) {
    ++index;
    for (i_0 = 0; i_0 < length_0; ++i_0) {
      result[i_0] = initDims_0(arrayClasses, castableTypeMapExprs, queryIdExprs, dimExprs, index, count, seedType);
    }
  }
  return result;
}

function initValues(arrayClass, castableTypeMap, queryId, array){
  $clinit_Array$ExpandoWrapper();
  wrapArray(array, expandoNames_0, expandoValues_0);
  array.___clazz$ = arrayClass;
  array.castableTypeMap$ = castableTypeMap;
  return array;
}

defineSeed(43, 1, {}, Array_0);
function $clinit_Array$ExpandoWrapper(){
  $clinit_Array$ExpandoWrapper = nullMethod;
  expandoNames_0 = [];
  expandoValues_0 = [];
  initExpandos(new Array_0, expandoNames_0, expandoValues_0);
}

function initExpandos(protoType, expandoNames, expandoValues){
  var i_0 = 0, value;
  for (var name_0 in protoType) {
    if (value = protoType[name_0]) {
      expandoNames[i_0] = name_0;
      expandoValues[i_0] = value;
      ++i_0;
    }
  }
}

function wrapArray(array, expandoNames, expandoValues){
  $clinit_Array$ExpandoWrapper();
  for (var i_0 = 0, c = expandoNames.length; i_0 < c; ++i_0) {
    array[expandoNames[i_0]] = expandoValues[i_0];
  }
}

var expandoNames_0, expandoValues_0;
function canCast(src, dstId){
  return src.castableTypeMap$ && !!src.castableTypeMap$[dstId];
}

function dynamicCastJso(src){
  if (src != null && (src.typeMarker$ == nullMethod || canCast(src, 1))) {
    throw new ClassCastException_0;
  }
  return src;
}

function instanceOf(src, dstId){
  return src != null && canCast(src, dstId);
}

function instanceOfJso(src){
  return src != null && src.typeMarker$ != nullMethod && !canCast(src, 1);
}

function round_int(x){
  return ~~Math.max(Math.min(x, 2147483647), -2147483648);
}

function init(){
  !!$stats && onModuleStart('com.google.gwt.useragent.client.UserAgentAsserter');
  !!$stats && onModuleStart('com.google.gwt.user.client.DocumentModeAsserter');
  !!$stats && onModuleStart('com.akjava.gwt.cndetect.client.Detect');
  $exportBridge();
}

function caught(e){
  if (instanceOf(e, Q$Throwable)) {
    return e;
  }
  return new JavaScriptException_0(e);
}

function onModuleStart(mainClassName){
  return $stats({moduleName:$moduleName, sessionId:$sessionId, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'onModuleLoadStart', className:mainClassName});
}

function Class_0(){
}

function createForArray(packageName, className, seedId){
  var clazz;
  clazz = new Class_0;
  isInstantiable(seedId != 0?-seedId:0) && setClassLiteral(seedId != 0?-seedId:0, clazz);
  return clazz;
}

function createForClass(packageName, className, seedId){
  var clazz;
  clazz = new Class_0;
  isInstantiable(seedId) && setClassLiteral(seedId, clazz);
  return clazz;
}

function getSeedFunction(clazz){
  var func = seedTable[clazz.seedId];
  clazz = null;
  return func;
}

function isInstantiable(seedId){
  return typeof seedId == 'number' && seedId > 0;
}

function setClassLiteral(seedId, clazz){
  var proto;
  clazz.seedId = seedId;
  if (seedId == 2) {
    proto = String.prototype;
  }
   else {
    if (seedId > 0) {
      var seed = getSeedFunction(clazz);
      if (seed) {
        proto = seed.prototype;
      }
       else {
        seed = seedTable[seedId] = function(){
        }
        ;
        seed.___clazz$ = clazz;
        return;
      }
    }
     else {
      return;
    }
  }
  proto.___clazz$ = clazz;
}

defineSeed(54, 1, {}, Class_0);
_.seedId = 0;
function ClassCastException_0(){
  $fillInStackTrace();
}

defineSeed(55, 30, makeCastMap([Q$Throwable]), ClassCastException_0);
function IllegalStateException_0(){
  $fillInStackTrace();
}

defineSeed(56, 30, makeCastMap([Q$Throwable]), IllegalStateException_0);
defineSeed(58, 1, {});
function Integer_0(value){
  this.value = value;
}

function valueOf(i_0){
  var rebase, result;
  if (i_0 > -129 && i_0 < 128) {
    rebase = i_0 + 128;
    result = ($clinit_Integer$BoxedValues() , boxedValues)[rebase];
    !result && (result = boxedValues[rebase] = new Integer_0(i_0));
    return result;
  }
  return new Integer_0(i_0);
}

defineSeed(57, 58, {}, Integer_0);
_.value = 0;
function $clinit_Integer$BoxedValues(){
  $clinit_Integer$BoxedValues = nullMethod;
  boxedValues = initDim(_3Ljava_lang_Integer_2_classLit, CM$, Q$Object, 256, 0);
}

var boxedValues;
function NullPointerException_0(){
  $fillInStackTrace();
}

function NullPointerException_1(){
  $fillInStackTrace();
}

defineSeed(60, 30, makeCastMap([Q$Throwable]), NullPointerException_0, NullPointerException_1);
function StackTraceElement_0(methodName){
}

defineSeed(61, 1, {}, StackTraceElement_0);
function $indexOf(this$static, str){
  return this$static.indexOf(str);
}

function $trim(this$static){
  if (this$static.length == 0 || this$static[0] > ' ' && this$static[this$static.length - 1] > ' ') {
    return this$static;
  }
  var r1 = this$static.replace(/^(\s*)/, '');
  var r2 = r1.replace(/\s*$/, '');
  return r2;
}

_ = String.prototype;
_.castableTypeMap$ = makeCastMap([Q$String]);
var $entry = entry;
function gwtOnLoad(errFn, modName, modBase, softPermutationId){
  $moduleName = modName;
  $moduleBase = modBase;
  if (errFn)
    try {
      $entry(init)();
    }
     catch (e) {
      errFn(modName);
    }
   else {
    $entry(init)();
  }
}

var Ljava_lang_Object_2_classLit = createForClass('java.lang.', 'Object', 1), Lcom_google_gwt_core_client_JavaScriptObject_2_classLit = createForClass('com.google.gwt.core.client.', 'JavaScriptObject$', 8), _3I_classLit = createForArray('', '[I', 62), Ljava_lang_Throwable_2_classLit = createForClass('java.lang.', 'Throwable', 32), Ljava_lang_Exception_2_classLit = createForClass('java.lang.', 'Exception', 31), Ljava_lang_RuntimeException_2_classLit = createForClass('java.lang.', 'RuntimeException', 30), Ljava_lang_StackTraceElement_2_classLit = createForClass('java.lang.', 'StackTraceElement', 61), _3Ljava_lang_StackTraceElement_2_classLit = createForArray('[Ljava.lang.', 'StackTraceElement;', 63), Lcom_google_gwt_lang_SeedUtil_2_classLit = createForClass('com.google.gwt.lang.', 'SeedUtil', 49), Ljava_lang_Number_2_classLit = createForClass('java.lang.', 'Number', 58), Ljava_lang_Class_2_classLit = createForClass('java.lang.', 'Class', 54), Ljava_lang_Integer_2_classLit = createForClass('java.lang.', 'Integer', 57), _3Ljava_lang_Integer_2_classLit = createForArray('[Ljava.lang.', 'Integer;', 64), Ljava_lang_String_2_classLit = createForClass('java.lang.', 'String', 2), Ljava_lang_ClassCastException_2_classLit = createForClass('java.lang.', 'ClassCastException', 55), Lcom_google_gwt_core_client_JavaScriptException_2_classLit = createForClass('com.google.gwt.core.client.', 'JavaScriptException', 29), Lcom_google_gwt_core_client_Scheduler_2_classLit = createForClass('com.google.gwt.core.client.', 'Scheduler', 36), Lcom_google_gwt_core_client_impl_SchedulerImpl_2_classLit = createForClass('com.google.gwt.core.client.impl.', 'SchedulerImpl', 38), _3_3I_classLit = createForArray('', '[[I', 65), Lcom_akjava_lib_common_graphics_Rect_2_classLit = createForClass('com.akjava.lib.common.graphics.', 'Rect', 21), _3_3Ljava_lang_Integer_2_classLit = createForArray('[[Ljava.lang.', 'Integer;', 66), _3_3_3Ljava_lang_Integer_2_classLit = createForArray('[[[Ljava.lang.', 'Integer;', 67), Lcom_akjava_gwt_lib_client_experimental_lbp_SimpleLBP_2_classLit = createForClass('com.akjava.gwt.lib.client.experimental.lbp.', 'SimpleLBP', 20), Ljava_lang_NullPointerException_2_classLit = createForClass('java.lang.', 'NullPointerException', 60), Lcom_google_common_base_Stopwatch_2_classLit = createForClass('com.google.common.base.', 'Stopwatch', 23), Ljava_lang_IllegalStateException_2_classLit = createForClass('java.lang.', 'IllegalStateException', 56), Lcom_google_common_base_Ticker_2_classLit = createForClass('com.google.common.base.', 'Ticker', 24), Lcom_google_common_base_Ticker$1_2_classLit = createForClass('com.google.common.base.', 'Ticker$1', 25);

var $stats = function(){};
var $sessionId = function(){};
var navigator = {};
navigator.userAgent = 'timobile';
$strongName = 'A2F1D50D093EF0968E1FB3BAD7F680BA';
$ti4jCompilationDate = 1411970541783;
$wnd.Array = function(){};
self.addEventListener('message', function(e) {   $workergwtbridge(e.data); }, false);
gwtOnLoad(null,'detect',null);
})();
