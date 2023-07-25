
let shouldAnime = true;
let blockInterval; 

function sendBlock() {

    
    if(!shouldAnime) return;
    const svg = d3.select("#graph-svg");

    // Add SVG filter for glow effect
    const defs = svg.append("defs");

    const filter = defs.append("filter")
        .attr("id", "glow");

    filter.append("feGaussianBlur")
        .attr("stdDeviation", "2.5")
        .attr("result", "coloredBlur");

    const feMerge = filter.append("feMerge");

    feMerge.append("feMergeNode")
        .attr("in", "coloredBlur");

    feMerge.append("feMergeNode")
        .attr("in", "SourceGraphic");

        // Create red gradient for block propagation animation
        const gradient = defs.append('linearGradient')
        .attr('id', 'redGradient')
        .attr('gradientUnits', 'userSpaceOnUse')
        .attr('x1', '0%').attr('y1', '0%')
        .attr('x2', '100%').attr('y2', '0%')
        .attr('spreadMethod', 'pad');

        gradient.append('stop')
        .attr('offset', '0%')
        .attr('stop-color', '#000')
        .attr('stop-opacity', 1);

        gradient.append('stop')
        .attr('offset', '100%')
        .attr('stop-color', '#f00')
        .attr('stop-opacity', 1);


    d3.select('.information')
        .style("opacity", 0)
        .html("Now the quorum members send the block skeleton data of the block they created to their local connections, and their connections will receive and gossip the block to their connections.<br><br>This makes for a spiderweb of sending and receiving of the latest block that was constructed by the quorum members.<br><br>Each node that receives the skeleton data also verifies the block themselves, and if valid the next step would be adding the block to their version of the ledger.")
        .transition()
        .duration(2000)
        .style("opacity", 1)
        .end()
        .then(() => {
            
            const button = d3.select('.information').append('button')
                .text('Add Block')
                .attr('class', 'step-buttons')
                .attr('id', 'add-block')
                .style('opacity', 0)
                .on('click', () => addBlock())
                .style('transition', 'opacity 2s');

            setTimeout(() => {
                button.style('opacity', 1);
            }, 100);
        });

        // Fade out paths
            d3.selectAll('.glow-path')
            .transition()
            .duration(2000) // change this to the desired fade-out duration in milliseconds
            .style('opacity', 0)
            .remove();

            // Fade out the quorum block
            d3.select('.quorum-block')
            .transition()
            .duration(2000) // change this to the desired fade-out duration in milliseconds
            .style('opacity', 0)
            .remove();

            // Fade out the "5 votes" text
            d3.select('.votes-text')
            .transition()
            .duration(2000) // change this to the desired fade-out duration in milliseconds
            .style('opacity', 0)
            .remove();


        // 1. Load the nodes and compute their positions
        d3.json("nodes.json").then(function(data) {
            const nodes = data;
            const svgContainer = document.querySelector(".container-svg");
            const containerWidth = svgContainer.clientWidth;
            const containerHeight = svgContainer.clientHeight;
            const radius = Math.min(containerWidth, containerHeight) / 2;
        
            // Collect the titles of the nodes already on screen
            const existingNodeTitles = new Set(d3.selectAll('.nodes circle').selectAll('title').nodes().map(node => node.textContent));
        
            // Assign new positions for all nodes
            nodes.forEach(function(d, i) {
                d.desiredX = radius * Math.cos((2 * Math.PI * i) / nodes.length) + containerWidth / 2;
                d.desiredY = radius * Math.sin((2 * Math.PI * i) / nodes.length) + containerHeight / 2;
            });

            d3.selectAll(".links")
            .transition()
            .duration(3000)
            .style("opacity", 1)
            .end()  // Wait for the fade out transition to end
            
        
            // Animate movement of existing nodes to their new positions
            d3.selectAll(".nodes circle")
                .data(nodes, d => d.id)
                .transition()
                .duration(1000)
                .attr("cx", d => d.desiredX)
                .attr("cy", d => d.desiredY);
        
            // Exclude nodes that are already on screen
            const newNodes = nodes.filter(node => !existingNodeTitles.has(node.id));
        
            // Animate new nodes
            const nodeEnter = d3.select(".nodes")
                .selectAll("circle")
                .data(newNodes, d => d.id)
                .enter()
                .append("circle")
                .attr("fill", "#222")
                .attr("filter", "url(#glow)")  
                .attr("cx", d => d.desiredX)
                .attr("cy", d => d.desiredY)
                .attr("r", 9);  // Initial radius of nodes
        
            nodeEnter.append("title")
                .text(d => d.id);
        });

        function getRandomPair(n) {
            if (n < 2) {
              throw new Error("Number must be at least 2");
            }
          
            let i1 = Math.floor(Math.random() * n);
            let i2;
            do {
              i2 = Math.floor(Math.random() * n);
            } while (i2 === i1);
            return [i1, i2];
          }
          
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

        function sendBlock() {
            const nodesData = d3.selectAll(".nodes circle").data();
            
            const gradients = {};
            gradients["blockSentLTR"] = createGradient(defs, "blockSentLTR", "#B82538");
            gradients["blockSentRTL"] = createGradient(defs, "blockSentRTL", "#B82538", true);

          
            if (nodesData.length >= 2) {
              const randomIndices = getRandomPair(nodesData.length);
              const sourceNode = nodesData[randomIndices[0]];
              const targetNode = nodesData[randomIndices[1]];
              
              let gradient = sourceNode.desiredX < targetNode.desiredX ? gradients[`blockSentRTL`] : gradients[`blockSentLTR`];
            
              const dx = targetNode.desiredX - sourceNode.desiredX;
              const dy = targetNode.desiredY - sourceNode.desiredY;
              const angle = Math.atan2(dy, dx);
            
              const sourceRadius = 9; // We set the node's radius to 9 above
              const targetRadius = 9;
              const x1 = sourceNode.desiredX + Math.cos(angle) * sourceRadius;
              const y1 = sourceNode.desiredY + Math.sin(angle) * sourceRadius;
              const x2 = targetNode.desiredX - Math.cos(angle) * targetRadius;
              const y2 = targetNode.desiredY - Math.sin(angle) * targetRadius;
            
              const path = svg.append("path",":first-child")
              .attr("d", `M${x1},${y1} L${x2},${y2}`)
              .attr("fill", "none")
              .attr("stroke", `url(#${gradient.attr('id')})`)
              .attr("stroke-width", 1)
              .attr("stroke-dasharray", "0 1")
              .attr("stroke-dashoffset", 0)
              .attr("filter", "url(#glow)")
              .attr("stroke-opacity", 1);
            
              const pathLength = path.node().getTotalLength();

           

            
              path
              .attr("stroke-dasharray", `${pathLength} ${pathLength}`)
              .attr("stroke-dashoffset", pathLength)
              .transition()
              .duration(2000)
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
              .attrTween("stroke-opacity", function() {
                return function(t) {
                    return t <= 0.5 ? 2 * t : 2 - 2 * t; // Increases first half of the transition, decreases in the second half
                };
              })
              .transition().delay(1000)
              
              .remove();
            }
          }
          blockInterval = setInterval(sendBlock,250); 
        
    } 
 

    
    function stopAnimation() {
        clearInterval(blockInterval); 
    }