/**
 * $Id: tagDisplay.java 2568 2009-04-19 22:15:02Z jsibert $
 *
 * Author: John Sibert
 * Copyright (c) 2008, 2009 John Sibert
 * 
 */

package client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.maps.client.*;
import com.google.gwt.maps.client.event.*;
import com.google.gwt.maps.client.geom.*;
import com.google.gwt.maps.client.control.*;
import com.google.gwt.maps.client.overlay.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.http.client.*;
import com.google.gwt.xml.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.WindowResizeListener;

import com.allen_sauer.gwt.log.client.Log;

import java.lang.*;
import java.util.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class tagDisplay implements EntryPoint
{
  double initLng = 0.0;
  double initLat = 0.0;
  int initZoom = 0;
  String logoURL = null; 
  String tagArrayURL = null;
  String aboutDatURL = null;
  String featuresURL = null;

  JSONArray recaps;
  JSONArray features;

  MapWidget animationMap;
  MapWidget recaptureMap;
  MapGrid animationGrid = null;
  MapGrid recaptureGrid = null;



  public native String getInitCode()
  /*-{ 
        return $wnd.code;
  }-*/;

  static DateTimeFormat dtfIn = DateTimeFormat.getFormat("MM/dd/yyyy");
  static DateTimeFormat dtfOut = dtfIn.getMediumDateFormat();

  public static Date getDate(String text)
  {
   return(dtfIn.parse(text));
  }

  public static LatLng getLatLng(JSONObject jo)
  {
    LatLng LLRet = LatLng.newInstance(getDouble(jo,"LAT"),getDouble(jo,"LNG"),true);
    return(LLRet);
  }

  public static long getMSDate(JSONObject jo, String find)
  {
     String dateString = getString(jo,find);
     Date date = getDate(dateString);
     return(date.getTime());
  }

  public static String getString(JSONObject jo, String find)
  {
     String s = null;

     JSONValue jv = null;
     if ((jv = jo.get(find)) == null)
     {
       return s;
     }

     JSONString js;
     if ((js = jv.isString()) == null)
     {
       return s;
     }

     s = js.stringValue();
     return s;
  }

  public static double getDouble(JSONObject jo, String find)
  {
     JSONValue jv = null;
     if ((jv = jo.get(find)) == null)
     {
       return java.lang.Double.NaN;
     }

     JSONNumber jn = null;
     if ((jn = jv.isNumber()) == null)
     {
       return java.lang.Double.NaN;
     }
     double x = jn.getValue();
     return x;  
  }

  public static int getInt(JSONObject jo, String find)
  {
    return (int)getDouble(jo,find);
  }

  void ExtractTagArray(Response res)
  {
    String resText = null;
    if (( resText = res.getText()) == null)
    {
       Window.alert("Response text null decoding recap data");
       Log.fatal("Response text null decoding recap data");
    }

    JSONValue resValue=null;
    try {
      resValue = JSONParser.parse(resText);
    } catch (JSONException e) {
      GWT.log("JSON parse exception: ", e);
    }

    if (resValue == null)
    { 
      Window.alert("resValue == null result parsing recap data");
      Log.fatal("resValue == null result parsing recap data");
    }

    JSONObject tagObject=null;
    if ((tagObject = resValue.isObject()) == null)
    {
       GWT.log("Tag data JSON object not found.",null);
    }

    if ((recaps = tagObject.get("TagList").isArray()) == null)
    {
       GWT.log("TagList not found in JSONObject ", null);
       Log.fatal("TagList not found in JSONObject");
    }

    // sanity check for recaps
    int ntag = recaps.size();
    Log.info("Length of recaps is "+java.lang.String.valueOf(ntag));
    if (ntag <= 0)
    {
      Window.alert("Tag array has no recaptures! Check file "+tagArrayURL);
      Log.fatal("Tag array has no recaptures! Check file "+tagArrayURL);
    }
    else
    {
      int nvalid = 0;
      for (int i = 0; i < ntag;  i++)
      {
        int errorCount = 0;
	JSONValue value = recaps.get(i);
	if (value instanceof JSONObject)
	{
	  JSONObject object = (JSONObject)value;
	  JSONValue valueTag = object.get("tag");
	  if (valueTag instanceof JSONObject)
	  {
	    JSONObject tag = (JSONObject)valueTag;
	    String SP = getString(tag,"SP");
            String TT =  tagDisplay.getString(tag,"TT");
            if ( (TT != null) && (TT.compareTo("AT") == 0) )
            {
              JSONArray posArray = null;
              if ((posArray = tag.get("PosList").isArray()) == null)
              {
                Log.fatal("PosList for has no recatpures for tag "+getString(tag,"ID"));
              }
              else
              {
                int npos = posArray.size();
                Log.info("AT tag "+getString(tag,"ID")+ " has "+
                         java.lang.String.valueOf(npos)+" positions");
              }
            }


          }
          else
          {
            errorCount = errorCount + 1;
          }
        }
        else
        {
          errorCount = errorCount + 1;
        }
        if (errorCount == 0)
          nvalid = nvalid + 1;
      }
    }
    Log.info("Length of recaps: "+java.lang.String.valueOf(ntag));


    final RootPanel rp =  RootPanel.get();
    Log.info("rp.getOffsetWidth()  = "+java.lang.String.valueOf(rp.getOffsetWidth()));
    Log.info("rp.getOffsetHeight() = "+java.lang.String.valueOf(rp.getOffsetHeight()));
    rp.setSize("100%","100%");
    Log.info("rp.getOffsetWidth()  = "+java.lang.String.valueOf(rp.getOffsetWidth()));
    Log.info("rp.getOffsetHeight() = "+java.lang.String.valueOf(rp.getOffsetHeight()));
    int rpw = rp.getOffsetWidth();//-10;
    int rph = rp.getOffsetHeight();//-60;
    Log.info(java.lang.String.valueOf(rpw)+"px, "+java.lang.String.valueOf(rph)+"px");

    TabPanel tp = new TabPanel();
    tp.setSize("100%","100%");

    tp.addTabListener(new TabListener() {
      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
          return true;
      }
      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        if (onBeforeTabSelected(sender, tabIndex)){
          if (tabIndex == 0)
            animationGrid.checkResize();
          else if (tabIndex == 1)
            recaptureGrid.checkResize();
        }
      }
    });

    Log.info("animationGrid:");
    animationGrid = new MapGrid(rpw, rph, initLat, initLng, initZoom);
    animationMap = animationGrid.getMap();
    Log.info("animationGrid.getMap");

    AnimationDisplay ad = new AnimationDisplay(animationMap, recaps, features, logoURL);
    Log.debug("AnimationDisplay construtor");
    animationGrid.setControlPanel(ad.getControl());
    Log.debug("setControlPanel");

    Log.info("recaptureGrid:");
    recaptureGrid = new MapGrid(rpw, rph, initLat, initLng, initZoom);
    recaptureMap = recaptureGrid.getMap();
    Log.info("recaptureGrid.getMap");

    RecaptureDisplay rd = new RecaptureDisplay(recaptureMap,recaps,logoURL, features);
    recaptureGrid.setControlPanel(rd.getControl());

    Window.addWindowResizeListener(new MapResizeListener());

    VerticalPanel textPanel = new VerticalPanel();
    textPanel.setStylePrimaryName("gwt-infoPanel");
    textPanel.add(new HTML("This display tool uses <a href=\"http://maps.google.com/\">Google Maps</a> to display fish tagging data. Position data for <b>both</b> electronic and conventional tags are displayed on the same map."));
    textPanel.add(new HTML("<b>Recapture Map Tab: </b>Displays tag recapture positions and geographic features as colored-coded icons. Clicking the icons reveals information about the fish, the tag release position and details about the recapture. Electronic tags are marked with icons congaing a black circle in the center. Clicking a recapture icon also draws a track from the recapture point to the release point."));
    textPanel.add(new HTML("<b>Animation Tab: </b>Provides a representation of the relative movement rates of the tagged fish. Conventional tags are assumed to move at a constant speed in a straight line from point of release to point of recapture. Tracks for electronic tags are statistical reconstructions based on from data recovered from the tags and are shown with lines connecting the segments. Click the play button to start the animation.<br><br>"));

    DockPanel dp = new DockPanel();
    SimplePanel eastPanel = new SimplePanel();
    SimplePanel westPanel = new SimplePanel();
    HTML heading = new HTML("<h1 align=center>Interactive Fish Tag Display</h1><br>");
    dp.add(heading,DockPanel.NORTH);
    dp.add(eastPanel, DockPanel.EAST);
    dp.add(westPanel, DockPanel.WEST);
    dp.add(textPanel, DockPanel.CENTER);
    dp.setCellWidth(westPanel, "20%");
    dp.setCellWidth(eastPanel, "20%");
    dp.setCellWidth(textPanel, "60%");
  
    Button dataButton = new Button("About The Tagging Data");
    dataButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        if (aboutDatURL != null)
        {
          final PopupPanel pop = new PopupPanel(true,true);
          Frame f = new Frame(aboutDatURL);
          f.setStylePrimaryName("gwt-infoFrame");
          pop.setWidget(f);
          pop.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
              int left = (Window.getClientWidth() - offsetWidth) / 4;
              int top = (Window.getClientHeight() - offsetHeight) / 3;
              pop.setPopupPosition(left, top);
            }
          });
        }
        else
        {
          Window.alert("Sorry, no information available about these data.");
        }

      }
    });
    dataButton.setStylePrimaryName("gwt-infoButton");
    dataButton.setTitle("Click for information about the data in this display");

    Button displayButton = new Button("About This Display");
    displayButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        final PopupPanel pop = new PopupPanel(true,true);
        Frame f = new Frame("readme.html");
        f.setStylePrimaryName("gwt-infoFrame");
        pop.setWidget(f);
        pop.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = 3*(Window.getClientWidth() - offsetWidth) / 4;
            int top = (Window.getClientHeight() - offsetHeight) / 3;
            pop.setPopupPosition(left, top);
          }
          });
      }
    });
    displayButton.setStylePrimaryName("gwt-infoButton");
    displayButton.setTitle("Click for information on how to adapt this display for your data");

    Grid infoGrid = new Grid(1, 2);
    infoGrid.setWidget(0,0,dataButton);
    infoGrid.setWidget(0,1,displayButton);
    dp.add(infoGrid,DockPanel.SOUTH);
    dp.setCellHorizontalAlignment(infoGrid, HasHorizontalAlignment.ALIGN_CENTER);
    
    tp.add(animationGrid,"Animation");
    tp.add(recaptureGrid,"Recapture Map");
    tp.add(dp,"Introduction");
    tp.selectTab(2);

    rp.add(tp);
  }
   
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() 
  { 
    Log.setUncaughtExceptionHandler();
    Log.setCurrentLogLevel(Log.LOG_LEVEL_OFF);
    //Log.setCurrentLogLevel(Log.LOG_LEVEL_DEBUG);
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        onModuleLoad2();
      }
    });
  }

  private void onModuleLoad2()
  {
    /**
    =========================================================================
    get map initizer information from JSON object stored in the html as javascript
    */
    String initString = getInitCode();

    JSONValue initValue=null;
    try
    {
      initValue = JSONParser.parse(initString);
    }
    catch (JSONException e)
    {
      Window.alert("JSON parse execption "+e);
      GWT.log("JSON parse execption ",e);
    }

    JSONObject initObject = null;
    if ((initObject = initValue.isObject()) == null)
    {
      GWT.log("initObject is unexpected type",null);
    }

    Log.info("initString: "+initString);
    initLng = getDouble(initObject,"centerLng");
    initLat = getDouble(initObject,"centerLat");
    initZoom = getInt(initObject,"initZoom");
    tagArrayURL = getString(initObject,"datURL");
    Log.info("Tag data file: "+tagArrayURL);
    featuresURL = getString(initObject,"featuresURL");
    Log.info("Features Tag data file: "+featuresURL);
    aboutDatURL = getString(initObject,"aboutDatURL");
    Log.info("About data file: "+aboutDatURL);
    logoURL = getString(initObject,"logoURL");
    Log.info("Logo image file: "+logoURL);

    //=========================================================================
    if (featuresURL != null)
    {
      RequestBuilder featuresRB = new RequestBuilder(RequestBuilder.POST,
          GWT.getModuleBaseURL()+featuresURL);
      String featureRequestData = null;
      RequestCallback FeatureCallback = new RequestCallback(){
        public void onError(Request req, Throwable ex) {
          Window.alert("feature request failed");
        }
        public void onResponseReceived(Request req, Response res) {
          extractFeatureArray(res);
        }
      };
    
      try {
        featuresRB.sendRequest(featureRequestData, FeatureCallback);
      }
      catch (RequestException e){
        Window.alert(e.getMessage());
      }
    }

    /**
    =========================================================================
    upload file from server and get recapture data as JSON object
    */
    RequestBuilder tag_arrayRB = new RequestBuilder(RequestBuilder.POST,
        GWT.getModuleBaseURL()+tagArrayURL);
    String requestData = "";
    RequestCallback callback = new RequestCallback(){
      public void onError(Request req, Throwable ex) {
        Window.alert("tag data request failed");
      }
      public void onResponseReceived(Request req, Response res) {
        String TagArrayText = res.getText();
        ExtractTagArray(res);
      }
    };

    try {
      tag_arrayRB.sendRequest(requestData,callback);
    }
    catch (RequestException e){
      Window.alert(e.getMessage());
    }

  } // onModuleLoad()


  void extractFeatureArray(Response res)
  {
    String resText = null;
    if (( resText = res.getText()) == null)
    {
       Window.alert("Response text null decoding feature data");
       Log.fatal("Response text null decoding feature data");
    }

    JSONValue resValue=null;
    try {
      resValue = JSONParser.parse(resText);
    } catch (JSONException e) {
      GWT.log("JSON parse exception in extractFeatureArray( : ", e);
 
    }

    if (resValue == null)
    { 
      Window.alert("resValue == null result parsing feature data");
      Log.fatal("resValue == null result parsing feature data");
    }

    JSONObject featureObject=null;
    if ((featureObject = resValue.isObject()) == null)
    {
       Log.fatal("features JSON object not found.");
    }

    if ((features = featureObject.get("FeatureList").isArray()) == null)
    {
       Log.fatal("FeatureList not found in JSONObject");
    }

    Log.info("Length of features is "+java.lang.String.valueOf(features.size()));

  }


  public static void HERE(String s)
  {
    Log.debug("HERE "+s);
  }


  public class MapResizeListener implements WindowResizeListener
  {
    boolean informResize = true;
    public void onWindowResized(int x, int y) 
    {
      animationGrid.resizeMap(x,y);
      recaptureGrid.resizeMap(x,y);
      informResize = !informResize;
    }
  }

} // public class tagDisplay implements EntryPoint


