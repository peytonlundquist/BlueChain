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
      
      // Highlight the links and the nodes on the ends of the links
      link
          .filter(l => l.target === d || l.source === d)
          .attr("stroke", "black")
          .attr("stroke-width", 3)
          .each(function(l) {
              // Enlarge the source node
              node.filter(n => n === l.source)
                  .attr("r", function(n) { return n.radius * 2; })  // Double the radius
                  .attr("font-weight", "bold");
  
              // Enlarge the target node
              node.filter(n => n === l.target)
                  .attr("r", function(n) { return n.radius * 2; })  // Double the radius
                  .attr("font-weight", "bold");
          });
  }
  
  function outed(event, d) {
      link.style("mix-blend-mode", "multiply");
      d3.select(this).attr("font-weight", null);
      
      // Reset the links and the nodes on the ends of the links
      link
          .filter(l => l.target === d || l.source === d)
          .attr("stroke", null)
          .attr("stroke-width", 1)
          .each(function(l) {
              // Reset the source node
              node.filter(n => n === l.source)
                  .attr("r", function(n) { return n.radius; })  // Reset the radius to its original size
                  .attr("font-weight", null);
  
              // Reset the target node
              node.filter(n => n === l.target)
                  .attr("r", function(n) { return n.radius; })  // Reset the radius to its original size
                  .attr("font-weight", null);
          });
  }
  
  
  node.append("title")
    .text(function(d) { return d.id; });
  
  // Load and parse the NDJSON data
  // Load and parse the NDJSON data
  
    function loadMessages() {
      d3.text("messages.ndjson")
        .then(function(text) {
          const messages = text.split('\n').filter(Boolean).map(JSON.parse);

          // Define the gradient
          const defs = svg.append('defs');
          
          const blueGradient = defs.append('linearGradient')
            .attr('id', 'gradient')
            .attr('x1', '0%')
            .attr('y1', '0%')
            .attr('x2', '100%')
            .attr('y2', '0%')
            .attr('spreadMethod', 'reflect');

          blueGradient.append('stop')
            .attr('offset', '0%')
            .attr('stop-color', '#add8e6')  // Light blue color
            .attr('stop-opacity', 1);

          blueGradient.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', '#add8e6')  // Light blue color
            .attr('stop-opacity', 0);

                    // Light gold gradient
          const gradientGold = defs.append('linearGradient')
          .attr('id', 'gradientGold')
          .attr('x1', '0%')
          .attr('y1', '0%')
          .attr('x2', '100%')
          .attr('y2', '0%')
          .attr('spreadMethod', 'reflect');

          gradientGold.append('stop')
          .attr('offset', '0%')
          .attr('stop-color', '#FFD700')  // Gold color
          .attr('stop-opacity', 1);

          gradientGold.append('stop')
          .attr('offset', '100%')
          .attr('stop-color', '#FFD700')  // Gold color
          .attr('stop-opacity', 0);

          // Light green gradient
          const gradientGreen = defs.append('linearGradient')
          .attr('id', 'gradientGreen')
          .attr('x1', '0%')
          .attr('y1', '0%')
          .attr('x2', '100%')
          .attr('y2', '0%')
          .attr('spreadMethod', 'reflect');

          gradientGreen.append('stop')
          .attr('offset', '0%')
          .attr('stop-color', '#98FB98')  // Light green color
          .attr('stop-opacity', 1);

          gradientGreen.append('stop')
          .attr('offset', '100%')
          .attr('stop-color', '#98FB98')  // Light green color
          .attr('stop-opacity', 0);

          // Light purple gradient
          const gradientPurple = defs.append('linearGradient')
          .attr('id', 'gradientPurple')
          .attr('x1', '0%')
          .attr('y1', '0%')
          .attr('x2', '100%')
          .attr('y2', '0%')
          .attr('spreadMethod', 'reflect');

          gradientPurple.append('stop')
          .attr('offset', '0%')
          .attr('stop-color', '#D8BFD8')  // Light purple color
          .attr('stop-opacity', 1);

          gradientPurple.append('stop')
          .attr('offset', '100%')
          .attr('stop-color', '#D8BFD8')  // Light purple color
          .attr('stop-opacity', 0);

          // Light pink gradient
          const gradientPink = defs.append('linearGradient')
          .attr('id', 'gradientPink')
          .attr('x1', '0%')
          .attr('y1', '0%')
          .attr('x2', '100%')
          .attr('y2', '0%')
          .attr('spreadMethod', 'reflect');

          gradientPink.append('stop')
          .attr('offset', '0%')
          .attr('stop-color', '#FFB6C1')  // Light pink color
          .attr('stop-opacity', 1);

          gradientPink.append('stop')
          .attr('offset', '100%')
          .attr('stop-color', '#FFB6C1')  // Light pink color
          .attr('stop-opacity', 0);

        const gradientCrimson = defs.append('linearGradient')
        .attr('id', 'gradientCrimson')
        .attr('x1', '0%')
        .attr('y1', '0%')
        .attr('x2', '100%')
        .attr('y2', '0%')
        .attr('spreadMethod', 'reflect');
      
      gradientCrimson.append('stop')
        .attr('offset', '0%')
        .attr('stop-color', '#DC143C')  // Crimson color
        .attr('stop-opacity', 1);
      
      gradientCrimson.append('stop')
        .attr('offset', '100%')
        .attr('stop-color', '#DC143C')  // Crimson color
        .attr('stop-opacity', 0);
      

          let gradient; 

          // Process each message
          messages.forEach(function(message, index) {
            const sourceNode = idToNode.get(message.message_from);
            const targetNode = idToNode.get(message.message_to);
            const messageType = message.message;  
            
            switch (messageType) {
                case "ADD_TRANSACTION" :
                  gradient = gradientGreen; 
                  break;
                case "QUORUM_READY" :
                  gradient = blueGradient;
                  break; 
                case "PING" :
                  gradient = gradientGold; 
                  break;
                case "RECEIVE_MEMPOOL" :
                  gradient = gradientPurple; 
                  break;
                case "RECEIVE_SIGNATURE" :
                  gradient = gradientPink; 
                  break;
                case "RECEIVE_SKELETON" :
                  gradient = gradientCrimson; 
                  break; 
                default :
                  gradient = gradientGreen;
                  break;  
            }

            // If either of the nodes does not exist, skip this message
            if (!sourceNode || !targetNode) {
              return;
            }

            // Calculate the angle of the line
            const dx = targetNode.x - sourceNode.x;
            const dy = targetNode.y - sourceNode.y;
            const angle = Math.atan2(dy, dx);

            // Calculate the start and end points of the line, adjusted so the line doesn't overlap the nodes
            const sourceRadius = sourceNode.radius || 0;
            const targetRadius = targetNode.radius || 0;
            const x1 = sourceNode.x + Math.cos(angle) * sourceRadius;
            const y1 = sourceNode.y + Math.sin(angle) * sourceRadius;
            const x2 = targetNode.x - Math.cos(angle) * targetRadius;
            const y2 = targetNode.y - Math.sin(angle) * targetRadius;

            

            // Create a path for the link between the source and target nodes
            const path = svg.insert("path",":first-child")
              .attr("d", `M${x1},${y1} L${x2},${y2}`)
              .attr("fill", "none")
              .attr("stroke", `url(#${gradient.attr('id')})`)
              .attr("stroke-width", 1)
              .attr("stroke-dasharray", "0 1")  // Start with a dash of length 0 and a gap of length 1
              .attr("stroke-dashoffset", 0)  // Start with an offset of 0
              .attr("filter", "url(#glow)")  // Apply the glow filter here
              .attr("stroke-opacity", 0);  // Set the initial stroke opacity to 0

            // Calculate the length of the path
            const pathLength = path.node().getTotalLength();

            // Animate the dash offset to make it appear as though the line is being drawn
            // Animate the dash offset to make it appear as though the line is being drawn
            path
              .attr("stroke-dasharray", `${pathLength} ${pathLength}`)  // Set the dash and gap lengths to the length of the path
              .attr("stroke-dashoffset", pathLength)  // Set the offset to the length of the path
              .transition()
              .duration(1000)
              .delay(index * 100)  // Delay each message to prevent them from all animating at once
              .attrTween("stroke-dashoffset", function() {
                return d3.interpolate(pathLength, 0);
              })
              .attrTween("stroke-dasharray", function() {
                return d3.interpolate("0 " + pathLength, pathLength + " 0");
              })
              .attr("stroke-opacity", 1)  // Increase the stroke opacity to 1 to make the line visible
              .transition()  // Add a new transition to make the line disappear
              .duration(1000)
              .attr("stroke-opacity", 0)  // Fade out the line by reducing its opacity to 0
              .remove();  // After the line has faded out, remove it from the SVG
          });
        })
        .catch(function(error) {
          console.log("Error loading messages:", error);
        })
        .finally(function() {
          // Call the loadMessages function recursively after a delay
          setTimeout(loadMessages, 200); // Delay in milliseconds (e.g., 5000 for 5 seconds)
        });
    }

    // Start loading and processing messages
    loadMessages();


}).catch(function(error) {
  console.log("Error loading data:", error);
});
