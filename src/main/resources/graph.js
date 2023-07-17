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
    d.radius = 7; // Initial radius of nodes
  });

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
    .attr("y2", function(d) { return d.target.y; })
   

  const node = svg.append("g")
    .attr("class", "nodes")
    .attr("stroke", "#fff")
    .attr("stroke-width", 1)
    .selectAll("circle")
    .data(nodes)
    .enter()
    .append("circle")
    .attr("fill", "gray")
    .attr("filter", "url(#glow)")  // Apply the glow filter here
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; })
    .attr("r", function(d) { return d.radius; })
    .on("mouseover", overed)
    .on("mouseout", outed);
    

    function overed(event, d) {
      link.style("mix-blend-mode", null);
      d3.select(this).attr("font-weight", "bold");
      link.filter(link => link.target === d)
        .attr("stroke", "green")
        .each(function(d) {
          node.filter(node => node === d.source)
            .attr("fill", "green")
            .attr("font-weight", "bold");
        });
      link.filter(link => link.source === d)
        .attr("stroke", "blue")
        .each(function(d) {
          node.filter(node => node === d.target)
            .attr("fill", "blue")
            .attr("font-weight", "bold");
        });
    }
    
    function outed(event, d) {
      link.style("mix-blend-mode", "multiply");
      d3.select(this).attr("font-weight", null);
      link.filter(link => link.target === d)
        .attr("stroke", null)
        .each(function(d) {
          node.filter(node => node === d.source)
            .attr("fill", null)
            .attr("font-weight", null);
        });
      link.filter(link => link.source === d)
        .attr("stroke", null)
        .each(function(d) {
          node.filter(node => node === d.target)
            .attr("fill", null)
            .attr("font-weight", null);
        });
    }
    
   

  node.append("title")
    .text(function(d) { return d.id; });
}).catch(function(error) {
  console.log("Error loading data:", error);
});
