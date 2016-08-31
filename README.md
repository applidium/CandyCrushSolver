# Candy Crush Solver

This is an Android project that solves the game Candy Crush.
This service scans the grid to find sweets, find the best move for the player and displays it on the screen, on top of Candy Crush. For more information, read here: https://applidium.com/en/news/candy_crush_solver/.

## Setup

This project uses the Android version of OpenCV libraries.
If you need to install OpenCV on OS X, you can follow [these instructions][openCV setup]
If our setup doesn't work on your computer, we advise you to follow this [tutorial][].

In order to run the tests, you might want to add `-Djava.library.path=src/test` to your VM options if you want to run your tests outside of gradle.
(For VM options in Android Studio, follow this path : Run -> Edit Configuration -> JUnit -> VM Options.)

Finally, if you want to use analytics or crash reporting, everything is ready in the code : just add an HockeyApp key to `/keystore/release.properties` (variable `keyAlias`). To get your own key, go to [HockeyApp website][] and create a new project.

[openCV setup]: https://gist.github.com/robb-broome/9222746
[tutorial]: https://www.youtube.com/watch?v=OTw_GIQNbD8
[HockeyApp website]: https://hockeyapp.net

## Project architecture

### Launch

`MainActivity` starts when the user launches the app, with its `SettingsFragment`. Several permissions are needed before beginning : overlay permission, accessibility access. Another button is here to start taking screenshots. When these three permissions are unlocked, our `HeadService starts`.

Besides, `TutoActivity`, and other tuto fragments compose a little tutorial. 
The `PermissionChecker` helps to know when overlay permission is on.

### Screenshot

Most of the code lays in the same named class. Once started in `SettingsFragment`, a new capture is saved each time the `ImageReader.OnImageAvailableListener` detects a change in the screen. The image is saved in the internal storage of the phone.

### Accessibility

The `HeadService` checks if the user is on Candy Crush or not. It won't start any action before that. We also pay attention that screenshots are actually taken (by checking its modification time). Moreover, we stop our service when the user quits Candy Crush.

```java
if (event.getPackageName().toString().equals("com.king.candycrushsaga") && Math.abs(lastModDate.getTime() - d.getTime()) < TIME_LIMIT) {
            launchBusinessService();
        } else {
            stopBusinessService();
        }
}
```

Then the app launches the `BusinessService` at regular intervals.

### Grid recognition

The `BusinessService calls the `FeaturesExtractor` and let the engine work. Several methods can be used here :

1. Call `private List<Sweet> extractSweetsForFeature(Mat img, int feature, int i, int orientation)` if you prefer a quick recognition. This function uses the pixel colors to find Candy Crush sweets. The integer `feature` represents its color code. But this methods won't be efficient for a black and white pattern. We chose this one in our solver, because the the algorithm performances are far better.
(Note : this recognition is not as precise as the next one, so don't forget to center your final display)

2. Call `public List<Sweet> extractSweetsForFeatureWithOpenCV(Mat img, Mat feature, int i)` if you prefer a really precise recognition. However, it is really slower (but depends of the size of your screenshot).

The engine also contains a `FeaturePainter` that can help you see the results of this grid recognition.

### Find a move

Then the class `MoveFinder` works to find every move on the screen, and gives at the end the best one. The numerous booleans present aim to find special moves (will 4 sweets be aligned ? Or 5 ? Or can I make a sweet bomb with this move ?). If the algorithm seems a bit confusing, find explanation on our [blogpost][].

[blogpost]: https://www.youtube.com/watch?v=OTw_GIQNbD8

### Solution display

Finally, the object HeadLayer represents the overlay on which we can draw. Here is the global setup : 

```java
private WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
           WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
```

And then you can add whatever you wan on the layout :

```java
private void addToWindowManager() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(frameLayout, params);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layoutInflater.inflate(R.layout.head, frameLayout);
        image = (ImageView) frameLayout.findViewById(R.id.solution);
    }
}
```

And don't forget to add this view in your `head.xml` file.
In our case, we chose to display the solution as a black transparent bitmap, with a white hole on the move we want to show.

## Make your own game solver !

This project aims to be as general as possible, in order to be reused for other logic game resolution. We encourage you to create your own solver, based on this project. For example, feel free to develop a sudoku solver ! 

## Known bugs

We are still working to fix the following bugs :

1. Orange sweets are mixed up with yellow ones on some devices, which leads to displaying wrong moves.

2. There is an offset on solution display on Samsung Galaxy S6.

Don't hesitate to contact us if you have any clues or if you find other errors.
