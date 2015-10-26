<% 
	String model = (String) request.getAttribute("model"); 
	String contextPath = request.getContextPath();
	String action = String.format("%s/upload", contextPath);
%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Upload</title>
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
	<div class="panel panel-default">
    	<div class="panel-heading"><strong>Data Upload</strong></div>
        <div class="panel-body">
        	<form action="<%= action %>" method="post" enctype="multipart/form-data">
        		<div class="form-inline">
        			<div class="form-group">
        				<input type="file" name="file"/>
        			</div>
        			<button type="submit" class="btn btn-sm btn-primary">Upload</button>
        		</div>
			</form>
			<br/>
			<div>Prediction results will be available at below link after a few minutes.</div>
			<br/>
			<div>
			<%
				if (model != null && !model.isEmpty()) {
			%>
				<a href="<%= model %>"><%= model %></a>
			<%
				}
			%>	
			</div>
        </div>
   	</div>
</div>  
</body>
</html>