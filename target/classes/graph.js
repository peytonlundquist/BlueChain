// Load the JSON data

const svgContainer = document.querySelector(".container-svg"); // Get the SVG container element
const containerWidth = svgContainer.clientWidth; // Get the width of the container
const containerHeight = svgContainer.clientHeight; // Get the height of the container

d3.json("nodes.json").then(function(data) {
  const nodes = data.map(function(d) {
    return {
      ...d,
      x: Math.random() * containerWidth,  // assign initial x
      y: Math.random() * containerHeight, // assign initial y
    };
  });
  const links = [];

  // Extract links from the node data
  nodes.forEach(function(node) {
    node.targets.forEach(function(target) {
      links.push({ source: node.id, target: target });
    });
  });



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
}); 