<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.io.File"%>    
<%@page import="java.util.List"%>
<%@page import="org.clairvaux.utils.FileUtils"%>
<% 
	String root = application.getRealPath("/");
	File modelDir = new File(root, "/data/models");
	List<String> modelFileNames = FileUtils.getFileNames(modelDir.getAbsolutePath());	
%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Clairvaux</title>
<style>
@import url(https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css);

body {
	padding-top: 60px;
}
  
@media (max-width: 980px) {
	body {
    	padding-top: 0;
	}
}

.background {
	fill: #fefefe;
}

.container.details div div.col-md-6 {
	padding: 8px 48px;
}

.container ul {
	list-style: none;
}

.jumbotron h1, h1 {
	font-size: 36px;
}

.jumbotron {
	background-color: transparent;
}

.jumbotron .container {
	background-color: transparent;
}

#matrix-img {
	width: 100%;
	max-width: 600px;
	height: auto;
}

#footer {
	padding-top: 60px;
	padding-bottom: 30px;
}

.citation,
.citation:hover {
	color:#000000 !important;
	text-decoration: none;
	outline: none;
}

.citation:before {
	font-size: smaller;
	margin-left: .5em;
	vertical-align: super;
}
</style>
</head>
<body>

<div class="jumbotron">
  <div class="container">
    <div class="row">
      <div class="col-md-6 text-center">
      	<img id="matrix-img" alt="Prediction matrix" src="images/matrix.png"/>
      </div>
      <div class="col-md-6 text-center">
        <h1>
          Realtime events prediction
          <br/>
          <small class="text-muted">
          Clairvaux is a web application that predicts conflict events using Numenta's brain-inspired technology. 
          </small>
        </h1>
      </div>
    </div>
  </div>
</div>

<div class="container details">
	<div class="row">
  		<div class="col-md-6">
  			<h4>Usage</h4>
 				<ol>
 					<li>
 					Download realtime <a href="http://www.acleddata.com/data/">data</a> in CSV format from ACLED<sup><a href="#citation_1">1</a></sup>.
 					Uploading monthly data is recommended.
 					Uploading all-inclusive data which dates back to 1997 is not needed.
 					The system is designed to learn incrementally from smaller chunks of data.
 					</li>
 					<li>Upload CSV data file <a href="upload">here</a>.</li>
 					<li>A viewer link appears after successful upload.</li>
 					<li>Wait for a few minutes as data is being processed. Then click on the link.</li>
 				</ol>
     	</div>
     	<div class="col-md-6">
  			<h4>Learned Models</h4>
     			<ul>
				<%
					if (modelFileNames != null) {
						for (String fileName : modelFileNames) {
							String modelUrl = FileUtils.constructUrl(request, "model", fileName);
				%>	
				<li><a href="<%= modelUrl %>"><%= modelUrl %></a></li>		
				<%			
						}
					}
				%>
				</ul> 
      	</div>
  	</div>
  	
  	<div class="row">
  		<div class="col-md-6">
  			<h4>Data Description</h4>
  			<div>The prediction engine uses the fields listed below. Refer to ACLED site for details about them.</div>
  			<ol>
  			<li>Event Date - day of the event</li>
  			<li>Event Type - a politically violent event</li>
  			<li>Interaction - code to represent interactions between groups involved in the event</li>
  			<li>Location - most specific location for the event </li>
  			</ol>
  		</div>
  		<div class="col-md-6">
  			<h4>Technical Description</h4>
  			<div>
  			This applications uses Hierarchical Temporal Memory<sup><a href="#citation_2">2</a></sup> (HTM) technology to make predictions about conflict events.
  			Refer to the HTM white paper for technical details.
  			Learning and prediction in Clairvaux is triggered by data file uploads.
  			After a new data file is uploaded, the system feeds data into an HTM network which is trained for some number of iterations. 
  			Training results are stored as spatiotemporal patterns in memory and used to make predictions. 
  			When a previously encountered file is uploaded, the HTM generates new predictions but its memory is unchanged. 
  			Source code for this application is available in Github<sup><a href="#citation_3">3</a></sup> repository.
  			</div>
  		</div>
  	</div>	
</div>  

<div id="footer">
	<div class="container text-centered">
		<div class="row">
			<a class="citation" id="citation_1"><sup>1</sup> Raleigh, Clionadh, Andrew Linke, Håvard Hegre and Joakim Karlsen. 2010. 
	Introducing ACLED-Armed Conflict Location and Event Data. Journal of Peace Research 47(5) 651-660.</a>
		</div>
		<div class="row">
			<a class="citation" id="citation_2"><sup>2</sup> http://numenta.com/learn/hierarchical-temporal-memory-white-paper.html</a>
		</div>
		<div class="row">
			<a class="citation" id="citation_3"><sup>3</sup> https://github.com/datjandra/Clairvaux</a>
		</div>
	</div>
</div>    	
</body>
</html>