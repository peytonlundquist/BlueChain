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

     // Update information paragraph and fade in
     const infoDiv = d3.select('.information')
     .style("opacity", 0)
     .html('Now the quorum members form a special connection with one another, and the other nodes will wait for the quorum members to come to consensus.<br><br>The other nodes are still active listening for tranactions but from this point forward we will focus on quorum members.<br><br>Quorum members form a special connection with one another, and now they will confirm transactions in the mempool with one another, so they can all construct the same block.<br><br>So when you are ready, press compile mempool!')
     .transition()
     .duration(3000)
     .style("opacity", 1);

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
    .duration(10000)
    .style("opacity", 0);

    d3.selectAll("line")
    .transition()
    .duration(10000)
    .style("opacity", 0);



    for (let i = 0; i < nodes.length; i++) {
        for (let j = i + 1; j < nodes.length; j++) {
            const sourceNode = {
                x: +nodes[i].getAttribute("cx"),
                y: +nodes[i].getAttribute("cy"),
                radius: +nodes[i].getAttribute("r")
            };

            const targetNode = {
                x: +nodes[j].getAttribute("cx"),
                y: +nodes[j].getAttribute("cy"),
                radius: +nodes[j].getAttribute("r")
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

            const path = svg.insert("path", ":first-child")
                .attr("d", `M${x1},${y1} L${x2},${y2}`)
                .attr("fill", "none")
                .attr("stroke", `#3A86FF`)
                .attr("stroke-width", 1)
                .attr("stroke-dasharray", "0 1")
                .attr("stroke-dashoffset", 0)
                .attr("filter", "url(#glow)")
                .attr("stroke-opacity", 0)
                .classed("glow-path", true); // Add the "glow-path" class for glowing effect

            const pathLength = path.node().getTotalLength();

            path
            .attr("stroke-dasharray", `${pathLength} ${pathLength}`)  // Set the dash and gap lengths to the length of the path
            .attr("stroke-dashoffset", pathLength)  // Set the offset to the length of the path
            .transition()
            .duration(2000)
            .delay(1000)  // Delay each message to prevent them from all animating at once
            .attrTween("stroke-dashoffset", function() {
              return d3.interpolate(pathLength, 0);
            })
            .attrTween("stroke-dasharray", function() {
              return d3.interpolate("0 " + pathLength, pathLength + " 0");
            })
            .attr("stroke-opacity", 1)  // Increase the stroke opacity to 1 to make the line visible
            
            

        }
    }
}

