/**
 * $Id: AnimationDisplay.java 2566 2009-04-15 01:43:21Z jsibert $
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

public class AnimationDisplay
{
  AnimationControlPanel control = null;
  Animator animation = null;

  AnimationDisplay(MapWidget map, JSONArray array, JSONArray features, String logoURL)
  {
    Log.debug("start AnimationDisplay(MapWidget map, JSONArray array, String logoURL)");

    animation = new Animator(map,array);
    control = new AnimationControlPanel(map, array, features, logoURL);
    if (control == null)
    {
       Log.fatal("null Control object in passed to AnimationDisplay(..)");
    }
  
  
    control.setAnimation(animation);
    animation.setControl(control);

    // only do this after the animation and controls have exchanged variables
    animation.Setup();
    Log.debug("end AnimationDisplay(MapWidget map, JSONArray array, String logoURL)");
  }

  public AnimationControlPanel getControl()
  {
    return control;
  }

  public Animator getAnimator()
  {
    return animation;
  }
}  
