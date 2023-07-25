function voteBlock() {
   
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

    d3.select('.information')
        .style("opacity", 0)
        .html("Now, each block has placed their vote for which block is correct.<br><br>For the block that gets the most votes, this is the block that the quorum members will propagate to the network.<br><br>When ready, you can send the block!")
        .transition()
        .duration(3000)
        .style("opacity", 1)
        .end()
        .then(() => {
          

            const button = d3.select('.information').append('button')
                .text('Send Block')
                .attr('class', 'step-buttons')
                .attr('id', 'send-block')
                .style('opacity', 0)
                .on('click', () => sendBlock())
                .style('transition', 'opacity 2s');

            setTimeout(() => {
                button.style('opacity', 1);
            }, 100);
        });


        const nodes = d3.selectAll('circle');

        //Calculate the centroid of the nodes
        let sumX = 0;
        let sumY = 0;
        nodes.each(function() {
            sumX += parseFloat(d3.select(this).attr('cx'));
            sumY += parseFloat(d3.select(this).attr('cy'));
        });
        let centroidX = sumX / nodes.size();
        let centroidY = sumY / nodes.size();
    
        //Select all quorum blocks
        const blocks = d3.selectAll('.quorum-block');
    
        //Get the number of quorum blocks
        const numOfBlocks = blocks.size();
    
        //Transition each block to the centroid of the nodes
        blocks.transition()
        .duration(3000)
        .attr('x', centroidX - 30) // -30 so that the center of the block aligns with the nodes' center
        .attr('y', centroidY - 30) // -30 so that the center of the block aligns with the nodes' center
        .end()
        .then(() => {
            //Create a text element to display the number of votes
            svg.append('text')
                .attr('x', centroidX + 40) // adjust these values to place the text correctly
                .attr('y', centroidY)
                .text(`${numOfBlocks} votes`)
                .attr('class', 'votes-text')
                .style("font-size", "18px")
                .style("font-weight", "bold")
                .style('opacity', 0)
                .transition()
                .duration(2000)
                .style('opacity', 1);

            // remove all blocks except the first one
            blocks.filter((d, i) => i > 0)
                .remove();
        });
    
}