function constructBlock() {


    const infoDiv = d3.select('.information')
        .style("opacity", 0)
        .html('Now, each quorum member uses their synchronized mempool to construct their own block, called the quorum block.<br><br>They will place their transactions from their mempool into the block, along with a pointer to the last block (so the blocks link together), and a merkle root hash that confirms validity of the transactions.<br><br>Once the block is constructed, they will then sign off on the block they created and send their signatures to the other quorum members.')
        .transition()
        .duration(3000)
        .style("opacity", 1)
        .end()
        .then(() => {
            const button = d3.select('.information').append('button')
                .text('Sign Block')
                .attr('class', 'step-buttons')
                .attr('id', 'sign-block')
                .style('opacity', 0)
                .on('click', () => signBlock())  
                .style('transition', 'opacity 2s');

            setTimeout(() => {
                button.style('opacity', 1);
            }, 100);
        });
    const svg = d3.select("#graph-svg");

    const nodes = svg.selectAll("circle").nodes();
    const mempools = svg.selectAll(".mempool").nodes();

    const radius = 100; 
    const blockWidth = 60;
    const blockHeight = 60;
    const lineHeight = 20;

    let centroidX = 0, centroidY = 0;
    let minY = Infinity, maxY = -Infinity;

    nodes.forEach((node) => {
        centroidX += node.cx.baseVal.value;
        centroidY += node.cy.baseVal.value;

        minY = Math.min(minY, node.cy.baseVal.value);
        maxY = Math.max(maxY, node.cy.baseVal.value);
    });

    centroidX /= nodes.length;
    centroidY /= nodes.length;

    const blocks = svg.selectAll(".quorum-block")
        .data(nodes)
        .enter()
        .append("rect")
        .attr("class", "quorum-block block")
        .attr("x", function(d) {
            if (d.cy.baseVal.value === minY || d.cy.baseVal.value === maxY) {
                return d.cx.baseVal.value - blockWidth / 2;
            } else if (d.cx.baseVal.value < centroidX) {
                return d.cx.baseVal.value - blockWidth / 2 - radius;
            } else {
                return d.cx.baseVal.value - blockWidth / 2 + radius;
            }
        })
        .attr("y", function(d) {
            if (d.cy.baseVal.value === minY) {
                return d.cy.baseVal.value - blockHeight / 2 - radius;
            } else if (d.cy.baseVal.value === maxY) {
                return d.cy.baseVal.value - blockHeight / 2 + radius;
            } else {
                return d.cy.baseVal.value - blockHeight / 2;
            }
        })
        .attr("width", blockWidth)
        .attr("height", blockHeight)
        .style('opacity', 0)
        .style("filter", "drop-shadow(0 0 2px #111111)");
    
    blocks.transition().duration(2000)
        .style('opacity', 1);

    const labels = svg.selectAll(".mempool-label")
        .data(mempools)
        .enter()
        .append("text")
        .attr("class", "mempool-label")
        .attr("x", function(d, i) {
            if (nodes[i].cy.baseVal.value === minY || nodes[i].cy.baseVal.value === maxY) {
                return nodes[i].cx.baseVal.value;
            } else if (nodes[i].cx.baseVal.value < centroidX) {
                return nodes[i].cx.baseVal.value - blockWidth / 2 - radius + blockWidth / 2;
            } else {
                return nodes[i].cx.baseVal.value - blockWidth / 2 + radius + blockWidth / 2;
            }
        })
        .attr("y", function(d, i) {
            if (nodes[i].cy.baseVal.value === minY) {
                return nodes[i].cy.baseVal.value - blockHeight / 2 - radius + lineHeight;
            } else if (nodes[i].cy.baseVal.value === maxY) {
                return nodes[i].cy.baseVal.value - blockHeight / 2 + radius + lineHeight;
            } else {
                return nodes[i].cy.baseVal.value - blockHeight / 2 + lineHeight;
            }
        })
        .attr("text-anchor", "middle") // align text to the center
        .style('opacity',0)
        .each(function(d) {
            const transactions = d.textContent.split("\n");
            d3.select(this).selectAll('tspan')
                .data(transactions)
                .enter()
                .append('tspan')
                .attr('x', this.getAttribute('x'))
                .attr('dy', function(d, i) {
                    return i ? lineHeight : 0;
                })
                .text(function(d) {
                    return d;
                });
        });
    
        labels.transition().duration(2000)
            .style('opacity',1); 

    svg.selectAll(".mempool")
        .transition().duration(2000)
        .style('opacity', 0)
        .remove();

    
}
