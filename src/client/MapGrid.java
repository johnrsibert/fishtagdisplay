/**
 * $Id: MapGrid.java 2566 2009-04-15 01:43:21Z jsibert $
 *
 * Author: John Sibert
 * Copyright (c) 2008, 2009 John Sibert
 *
 */
package client;
import com.google.gwt.maps.client.*;
import com.google.gwt.maps.client.event.*;
import com.google.gwt.maps.client.geom.*;
import com.google.gwt.maps.client.control.*;
import com.google.gwt.maps.client.overlay.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import com.google.gwt.http.client.*;
import com.google.gwt.xml.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.allen_sauer.gwt.log.client.Log;

import java.lang.*;
import java.util.*;


public class MapGrid extends Grid
{
  MapWidget map = null;
  HorizontalPanel mp = null;
  String smpw = null;
  String scpw = null;
  String sph = null;
  VerticalPanel Control = null;


  MapGrid(int rpw, int rph, double initLat, double initLng, int initZoom)
  {
    Log.info("initLng = "+java.lang.String.valueOf(initLng));
    Log.info("initLat = "+java.lang.String.valueOf(initLat));
    Log.info("initZoom = "+java.lang.String.valueOf(initZoom));
    resize(1,2);
    smpw = java.lang.String.valueOf(3*rpw/4)+"px";
    scpw = java.lang.String.valueOf(rpw/4)+"px";
    sph  = java.lang.String.valueOf(rph)+"px";
    getCellFormatter().setAlignment(0,0,HasHorizontalAlignment.ALIGN_CENTER,
                                        HasVerticalAlignment.ALIGN_TOP);
    getCellFormatter().setAlignment(0,1,HasHorizontalAlignment.ALIGN_CENTER,
                                        HasVerticalAlignment.ALIGN_TOP);

    getCellFormatter().addStyleName(0,0,"gwt-MapPanel");
    getCellFormatter().addStyleName(0,1,"gwt-ControlPanel");

    //map = new MapWidget(new LatLng(initLat, initLng), initZoom);
    //map = new MapWidget(LatLng.newInstance(initLat, initLng,true), initZoom);
    final LatLng centerPos = LatLng.newInstance(initLat, initLng,false);
    map = new MapWidget(centerPos, initZoom);
    map.addControl(new LargeMapControl());
    map.addControl(new MapTypeControl());
    map.addControl(new ScaleControl());
    map.setCurrentMapType(MapType.getSatelliteMap());
    map.setSize(smpw,sph);
    //map.addOverlay(new Marker(centerPos));

    mp = new HorizontalPanel();
    mp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mp.add(map);
    setWidget(0,0,mp);
  }

  public void checkResize()
  {
    resizeMap(Window.getClientWidth(),Window.getClientHeight());
  }

  public void resizeMap(int width, int height)
  {
    int newWidth = 3*width/4;
    int newHeight = height;
    LatLng newCenter = map.getCenter();
    map.setSize( newWidth + "px", newHeight + "px"); 
    map.setCenter(newCenter);
    Control.setSize((width/4)+"px", newHeight + "px"); 
  }

  public void setControlPanel(VerticalPanel cp)
  {
    Control = cp;
    setWidget(0,1,Control);
  }

  public VerticalPanel getControlPanel()
  {
     return Control;
  }

  public MapWidget getMap()
  {
     return map;
  }
}
