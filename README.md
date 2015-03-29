#Fish Tag Display-

##Summary

This display tool uses Google Maps to display fish tagging data. Position data for both electronic and conventional tags are displayed on the same map. [Hawaii tuna example](http://www.admb-foundation.org/FishTagDisplay/tagDisplay.html). It is designed to be easily adapted to different data.
Key Features

The animation is a representation of the relative movement rates of the tagged fish. Conventional tags are assumed to move at a constant speed in a straight line from point of release to point of recapture. Tracks for electronic tags are statistical reconstuctions based on from data recovered from the tags and are shown with lines connecting the segments. Click the play button to start the animation.

Recapture positions and geographic features are represented as colored-coded icons. Clicking the icons reveals information about the fish, the tag release position and details about the recapture. Electronic tags are marked with icons containing a black circle in the center. Clicking a recapture icon also draws a track from the recapture point to the release point. 

##Usage

###Recapture Tab

The Recapture display consists of two panels. The left panel is a Google map showing the recapture positions of all reported tags. The map has the usual Google Map controls for panning, zooming and selection of map type. Recapture positions are shown as small tear-drop shaped icons color-coded to the species of fish tagged. Recapture icons additionally marked with a black dot in the center indicate that the recaptured fish was tagged with some sort of electronic tag. The slightly larger aqua colored icons indicate geographic or other features that might be of interest to tunas or tuna fishermen.

The right panel has a single control button and a Legend panel toward the bottom indicating the color coding of the recapture icons. The "Hide Features" button toggles to hide or show the features.

As you move the cursor over the icons on the map, the cursor changes shape to indicate an active icon. Clicking an icon reveals information about the recaptured fish and draws a "track" from the point of release to the point of recapture. In the case of conventional tags, the track is simply a straight line (on a Mercator projection) connecting the point of release to the point of recapture. In the case of electronic tags, the track is estimated from the data recorded in the tag, usually by a method based on solar irradiance possibly with additional processing to reconstruct the most probable track. The recapture information includes species, geocoded release and recapture positions, great circle distance traveled in km, days at liberty. The sizes of the fish in cm at release and recapture and the nationality and gear type of the recapture vessel are also given if reported,

###Animation Tab

The Animation display consists of two panels. The left panel consists of a Google Map with the usual controls for panning, zooming and selection of map type and aqua-colored icons indicating geographic or other features that might be of interest to tunas or tuna fishermen.

The right panel contains control buttons and informative boxes about the animation and a legend. The "Hide Features" button simply toggles to hide or show the features.

The two round buttons control the action. The Start button is the blue button with the inner white triangle pointing to the right. Pressing this button starts that tag releases, and the button changes to a red Stop button with an inner white square. Pressing the Reset button with the white triangle pointing to the left stops the action, clears all of the tags from the map and resets the date to the date of the first tag release.

Each tagged fish is animated as a chain of colored balls, or "segments" of a "worm". The default worm length is 5 segments. As time passes, the worms move relentlessly from the point of release to the point of recapture. Conventional tags are assumed to move in a strait line at a constant speed from point of release to point of recapture. Tracks for electronic tags are statistical reconstructions based on from data recovered from the tags using state-space models developed.  The track segments from electronic tags are indicated by a line connecting the segments.

##Programming Notes
###Java Programming.
Fish Tag Display is written in Java using the Google Web Toolkit version GWT 1.5.3 for linux, Google Maps 1.0 Library, and the GWT Logging Library.

###Using Different Data.
The display can be easily modified by changing some input fields in the main html file, tagDisplay.html.

The following bit of javascrpt code is executed when the display is loaded by the browser and sets the value of several variables.

```javascript
<script type="text/javascript">
       var code = '{\
                     "centerLng" : -157.0,\
                     "centerLat" :   21.5,\
                      "initZoom" :      5,\
                        "datURL" : "HTTP-subset.JSON",\
                   "featuresURL" : "HTTP-features.JSON",\
                   "aboutDatURL" : "aboutHTTP-data.html",\
                       "logoURL" : "http://imina.soest.hawaii.edu/PFRP/images/pfrp_tuna_bigger1.gif"\
                   }';
</script>
```

These variables are interpreted by the display as follows:

centerLng: 	Longitude of the map center in decimal degrees (east longitude positive, west negative) .
 Required.
centerLat: 	Latitude of the map center in decimal degrees (north latitude positive, south negative).
	Required.
initZoom: 	Initial zoom of the map.
	Required.
datURL: 	Name of the file containing the tag release and recapture information. See below for format of tagging data.
	Required.
featuresURL: 	Name of the file containing the names and positions of features. See below for format of features data.
	Optional.
aboutDatURL: 	Valid URL of a valid HTML file describing the data used in the display. 	Optional.
logoURL: 	URL of an image to use a logo on the control panels. 	Optional.


Data File Formats
The tagging and features data are served in JavaScript Object Notation (JSON) format. The most difficult task in adapting this display to your data is getting your data into valid JSON data structures.

Tag Release and Recapture

```javascript
{
"TagList" : [
  {"tag" : {
     "ID" : "X16029",
     "SP" : "Y",
     "rel" : {"DD" : "1/8/1999", "LNG" : -157.18, "LAT" : 20.83, "LEN" : 34},
     "cap" : {"DD" : "4/3/1999", "LNG" : -157.18, "LAT" : 20.83, "LEN" : 44.4, "FL" : "HW", "GG" : "HL"}
  }},
  {"tag" : {
     "ID" : "X16026",
     "SP" : "Y",
     "rel" : {"DD" : "1/8/1999", "LNG" : -157.18, "LAT" : 20.83, "LEN" : 36},
     "cap" : {"DD" : "4/5/1999", "LNG" : -157.18, "LAT" : 20.83, "LEN" : 43.1, "FL" : "HW", "GG" : "PL"}
  }},
  .
  .
  .
  {"tag" : {
     "ID" : "Big241",
     "SP" : "B",
     "TT" : "AT",
     "rel" : {"DD" : "1/21/1999", "LNG" : -158.25, "LAT" : 18.65},
     "cap" : {"DD" : "4/7/1999", "LNG" : -151.232, "LAT" : 13.3192, "FL" : "HW", "GG" : "HL"},
     "PosList"  : [
       {"pos" : {"DD" : "1/21/1999", "LNG" : -158.25, "LAT" : 18.65}},
       {"pos" : {"DD" : "1/22/1999", "LNG" : -158.318, "LAT" : 18.5265}},
       {"pos" : {"DD" : "1/23/1999", "LNG" : -157.418, "LAT" : 18.5595}},
       .
       .  
       .
       {"pos" : {"DD" : "4/4/1999", "LNG" : -152.762, "LAT" : 13.0111}},
       {"pos" : {"DD" : "4/5/1999", "LNG" : -152.105, "LAT" : 12.9631}},
       {"pos" : {"DD" : "4/6/1999", "LNG" : -151.665, "LAT" : 13.1432}},
       {"pos" : {"DD" : "4/7/1999", "LNG" : -151.232, "LAT" : 13.3192}} ]
  }},
  .
  .
  .

  {"tag" : {
     "ID" : "X15567",
     "SP" : "B",
     "rel" : {"DD" : "12/26/1999", "LNG" : -155.43, "LAT" : 20.27, "LEN" : 67},
     "cap" : {"DD" : "1/12/2002", "LNG" : -145.08, "LAT" : 21.9, "LEN" : 140, "FL" : "JP", "GG" : "LL"}
  }},
]}
```

Features

```javascript
{"FeatureList":[
{"LAT" : 18.65,"LNG" :-158.25,"NAME" : "Cross Seamount"},
{"LAT" : 23.43,"LNG" :-162.21,"NAME" : "NOAA Data Buoy 51001"},
{"LAT" : 17.19,"LNG" :-157.78,"NAME" : "NOAA Data Buoy 51002"},
{"LAT" : 19.22,"LNG" :-160.82,"NAME" : "NOAA Data Buoy 51003"},
{"LAT" : 17.52,"LNG" :-152.48,"NAME" : "NOAA Data Buoy 51004"},
{"LAT" : 28.20,"LNG" :-177.37,"NAME" : "Midway Islands"},
{"LAT" : 30.27,"LNG" :178.72,"NAME" : "Hancock Seamount"},
{"LAT" : 29.48,"LNG" :153.49,"NAME" : "Shatsky Rise"},
{"LAT" : 35.25,"LNG" :171.58,"NAME" : "Koko Guyot (Emperor Seamounts)"},
{"LAT" : 25.33,"LNG" :-172.06,"NAME" : "Northhampton Seamounts?"},
{"LAT" : 11.48,"LNG" :-168.52,"NAME" : "Northampton Seamounts?"}
]}
```

##Credits
Java programming by John Sibert ("a real learning experience") with a lot of help from his colleague Johnoel Ancheta.

The animation control button images were adapted from Crystal Project Icons

The tag recapture and feature icons were adapted from Sam Kuhn's public domain "web hues". As far as I can tell the original URL for these icons is no longer on line, but similar icons can be found at http://sparce.cs.pdx.edu/mash-o-matic/tools.html.

Copyright (c) 2008 - 2015 John Sibert
