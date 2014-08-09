Fenster
=============================

A library to display videos in a `TextureView` using a custom `MediaPlayer` controller as described in this blog post http://www.malmstein.com/how-to-use-a-textureview-to-display-a-video-with-custom-media-player-controls/

![Demo gif](https://raw.githubusercontent.com/malmstein/Fenster/master/art/video_example.gif)

Install (Available soon)
=============================

To get the current snapshot version:

```groovy
buildscript {
    repositories {
        mavenCentral()
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots/"
        }
    }
    dependencies {
        classpath 'com.malmstein:fenster:0.1-SNAPSHOT'
    }
}
```

Displaying a video
=============================

### Add a TextureVideoView and a PlayerController to your Activity or Fragment

```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:tools="http://schemas.android.com/tools"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:background="@color/default_bg"
  tools:context=".DemoActivity">

  <com.malmstein.fenster.TextureVideoView
    android:id="@+id/play_video_texture"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:keepScreenOn="true"
    android:gravity="center" />

  <com.malmstein.fenster.PlayerController
    android:id="@+id/play_video_controller"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
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

License
-------

    (c) Copyright 2014 David Gonzalez

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.