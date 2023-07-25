function formQuorum(qMembers) {
    const svg = d3.select("#graph-svg");
    const newPaths = svg.selectAll(".glow-path").remove();

    const nodes = d3.selectAll("circle")
        .filter(function() {
            const titleText = this.querySelector("title").textContent;
            const isIncluded = qMembers.includes(titleText);
            console.log('Checking node with title', titleText, 'included:', isIncluded);
            return isIncluded;
        })
        .nodes();

        let center = { x: svg.node().getBoundingClientRect().width / 2, y: svg.node().getBoundingClientRect().height / 2 };
        let radius = Math.min(center.x, center.y) * 0.8;  // 80% of the minimum of width/height
        let angleSlice = Math.PI * 2 / nodes.length;  // The slice of the circle for each node
        
        nodes.forEach((node, i) => {
            let angle = angleSlice * i;
            let newPosX = center.x + radius * Math.cos(angle - Math.PI/2);
            let newPosY = center.y + radius * Math.sin(angle - Math.PI/2);
            
            d3.select(node)
                .transition()
                .duration(5000)
                .attr("cx", newPosX)
                .attr("cy", newPosY);
        });
        

    const infoDiv = d3.select('.information')
    .style("opacity", 0)
    .html('Now the quorum members form a special connection with one another, and the other nodes will wait for the quorum members to come to consensus.<br><br>The other nodes are still active listening for tranactions but from this point forward we will focus on quorum members.<br><br>Quorum members form a special connection with one another, and now they will confirm transactions in the mempool with one another, so they can all construct the same block.<br><br>So when you are ready, press compile mempool!')
    .transition()
    .duration(3000)
    .style("opacity", 1)
    .end()  // Wait for the transition to end
    .then(() => {  // After transition ended, append the button
      const button = d3.select('.information').append('button')
          .text('Synchronize Mempool')
          .attr('class', 'step-buttons')
          .attr('id', 'compile-mempool')
          .style('opacity', 0) // start with 0 opacity
          .on('click', () => compileMempool())
          .style('transition', 'opacity 2s'); // CSS transition
          
      setTimeout(() => {
        button.style('opacity', 1); // after a delay, final opacity
      }, 100);
    });



 // Fade out generator div
    const generatorDiv = d3.select('.generator-svg')
        .transition()
        .duration(500)
        .style("opacity", 0)
        .remove();

    const hashInsert = d3.select(".hash-element")
        .transition()
        .duration(500)
        .style("opacity", 0)
        .remove(); 

    
    const qnums = d3.selectAll(".qMember")
            .transition()
            .duration(500)
            .style("opacity", 0)
            .remove();

            d3.selectAll("circle")
            .filter(function() {
                const titleText = this.querySelector("title").textContent;
                const isIncluded = qMembers.includes(titleText);
                return !isIncluded;
            })
            .transition()
            .duration(3000)
            .style("opacity", 0)
            .end()  // Wait for the fade out transition to end
            .then(() => {  // Then start a new transition to remove the elements
                d3.selectAll("circle")
                    .filter(function() {
                        const titleText = this.querySelector("title").textContent;
                        const isIncluded = qMembers.includes(titleText);
                        return !isIncluded;
                    })
                    .transition()
                    .duration(0)  // No delay for removal
                    .remove();
            });
        
        d3.selectAll(".links")
            .transition()
            .duration(3000)
            .style("opacity", 0)
            .end()  // Wait for the fade out transition to end
            .then(() => {  // Then start a new transition to remove the elements
                d3.selectAll(".links")
                    .transition()
                    .duration(0)  // No delay for removal
                    .remove();
            });

 
    setTimeout(() => {
        const g = svg.append('g')  // Create a group element
            .attr('filter', 'url(#glow)');  // Apply the filter to the group
        for (let i = 0; i <= nodes.length; i++) {
            let targetNodeIndex = (i + 1) % nodes.length;
            const sourceNode = {
                x: +nodes[i].getAttribute("cx"),
                y: +nodes[i].getAttribute("cy"),
                radius: +nodes[i].getAttribute("r")
            };
        
            const targetNode = {
                x: +nodes[targetNodeIndex].getAttribute("cx"),
                y: +nodes[targetNodeIndex].getAttribute("cy"),
                radius: +nodes[targetNodeIndex].getAttribute("r")
            };
            const dx = targetNode.x - sourceNode.x;
            const dy = targetNode.y - sourceNode.y;
            const angle = Math.atan2(dy, dx);
    
            const sourceRadius = sourceNode.radius || 0;
            const targetRadius = targetNode.radius || 0;
            const x1 = sourceNode.x + Math.cos(angle) * sourceRadius;
            const y1 = sourceNode.y + Math.sin(angle) * sourceRadius;
            const x2 = targetNode.x - Math.cos(angle) * targetRadius;
            const y2 = targetNode.y - Math.sin(angle) * targetRadius;
    
            // Append paths to the group element instead of SVG
            const path = g.insert("path", ":first-child") 
                .attr("d", `M${x1},${y1} L${x2},${y2}`)
                .attr("fill", "none")
                .attr("stroke", `#3A86FF`)
                .attr("stroke-width", 1)
                .attr("stroke-dasharray", "0 1")
                .attr("stroke-dashoffset", 0)
                .attr("stroke-opacity", 0)
                .classed("glow-path", true);
    
            const pathLength = path.node().getTotalLength();
    
            path
                .attr("stroke-dasharray", `${pathLength} ${pathLength}`)
                .attr("stroke-dashoffset", pathLength)
                .transition()
                .duration(1000)
                .delay(1000 * i)
                .attrTween("stroke-dashoffset", function() {
                  return d3.interpolate(pathLength, 0);
                })
                .attrTween("stroke-dasharray", function() {
                  return d3.interpolate("0 " + pathLength, pathLength + " 0");
                })
                .attr("stroke-opacity", 1);
        }
    }, 5000)};