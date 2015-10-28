<?xml version="1.0" encoding="ISO-8859-1" ?>
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

#matrix-img {
	width: 100%;
	max-width: 600px;
	height: auto;
}
</style>
</head>
<body>

<div class="jumbotron">
  <div class="container">
    <div class="row">
      <div class="col-md-6 text-center">
      	<img id="matrix-img" alt="Prediction matrix" src="images/matrix.png">
      </div>
      <div class="col-md-6 text-center">
        <h1>
          Realtime events prediction
          <br/>
          <small class="text-muted">
          Uses Numenta's brain-inspired technology to find patterns in armed conflict data, make predictions, and detect anomalies. 
          </small>
        </h1>
      </div>
    </div>
  </div>
</div>

<div class="container">
	<div class="row">
  		<div class="col-md-6">
  			<h4>Usage</h4>
 				<ul>
 					<li>Obtain realtime <a href="http://www.acleddata.com/data/">data</a> from ACLED.</li>
 					<li>Upload CSV data file <a href="upload">here.</a></li>
 					<li>A model viewer link is created after successful upload.</li>
 					<li>Click model viewer link after a few minutes.</li>
 				</ul>
     	</div>
     	<div class="col-md-6">
  			<h4>Models</h4>
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
</div>      	
</body>
</html>