haello-Android-Application
==========================

haello is an Android application that uses the [haello RESTful web service](https://github.com/2lastview/haello-Rest-Service)
to extract text from images and translate said text into a specified language. Further it gives you
the possibility to enrich this translation with information from Wikipedia, Wiktionary and Google. This
repository contains the source code for haello.

####Description

* You can choose an image from either the gallery or by taking one with the camera.
![main activity camera]()
![main activity gallery]()

* You can choose a source language by tapping on FROM. If you select "Unknown" the web service will
detect the language automatically using google translate. By tapping on TO you can select the target
language. There is also a third option for editing the image.
![main activity language]()

* After tapping on GO the extracted text and translation are shown. By pressing on a single word for
a little longer you can search for it in Wikipedia, Wiktionary or on Google.
![result activity results]()
![result activity enrich]()

* By tapping CORRECT AND RETRY you can correct the mistakes made by the ocr engine manually.
![retry activity retry]()

* By swiping left, or tapping on the IMAGE tab, you can see the chosen image.
![retry activity image]()

####Build

Import the root folder into your IDE (tested on Android Studio). Find the class [translate](). There the
TRANSLATE_URL can be pointed to the URL where a implementation of the [haello RESTful web service](https://github.com/2lastview/haello-Rest-Service)
is deployed. Then simply build and run the project.

####Support
The application has been tested with SDK version 16 and above.

####Copyright and License

Author: Moritz Tomasi (moritz.tomasi at gmail dot com)
License: [Apache 2.0 License]()
