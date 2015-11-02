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
<title>Co-occurrence Matrix</title>
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

#matrix {
	text-align: center;
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
</style>
</head>
<body>
	<div class="container">
		<div class="row">
			<h1><%= title %></h1>
			<p>
			Sequence prediction results are visualized as a co-occurrence matrix.
			Rows are the actual events, while columns are the next step's predicted events.
			Each cell represents an actual event followed by the predicted event.
			Darker cells indicate actual and predicted events that occur together more frequently.
			For example, if sequence "Riots/Protests" followed by "Violence against civilians" is predicted to occur many times, then the cell has a darker color.
			If "Riots/Protests" followed by "Violence against civilians" is predicted to occur zero or few times", then the cell is blank or transparent.
			</p>
		</div>
		<div class="row">
			<div class="col-xs-12 col-sm-12 col-md-12">
				<aside style="margin-top: 80px;">
					<p>
						Order: <select id="order">
							<option value="name">by Name</option>
							<option value="count">by Frequency</option>
						</select>
					</p>
				</aside>
				<div id="matrix"></div>
			</div>
		</div>
	</div>
</body>
<script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
<script>
var margin = {
        top: 240,
        right: 0,
        bottom: 80,
        left: 240
    },
    width = 640,
    height = 640;

var x = d3.scale.ordinal().rangeBands([0, width]),
    z = d3.scale.linear()
    .domain([0, 4]).clamp(true),
    c = d3.scale.category10().domain(
        d3.range(10));

var svg = d3.select("#matrix").append("svg")
	.attr("width", width + margin.left + margin.right)
	.attr("height", height + margin.top + margin.bottom).style("margin-left", -margin.left + "px").append("g")
	.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var div = d3.select("body").append("div")	
	.attr("class", "tooltip")				
	.style("opacity", 1);

d3.json('<%= model %>', 
		function(graph) {
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
                    }).each(row);

            row.append("line")
            	.attr("x2", width);

            row.append("text")
            	.attr("font-size", "12px")
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
            	.attr("font-size", "12px")
            	.attr("x", 6)
            	.attr("y", x.rangeBand() / 2)
            	.attr("dy", ".32em")
            	.attr("text-anchor", "start").text(function(d, i) {
                return nodes[i].name;
            });

            function row(row) {
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
                div.transition()		
                	.duration(200)		
                	.style("opacity", .9);
                div.html("Count: " + p.z + "<br/>" + "Final Probability: " + p.probability)	
                	.style("left", (d3.event.pageX) + "px")		
                	.style("top", (d3.event.pageY - 28) + "px");
            }

            function mouseout() {
                d3.selectAll("text").classed("active", false);
                div.transition()		
                	.duration(500)		
                	.style("opacity", 0);
            }

            d3.select("#order").on("change", function() {
                order(this.value);
            });

            function order(value) {
                x.domain(orders[value]);
                var t = svg.transition().duration(1000);

                t.selectAll(".row").delay(function(d, i) {
                    return x(i) * 4;
                }).attr("transform", function(d, i) {
                    return "translate(0," + x(i) + ")";
                }).selectAll(".cell").delay(function(d) {
                    return x(d.x) * 4;
                }).attr("x", function(d) {
                    return x(d.x);
                });

                t.selectAll(".column").delay(function(d, i) {
                    return x(i) * 4;
                }).attr("transform", function(d, i) {
                    return "translate(" + x(i) + ")rotate(-90)";
                });
            }
        });
</script>
</html>
