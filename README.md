# Bayesian Ranking for Finding Null Pointers in Java
Check your Java programs for null pointers faster.

## Why?
Static analysis tools often tackle undecidable problems through conservative approximation:
the analysis results (alarms) contain false positives that programmers must refute manually
using domain specific knowledge. [Raghothaman et al.](https://www.cis.upenn.edu/~sulekha/pubs/pldi18_bingo.pdf) present a better approach where
alarms (possible data races) are derived using logic rules in Datalog. Likelihoods are attached to alarms by modelling their derivation tree probabilistically using a Bayesian network. These likelihoods are updated in real time as the programmer provides feedback by disproving or verifying the top ranked alarm. This project aims to study the approach on a
null pointer analysis for Java.

## How it works?
<img src="https://github.com/padr31/pd451-project/blob/master/bayesian_ranking.png" alt="bayesian ranking" width="400"/>

## How to use?
Use one run of the program for one ranking session. Input source folder at the top. Make sure it is a copy of a source folder, because the compiled files will not be cleaned. Press Start Null Pointer Analysis, wait until analysis finishes (depends on program size). Findings and their probabilities are output in a list, and the most likely alarm is presented for inspection. After inspection, select whether the presented variable was a true positive null pointer, or a false positive, and hit Feedback. The feedback will be incorporated into the Bayesian network, and alarms re-ranked.

<img src="https://github.com/padr31/pd451-project/blob/master/bayesian_ranking_gui.png" alt="ranking gui" width="400"/>

The GUI was not the main focus of this work, it will be finished in July 2020. Use as is, at your own risk.

## How to build?
The project has several dependencies, Neo4j server, features-javac, and vanillalog.

### Clone the project
```
git clone git@github.com:padr31/pd451-project.git
cd pd451-project
```
Prefer installing other dependencies in this folder to keep a clean structure.

### Install Neo4j Server

1. Download the [Neo4j Community Server](https://neo4j.com/download-center/#community).
2. Run the Neo4j server: 
```
cd neo4j-server/bin
./neo4j console
```
3. Set the address, username, and password of the server in `pipeline/configuration.properties`
The server needs to run during ranking as it is used to extract features from the Java AST graph. 

### Install features-javac
[Features-javac](https://github.com/acr31/features-javac) is a Java compiler plugin used for obtaining the Java AST. 
1. Clone the repository into top folder: `git clone https://github.com/acr31/features-javac.git`
2. Build features-javac
```
cd features-javac
mvn clean compile package
```
3. Insert your path to the built JAR file into `pipeline/configuration.properties` file, e.g. `~/repos/pd451-project/features-javac/extractor/target/features-javac-extractor-1.0.0-SNAPSHOT-jar-with-dependencies.jar`, this is needed for ranking to operate

### Install vanillalog
[Vanillalog](https://github.com/madgen/vanillalog) is a frontend for evaluating Datalog programs and uses the backend [exalog](https://github.com/madgen/exalog).
1. Clone the repository into top folder: `git clone git@github.com:madgen/vanillalog.git`
2. Follow the vanillalog build instructions, you will need to install the [Haskell Tool Stack](https://docs.haskellstack.org/en/stable/README/)
3. Make sure you can run stack exec -- vanillalog

### Build Ranking Pipeline
1. Go into `cd pipeline`
2. Build the project `mvn clean compile package`
3. Run the project `java -jar target/feature-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar`
4. You should see the GUI displayed above
5. Exit the window to exit program

## License 
[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html)
