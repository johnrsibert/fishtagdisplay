/**
 * $Id: Animator.java 2566 2009-04-15 01:43:21Z jsibert $
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
import com.google.gwt.maps.client.overlay.Icon;
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

/**
=========================================================================
classes and functions used by animation
*/
public class Animator
{
  MapWidget animationMap = null;
  JSONArray recaps = null;
  AnimationControlPanel Controls = null;
  int wLen  = 0;
  int wLen1 = 0;
  int wLen2 = 0;
  int rate = 0;
  Date Day1;
  long CurrentMS = 0;
  Date CurrentDate;
  long LastMS = 0;;
  Date LastDate;
  long dayMS = 0;
  Timer AnimationTimer = null;
  //MarkerOptions gmo = null;
  Marker [][] wMarker = null;
  Polyline [][] wLine = null;
  HashMap imageMap = null;
  HashMap fishColor = null;
  long [] RelMS = null;
  long [] CapMS = null;
  int n1 = 0;
  int n2 = 0;
  int ntag = 0;
  int releaseCount = 0;
  int segmentCount = 0;
  double [] X = null; 
  double [] Y = null; 
  double [] dx = null; 
  double [] dy = null; 
  String [] SP = null;
  String [] TT = null;
  int [] dal = null;
  Random rng = null;
  //Icon wormIcon = null;

  public void HERE(String s)
  {
    Log.debug(s);
  }

  Animator(MapWidget map, JSONArray array)
  {
    HERE("entering Animator(JSONArray array, MapWidget map)");
    animationMap = map;
    recaps = array;
    HERE("before ntag = recaps.size();");
    if (recaps != null)
      ntag = recaps.size();
    n1 = 0;
    n2 = ntag;
    CurrentDate = new Date();
    LastDate = new Date();

    imageMap = new HashMap(3);
    imageMap.put("Y", GWT.getModuleBaseURL()+"images/yellow-ball-5x5.png");
    imageMap.put("B", GWT.getModuleBaseURL()+"images/red-ball-5x5.png");
    imageMap.put("S", GWT.getModuleBaseURL()+"images/brightgreen-ball-5x5.png");

    fishColor = new HashMap(3);
    fishColor.put("Y","#FFFF00");
    fishColor.put("B","#FF0000");
    fishColor.put("S","#00FF00");

    dayMS = 86400000; //24*60*60*1000 milliseconds / day

    AnimationTimer = new Timer() {
      public void run() {
        OneDay();
      }
    };

    X = new double[ntag];
    Y = new double[ntag];
    dx = new double[ntag];
    dy = new double[ntag];
    RelMS = new long[ntag];
    CapMS = new long[ntag];
    SP = new String[ntag];
    TT = new String[ntag];
    dal = new int[ntag];

    for (int i = n1; i < n2; i++)
    {
      JSONValue value = recaps.get(i);
      if (value instanceof JSONObject)
      {
        JSONObject object = (JSONObject)value;
        JSONValue valueTag = object.get("tag");
        if (valueTag instanceof JSONObject)
        {
          JSONObject tag = (JSONObject)valueTag;
          JSONObject rel = tag.get("rel").isObject();
          JSONObject cap = tag.get("cap").isObject();
          SP[i] = tagDisplay.getString(tag,"SP");
          TT[i] = tagDisplay.getString(tag,"TT");

          RelMS[i] = 1000*(tagDisplay.getMSDate(rel,"DD")/1000);
          CapMS[i] = 1000*(tagDisplay.getMSDate(cap,"DD")/1000);
          double dt = (double)((CapMS[i]-RelMS[i])/dayMS) + (double)0.01;
          if (dt <= 0.0)
          {
            //GLog.write("Negative time step ("+ java.lang.String.valueOf(dt) +") found for tag "
              //+ java.lang.String.valueOf(i) + ": " + getString(tag,"ID"));
            dt = -dt;
          }

          dy[i] = (double)(tagDisplay.getDouble(cap,"LAT") - 
                           tagDisplay.getDouble(rel,"LAT"))/dt;
          double x1 = tagDisplay.getDouble(rel,"LNG");
          if (x1 < 0)
            x1 = 360.0+x1;
          double x2 = tagDisplay.getDouble(cap,"LNG");
          if (x2 < 0)
            x2 = 360.0+x2;
          dx[i] = (x2 - x1)/dt;

          if (CapMS[i] > LastMS)
          {
            LastMS = CapMS[i];
          }
        }
        else
        {
          //GLog.write("tag "+java.lang.String.valueOf(i)+
                           //" is not a valid JSON tag element");
         }
      }
      else
      {
        //GLog.write("element "+java.lang.String.valueOf(i)+" is not a valid JSON object");
      }
    }
    CurrentMS = RelMS[n1];
    LastMS = LastMS + dayMS;

    LastDate.setTime(LastMS);
    //GLog.write("Last recap: "+LastDate.toString());

    //Setup();
  }

