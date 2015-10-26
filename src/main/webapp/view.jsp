<%@page import="org.clairvaux.utils.FileUtils"%>
<% 
	String model = (String) request.getAttribute("model"); 
	String title = "";
	if (model != null) {
		title = FileUtils.extractBaseName(model);
	}
%>
<html class="ocks-org do-not-copy">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
</head>

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
</style>
<body>
	<div class="container">
		<div class="row">
			<h1><%= title %></h1>
			<p>
			Predictions results are visualized as a co-occurrence matrix.
			Rows are the actual values, while columns are the next step's predicted values.
			Cell opacity is based on {actual, predicted} counts.
			If event "Violence against civilians" is predicted to occur many times after "Riots/Protests", then the cell is filled with a solid color.
			If event "Violence against civilians" is predicted to occur zero or few times after "Riots/Protests", then the cell is blank or transparent.
			</p>
		</div>
		<div class="row">
			<div class="col-xs-12 col-sm-12 col-md-12">
				<aside style="margin-top: 80px;">
					<p>
						Order: <select id="order">
							<option value="name">by Name</option>
							<option value="count">by Frequency</option>
							<option value="group">by Cluster</option>
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
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js"
	charset="utf-8"></script>
<script>
	var margin = {
		top : 80,
		right : 0,
		bottom : 10,
		left : 80
	}, width = 720, height = 720;

	var x = d3.scale.ordinal().rangeBands([ 0, width ]), z = d3.scale.linear()
			.domain([ 0, 4 ]).clamp(true), c = d3.scale.category10().domain(
			d3.range(10));

	var svg = d3.select("#matrix").append("svg").attr("width",
			width + margin.left + margin.right).attr("height",
			height + margin.top + margin.bottom).style("margin-left",
			-margin.left + "px").append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");

	d3
			.json(
					'<%= model %>',
					function(graph) {
						var matrix = [], nodes = graph.nodes, n = nodes.length;

						// Compute index per node.
						nodes.forEach(function(node, i) {
							node.index = i;
							node.count = 0;
							matrix[i] = d3.range(n).map(function(j) {
								return {
									x : j,
									y : i,
									z : 0
								};
							});
						});

						// Convert links to matrix; count character occurrences.
						graph.links.forEach(function(link) {
							matrix[link.source][link.target].z += link.value;
							matrix[link.target][link.source].z += link.value;
							matrix[link.source][link.source].z += link.value;
							matrix[link.target][link.target].z += link.value;
							nodes[link.source].count += link.value;
							nodes[link.target].count += link.value;
						});

						// Precompute the orders.
						var orders = {
							name : d3.range(n).sort(
									function(a, b) {
										return d3.ascending(nodes[a].name,
												nodes[b].name);
									}),
							count : d3.range(n).sort(function(a, b) {
								return nodes[b].count - nodes[a].count;
							}),
							group : d3.range(n).sort(function(a, b) {
								return nodes[b].group - nodes[a].group;
							})
						};

						// The default sort order.
						x.domain(orders.name);

						svg.append("rect").attr("class", "background").attr(
								"width", width).attr("height", height);

						var row = svg.selectAll(".row").data(matrix).enter()
								.append("g").attr("class", "row").attr(
										"transform", function(d, i) {
											return "translate(0," + x(i) + ")";
										}).each(row);

						row.append("line").attr("x2", width);

						row.append("text").attr("x", -6).attr("y",
								x.rangeBand() / 2).attr("dy", ".32em").attr(
								"text-anchor", "end").text(function(d, i) {
							return nodes[i].name;
						});

						var column = svg.selectAll(".column").data(matrix)
								.enter().append("g").attr("class", "column")
								.attr(
										"transform",
										function(d, i) {
											return "translate(" + x(i)
													+ ")rotate(-90)";
										});

						column.append("line").attr("x1", -width);

						column.append("text").attr("x", 6).attr("y",
								x.rangeBand() / 2).attr("dy", ".32em").attr(
								"text-anchor", "start").text(function(d, i) {
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
										return z(d.z);
									})
									.style(
											"fill",
											function(d) {
												return nodes[d.x].group == nodes[d.y].group ? c(nodes[d.x].group)
														: null;
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
						}

						function mouseout() {
							d3.selectAll("text").classed("active", false);
						}

						d3.select("#order").on("change", function() {
							clearTimeout(timeout);
							order(this.value);
						});

						function order(value) {
							x.domain(orders[value]);

							var t = svg.transition().duration(2500);

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

						var timeout = setTimeout(function() {
							order("group");
							d3.select("#order").property("selectedIndex", 2)
									.node().focus();
						}, 5000);
					});
</script>
</html>
