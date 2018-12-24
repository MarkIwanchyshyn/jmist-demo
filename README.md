WARNING still pointing to 0.1.6-SNAPSHOT for SpheroidGeometry.

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

## Thanks

Thanks to Bradley Kimmel for help with making this demo.

Thanks to Elliot Kroo for the [Gif Writer](http://elliot.kroo.net/software/java/GifSequenceWriter/)
