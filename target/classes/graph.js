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
    d.radius = 9; // Initial radius of nodes
  });

  const link = svg.append("g")
    .attr("class", "links")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.25)
    .selectAll("line")
    .data(links)
    .enter()
    .append("line")
    .attr("stroke-width", 0.9)
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
    .attr("fill", "#222")
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
        
        const defs = svg.append('defs');
  
        // Define gradients for each direction for each color
        const gradients = {
          'blueGradientLTR': createGradient(defs, 'blueGradientLTR', '#add8e6'),
          'blueGradientRTL': createGradient(defs, 'blueGradientRTL', '#add8e6', true),
          'gradientGoldLTR': createGradient(defs, 'gradientGoldLTR', '#FFD700'),
          'gradientGoldRTL': createGradient(defs, 'gradientGoldRTL', '#FFD700', true),
          'gradientGreenLTR': createGradient(defs, 'gradientGreenLTR', '#98FB98'),
          'gradientGreenRTL': createGradient(defs, 'gradientGreenRTL', '#98FB98', true),
          'gradientPurpleLTR': createGradient(defs, 'gradientPurpleLTR', '#D8BFD8'),
          'gradientPurpleRTL': createGradient(defs, 'gradientPurpleRTL', '#D8BFD8', true),
          'gradientPinkLTR': createGradient(defs, 'gradientPinkLTR', '#FFB6C1'),
          'gradientPinkRTL': createGradient(defs, 'gradientPinkRTL', '#FFB6C1', true),
          'gradientCrimsonLTR': createGradient(defs, 'gradientCrimsonLTR', '#DC143C'),
          'gradientCrimsonRTL': createGradient(defs, 'gradientCrimsonRTL', '#DC143C', true),
        };
  
        // Helper function for creating a gradient
        function createGradient(defs, id, color, reverse = false) {
          const gradient = defs.append('linearGradient')
            .attr('id', id)
            .attr('x1', reverse ? '100%' : '0%')
            .attr('y1', '0%')
            .attr('x2', reverse ? '0%' : '100%')
            .attr('y2', '0%')
            .attr('spreadMethod', 'reflect');
  
          gradient.append('stop')
            .attr('offset', '0%')
            .attr('stop-color', color)
            .attr('stop-opacity', 1);
  
          gradient.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', color)
            .attr('stop-opacity', 0);
  
          return gradient;
        }
  
        // Process each message
        messages.forEach(function(message, index) {
          const sourceNode = idToNode.get(message.message_from);
          const targetNode = idToNode.get(message.message_to);
          const messageType = message.message;  
  
          let gradient; 
          if (sourceNode.x < targetNode.x) {
            switch (messageType) {
              case "ADD_TRANSACTION" :
                gradient = gradients['gradientGreenRTL']; 
                break;
              case "QUORUM_READY" :
                gradient = gradients['blueGradientRTL'];
                break; 
              case "PING" :
                gradient = gradients['gradientGoldRTL']; 
                break;
              case "RECEIVE_MEMPOOL" :
                gradient = gradients['gradientPurpleRTL']; 
                break;
              case "RECEIVE_SIGNATURE" :
                gradient = gradients['gradientPinkRTL']; 
                break;
              case "RECEIVE_SKELETON" :
                gradient = gradients['gradientCrimsonRTL']; 
                break; 
              default :
                gradient = gradients['gradientGreenRTL'];
                break;  
            }
          } else {
            switch (messageType) {
              case "ADD_TRANSACTION" :
                gradient = gradients['gradientGreenLTR']; 
                break;
              case "QUORUM_READY" :
                gradient = gradients['blueGradientLTR'];
                break; 
              case "PING" :
                gradient = gradients['gradientGoldLTR']; 
                break;
              case "RECEIVE_MEMPOOL" :
                gradient = gradients['gradientPurpleLTR']; 
                break;
              case "RECEIVE_SIGNATURE" :
                gradient = gradients['gradientPinkLTR']; 
                break;
              case "RECEIVE_SKELETON" :
                gradient = gradients['gradientCrimsonLTR']; 
                break; 
              default :
                gradient = gradients['gradientGreenLTR'];
                break;  
            }
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
    .attr("stroke-width", 1)  // Set initial stroke-width
    .transition()
    .duration(1000)
    .delay(index * 100)  // Delay each message to prevent them from all animating at once

    .attrTween("stroke-dashoffset", function() {
        return function(t) {
            const offset = d3.interpolate(pathLength, 0)(t);
            const width = (offset > pathLength / 2) ? 2 * (1 - offset / pathLength) : 2 * (offset / pathLength);
            this.setAttribute("stroke-width", Math.max(width, 1));
            return offset;
        };
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
