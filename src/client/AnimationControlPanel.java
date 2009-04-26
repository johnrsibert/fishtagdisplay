/**
 * $Id: AnimationControlPanel.java 2566 2009-04-15 01:43:21Z jsibert $
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
import com.google.gwt.http.client.*;
import com.google.gwt.xml.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.lang.*;
import java.util.*;

public class AnimationControlPanel extends VerticalPanel
{

  public class AnimationControlButton extends CustomButton
  {
    AnimationControlButton(String imageURL, String title, ClickListener listener) 
    {
      super(new Image(imageURL));
      addClickListener(listener);
      setTitle(title);
     } 
  }
 
  public class AnimationParameterGrid extends Grid
  {
    AnimationParameterGrid(int nrow)
    {
      super(nrow,2);
      getColumnFormatter().setStyleName(0,"gwt-g20");
    }
    void SetRow(int row, java.lang.String title, Widget widget)
    {
      setWidget(row,0,new Label(title));
      setWidget(row,1,widget);
    }
  }

  AnimationControlButton play = null;
  AnimationControlButton stop = null;
  AnimationControlButton reset = null;
  TextBox DateBox = null;
  TextBox RateBox = null;
  TextBox WormBox = null;
  TextBox ReleaseCounter = null;
  TextBox SegmentCounter = null;
  Animator animation = null;
  Grid acg = null;

  ClickListener ClickStop = new ClickListener()
  {
    public void onClick(Widget sender){
      acg.setWidget(0,1,play);
      animation.Stop();
    }
  };
  ClickListener ClickReset = new ClickListener()
  {
    public void onClick(Widget sender){
      acg.setWidget(0,1,play);
      animation.Stop();
      animation.Setup();
    }
  };
  ClickListener ClickPlay = new ClickListener()
  {
    public void onClick(Widget sender){
      acg.setWidget(0,1,stop);
      animation.Start();
    }
  };
  /*
  Date getDate(String text)
  {
    return(dtfIn.parse(text));
  }
  */
  void setDateBox(Date d)
  {
    DateBox.setText(tagDisplay.dtfOut.format(d));
  }

  Date setDateBox(String text)
  {
    Date tDate = tagDisplay.getDate(text);
    DateBox.setText(tagDisplay.dtfOut.format(tDate));
    return(tDate);
  }
  /*
  void setTagIDBox(String ID)
  {
     TagIDBox.setText(ID);
  }
  */
  void setReleaseCounter(int count)
  {
    ReleaseCounter.setText(java.lang.String.valueOf(count));
  }

  void setSegmentCounter(int count)
  {
    SegmentCounter.setText(java.lang.String.valueOf(count));
  }

  int getTextBoxInt(TextBox box)
  {
    String sLength = box.getText();
    java.lang.Integer iLength = java.lang.Integer.valueOf(sLength);
    return(iLength.intValue());
  }

  int getWormLength()
  {
    return(getTextBoxInt(WormBox));
  }

  int getRate()
  {
    return(getTextBoxInt(RateBox));
  } 

  public void setAnimation(Animator anAnimator)
  {
    animation = anAnimator;
  }

  AnimationControlPanel(MapWidget aMap, JSONArray recaps, JSONArray features, String logoURL)
  {
    setStylePrimaryName("gwt-ControlPanel");

    acg = new Grid(1, 2);
    play = new AnimationControlButton(GWT.getModuleBaseURL()+"images/player_play.png","Start Animation",ClickPlay);
    stop = new AnimationControlButton(GWT.getModuleBaseURL()+"images/red_stop.png","Stop Animation",ClickStop);
    reset = new AnimationControlButton(GWT.getModuleBaseURL()+"images/left_end.png","Reset",ClickReset);


    DateBox = new TextBox();
    RateBox = new TextBox();
    RateBox.addChangeListener(new ChangeListener(){
      public void onChange(Widget sender) 
      {
        animation.Setup(); 
      }
    });
         
    WormBox = new TextBox();
    WormBox.addChangeListener(new ChangeListener(){
      public void onChange(Widget sender) 
      {
        Window.alert("Dynamic worm resizing not fully supported at this time. " +
                     "Lots a luck.");
        animation.Setup(); 
      }
    });

    //TagIDBox = new TextBox();
    ReleaseCounter = new TextBox();
    SegmentCounter = new TextBox();
      
    setHorizontalAlignment(ALIGN_CENTER);
    setVerticalAlignment(ALIGN_TOP);

    // display is too tall with logo
    if (logoURL != null)
      add(new Image(logoURL));

    add(new HTML("<h3 align=center>Animation Controls</h3>"));

    if (features !=  null)
      add (new FeatureButton(aMap, features));

    acg.setBorderWidth(1);
    acg.setWidget(0,0,reset);
    acg.setWidget(0,1,play);
    add(acg);

    AnimationParameterGrid g2 = new AnimationParameterGrid(5);
    g2.setBorderWidth(1);

    JSONValue value = recaps.get(1);
    JSONObject object = (JSONObject)value;
    JSONValue valueTag = object.get("tag");
    JSONObject tag = (JSONObject)valueTag;
    JSONObject rel = tag.get("rel").isObject();



    long startMS = 1000*(tagDisplay.getMSDate(rel,"DD")/1000);
    Date startDate = new Date();
    startDate.setTime(startMS);
 
    DateBox.setMaxLength(12);
    DateBox.setVisibleLength(12);
    setDateBox(startDate);
    g2.SetRow(0,"Date",DateBox);

    RateBox.setMaxLength(5);
    RateBox.setVisibleLength(5);
    RateBox.setText("5");
    g2.SetRow(1,"Rate (ms/day)",RateBox);

    WormBox.setMaxLength(2);
    WormBox.setVisibleLength(2);
    WormBox.setText("5");
    g2.SetRow(2,"Worm Length",WormBox);

    //TagIDBox.setMaxLength(6);
    //TagIDBox.setVisibleLength(6);
    //g2.SetRow(3,"Added Tag",TagIDBox);

    ReleaseCounter.setMaxLength(6);
    ReleaseCounter.setVisibleLength(6);
    ReleaseCounter.setText("0");
    g2.SetRow(3,"Tags Released",ReleaseCounter);

    SegmentCounter.setMaxLength(6);
    SegmentCounter.setVisibleLength(6);
    SegmentCounter.setText("0");
    g2.SetRow(4,"Segments",SegmentCounter);
 
    add(g2);

    add(new HTML("<h4 align=center>Legend</h4>"));
    Grid lg = new Grid(3,2);
    lg.setStylePrimaryName("tagDisplay-animation-legend");
    lg.setBorderWidth(1);
    lg.setCellPadding(3);
    lg.setWidget(0,0,new Label("Skipjack"));
    lg.setWidget(0,1,new Image(GWT.getModuleBaseURL()+"images/brightgreen-ball-5x5.png"));
    lg.setWidget(1,0,new Label("Yellowfin"));
    lg.setWidget(1,1,new Image(GWT.getModuleBaseURL()+"images/yellow-ball-5x5.png"));
    lg.setWidget(2,0,new Label("Bigeye"));
    lg.setWidget(2,1,new Image(GWT.getModuleBaseURL()+"images/red-ball-5x5.png"));
    add(lg);
  }

}
