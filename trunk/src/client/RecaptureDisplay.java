/**
 * $Id: RecaptureDisplay.java 2566 2009-04-15 01:43:21Z jsibert $
 *
 * Author: John Sibert
 * Copyright (c) 2008, 2009 John Sibert
 *
 */
package client;

import com.google.gwt.core.client.EntryPoint;
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

import com.allen_sauer.gwt.log.client.Log;

import java.lang.*;
import java.util.*;

public class RecaptureDisplay
{
  JSONArray tagArray = null;
  JSONArray features = null;
  MapWidget recaptureMap = null;
  RecaptureControlPanel control = null;
  HashMap cName = null;
  HashMap sName = null;
  HashMap fishColor = null;
  HashMap imageMap = null;
  HashMap ATimageMap = null;

  public RecaptureDisplay(MapWidget map, JSONArray array, String logoURL, JSONArray fa)
  {
    tagArray = array;
    features = fa;
    recaptureMap = map;

    control = new RecaptureControlPanel(logoURL,features);

    cName = new HashMap(3);
    cName.put("Y", "Yellowfin");
    cName.put("B", "Bigeye");
    cName.put("S", "Skipjack");

    sName = new HashMap(3);
    sName.put("Y", "Thunnus albacares");
    sName.put("B", "Thunnus obesus");
    sName.put("S", "Katsuwonus pelamis");
 
    fishColor = new HashMap(3);
    fishColor.put("Y","#FFFF00");
    fishColor.put("B","#FF0000");
    fishColor.put("S","#00FF00");

    imageMap = new HashMap(3);
    imageMap.put("Y", GWT.getModuleBaseURL()+"images/yellow-icon-12x20.png");
    imageMap.put("B", GWT.getModuleBaseURL()+"images/red-icon-12x20.png");
    imageMap.put("S", GWT.getModuleBaseURL()+"images/brightgreen-icon-12x20.png");
    ATimageMap = new HashMap(3);
    ATimageMap.put("Y", GWT.getModuleBaseURL()+"images/yellow-icon-12x20-dot.png");
    ATimageMap.put("B", GWT.getModuleBaseURL()+"images/red-icon-12x20-dot.png");
    ATimageMap.put("S", GWT.getModuleBaseURL()+"images/brightgreen-icon-12x20-dot.png");

    Icon recapIcon = Icon.newInstance();
    recapIcon.setIconSize(Size.newInstance(12, 20));
    recapIcon.setIconAnchor(Point.newInstance(6, 20));
    recapIcon.setInfoWindowAnchor(Point.newInstance(6, 6));
    MarkerOptions gmo = MarkerOptions.newInstance();

    int n1 = 0;   
    int n2 = tagArray.size();
    for (int i = n1; i <  n2; i++)
    {
       JSONValue value = tagArray.get(i);
       if (value instanceof JSONObject)
       {
         JSONObject object = (JSONObject)value;
         JSONValue valueTag = object.get("tag");
         if (valueTag instanceof JSONObject)
         {
           JSONObject tag = (JSONObject)valueTag;
           String SP = tagDisplay.getString(tag,"SP");
           String TT =  tagDisplay.getString(tag,"TT");
           if ( (TT != null) && (TT.compareTo("AT") == 0) )
           {
             Log.debug(tagDisplay.getString(tag,"ID") +": "+ TT+": "+(String)ATimageMap.get(SP));
             recapIcon.setImageURL((String)ATimageMap.get(SP)); 
           }
           else
             recapIcon.setImageURL((String)imageMap.get(SP)); 
           gmo.setIcon(recapIcon);
           JSONObject cap = tag.get("cap").isObject();
           Marker marker = new Marker(tagDisplay.getLatLng(cap),gmo);
           marker.addMarkerClickHandler(new RecapMarkerEvenetListener(tag,marker));
           recaptureMap.addOverlay(marker);
         }
         else
         {
           Log.debug("element "+java.lang.String.valueOf(i)+
                   " is not a valid JSON tag element");
         }
       } 
       else
       {
          Log.debug("element "+java.lang.String.valueOf(i)+
                  " is not a valid JSON object");
       }
    }
  }

  public RecaptureControlPanel getControl()
  {
    return control;
  }

  public class RecaptureControlPanel extends VerticalPanel
  {
    RecaptureControlPanel(String logoURL, JSONArray features)
    {
      Log.debug("start of  RecaptureControlPanel(String logoURL)");
      setStylePrimaryName("gwt-ControlPanel");

      setHorizontalAlignment(ALIGN_CENTER);
      setVerticalAlignment(ALIGN_TOP);
      add(new Image(logoURL));
  
      add(new HTML("<h3 align=center>Recapture Display</h3>"));
      add(new HTML("<br align=center>Click icons on map to reveal information about tag releases and recaptures and features</br><br>"));
      if (features != null)
        add(new FeatureButton(recaptureMap, features));
      add(new HTML("<h4 align=center>Legend</h4>"));
      Grid lg = new Grid(3,2);
      lg.setBorderWidth(1);
      lg.setCellPadding(3);
      lg.setWidget(0,0,new Label("Skipjack"));
      lg.setWidget(0,1,new Image(GWT.getModuleBaseURL()+"images/brightgreen-icon-12x20.png"));
      lg.setWidget(1,0,new Label("Yellowfin"));
      lg.setWidget(1,1,new Image(GWT.getModuleBaseURL()+"images/yellow-icon-12x20.png"));
      lg.setWidget(2,0,new Label("Bigeye"));
      lg.setWidget(2,1,new Image(GWT.getModuleBaseURL()+"images/red-icon-12x20.png"));
      add(lg);

      Log.debug("  end of  RecaptureControlPanel(String logoURL)");
    }
  }
 
