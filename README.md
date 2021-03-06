# Magneto

###What is it?
Project born as a proof of concept of my master's degree:
<a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/" target="_blank">Interacting with mobile devices using magnetic fields</a>.
The main goal is to use the compass sensor together with magnets as an input device. If you want to see the results, I created an open source library called Magneto to help you getting your future magnetic-driven application on the way. You can also find an application showing how to use the library and some possible activities that can be created.

###Documentation
You don't have to know much about magnetic fields and android sensor programming to use the library, but it surely helps. All the classes are commented (enough I hope) but to really understand what's going on you should take a look at Section 4.5 of the master's thesis (I promise it's not that long!).

###Usage
Not much to be said, just add the MagnetoFragment to your poject, implement the MagneticSensorEventListner provided and you're ready to go! There are a lot of *helper classes to avoid doing all the boring stuff. As long as you read the comments in the code you should be fine. Be sure to check where the compass sensor is located on your device (use the Display Readings activity with a magnet to see where the magnetic readings show the highest values, then use the RadialPositionExample activity to check if you are right).  Notice that the application that comes with the library was created on a Samsung Galaxy S3, so displays smaller than 4.8 may have problems with the UI. 

###Licensing
<a href="http://creativecommons.org/licenses/by-sa/4.0/" target="_blank">Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)</a>
