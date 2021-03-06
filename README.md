
# JMIST Demo
Demonstration for [JMIST](http://jmist.eandb.ca/)

## Installation

Make sure Java and [Maven](https://maven.apache.org/install.html) are installed.
On Ubuntu you can run:
```
sudo apt-get install maven
```

Make sure maven works by running:
```
mvn -version
```

Clone this repo:
```
git clone https://github.com/MarkIwanchyshyn/jmist-demo.git
```

For editting or running offline [Intellij](https://www.jetbrains.com/help/idea/install-and-set-up-product.html) is recommended.

## Running

Move to the project directory:
```
cd jmist-demo
```

Build with:
```
mvn package
```

This will output the compiled jar file under target/

run the demo:
```
java -jar target/jmist-demo-1.0-SNAPSHOT-jar-with-dependencies.jar 1
```

Change the number at the end for the demo number, see Main.java for possible demos.

To hide the progress indicatiors (for example if running over ssh) run as:
```
java -jar target/jmist-demo-1.0-SNAPSHOT-jar-with-dependencies.jar 1 hide
```


## Output

This demo creates two folders in the running folder:
* Renders, which contains the image output.
* Working, which contains working files used by the renderer.



## Windows

If you have maven and java installed the above instructions worked for my windows without changes.
Otherwise you can install intellij and it should just work, because intellij has a maven version built-in (tested on 2014 and 2018).


## Offline

If you are unable or unwilling to use maven, the dependencies are provided as jars and can be used by Intellij.


To open the project in intellij, use Import not Open.  Then choose Import from External Model and choose Maven.  From there the defaults should be fine.


To do this go to: 
File -> Project Structure -> Libraries and press + for 'New Project Library'.  Select Java then select the libs folder in the checked out repository.


Now to run the program.  A runConfiguraton has been provided under .idea/ but I was having trouble getting intellij to recognize it so the instructions are here as reference.
Run -> Edit Configurations... .  Add a new Applicaton config, then: set the Main class as com.jmist.demo.Main, and set the program arguments to 1 (for the first demo).  The rest of the defaults should be fine.

Now when you press 'run' the program should compile and run as expected.


## Thanks

Thanks to Bradley Kimmel for help with making this demo.

Thanks to Elliot Kroo for the [Gif Writer](http://elliot.kroo.net/software/java/GifSequenceWriter/)
