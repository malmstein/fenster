Fenster
=============================

A library to display videos in a `TextureView` using a custom `MediaPlayer` controller as described in this blog post http://www.malmstein.com/how-to-use-a-textureview-to-display-a-video-with-custom-media-player-controls/

![Demo gif](https://raw.githubusercontent.com/malmstein/Fenster/master/art/video_example.gif)

Install
=============================

[ ![Download](https://api.bintray.com/packages/malmstein/maven/fenster/images/download.svg) ](https://bintray.com/malmstein/maven/fenster/_latestVersion)

To get the current snapshot version:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.malmstein:fenster:0.0.2'
    }
}
```

Displaying a video with custom controller
=============================

### Add a TextureVideoView and a PlayerController to your Activity or Fragment

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:tools="http://schemas.android.com/tools"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:background="@color/default_bg"
  tools:context=".DemoActivity">

  <com.malmstein.fenster.view.FensterVideoView
    android:id="@+id/play_video_texture"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:keepScreenOn="true"
    android:fitsSystemWindows="true" />

  <com.malmstein.fenster.controller.MediaFensterPlayerController
    android:id="@+id/play_video_controller"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_alignParentBottom="true"
    android:animateLayoutChanges="true"
    android:fitsSystemWindows="true" />

</FrameLayout>
```

### Setting video URL

In order to display a video, simply set the video URL and call start. **You can also start the video from a desired second too**.


```java
@Override
protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    textureView = (TextureVideoView) findViewById(R.id.play_video_texture);
    playerController = (PlayerController) findViewById(R.id.play_video_controller);

    textureView.setMediaController(playerController);

    textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4",
                PlayerController.DEFAULT_VIDEO_START);
    textureView.start();
}
```

### Exposed listeners

By default there are the exposed listeners. The `NavigationListener` will listen to the to **Previous** and **Next** events triggered
by the controller. The `VisibilityListener` will be triggered when the `PlayerController` visibility changes.

```java
playerController.setNavigationListener(this);
playerController.setVisibilityListener(this);
```

Using the Gesture Detection Player Controller
=============================

### Attach a listener to your player controller

As described in this blog post http://www.malmstein.com/how-to-use-a-textureview-to-display-a-video-with-custom-media-player-controls/
it's very simple to use. Just add a listener to Player Controller

```java
playerController.setFensterEventsListener(this);
```

The Fenster Events Listener allows you to react to the gestures

```java
public interface FensterEventsListener {

    void onTap();

    void onHorizontalScroll(MotionEvent event, float delta);

    void onVerticalScroll(MotionEvent event, float delta);

    void onSwipeRight();

    void onSwipeLeft();

    void onSwipeBottom();

    void onSwipeTop();
}
```

### Use MediaPlayerController instead of SimpleMediaPlayerController

MediaFensterPlayerController also shows volume and brightness controls, if you just want to use a simple media controller
then the recommendation is to use SimpleMediaFensterPlayerController

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:tools="http://schemas.android.com/tools"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:background="@color/default_bg"
  tools:context=".DemoActivity">

  <com.malmstein.fenster.view.FensterVideoView
    android:id="@+id/play_video_texture"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:keepScreenOn="true"
    android:fitsSystemWindows="true" />

  <com.malmstein.fenster.controller.SimpleMediaFensterPlayerController
    android:id="@+id/play_video_controller"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_alignParentBottom="true"
    android:animateLayoutChanges="true"
    android:fitsSystemWindows="true" />

</FrameLayout>
```

### Using the Volume and Brightness Seekbar

Add them to your layout:

```xml
  <com.malmstein.fenster.seekbar.BrightnessSeekBar
    android:id="@+id/media_controller_volume"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

```xml
  <com.malmstein.fenster.seekbar.VolumeSeekBar
    android:id="@+id/media_controller_volume"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Initialise them from your Fragment or Activity:

```java
mVolume = (VolumeSeekBar) findViewById(R.id.media_controller_volume);
mVolume.initialise(this);

mBrightness = (BrightnessSeekBar) findViewById(R.id.media_controller_brightness);
mBrightness.initialise(this);

```

You'll get a callback when the seekbar is being dragged:

```java
@Override
public void onVolumeStartedDragging() {
    mDragging = true;
}

@Override
public void onVolumeFinishedDragging() {
    mDragging = false;
}

@Override
public void onBrigthnessStartedDragging() {
    mDragging = true;
}

@Override
public void onBrightnessFinishedDragging() {
    mDragging = false;
}
```

Support for different video origins
=============================

The `setVideo()` method allows you to load remote or local video files. You can also set the start time of the video 
(useful if you want to resume content), passing in a integer which corresponds to Milliseconds.

### Loading a remote stream


```java
@Override
protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    textureView = (TextureVideoView) findViewById(R.id.play_video_texture);
    playerController = (PlayerController) findViewById(R.id.play_video_controller);

    textureView.setMediaController(playerController);

    textureView.setVideo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
    textureView.start();
}
```

### Loading a local stream

`Fenster` uses the [AssetFileDescriptor](http://developer.android.com/intl/es/reference/android/content/res/AssetFileDescriptor.html) in 
order to load a local video stream.


```java
@Override
protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    textureView = (TextureVideoView) findViewById(R.id.play_video_texture);
    playerController = (PlayerController) findViewById(R.id.play_video_controller);

    textureView.setMediaController(playerController);
    
    AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(R.raw.big_buck_bunny);
    textureView.setVideo(assetFileDescriptor);
   
    textureView.start();
}
```


Support for video scaling modes
=============================

Sets video scaling mode. To make the target video scaling mode effective during playback, 
the default video scaling mode is VIDEO_SCALING_MODE_SCALE_TO_FIT. Uses [setVideoScalingMode](http://developer.android.com/intl/es/reference/android/media/MediaPlayer.html)

There are two different video scaling modes: `scaleToFit` and `crop`

In order to use it, `Fenster` allows you to pass in an argument from the xml layout:

```xml
<com.malmstein.fenster.view.FensterVideoView
  android:id="@+id/play_video_texture"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:scaleType="crop" />
```

  
License
-------

    (c) Copyright 2016 David Gonzalez

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
