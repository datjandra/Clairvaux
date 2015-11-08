<%@page import="org.clairvaux.utils.FileUtils"%>
<% 
	String model = (String) request.getAttribute("model"); 
	String title = "";
	if (model != null) {
		title = FileUtils.extractBaseName(model);
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html class="ocks-org do-not-copy">
<head>
<meta charset="utf-8"></meta>
<meta name="viewport" content="width=device-width, initial-scale=1"></meta>
<title>Predictive Visualization of Armed Conflict Events</title>
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

@import url(http://bost.ocks.org/mike/style.css?aea6f0a);

line {
	stroke: #fff;
}

text.active {
	fill: red;
}

div.tooltip {	
	font-size: 1em;
	font-weight: bold;
  	font-style: normal;
	position: absolute;			
    text-align: center;			 
    padding: 15px;
  	margin: 1em 0 3em;
    color: #fff;
    pointer-events: none;
  	background: #075698;;
  	background:-webkit-gradient(linear, 0 0, 0 100%, from(#2e88c4), to(#075698));
  	background:-moz-linear-gradient(#2e88c4, #075698);
  	background:-o-linear-gradient(#2e88c4, #075698);
  	background:linear-gradient(#2e88c4, #075698);
  	-webkit-border-radius:10px;
  	-moz-border-radius:10px;
  	border-radius:10px;			
}

.stat {
	font-size: 40px;
}

.panel-blue {
    border-color: #337AB7;
}

.panel-blue > .panel-heading {
    border-color: #337AB7;
    color: #fff;
    background-color: #337AB7;
}

.panel-green {
    border-color: #5cb85c;
}

.panel-green > .panel-heading {
    border-color: #5cb85c;
    color: #fff;
    background-color: #5cb85c;
}

.panel-red {
    border-color: #d9534f;
}

.panel-red > .panel-heading {
    border-color: #d9534f;
    color: #fff;
    background-color: #d9534f;
}

.panel-orange {
    border-color: #FFAB24;
}

.panel-orange > .panel-heading {
    border-color: #FFAB24;
    color: #fff;
    background-color: #FFAB24;
}

.right-align {
	text-align: right;
}

.center-align {
	text-align: center;
}
</style>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="col-xs-12 col-sm-12 col-md-12">
				<h1 class="center-align"><%= title %></h1>
			</div>
		</div>
		<div class="row">
			<h2>Dashboard</h2>
			<div class="col-xs-3 col-sm-3 col-md-3">
				<div class="panel panel-blue">
                	<div class="panel-heading">
                		<div class="row">
                        	<div class="col-xs-12 right-align">
                        		<div id="correct_stat" class="stat"></div>
                       			<div>Correct</div>
                           	</div>
                        </div>        
                   	</div>
                   	<div class="panel-footer">
                        <span class="pull-left">Correctly Predicted Rows</span>
                        <div class="clearfix"></div>
                    </div>
                </div>        
			</div>
			<div class="col-xs-3 col-sm-3 col-md-3">
				<div class="panel panel-green">
                	<div class="panel-heading">
                		<div class="row">
                        	<div class="col-xs-12 right-align">
                				<div id="predicted_stat" class="stat"></div>
                       			<div>Predicted</div>
                       		</div>
                       	</div>
                   	</div>
                   	<div class="panel-footer">
                        <span class="pull-left">Predicted Rows</span>
                        <div class="clearfix"></div>
                    </div>
                </div>
			</div>
			<div class="col-xs-3 col-sm-3 col-md-3">
				<div class="panel panel-orange">
                	<div class="panel-heading">
                		<div class="row">
                        	<div class="col-xs-12 right-align">
                				<div id="total_stat" class="stat"></div>
                       			<div>Total</div>
                       		</div>
                       	</div>
                   	</div>
                   	<div class="panel-footer">
                        <span class="pull-left">Row Count * Training Cycles</span>
                        <div class="clearfix"></div>
                    </div>
                </div>
			</div>
			<div class="col-xs-3 col-sm-3 col-md-3">
				<div class="panel panel-red">
                	<div class="panel-heading">
                		<div class="row">
                        	<div class="col-xs-12 right-align">
                				<div id="accuracy_stat" class="stat"></div>
                       			<div>Accuracy</div>
                       		</div>
                       	</div>
                   	</div>
                   	<div class="panel-footer">
                        <span class="pull-left">Correct / Predicted</span>
                        <div class="clearfix"></div>
                    </div>
                </div>
			</div>
		</div>
		<div class="row">
			<h2>Co-occurence Matrix</h2>
			<p>
			Shows the number of times that actual and predicted events occur together.
			Rows are actual events, and columns are predicted events for the next step.
			Hover over each cell to see the predicted counts and final probabilities.
			Probability distribution changes as the network observes more data.
			</p>
			<div class="col-xs-12 col-sm-12 col-md-12">
				<div id="aggregate" class="center-align"></div>
			</div>
		</div>
		<div class="row">
			<h2>Confusion Matrix</h2>
			<p>
			Shows the accuracy of the prediction engine.
			Rows are predicted events, and columns are actual events for the next step.
			Correctly predicted events are counted in diagonal entries indicated by green text.
			</p>
			<div class="col-xs-12 col-sm-12 col-md-12">
				<div id="quality" class="center-align"></div>
			</div>
		</div>
	</div>
	<div id="tooltip-container"></div>
</body>
<script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>

<script>
var tooltip = d3.select("#tooltip-container").append("div")	
	.attr("class", "tooltip")				
	.style("opacity", 1);

function refreshAll(url, aggregateId, qualityId) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', encodeURI(url));
  xhr.onload = function() {
    if (xhr.status === 200) {
    	var model = JSON.parse(xhr.responseText);
    	refreshDashboard(model);
    	refreshAggregateMatrix(model, aggregateId);
    	refreshConfusionMatrix(model, qualityId);
    } else {
        console.log('Request failed.  Returned status of ' + xhr.status);
    }
  };
  xhr.send();
}
	
function round(value, decimals) {
	return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}	
	
function refreshDashboard(model) {
	$("#correct_stat").text(model.quality.correct);	
	$("#predicted_stat").text(model.quality.predicted);	
	$("#total_stat").text(model.quality.total);	
	$("#accuracy_stat").text(round(model.quality.accuracy,5));	
}	
	
function refreshAggregateMatrix(model, element) {
	var graph = model.aggregate;
	var margin = {
	        top: 280,
	        right: 0,
	        bottom: 80,
	        left: 280
	    },
	    width = 720,
	    height = 720;

	var x = d3.scale.ordinal().rangeBands([0, width]),
	    z = d3.scale.linear()
	    .domain([0, 4]).clamp(true),
	    c = d3.scale.category10().domain(
	        d3.range(10));
	
	var svg = d3.select(element).append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom).style("margin-left", -margin.left + "px").append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	var matrix = [],
    	nodes = graph.nodes,
    	n = nodes.length;
	
	// Compute index per node.
    nodes.forEach(function(node, i) {
        node.index = i;
        node.count = 0;
        matrix[i] = d3.range(n).map(function(j) {
            return {
                x: j,
                y: i,
                z: 0
            };
        });
    });
	
 	// Convert links to matrix;
    var zMax = 1;
    graph.links.forEach(function(link) {
    	matrix[link.source][link.target].z = link.value;
    	matrix[link.source][link.target].probability = link.probability;
    	nodes[link.source].count += link.value;
        nodes[link.target].count += link.value;
        zMax = Math.max(zMax, link.value);
    });
    
 	// Precompute the orders.
    var orders = {
        name: d3.range(n).sort(
            function(a, b) {
                return d3.ascending(nodes[a].name,
                    nodes[b].name);
            }),
        count: d3.range(n).sort(function(a, b) {
            return nodes[b].count - nodes[a].count;
        })
    };

    // The default sort order.
    x.domain(orders.name);
    
    svg.append("rect")
		.attr("class", "background")
		.attr("width", width)
		.attr("height", height);

	var row = svg.selectAll(".row").data(matrix).enter()
    	.append("g")
    	.attr("class", "row")
    	.attr("transform",
        	function(d, i) {
            	return "translate(0," + x(i) + ")";
        	}).each(setupAggregateRow);

	row.append("line")
		.attr("x2", width);

	row.append("text")
		.attr("font-size", "10px")
		.attr("x", -6)
		.attr("y", x.rangeBand() / 2)
		.attr("dy", ".32em")
		.attr("text-anchor", "end").text(function(d, i) {
    		return nodes[i].name;
		});
	
	var column = svg.selectAll(".column").data(matrix)
    	.enter().append("g")
    	.attr("class", "column")
    	.attr("transform",
        	function(d, i) {
            	return "translate(" + x(i) + ")rotate(-90)";
        	});

	column.append("line")
		.attr("x1", -width);

	column.append("text")
		.attr("font-size", "10px")
		.attr("x", 6)
		.attr("y", x.rangeBand() / 2)
		.attr("dy", ".32em")
		.attr("text-anchor", "start").text(function(d, i) {
    		return nodes[i].name;
		});
	
	function setupAggregateRow(row) {
        var cell = d3
            .select(this)
            .selectAll(".cell")
            .data(row.filter(function(d) {
                return d.z;
            }))
            .enter()
            .append("rect")
            .attr("class", "cell")
            .attr("x", function(d) {
                return x(d.x);
            })
            .attr("width", x.rangeBand())
            .attr("height", x.rangeBand())
            .style("fill-opacity", function(d) {
            	return d.z / zMax;
            })
            .style(
                "fill",
                function(d) {
                    return nodes[d.x].group == nodes[d.y].group ? c(nodes[d.x].group) : null;
                }).on("mouseover", mouseover).on(
                "mouseout", mouseout);
    }
	
	function mouseover(p) {
        d3.selectAll(".row text").classed("active",
            function(d, i) {
                return i == p.y;
            });
        d3.selectAll(".column text").classed("active",
            function(d, i) {
                return i == p.x;
            });
        tooltip.transition()		
        	.duration(200)		
        	.style("opacity", .9);
        tooltip.html("Count: " + p.z + "<br/>" + "Final Probability: " + p.probability)	
        	.style("left", (d3.event.pageX) + "px")		
        	.style("top", (d3.event.pageY - 28) + "px");
    }

    function mouseout() {
        d3.selectAll("text").classed("active", false);
        tooltip.transition()		
        	.duration(500)		
        	.style("opacity", 0);
    }
}

function refreshConfusionMatrix(model, element) {
	var graph = model.quality;
	var margin = {
	        top: 280,
	        right: 0,
	        bottom: 80,
	        left: 280
	    },
	    width = 720,
	    height = 720;

	var x = d3.scale.ordinal().rangeBands([0, width]),
	    z = d3.scale.linear()
	    .domain([0, 4]).clamp(true),
	    c = d3.scale.category10().domain(
	        d3.range(10));
	
	var svg = d3.select(element).append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom).style("margin-left", -margin.left + "px").append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");		
	
	var matrix = [],
    	nodes = graph.nodes,
    	n = nodes.length;
	
	// Compute index per node.
    nodes.forEach(function(node, i) {
        node.index = i;
        node.count = 0;
        matrix[i] = d3.range(n).map(function(j) {
            return {
                x: j,
                y: i,
                z: 0
            };
        });
    });
	
 	// Convert links to matrix;    
    graph.links.forEach(function(link) {
    	matrix[link.source][link.target].z = link.value;    	
    	nodes[link.source].count += link.value;
        nodes[link.target].count += link.value;        
    });     	
    
 	// Precompute the orders.
    var orders = {
        name: d3.range(n).sort(
            function(a, b) {
                return d3.ascending(nodes[a].name,
                    nodes[b].name);
            }),
        count: d3.range(n).sort(function(a, b) {
            return nodes[b].count - nodes[a].count;
        })
    };
    
 	// The default sort order.
    x.domain(orders.name)
    
    svg.append("rect")
		.attr("class", "background")
		.attr("width", width)
		.attr("height", height);

	var row = svg.selectAll(".row").data(matrix).enter()
    	.append("g")
    	.attr("class", "row")
    	.attr("transform",
        	function(d, i) {
            	return "translate(0," + x(i) + ")";
        	}).each(setupQualityRow);

	row.append("text")
		.attr("font-size", "10px")
		.attr("x", -6)
		.attr("y", x.rangeBand() / 2)
		.attr("dy", ".32em")
		.attr("text-anchor", "end").text(function(d, i) {
    		return nodes[i].name;
		});
	
	var column = svg.selectAll(".column").data(matrix)
    	.enter().append("g")
    	.attr("class", "column")
    	.attr("transform",
        	function(d, i) {
            	return "translate(" + x(i) + ")rotate(-90)";
        	});

	column.append("text")
		.attr("font-size", "10px")
		.attr("x", 6)
		.attr("y", x.rangeBand() / 2)
		.attr("dy", ".32em")
		.attr("text-anchor", "start").text(function(d, i) {
    		return nodes[i].name;
		});
	
	function setupQualityRow(row) {
    	var cell = d3
	        .select(this)
	        .selectAll(".cell")
	        .data(row)
	        .enter()
	        .append("text")
	        .attr("font-size", "12px")
	        .attr("transform",
	    		function(d, i) {
	        		return "translate(" + x(i) + ")";
	    	})
	    	.style("fill", function(d) {
	        	return (d.x == d.y ? "#5cb85c" : "#d9534f");
	        })
	        .style("font-weight", function(d) {
	        	return (d.x == d.y ? "bold" : "");
	        })
	        .attr("x", x.rangeBand() / 2) 
	    	.attr("y", x.rangeBand() / 2)
	    	.attr("dy", ".32em")
	    	.attr("text-anchor", "middle").text(function(d, i) {
	    		return d.z;
			});
    		
    		d3
	        .select(this)
	        .selectAll(".cell")
	        .data(row)
	        .enter()
	        .append("rect")
	        .attr("class", "cell")
	        .attr("x", function(d) {
	            return x(d.x);
	        })
	        .attr("width", x.rangeBand())
	        .attr("height", x.rangeBand())
	        .style("fill-opacity", 0.1)
	        .style("fill", "#efefef");
    }
}
</script>

<script>
refreshAll('<%= model %>', '#aggregate', '#quality');
</script>
</html>
