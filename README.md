## Synopsis

This project aims to predict and visualize armed conflict data for African states. 
Realtime data can be obtained from [ACLED (Armed Conflict Location and Event Data Project)] (http://www.acleddata.com).
Prediction engine uses Numenta's [Hierarchical Temporal Memory (HTM)] (http://numenta.org/) model, which is based on a brain-inspired theory of neuroscience.


## Motivation

The primary motivation is to learn about Numenta's HTM framework. 
A secondary motivation is to explore techniques for categorical prediction and predictive visualization.


## Data Description

This application attempts to predict one of nine event types coded by ACLED, such as "Battle-No change of territory" or "Riots/Protests".
ACLED data files are sorted by GWNO (numeric code for each country), then by ascending date.
The current system will then learn sequence of events per country.
Other context fields may be used to such as learning events per actor, for example.
In that case, data file should be re-sorted by the context field (actor, city, etc.), then by ascending date.
These are the current inputs to the HTM engine:

1. GWNO - country code, not encoded and used to reset event sequences
2. Event Date - day of the event encoded as date
3. Event type - type of conflict event, encoded as category
4. Interaction - code to indicate interaction between actors involved in the event, encoded as category



## Technical Description

The prediction engine uses the HTM model. 
Refer to the white paper below for technical details. 
To summarize briefly, input data is converted to a "Sparse Distributed Representation" (SDR), a vector of bits (few 1's and lots of 0's) similar to neurons in the brain.
SDR sequences are passed to the HTM, stored in memory, and used for predictions about what inputs will likely arrive next.

For this application data is fed by uploading comma-separated (CSV) files.
When a new file is encountered, the HTM learns from the input data and simultaneously generates predictions.
When a previously learned file is uploaded again, the HTM makes predictions but learning is disabled.


## Experimental Results

When the very first file is uploaded, prediction accuracy is about 10-15 percent, as HTM has no prior information.
After 2-3 uploads, accuracy goes up to about 30 percent.
After >3 uploads accuracy may increase to about 40 percent.
It's not yet known whether prediction accuracy will significantly exceed 40 percent with additional uploads.
Most correct predictions converge to "Battle-No change of territory" and "Riots/Protests" events, as they are much more frequent than others.
Predictions for the other event types should improve with more observations of their occurrence.


## Build and Deployment

A development tool with Maven support like Eclipse or IntelliJ IDEA is recommended. 
Build a WAR archive using Maven, then deploy the WAR on a servlet container like Tomcat.
An optional CLAIRVAUX_TRAINING_CYCLES property may be set with the -D flag, such as "-DCLAIRVAUX_TRAINING_CYCLES=100".
This property sets the number of training cycles when the HTM is in learning mode.
If using Tomcat as servlet engine, the property may be set at environment level without modifying configuration files.
Examples for Windows and Unix/Linux systems: 

```
set CATALINA_OPTS="-DCLAIRVAUX_TRAINING_CYCLES=100"
export CATALINA_OPTS="-DCLAIRVAUX_TRAINING_CYCLES=100"
```

## References
```
Raleigh, Clionadh, Andrew Linke, Håvard Hegre and Joakim Karlsen. 2010. Introducing ACLED-Armed Conflict Location and Event Data. Journal of Peace Research 47(5) 651-660.

http://numenta.com/learn/hierarchical-temporal-memory-white-paper.html
```