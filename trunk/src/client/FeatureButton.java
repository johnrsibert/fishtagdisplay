/**
 * $Id: FeatureButton.java 2566 2009-04-15 01:43:21Z jsibert $
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

import com.allen_sauer.gwt.log.client.Log;

import java.lang.*;
import java.util.*;

public class FeatureButton extends ToggleButton
{
  MapWidget map = null;
  Marker[] zMarker = null;
  int nFeature = 0;

  FeatureButton(MapWidget aMap, JSONArray features)
  {
    super("Hide Features","Show Features");
    setWidth("8em");
    setDown(false);
  
    map = aMap;    
    nFeature = features.size();

    MarkerOptions gmo = MarkerOptions.newInstance();
    Icon featureIcon = Icon.newInstance();
    featureIcon.setIconSize(Size.newInstance(18,30));    
    featureIcon.setIconAnchor(Point.newInstance(9, 30));
    featureIcon.setInfoWindowAnchor(Point.newInstance(9, 9));
    featureIcon.setImageURL(GWT.getModuleBaseURL()+"images/aqua-icon.png");
    featureIcon.setInfoWindowAnchor(Point.newInstance(9, 9));

    gmo.setIcon(featureIcon);
    final InfoWindow info = map.getInfoWindow();

    zMarker = new Marker[nFeature];

    for (int i = 0; i < nFeature; i++)
    {
      JSONValue featureValue = features.get(i);
      if (featureValue instanceof JSONObject)
      {
        JSONObject feature = (JSONObject)featureValue;
        final LatLng pos = tagDisplay.getLatLng(feature);
        final HTML name = new HTML("<br>"+tagDisplay.getString(feature,"NAME"));
        name.setStylePrimaryName("gwt-FeatureNameHTML");
        Marker fMarker =  new Marker(pos,gmo);
        fMarker.addMarkerClickHandler(new MarkerClickHandler(){
          public void onClick(MarkerClickHandler.MarkerClickEvent event){ 
              info.open(pos, new InfoWindowContent(name));
            }
        }); 

        zMarker[i] = fMarker;
        map.addOverlay(zMarker[i]);
      }
      //else ! (value instanceof JSONOBject)
      // need some error messages
     }
  }

  protected void onClick()
  {
     if (isDown())
     {
       for (int i = 0; i < nFeature; i++)
         zMarker[i].setVisible(true);
       setDown(false);
     }
     else
     {
       for (int i = 0; i < nFeature; i++)
         zMarker[i].setVisible(false);
       setDown(true);
     }
  }


}
