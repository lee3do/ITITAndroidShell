;(function () {
  'use strict';
  /**
   * @preserve FastClick: polyfill to remove click delays on browsers with touch UIs.
   *
   * @codingstandard ftlabs-jsv2
   * @copyright The Financial Times Limited [All Rights Reserved]
   * @license MIT License (see LICENSE.txt)
   */

  /*jslint browser:true, node:true*/
  /*global define, Event, Node*/


  /**
   * Instantiate fast-clicking listeners on the specified layer.
   *
   * @constructor
   * @param {Element} layer The layer to listen on
   * @param {Object} [options={}] The options to override the defaults
   */
  function FastClick(layer, options) {
  var oldOnClick;

  options = options || {};

  /**
   * Whether a click is currently being tracked.
   *
   * @type boolean
   */
  this.trackingClick = false;


  /**
   * Timestamp for when click tracking started.
   *
   * @type number
   */
  this.trackingClickStart = 0;


  /**
   * The element being tracked for a click.
   *
   * @type EventTarget
   */
  this.targetElement = null;


  /**
   * X-coordinate of touch start event.
   *
   * @type number
   */
  this.touchStartX = 0;


  /**
   * Y-coordinate of touch start event.
   *
   * @type number
   */
  this.touchStartY = 0;


  /**
   * ID of the last touch, retrieved from Touch.identifier.
   *
   * @type number
   */
  this.lastTouchIdentifier = 0;


  /**
   * Touchmove boundary, beyond which a click will be cancelled.
   *
   * @type number
   */
  this.touchBoundary = options.touchBoundary || 10;


  /**
   * The FastClick layer.
   *
   * @type Element
   */
  this.layer = layer;

  /**
   * The minimum time between tap(touchstart and touchend) events
   *
   * @type number
   */
  this.tapDelay = options.tapDelay || 200;

  /**
   * The maximum time for a tap
   *
   * @type number
   */
  this.tapTimeout = options.tapTimeout || 700;

  if (FastClick.notNeeded(layer)) {
  return;
  }

  // Some old versions of Android don't have Function.prototype.bind
  function bind(method, context) {
  return function() { return method.apply(context, arguments); };
  }


  var methods = ['onMouse', 'onClick', 'onTouchStart', 'onTouchMove', 'onTouchEnd', 'onTouchCancel'];
  var context = this;
  for (var i = 0, l = methods.length; i < l; i++) {
  context[methods[i]] = bind(context[methods[i]], context);
  }

  // Set up event handlers as required
  if (deviceIsAndroid) {
  layer.addEventListener('mouseover', this.onMouse, true);
  layer.addEventListener('mousedown', this.onMouse, true);
  layer.addEventListener('mouseup', this.onMouse, true);
  }

  layer.addEventListener('click', this.onClick, true);
  layer.addEventListener('touchstart', this.onTouchStart, false);
  layer.addEventListener('touchmove', this.onTouchMove, false);
  layer.addEventListener('touchend', this.onTouchEnd, false);
  layer.addEventListener('touchcancel', this.onTouchCancel, false);

  // Hack is required for browsers that don't support Event#stopImmediatePropagation (e.g. Android 2)
  // which is how FastClick normally stops click events bubbling to callbacks registered on the FastClick
  // layer when they are cancelled.
  if (!Event.prototype.stopImmediatePropagation) {
  layer.removeEventListener = function(type, callback, capture) {
  var rmv = Node.prototype.removeEventListener;
  if (type === 'click') {
  rmv.call(layer, type, callback.hijacked || callback, capture);
  } else {
  rmv.call(layer, type, callback, capture);
  }
  };

  layer.addEventListener = function(type, callback, capture) {
  var adv = Node.prototype.addEventListener;
  if (type === 'click') {
  adv.call(layer, type, callback.hijacked || (callback.hijacked = function(event) {
                                              if (!event.propagationStopped) {
                                              callback(event);
                                              }
                                              }), capture);
  } else {
  adv.call(layer, type, callback, capture);
  }
  };
  }

  // If a handler is already declared in the element's onclick attribute, it will be fired before
  // FastClick's onClick handler. Fix this by pulling out the user-defined handler function and
  // adding it as listener.
  if (typeof layer.onclick === 'function') {

  // Android browser on at least 3.2 requires a new reference to the function in layer.onclick
  // - the old one won't work if passed to addEventListener directly.
  oldOnClick = layer.onclick;
  layer.addEventListener('click', function(event) {
                         oldOnClick(event);
                         }, false);
  layer.onclick = null;
  }
  }

  /**
   * Windows Phone 8.1 fakes user agent string to look like Android and iPhone.
   *
   * @type boolean
   */
  var deviceIsWindowsPhone = navigator.userAgent.indexOf("Windows Phone") >= 0;

  /**
   * Android requires exceptions.
   *
   * @type boolean
   */
  var deviceIsAndroid = navigator.userAgent.indexOf('Android') > 0 && !deviceIsWindowsPhone;


  /**
   * iOS requires exceptions.
   *
   * @type boolean
   */
  var deviceIsIOS = /iP(ad|hone|od)/.test(navigator.userAgent) && !deviceIsWindowsPhone;


  /**
   * iOS 4 requires an exception for select elements.
   *
   * @type boolean
   */
  var deviceIsIOS4 = deviceIsIOS && (/OS 4_\d(_\d)?/).test(navigator.userAgent);


  /**
   * iOS 6.0-7.* requires the target element to be manually derived
   *
   * @type boolean
   */
  var deviceIsIOSWithBadTarget = deviceIsIOS && (/OS [6-7]_\d/).test(navigator.userAgent);

  /**
   * BlackBerry requires exceptions.
   *
   * @type boolean
   */
  var deviceIsBlackBerry10 = navigator.userAgent.indexOf('BB10') > 0;

  /**
   * Determine whether a given element requires a native click.
   *
   * @param {EventTarget|Element} target Target DOM element
   * @returns {boolean} Returns true if the element needs a native click
   */
  FastClick.prototype.needsClick = function(target) {
  switch (target.nodeName.toLowerCase()) {

  // Don't send a synthetic click to disabled inputs (issue #62)
  case 'button':
  case 'select':
  case 'textarea':
  if (target.disabled) {
  return true;
  }

  break;
  case 'input':

  // File inputs need real clicks on iOS 6 due to a browser bug (issue #68)
  if ((deviceIsIOS && target.type === 'file') || target.disabled) {
  return true;
  }

  break;
  case 'label':
  case 'iframe': // iOS8 homescreen apps can prevent events bubbling into frames
  case 'video':
  return true;
  }

  return (/\bneedsclick\b/).test(target.className);
  };


  /**
   * Determine whether a given element requires a call to focus to simulate click into element.
   *
   * @param {EventTarget|Element} target Target DOM element
   * @returns {boolean} Returns true if the element requires a call to focus to simulate native click.
   */
  FastClick.prototype.needsFocus = function(target) {
  switch (target.nodeName.toLowerCase()) {
  case 'textarea':
  return true;
  case 'select':
  return !deviceIsAndroid;
  case 'input':
  switch (target.type) {
  case 'button':
  case 'checkbox':
  case 'file':
  case 'image':
  case 'radio':
  case 'submit':
  return false;
  }

  // No point in attempting to focus disabled inputs
  return !target.disabled && !target.readOnly;
  default:
  return (/\bneedsfocus\b/).test(target.className);
  }
  };


  /**
   * Send a click event to the specified element.
   *
   * @param {EventTarget|Element} targetElement
   * @param {Event} event
   */
  FastClick.prototype.sendClick = function(targetElement, event) {
  var clickEvent, touch;

  // On some Android devices activeElement needs to be blurred otherwise the synthetic click will have no effect (#24)
  if (document.activeElement && document.activeElement !== targetElement) {
  document.activeElement.blur();
  }

  touch = event.changedTouches[0];

  // Synthesise a click event, with an extra attribute so it can be tracked
  clickEvent = document.createEvent('MouseEvents');
  clickEvent.initMouseEvent(this.determineEventType(targetElement), true, true, window, 1, touch.screenX, touch.screenY, touch.clientX, touch.clientY, false, false, false, false, 0, null);
  clickEvent.forwardedTouchEvent = true;
  targetElement.dispatchEvent(clickEvent);
  };

  FastClick.prototype.determineEventType = function(targetElement) {

  //Issue #159: Android Chrome Select Box does not open with a synthetic click event
  if (deviceIsAndroid && targetElement.tagName.toLowerCase() === 'select') {
  return 'mousedown';
  }

  return 'click';
  };


  /**
   * @param {EventTarget|Element} targetElement
   */
  FastClick.prototype.focus = function(targetElement) {
  var length;

  // Issue #160: on iOS 7, some input elements (e.g. date datetime month) throw a vague TypeError on setSelectionRange. These elements don't have an integer value for the selectionStart and selectionEnd properties, but unfortunately that can't be used for detection because accessing the properties also throws a TypeError. Just check the type instead. Filed as Apple bug #15122724.
  if (deviceIsIOS && targetElement.setSelectionRange && targetElement.type.indexOf('date') !== 0 && targetElement.type !== 'time' && targetElement.type !== 'month' && targetElement.type !== 'email') {
  length = targetElement.value.length;
  targetElement.setSelectionRange(length, length);
  } else {
  targetElement.focus();
  }
  };


  /**
   * Check whether the given target element is a child of a scrollable layer and if so, set a flag on it.
   *
   * @param {EventTarget|Element} targetElement
   */
  FastClick.prototype.updateScrollParent = function(targetElement) {
  var scrollParent, parentElement;

  scrollParent = targetElement.fastClickScrollParent;

  // Attempt to discover whether the target element is contained within a scrollable layer. Re-check if the
  // target element was moved to another parent.
  if (!scrollParent || !scrollParent.contains(targetElement)) {
  parentElement = targetElement;
  do {
  if (parentElement.scrollHeight > parentElement.offsetHeight) {
  scrollParent = parentElement;
  targetElement.fastClickScrollParent = parentElement;
  break;
  }

  parentElement = parentElement.parentElement;
  } while (parentElement);
  }

  // Always update the scroll top tracker if possible.
  if (scrollParent) {
  scrollParent.fastClickLastScrollTop = scrollParent.scrollTop;
  }
  };


  /**
   * @param {EventTarget} targetElement
   * @returns {Element|EventTarget}
   */
  FastClick.prototype.getTargetElementFromEventTarget = function(eventTarget) {

  // On some older browsers (notably Safari on iOS 4.1 - see issue #56) the event target may be a text node.
  if (eventTarget.nodeType === Node.TEXT_NODE) {
  return eventTarget.parentNode;
  }

  return eventTarget;
  };


  /**
   * On touch start, record the position and scroll offset.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.onTouchStart = function(event) {
  var targetElement, touch, selection;

  // Ignore multiple touches, otherwise pinch-to-zoom is prevented if both fingers are on the FastClick element (issue #111).
  if (event.targetTouches.length > 1) {
  return true;
  }

  targetElement = this.getTargetElementFromEventTarget(event.target);
  touch = event.targetTouches[0];

  if (deviceIsIOS) {

  // Only trusted events will deselect text on iOS (issue #49)
  selection = window.getSelection();
  if (selection.rangeCount && !selection.isCollapsed) {
  return true;
  }

  if (!deviceIsIOS4) {

  // Weird things happen on iOS when an alert or confirm dialog is opened from a click event callback (issue #23):
  // when the user next taps anywhere else on the page, new touchstart and touchend events are dispatched
  // with the same identifier as the touch event that previously triggered the click that triggered the alert.
  // Sadly, there is an issue on iOS 4 that causes some normal touch events to have the same identifier as an
  // immediately preceeding touch event (issue #52), so this fix is unavailable on that platform.
  // Issue 120: touch.identifier is 0 when Chrome dev tools 'Emulate touch events' is set with an iOS device UA string,
  // which causes all touch events to be ignored. As this block only applies to iOS, and iOS identifiers are always long,
  // random integers, it's safe to to continue if the identifier is 0 here.
  if (touch.identifier && touch.identifier === this.lastTouchIdentifier) {
  event.preventDefault();
  return false;
  }

  this.lastTouchIdentifier = touch.identifier;

  // If the target element is a child of a scrollable layer (using -webkit-overflow-scrolling: touch) and:
  // 1) the user does a fling scroll on the scrollable layer
  // 2) the user stops the fling scroll with another tap
  // then the event.target of the last 'touchend' event will be the element that was under the user's finger
  // when the fling scroll was started, causing FastClick to send a click event to that layer - unless a check
  // is made to ensure that a parent layer was not scrolled before sending a synthetic click (issue #42).
  this.updateScrollParent(targetElement);
  }
  }

  this.trackingClick = true;
  this.trackingClickStart = event.timeStamp;
  this.targetElement = targetElement;

  this.touchStartX = touch.pageX;
  this.touchStartY = touch.pageY;

  // Prevent phantom clicks on fast double-tap (issue #36)
  if ((event.timeStamp - this.lastClickTime) < this.tapDelay) {
  event.preventDefault();
  }

  return true;
  };


  /**
   * Based on a touchmove event object, check whether the touch has moved past a boundary since it started.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.touchHasMoved = function(event) {
  var touch = event.changedTouches[0], boundary = this.touchBoundary;

  if (Math.abs(touch.pageX - this.touchStartX) > boundary || Math.abs(touch.pageY - this.touchStartY) > boundary) {
  return true;
  }

  return false;
  };


  /**
   * Update the last position.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.onTouchMove = function(event) {
  if (!this.trackingClick) {
  return true;
  }

  // If the touch has moved, cancel the click tracking
  if (this.targetElement !== this.getTargetElementFromEventTarget(event.target) || this.touchHasMoved(event)) {
  this.trackingClick = false;
  this.targetElement = null;
  }

  return true;
  };


  /**
   * Attempt to find the labelled control for the given label element.
   *
   * @param {EventTarget|HTMLLabelElement} labelElement
   * @returns {Element|null}
   */
  FastClick.prototype.findControl = function(labelElement) {

  // Fast path for newer browsers supporting the HTML5 control attribute
  if (labelElement.control !== undefined) {
  return labelElement.control;
  }

  // All browsers under test that support touch events also support the HTML5 htmlFor attribute
  if (labelElement.htmlFor) {
  return document.getElementById(labelElement.htmlFor);
  }

  // If no for attribute exists, attempt to retrieve the first labellable descendant element
  // the list of which is defined here: http://www.w3.org/TR/html5/forms.html#category-label
  return labelElement.querySelector('button, input:not([type=hidden]), keygen, meter, output, progress, select, textarea');
  };


  /**
   * On touch end, determine whether to send a click event at once.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.onTouchEnd = function(event) {
  var forElement, trackingClickStart, targetTagName, scrollParent, touch, targetElement = this.targetElement;

  if (!this.trackingClick) {
  return true;
  }

  // Prevent phantom clicks on fast double-tap (issue #36)
  if ((event.timeStamp - this.lastClickTime) < this.tapDelay) {
  this.cancelNextClick = true;
  return true;
  }

  if ((event.timeStamp - this.trackingClickStart) > this.tapTimeout) {
  return true;
  }

  // Reset to prevent wrong click cancel on input (issue #156).
  this.cancelNextClick = false;

  this.lastClickTime = event.timeStamp;

  trackingClickStart = this.trackingClickStart;
  this.trackingClick = false;
  this.trackingClickStart = 0;

  // On some iOS devices, the targetElement supplied with the event is invalid if the layer
  // is performing a transition or scroll, and has to be re-detected manually. Note that
  // for this to function correctly, it must be called *after* the event target is checked!
  // See issue #57; also filed as rdar://13048589 .
  if (deviceIsIOSWithBadTarget) {
  touch = event.changedTouches[0];

  // In certain cases arguments of elementFromPoint can be negative, so prevent setting targetElement to null
  targetElement = document.elementFromPoint(touch.pageX - window.pageXOffset, touch.pageY - window.pageYOffset) || targetElement;
  targetElement.fastClickScrollParent = this.targetElement.fastClickScrollParent;
  }

  targetTagName = targetElement.tagName.toLowerCase();
  if (targetTagName === 'label') {
  forElement = this.findControl(targetElement);
  if (forElement) {
  this.focus(targetElement);
  if (deviceIsAndroid) {
  return false;
  }

  targetElement = forElement;
  }
  } else if (this.needsFocus(targetElement)) {

  // Case 1: If the touch started a while ago (best guess is 100ms based on tests for issue #36) then focus will be triggered anyway. Return early and unset the target element reference so that the subsequent click will be allowed through.
  // Case 2: Without this exception for input elements tapped when the document is contained in an iframe, then any inputted text won't be visible even though the value attribute is updated as the user types (issue #37).
  if ((event.timeStamp - trackingClickStart) > 100 || (deviceIsIOS && window.top !== window && targetTagName === 'input')) {
  this.targetElement = null;
  return false;
  }

  this.focus(targetElement);
  this.sendClick(targetElement, event);

  // Select elements need the event to go through on iOS 4, otherwise the selector menu won't open.
  // Also this breaks opening selects when VoiceOver is active on iOS6, iOS7 (and possibly others)
  if (!deviceIsIOS || targetTagName !== 'select') {
  this.targetElement = null;
  event.preventDefault();
  }

  return false;
  }

  if (deviceIsIOS && !deviceIsIOS4) {

  // Don't send a synthetic click event if the target element is contained within a parent layer that was scrolled
  // and this tap is being used to stop the scrolling (usually initiated by a fling - issue #42).
  scrollParent = targetElement.fastClickScrollParent;
  if (scrollParent && scrollParent.fastClickLastScrollTop !== scrollParent.scrollTop) {
  return true;
  }
  }

  // Prevent the actual click from going though - unless the target node is marked as requiring
  // real clicks or if it is in the whitelist in which case only non-programmatic clicks are permitted.
  if (!this.needsClick(targetElement)) {
  event.preventDefault();
  this.sendClick(targetElement, event);
  }

  return false;
  };


  /**
   * On touch cancel, stop tracking the click.
   *
   * @returns {void}
   */
  FastClick.prototype.onTouchCancel = function() {
  this.trackingClick = false;
  this.targetElement = null;
  };


  /**
   * Determine mouse events which should be permitted.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.onMouse = function(event) {

  // If a target element was never set (because a touch event was never fired) allow the event
  if (!this.targetElement) {
  return true;
  }

  if (event.forwardedTouchEvent) {
  return true;
  }

  // Programmatically generated events targeting a specific element should be permitted
  if (!event.cancelable) {
  return true;
  }

  // Derive and check the target element to see whether the mouse event needs to be permitted;
  // unless explicitly enabled, prevent non-touch click events from triggering actions,
  // to prevent ghost/doubleclicks.
  if (!this.needsClick(this.targetElement) || this.cancelNextClick) {

  // Prevent any user-added listeners declared on FastClick element from being fired.
  if (event.stopImmediatePropagation) {
  event.stopImmediatePropagation();
  } else {

  // Part of the hack for browsers that don't support Event#stopImmediatePropagation (e.g. Android 2)
  event.propagationStopped = true;
  }

  // Cancel the event
  event.stopPropagation();
  event.preventDefault();

  return false;
  }

  // If the mouse event is permitted, return true for the action to go through.
  return true;
  };


  /**
   * On actual clicks, determine whether this is a touch-generated click, a click action occurring
   * naturally after a delay after a touch (which needs to be cancelled to avoid duplication), or
   * an actual click which should be permitted.
   *
   * @param {Event} event
   * @returns {boolean}
   */
  FastClick.prototype.onClick = function(event) {
  var permitted;

  // It's possible for another FastClick-like library delivered with third-party code to fire a click event before FastClick does (issue #44). In that case, set the click-tracking flag back to false and return early. This will cause onTouchEnd to return early.
  if (this.trackingClick) {
  this.targetElement = null;
  this.trackingClick = false;
  return true;
  }

  // Very odd behaviour on iOS (issue #18): if a submit element is present inside a form and the user hits enter in the iOS simulator or clicks the Go button on the pop-up OS keyboard the a kind of 'fake' click event will be triggered with the submit-type input element as the target.
  if (event.target.type === 'submit' && event.detail === 0) {
  return true;
  }

  permitted = this.onMouse(event);

  // Only unset targetElement if the click is not permitted. This will ensure that the check for !targetElement in onMouse fails and the browser's click doesn't go through.
  if (!permitted) {
  this.targetElement = null;
  }

  // If clicks are permitted, return true for the action to go through.
  return permitted;
  };


  /**
   * Remove all FastClick's event listeners.
   *
   * @returns {void}
   */
  FastClick.prototype.destroy = function() {
  var layer = this.layer;

  if (deviceIsAndroid) {
  layer.removeEventListener('mouseover', this.onMouse, true);
  layer.removeEventListener('mousedown', this.onMouse, true);
  layer.removeEventListener('mouseup', this.onMouse, true);
  }

  layer.removeEventListener('click', this.onClick, true);
  layer.removeEventListener('touchstart', this.onTouchStart, false);
  layer.removeEventListener('touchmove', this.onTouchMove, false);
  layer.removeEventListener('touchend', this.onTouchEnd, false);
  layer.removeEventListener('touchcancel', this.onTouchCancel, false);
  };


  /**
   * Check whether FastClick is needed.
   *
   * @param {Element} layer The layer to listen on
   */
  FastClick.notNeeded = function(layer) {
  var metaViewport;
  var chromeVersion;
  var blackberryVersion;
  var firefoxVersion;

  // Devices that don't support touch don't need FastClick
  if (typeof window.ontouchstart === 'undefined') {
  return true;
  }

  // Chrome version - zero for other browsers
  chromeVersion = +(/Chrome\/([0-9]+)/.exec(navigator.userAgent) || [,0])[1];

  if (chromeVersion) {

  if (deviceIsAndroid) {
  metaViewport = document.querySelector('meta[name=viewport]');

  if (metaViewport) {
  // Chrome on Android with user-scalable="no" doesn't need FastClick (issue #89)
  if (metaViewport.content.indexOf('user-scalable=no') !== -1) {
  return true;
  }
  // Chrome 32 and above with width=device-width or less don't need FastClick
  if (chromeVersion > 31 && document.documentElement.scrollWidth <= window.outerWidth) {
  return true;
  }
  }

  // Chrome desktop doesn't need FastClick (issue #15)
  } else {
  return true;
  }
  }

  if (deviceIsBlackBerry10) {
  blackberryVersion = navigator.userAgent.match(/Version\/([0-9]*)\.([0-9]*)/);

  // BlackBerry 10.3+ does not require Fastclick library.
  // https://github.com/ftlabs/fastclick/issues/251
  if (blackberryVersion[1] >= 10 && blackberryVersion[2] >= 3) {
  metaViewport = document.querySelector('meta[name=viewport]');

  if (metaViewport) {
  // user-scalable=no eliminates click delay.
  if (metaViewport.content.indexOf('user-scalable=no') !== -1) {
  return true;
  }
  // width=device-width (or less than device-width) eliminates click delay.
  if (document.documentElement.scrollWidth <= window.outerWidth) {
  return true;
  }
  }
  }
  }

  // IE10 with -ms-touch-action: none or manipulation, which disables double-tap-to-zoom (issue #97)
  if (layer.style.msTouchAction === 'none' || layer.style.touchAction === 'manipulation') {
  return true;
  }

  // Firefox version - zero for other browsers
  firefoxVersion = +(/Firefox\/([0-9]+)/.exec(navigator.userAgent) || [,0])[1];

  if (firefoxVersion >= 27) {
  // Firefox 27+ does not have tap delay if the content is not zoomable - https://bugzilla.mozilla.org/show_bug.cgi?id=922896

  metaViewport = document.querySelector('meta[name=viewport]');
  if (metaViewport && (metaViewport.content.indexOf('user-scalable=no') !== -1 || document.documentElement.scrollWidth <= window.outerWidth)) {
  return true;
  }
  }

  // IE11: prefixed -ms-touch-action is no longer supported and it's recomended to use non-prefixed version
  // http://msdn.microsoft.com/en-us/library/windows/apps/Hh767313.aspx
  if (layer.style.touchAction === 'none' || layer.style.touchAction === 'manipulation') {
  return true;
  }

  return false;
  };


  /**
   * Factory method for creating a FastClick object
   *
   * @param {Element} layer The layer to listen on
   * @param {Object} [options={}] The options to override the defaults
   */
  FastClick.attach = function(layer, options) {
  return new FastClick(layer, options);
  };


  if (typeof define === 'function' && typeof define.amd === 'object' && define.amd) {

  // AMD. Register as an anonymous module.
  define(function() {
         return FastClick;
         });
  } else if (typeof module !== 'undefined' && module.exports) {
  module.exports = FastClick.attach;
  module.exports.FastClick = FastClick;
  } else {
  window.FastClick = FastClick;
  }
}());
//
//
//
//
window.shellInvokeCallbacks=[];
//
window.callbackCounter=1;
//
window.shellInvokeCallback=function(name,args){
    var t=window.shellInvokeCallbacks[name];
    delete window.shellInvokeCallbacks[name];
    t(args);
}
window.nextInvokeCallback=function(callback){
    if(callback==null){
        return null;
    }
    window.callbackCounter++;
    var callbackName="callback-"+window.callbackCounter;
    window.shellInvokeCallbacks[callbackName]=callback;
    return callbackName;
}
//page lifecycle
window.pageLoad=function(args){
    window.pageArgs=args;
    app.setNavigationBarTitle({title:window.document.title})
    if(window.page&&window.page.pageLoad){
        window.page.pageLoad(args);
    }
    FastClick.attach(window.document.body);
}
//
window.pageUnLoad=function(args){
    if(window.page&&window.page.pageUnLoad){
        window.page.pageUnLoad(args);
    }
}
//
window.pageShow=function(args){
    if(window.page&&window.page.pageShow){
        window.page.pageShow(args);
    }
}
//
window.pageHide=function(args){
    if(window.page&&window.page.pageHide){
        window.page.pageHide(args);
    }
}
//
/*
args={
    name:"messageName",
    body:{} //message body
}
args.name
    pageMessage //其它页面发送的消息
    variableChanged //setVariable 以后调用
    UIApplicationUserDidTakeScreenshot //用户截图
    UIKeyboardDidShow //键盘显示
    UIKeyboardDidHide //键盘隐藏
    UIApplicationWillResignActive //应用切换到后台
    UIApplicationDidBecomeActive //应用恢复到前台
*/
window.pageMessage=function(args){
    if(window.page&&window.page.pageMessage){
        window.page.pageMessage(args);
    }
}
//
window.pageScrollToBottom=function(args){
    if(window.page&&window.page.pageScrollToBottom){
        window.page.pageScrollToBottom(args);
    }
}
//
window.pageNavigationItemClicked=function(args){
    if(window.page&&window.page.pageNavigationItemClicked){
        window.page.pageNavigationItemClicked(args);
    }
}
//
window.pagePullToRefresh=function(args){
    if(window.page&&window.page.pagePullToRefresh){
        window.page.pagePullToRefresh(args);
    }
}
//
window.pageNavigationBarSegmentSelected=function(args){
    if(window.page&&window.page.pageNavigationBarSegmentSelected){
        window.page.pageNavigationBarSegmentSelected(args);
    }
}
//
window.app={
    invoke:function(bridge,func,args){
        if(window.webkit&& window.webkit.messageHandlers){
            //ios
            window.webkit.messageHandlers.shell.postMessage({
                                                            bridge:bridge,
                                                            func:func,
                                                            args:args,
                                                            })
        }else{
            //android
            console.log("AndroidPostMessage",JSON.stringify({'func':func,'args':args}));
            window[bridge].postMessage(JSON.stringify({'func':func,'args':args}))
        }
    },
    invokeApp:function(func,args){
         app.invoke('AppBridge',func,args);
    },
    //发送消息
    postMessage:function(args){
        app.invokeApp('postMessage',{
            name:args.name,
            args:args.args
        })
    },
    //request
    request:function(obj){
        app.invokeApp('request',{
            url:obj.url,
            method:obj.method,
            data:obj.data,
            header:obj.header,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //log
    log:function(obj){
        if (typeof obj == "string"){
            obj={
                message:obj
            }
        }
        app.invokeApp('log',{
            message:obj.message
        })
    },
    /*
    *type info error success
    *
    */
    //显示toast
    showToast:function(obj){
        if (typeof obj == "string"){
            obj={
                message:obj
            }
        }
        app.invokeApp('showToast',{
            type:obj.type,
            message:obj.message
        })
    },
    //显示loading
    showLoading:function(){
        app.invokeApp('showLoading',{})
    },
    //隐藏loading
    hideLoading:function(){
        app.invokeApp('hideLoading',{})
    },
    //显示对话框
    showModal:function(obj){
        app.invokeApp('showModal',{
            title:obj.title,
            message:obj.message,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //显示选择sheet
    showActionSheet:function(obj){
        app.invokeApp('showActionSheet',{
            title:obj.title,
            message:obj.message,
            options:obj.options,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //查询系统信息
    getSystemInfo:function(obj){
        app.invokeApp('getSystemInfo',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //查询网络状态
    getNetworkType:function(obj){
        app.invokeApp('getNetworkType',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //设置变量
    setVariable:function(obj){
        app.invokeApp('setVariable',{
           key:obj.key,
           value:obj.value,
           callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //返回变量
    getVariable:function(obj){
        app.invokeApp('getVariable',{
            key:obj.key,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //返回存储
    removeVariable:function(obj){
        app.invokeApp('removeVariable',{
            key:obj.key,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //设置存储
    setStorage:function(obj){
        app.invokeApp('setStorage',{
           key:obj.key,
           value:obj.value,
           callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //返回存储
    getStorage:function(obj){
        app.invokeApp('getStorage',{
            key:obj.key,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //返回存储
    removeStorage:function(obj){
        app.invokeApp('removeStorage',{
            key:obj.key,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //存储信息
    getStorageInfo:function(obj){
        app.invokeApp('getStorageInfo',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //标题栏标题
    setNavigationBarTitle:function(obj){
        app.invokeApp('setNavigationBarTitle',{
           title:obj.title,
           image:obj.image,
           callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },

    setNavigationBarSegment:function(obj){
        app.invokeApp('setNavigationBarSegment',{
            items:obj.items,
            color:obj.color,
            selectedColor:obj.selectedColor,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },

    selectNavigationBarSegment:function(obj){
        app.invokeApp('selectNavigationBarSegment',{
            index:obj.index,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },

    getQueryString:function(query){
        if(query){
            var t=[];
            for(var k in query){
                t.push(k+"="+query[k]);
            }
            return t.join("&")
        }
        return null;
    },
    //
    loadPage:function(obj){
        app.invokeApp('loadPage',{
           path:obj.path,
           query:app.getQueryString(obj.query),
           callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //入栈
    pushPage:function(obj){
        app.invokeApp('pushPage',{
           path:obj.path,
           query:app.getQueryString(obj.query),
           callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //模态显示页面
    /*
    *  type
    *  alert
    *  popup
    *  topHalf
    *  bottomHalf
    *  fullScreen
    *  custom
    //
    *  tapDismiss  点击空白处或者滑动关闭
    */
    presentPage:function(obj){
        app.invokeApp('presentPage',{
            path:obj.path,
            query:app.getQueryString(obj.query),
            type:obj.type,
            navigate:obj.navigate,
            height:obj.height,
            blur:obj.blur,
            tapDismiss:obj.tapDismiss,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //弹出页面
    popPage:function(obj){
        app.invokeApp('popPage',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //弹出到根
    popToRootPage:function(obj){
        app.invokeApp('popToRootPage',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //关闭打开的窗口
    dismissPage:function(obj){
        app.invokeApp('dismissPage',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //页面滑动回弹
    enableBounces:function(obj){
        app.invokeApp('enableBounces',{
            enable:obj.enable,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    setTabBarSelectedIndex:function(obj){
        app.invokeApp('setTabBarSelectedIndex',{
            index:obj.index,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getTabBarSelectedIndex:function(obj){
        app.invokeApp('getTabBarSelectedIndex',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //tabbar 徽标
    setTabBarBadge:function(obj){
        app.invokeApp('setTabBarBadge',{
            badge:obj.badge,
            index:obj.index,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //app小红点
    setApplicationBadge:function(obj){
        app.invokeApp('setApplicationBadge',{
            badge:obj.badge,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //当前gps信息
    getLocation:function(obj){
        app.invokeApp('getLocation',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    canOpenURLSchema:function(obj){
        app.invokeApp('canOpenURLSchema',{
            url:obj.url,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    openURLSchema:function(obj){
        app.invokeApp('openURLSchema',{
            url:obj.url,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //预览图片
    previewImage:function(obj){
        app.invokeApp('previewImage',{
            urls:obj.urls,
            index:obj.index,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    saveImageToAlbum:function(obj){
        app.invokeApp('saveImageToAlbum',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    downloadFile:function(obj){
        app.invokeApp('downloadFile',{
            url:obj.url,
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    //fullpath 指定的是文件的绝对路径
    //
    uploadFile:function(obj){
        app.invokeApp('uploadFile',{
            url:obj.url,
            path:obj.path,
            fullpath:obj.fullpath,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getFileURL:function(obj){
        app.invokeApp('getFileURL',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getFilePath:function(obj){
        app.invokeApp('getFilePath',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    readFile:function(obj){
        app.invokeApp('readFile',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    writeFile:function(obj){
        app.invokeApp('writeFile',{
            path:obj.path,
            content:obj.content,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    isFileExists:function(obj){
        app.invokeApp('isFileExists',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    createDirectory:function(obj){
        app.invokeApp('createDirectory',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    deleteFile:function(obj){
        app.invokeApp('deleteFile',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    listFiles:function(obj){
        app.invokeApp('listFiles',{
            path:obj.path,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    moveFile:function(obj){
        app.invokeApp('moveFile',{
            source:obj.source,
            dest:obj.dest,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    //
    setNavigationBarVisible:function(obj){
        app.invokeApp('setNavigationBarVisible',{
            visible:obj.visible,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    setNavigationBarItem:function(obj){
        app.invokeApp('setNavigationBarItem',{
            position:obj.position,
            title:obj.title,
            image:obj.image,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    setNavigationBarItems:function(obj){
        app.invokeApp('setNavigationBarItems',{
            position:obj.position,
            titles:obj.titles,
            images:obj.images,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    setNavigationBarItemEnable:function(obj){
        app.invokeApp('setNavigationBarItemEnable',{
            position:obj.position,
            index:obj.index,
            enable:obj.enable,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    setPasteboard:function(obj){
        app.invokeApp('setPasteboard',{
            string:obj.string,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getPasteboard:function(obj){
        app.invokeApp('getPasteboard',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    stopPullToRefresh:function(obj){
        app.invokeApp('stopPullToRefresh',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    enablePullToRefresh:function(obj){
        app.invokeApp('enablePullToRefresh',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    /*
    type:photo video all
    source:both  camera photo
    format: png jpeg
    quality: 0 - 1 jpeg 有效
    */
    showImagePicker:function(obj){
        app.invokeApp('showImagePicker',{
            type:obj.type,
            source:obj.source,
            limit:obj.limit,
            format:obj.format,
            quality:obj.quality,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    scanQRCode:function(obj){
        app.invokeApp('scanQRCode',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    openLocation:function(obj){
        app.invokeApp('openLocation',{
            title:obj.title,
            subtitle:obj.subtitle,
            latitude:obj.latitude,
            longitude:obj.longitude,
            delta:obj.delta,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //相对路径
    unzip:function(obj){
        app.invokeApp('unzip',{
            path:obj.path,
            dest:obj.dest,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    //duration 单位秒
    //录制结束后 会回调callback 返回录制的文件地址
    //
    startAudioRecord:function(obj){
        app.invokeApp('startAudioRecord',{
            duration:obj.duration,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    pauseAudioRecord:function(obj){
        app.invokeApp('pauseAudioRecord',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    stopAudioRecord:function(obj){
        app.invokeApp('stopAudioRecord',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    getAudioRecordStatus:function(obj){
        app.invokeApp('getAudioRecordStatus',{
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    //
    showPickerView:function(obj){
        app.invokeApp('showPickerView',{
            items:obj.items,
            title:obj.title,
            select:obj.select,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    /*
    *mode:date time dateAndTime
    *
    */
    showDatePickerView:function(obj){
        app.invokeApp('showDatePickerView',{
            title:obj.title,
            mode:obj.mode,
            minDate:obj.minDate,
            maxDate:obj.maxDate,
            date:obj.date,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },
    showWebView:function(obj){
        app.invokeApp('showWebView',{
            url:obj.url,
            callback:nextInvokeCallback(obj?obj.callback:null)
        })
    },



}