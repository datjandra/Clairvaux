## Synopsis

This project aims to predict and visualize armed conflict data for African states. 
Realtime data can be obtained from [ACLED (Armed Conflict Location and Event Data Project)] (http://www.acleddata.com).
Prediction engine uses Numenta's [Hierarchical Temporal Memory (HTM)] (http://numenta.org/) model, which is based on a brain-inspired theory of neuroscience.

## Motivation

The primary motivation is to learn about Numenta's HTM framework. 
A secondary motivation is to explore techniques for categorical prediction and predictive visualization.

## Build and Deployment

A development tool with Maven support like Eclipse or IntelliJ IDEA is recommended. 
Build a WAR archive using Maven, then deploy the WAR on a servlet container like Tomcat.
An optional CLAIRVAUX_TRAINING_CYCLES property may be set with the -D flag, such as "-DCLAIRVAUX_TRAINING_CYCLES=100".
This property sets the number of training cycles when the HTM is in learning mode.

