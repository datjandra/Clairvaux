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
</style>
</head>
<body>
<div class="container">
	<div class="row">
  		<div class="col-md-offset-2 col-md-8">
    		<div class="panel panel-default">
      			<div class="panel-heading text-center">
      			Introduction
      			</div>
      			<div class="panel-body">
      				<ul>
      					<li>Upload data <a href="upload">here.</a></li>
      					<li>Wait for a few minutes as file is being processed.</li>
      					<li>Refresh this page and find the model link below.</li>
      				</ul>
      			</div>
     		</div>
  		</div>
  	</div>	
  	
  	<div class="row">
  		<div class="col-md-offset-2 col-md-8">
    		<div class="panel panel-default">
      			<div class="panel-heading text-center">
      			Models
      			</div>
      			<div class="panel-body">
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
  	</div>	
  	
</div>
</body>
</html>