  public void setControl(AnimationControlPanel aControl)
  {
    if (aControl == null)
    {
       Log.debug("null Control object passed in Animator.setControl(AnimationControlPanel aControl)");
    }
    else
    {
       Log.debug("Control object OK in Animator.setControl(AnimationControlPanel aControl)");
    }
    Controls = aControl;

  }

  public void Setup()
  {
    rate = Controls.getRate();
    wLen = Controls.getWormLength();
    wLen1 = wLen - 1;
    wLen2 = wLen - 2;
    Log.debug("worm length is "+java.lang.String.valueOf(wLen));

    if (wMarker == null)
    {
      Log.debug("allocating wMarker in Setup()");
      wMarker = new Marker[ntag][wLen];
      wLine = new Polyline[ntag][wLen1];
    }
    else
      removeAll();

    Controls.setSegmentCounter(segmentCount);

    JSONObject tag = null; 
    JSONObject rel = null;
    for (int i = n1; i < n2; i++)
    {
      X[i] = 0;
      Y[i] = 0;
      dal[i] = 0;
      for (int k = 0; k < wLen; k++)
      {
        wMarker[i][k] = null;
        if (k < wLen1)
          wLine[i][k] = null;
      }
      JSONValue value = recaps.get(i);
      if (value instanceof JSONObject)
      {
        JSONObject object = (JSONObject)value;
        JSONValue valueTag = object.get("tag");
        if (valueTag instanceof JSONObject)
        {
          tag = (JSONObject)valueTag;
          JSONValue valueRel = tag.get("rel");
          if (valueRel instanceof JSONObject)
          {
            rel = (JSONObject)valueRel;
            if (rel.containsKey("LNG") == true && rel.containsKey("LAT"))
            {
              //X[i] = getDouble(rel,"LNG")+(2*rng.nextDouble()-1)/10;
              //Y[i] = getDouble(rel,"LAT")+(2*rng.nextDouble()-1)/10;
              X[i] = tagDisplay.getDouble(rel,"LNG");
              Y[i] = tagDisplay.getDouble(rel,"LAT");
            }
          }
        }
      }
    }

    CurrentMS = RelMS[n1]-dayMS;
    CurrentDate.setTime(CurrentMS);
    Controls.setDateBox(CurrentDate);
    releaseCount = 0;
    Controls.setReleaseCounter(releaseCount);
    segmentCount = 0;
    Controls.setSegmentCounter(segmentCount);

    Log.trace("Finished Animator Setup()");
  } 

  public void OneDay()
  {
    Icon wormIcon = Icon.newInstance();
    wormIcon.setIconSize(Size.newInstance(5,5));
    wormIcon.setIconAnchor(Point.newInstance(3,3));
    MarkerOptions gmo = MarkerOptions.newInstance();
    for (int i = n1; i < n2; i++)
    {
      // semi-superfluous test which skips tags recaptured before they were released
      if ( (CurrentMS >= RelMS[i]) &&  (CurrentMS <= CapMS[i]) )
      {
        long HeadMS = RelMS[i] + (wLen-1)*dayMS;
        long TailMS = CapMS[i] - (wLen-1)*dayMS;
        Log.debug("imageMap.get(SP[i]): " + imageMap.get(SP[i]));
        wormIcon.setImageURL((String)imageMap.get(SP[i])); 
        Log.debug("wormIcon.getImageURL(): " + wormIcon.getImageURL());
        gmo.setIcon(wormIcon);
        HERE("after  gmo.setIcon(wormIcon);");

        Marker head =  new Marker(LatLng.newInstance(Y[i],X[i]),gmo);
        if (CurrentMS < TailMS)
        {
          if (CurrentMS == RelMS[i])
          {
            releaseCount = releaseCount + 1;
            Controls.setReleaseCounter(releaseCount);
          }
          else
          {
            shiftSegments(i);
          }
          wMarker[i][0] = head;
          addSegment(i);
        }
        else
        {
          if (CurrentMS == CapMS[i])
          {
            if (wMarker[i][0] != null)
            {
              removeSegment(i,0);
              if (wLine[i][0] != null)
                removeLine(i,0);
            }
            // miscounts tags at liberty of multiple recaps on same day
            //releaseCount = releaseCount - 1;
            //Controls.setReleaseCounter(releaseCount);
          }
          else
          {
            shiftSegments(i);
            wMarker[i][0] = head;
            addSegment(i);
            int k = wLen1;
            while ( (wMarker[i][k] == null) && (k > 0) )
            {
              k = k - 1;
            }
            if (wMarker[i][k] != null)
              removeSegment(i,k);

            if (k > 0)
            {
              if (wMarker[i][k-1] != null)
                removeSegment(i,k-1);
              if (wLine[i][k-1] != null)
                removeLine(i,k-1);
            }
            if (k > 1)
            {
              if (wLine[i][k-2] != null)
                removeLine(i,k-2);
            }
          }
        }
        UpdateXY(i);
      }   
    }
    CurrentMS = CurrentMS + dayMS;
    CurrentDate.setTime(CurrentMS);
    Controls.setDateBox(CurrentDate);
    Controls.setSegmentCounter(segmentCount);

    if (CurrentMS >= LastMS)
    {
      /*
      // checks unreleased tags
      for (int i = n1; i < n2; i++)
      {
        if (wMarker[i][0]==null)  
        {
           JSONObject tag = recaps.get(i).isObject().get("tag").isObject();
           GLog.write("Tag "+ getString(tag,"ID") + " not released.");
        }
      }
      */
      Stop();
      GWT.log("Called Stop() in public void OneDay()",null);
    }
  }

