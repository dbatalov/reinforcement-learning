# reinforcement-learning
Java implementation of Q-learning with various "worlds" and experiments.


## Intellij 

apt-get install openjfx openjfx-source

- Import the project as a maven project
- Check import automatically "Import maven projects automatically"
- Automatically download sources and documentation

Add jre/lib/ext/jfxrt.jar to SDK classpath
add javafx-src.zip to Sourcepath

If you are using Java9, apply the following patch:
(set target 1.9)

```
--- a/RocketLander/pom.xml
+++ b/RocketLander/pom.xml
@@ -8,8 +8,8 @@
     <name>RocketLander</name>
     <url>https://github.com/dbatalov/reinforcement-learning</url>
     <properties>
-        <maven.compiler.source>1.8</maven.compiler.source>
-        <maven.compiler.target>1.8</maven.compiler.target>
+        <maven.compiler.source>1.9</maven.compiler.source>
+        <maven.compiler.target>1.9</maven.compiler.target>
     </properties>
     <build>
         <resources>
```
