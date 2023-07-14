/* Load the JSON data
d3.json("nodes.json").then(function(data) {
  const nodes = data;
  const links = [];

  // Extract links from the node data
  nodes.forEach(function(node) {
    node.targets.forEach(function(target) {
      links.push({ source: node.id, target: target });
    });
  });

  const svgContainer = document.querySelector(".container-svg"); // Get the SVG container element
  const containerWidth = svgContainer.clientWidth; // Get the width of the container
  const containerHeight = svgContainer.clientHeight; // Get the height of the container

  const simulation = d3.forceSimulation(nodes)
    .force("link", d3.forceLink(links).distance(100).id(function(d) { return d.id; }))
    .force("charge", d3.forceManyBody())
    .force("center", d3.forceCenter(containerWidth / 2, containerHeight / 2)); // Adjust the center coordinates

  const svg = d3.select("#graph-svg")
    .attr("class", "container-svg")
    .attr("width", "100%") // Set the width to 100% of the parent element
    .attr("height", "100%"); //

  const link = svg.append("g")
    .attr("class", "links")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.25)
    .selectAll("line")
    .data(links)
    .enter()
    .append("line")
    .attr("stroke-width", function(d) { return Math.sqrt(d.value); });

  const node = svg.append("g")
    .attr("class", "nodes")
    .attr("stroke", "#fff")
    .attr("stroke-width", 1)
    .selectAll("circle")
    .data(nodes)
    .enter()
    .append("circle")
    .attr("r", 5)
    .attr("fill", "gray")
    .call(d3.drag()
      .on("start", dragstarted)
      .on("drag", dragged)
      .on("end", dragended));

  node.append("title")
    .text(function(d) { return d.id; });

  function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
  }

  function dragged(event, d) {
    d.fx = event.x;
    d.fy = event.y;
  }

  function dragended(event, d) {
    if (!event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
  }

  // Continuous tick function
  function ticked() {
    link
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

    node
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });
  }

  // Start the simulation and continuously update the positions
  simulation.on("tick", ticked);

}).catch(function(error) {
  console.log("Error loading data:", error);
}); */
d3.json("nodes.json").then(function(data) {
  const nodes = data;
  const idToNode = new Map(nodes.map(node => [node.id, node]));
  const links = [];

  // Extract links from the node data
  nodes.forEach(function(node) {
    node.targets.forEach(function(target) {
      links.push({ source: idToNode.get(node.id), target: idToNode.get(target) });
    });
  });

  const svgContainer = document.querySelector(".container-svg"); // Get the SVG container element
  const containerWidth = svgContainer.clientWidth; // Get the width of the container
  const containerHeight = svgContainer.clientHeight; // Get the height of the container

  const svg = d3.select("#graph-svg")
    .attr("class", "container-svg")
    .attr("width", containerWidth)
    .attr("height", containerHeight);

  const radius = Math.min(containerWidth, containerHeight) / 2;

  nodes.forEach(function(d, i) {
    d.x = d.fx = radius * Math.cos((2 * Math.PI * i) / nodes.length) + containerWidth / 2;
    d.y = d.fy = radius * Math.sin((2 * Math.PI * i) / nodes.length) + containerHeight / 2;
    d.radius = 5; // Initial radius of nodes
  });

  // Make a copy of the initial positions
  const initialPositions = nodes.map(node => ({ ...node }));

  const link = svg.append("g")
    .attr("class", "links")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.25)
    .selectAll("line")
    .data(links)
    .enter()
    .append("line")
    .attr("stroke-width", function(d) { return Math.sqrt(0.2 || 1); })
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; });

  const node = svg.append("g")
    .attr("class", "nodes")
    .attr("stroke", "#fff")
    .attr("stroke-width", 1)
    .selectAll("circle")
    .data(nodes)
    .enter()
    .append("circle")
    .attr("fill", "gray")
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; })
    .attr("r", function(d) { return d.radius; })
    .call(d3.drag()
      .on("start", dragstarted)
      .on("drag", dragged)
      .on("end", dragended));

  function dragstarted(event, d) {
    d3.select(this).raise().attr("stroke", "black");
  }

  function dragged(event, d) {
    d.x = event.x;
    d.y = event.y;
    link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });
    d3.select(this).attr("cx", d.x).attr("cy", d.y);
  }

  function dragended(event, d) {
    // Reset to the initial positions
    const initialPosition = initialPositions.find(node => node.id === d.id);
    d.x = initialPosition.x;
    d.y = initialPosition.y;
    link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });
    d3.select(this).attr("cx", d.x).attr("cy", d.y);
    d3.select(this).attr("stroke", null);
  }

  node.append("title")
    .text(function(d) { return d.id; });


}).catch(function(error) {
  console.log("Error loading data:", error);
});