  public class RecapInfoOverlay
  {
    LatLng[] Verts = null;
    Polyline line = null;

    RecapInfoOverlay(JSONObject tag, Marker marker)
    {
      Log.setCurrentLogLevel(Log.LOG_LEVEL_INFO);
      String ID = tagDisplay.getString(tag,"ID");
      String SP = tagDisplay.getString(tag,"SP");
      String sex = tagDisplay.getString(tag,"SEX");
      if (sex == null)
         sex = "NA";
      String TT =  tagDisplay.getString(tag,"TT");
      if (TT == null)
         TT = "Conventional";
 
      JSONObject rel = tag.get("rel").isObject();
      JSONObject cap = tag.get("cap").isObject();
      LatLng relPos = tagDisplay.getLatLng(rel);
      LatLng capPos = tagDisplay.getLatLng(cap);
      long relDate = tagDisplay.getMSDate(rel,"DD");
      long capDate = tagDisplay.getMSDate(cap,"DD");
      long dal = 1+(capDate-relDate)/86400000;

      int kmTravelled = (int)(relPos.distanceFrom(capPos)/1000.0);
      String kmTravelledS = java.lang.String.valueOf(kmTravelled);

      VerticalPanel tagInfoPanel = new VerticalPanel();
      tagInfoPanel.setStylePrimaryName("gwt-tagInfoWindowPanel");
      tagInfoPanel.add(new HTML("<b>Tag ID: "+ID+"</b><br>"+
                                 " Species: "+(String)cName.get(SP)+
                                 " (<i>"+(String)sName.get(SP)+"</i>)<br>"+
                                 " Sex: "+ sex+"<br>Tag Type: "+TT+"<br>"+
                                 " Travelled: "+kmTravelledS+
                                 " km in "+java.lang.String.valueOf(dal)+" days"));


      tagInfoPanel.add(getPositionInfo(rel,"Release",false));
      tagInfoPanel.add(getPositionInfo(cap,"Recapture",true));
      InfoWindow info = recaptureMap.getInfoWindow();
      info.open(marker.getPoint(),new InfoWindowContent(tagInfoPanel));

      if ( (TT != null) && (TT.compareTo("AT") == 0) )
      {
        JSONArray posArray = null;
        if ((posArray = tag.get("PosList").isArray()) == null)
        {
          Log.fatal("PosList for has no recatpures for tag "+
                    tagDisplay.getString(tag,"ID")+ " in RecaptureDisplay");
        }
        else
        {
          int npos = posArray.size();
          Log.info("AT tag "+tagDisplay.getString(tag,"ID")+ " has "+
                         java.lang.String.valueOf(npos)+" positions");
          Verts = new LatLng[npos];
          for (int p = 0; p < npos; p++)
          {
            JSONValue posValue = posArray.get(p);
            Log.debug(posValue.toString());
            if (posValue instanceof JSONObject)
            {
              JSONObject posObject = (JSONObject)posValue;
              JSONValue pos = posObject.get("pos");
              Log.debug(pos.toString());
              Verts[p] = tagDisplay.getLatLng((JSONObject)pos);
            }
            else
            {
              Log.fatal("unable to generate position JSONObject");
            }
            Log.debug(Verts[p].toString());
          }
        }

      }
      else
      {
        Verts = new LatLng[2];
        Verts[0] = relPos;
        Verts[1] = capPos;
      }
      line = new Polyline(Verts,(String)fishColor.get(SP),2);  
      if (line == null)
        Window.alert("null Polyline in  RecapInfoOverlay(JSONObject tag, Marker marker)");
      else
        recaptureMap.addOverlay(line); 

      recaptureMap.addInfoWindowCloseHandler(
        new MapInfoWindowCloseHandler(){
	  public void onInfoWindowClose(
            MapInfoWindowCloseHandler.MapInfoWindowCloseEvent event){
              if (line != null)
              {
                int vCount = line.getVertexCount();
                Log.debug("points in line = "+java.lang.String.valueOf(vCount));
                for (int v = 0; v < vCount; v++)
                  Log.debug("vertex "+java.lang.String.valueOf(v)+": "+line.getVertex(v).toString());
                recaptureMap.removeOverlay(line);
                line=null;
              }
        }
      });
    }

    public HTML getPositionInfo(JSONObject pos, java.lang.String label, boolean recap)
    {
      String fishLengthStr = null;
      double fishLength = tagDisplay.getDouble(pos,"LEN");
      if (Double.isNaN(fishLength))
        fishLengthStr = "NA";
      else
        fishLengthStr = java.lang.String.valueOf(fishLength);

      String posInfo = "<b>"+label+"</b>:";
      posInfo = posInfo + "<br> Date: "+ tagDisplay.getString(pos,"DD") + "<br> Positon: "+
              java.lang.String.valueOf(tagDisplay.getDouble(pos,"LNG")) + " x " +
              java.lang.String.valueOf(tagDisplay.getDouble(pos,"LAT"))
               + "<br> Length: " + fishLengthStr + " cm";

      if (recap)
      {
        String recoveryInfo =  "<br> Flag: " + tagDisplay.getString(pos,"FL") +
                               "; gear: " + tagDisplay.getString(pos,"GG");
        posInfo = posInfo + recoveryInfo;
      }

      return new HTML(posInfo);
    }
  }


  public class RecapMarkerEvenetListener implements MarkerClickHandler
  {
    JSONObject tag;
    RecapInfoOverlay info;
    Marker marker;

    RecapMarkerEvenetListener(JSONObject atag, Marker amarker) {
         tag = atag;
         marker = amarker;
    }

    public void onClick(MarkerClickHandler.MarkerClickEvent event)
    {
       RecapInfoOverlay info = new RecapInfoOverlay(tag, marker);
    }
  }

}