  public void UpdateXY(int i)
  {
    if (TT[i] == null)
    {
      X[i] = X[i]+dx[i];
      Y[i] = Y[i]+dy[i];
    }
    else
    {
      JSONValue value = recaps.get(i);
      JSONObject object = (JSONObject)value;
      JSONValue vTag = object.get("tag");
      JSONObject tag = (JSONObject)vTag;

      JSONArray posArray = tag.get("PosList").isArray();
      int npos = posArray.size();
      int p = dal[i]+1;
      if (p < npos)
      {
        JSONValue posValue = posArray.get(p);
        if (posValue instanceof JSONObject)
        {
          JSONObject posObject = (JSONObject)posValue;
          JSONValue pos = posObject.get("pos");
          X[i] = tagDisplay.getDouble(pos.isObject(),"LNG");
          Y[i] = tagDisplay.getDouble((JSONObject)pos,"LAT");
        }
        else
        {
          Log.fatal("unable to generate position JSONObject");
        }
      }
      dal[i] = p;
    }
  }

  public void Start()
  {
    Log.debug(" public void Start()");
    rate = Controls.getRate();
    Log.debug("rate = "+java.lang.String.valueOf(rate));
    AnimationTimer.scheduleRepeating(rate);
  }

  public void Stop()
  {
    AnimationTimer.cancel();
  }

  public void shiftSegments(int w)
  {
    if (wMarker[w][wLen1] != null)
      removeSegment(w,wLen1);

   if (wLine[w][wLen2] != null)
      removeLine(w, wLen2);

    for (int k = wLen2; k >= 0; k--)
    {
      if (wMarker[w][k]!=null)
      {
        wMarker[w][k+1] = wMarker[w][k];
        if (TT[w] != null)
        {
          if (k < wLen2)
          {
            if (wLine[w][k] != null)
              wLine[w][k+1] = wLine[w][k];
          }
        }
      } 
    }
    wMarker[w][0] = null;
    wLine[w][0] = null;
  }

  public void addSegment(int w)
  {
    HERE("w = "+java.lang.String.valueOf(w));
    HERE("marker icon: " + wMarker[w][0].getIcon().getImageURL());
    HERE("before animationMap.addOverlay(wMarker[w][0]);wMarker[w][0] = "+java.lang.String.valueOf(wMarker[w][0]));
    animationMap.addOverlay(wMarker[w][0]);
    HERE("after  animationMap.addOverlay(wMarker[w][0]);");
    segmentCount = segmentCount + 1;
    if (TT[w] != null)
    {
      if (wMarker[w][1] != null)
      {
         LatLng Verts[] = new LatLng[2];
         Verts[0] = wMarker[w][0].getPoint();
         Verts[1] = wMarker[w][1].getPoint();
         wLine[w][0] = new Polyline(Verts,(String)fishColor.get(SP[w]),2);
         animationMap.addOverlay(wLine[w][0]);

      }
    }
  }

  public void removeSegment(int t, int s)
  {
    animationMap.removeOverlay(wMarker[t][s]);
    wMarker[t][s] = null;
    segmentCount = segmentCount - 1;
  }

  public void removeLine(int t, int s)
  {
    if (TT[t] != null)
    {
      animationMap.removeOverlay(wLine[t][s]);
      wLine[t][s] = null;
    }
  }

  public void removeAll()
  {
    for (int i = n1; i < n2; i++)
    {
      for (int k = 0; k < wLen; k++)
      {
        if (wMarker[i][k] != null)
        {
          removeSegment(i,k);
          segmentCount = segmentCount - 1;
        }
      }
      Controls.setSegmentCounter(segmentCount);
    }
  }

  public void writeWormState(int i)
  {
    //GLog.write(getWormState(i));
    GWT.log(getWormState(i),null);
  }

  public String getWormState(int i)
  {
    String state = "Track "+java.lang.String.valueOf(i)+" : ";
    for (int j = (wLen1); j >= 0; j--)
    {
       if (wMarker[i][j] == null)
         state = state + "o";
       else
         state = state + "+";
    }
    return state;
  }
} // public class Animator

