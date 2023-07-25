function signBlock() {
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
        .html("What does signing the block actually mean?<br><br>After each quorum member constructs their block, they will then sign their block using the hash the block they created generated, and their private key.<br><br>The private key is basically their secret address, and by signing the block with this, it ensures the validity of the member's block.<br><br>After each member signs their block, they will send their signature to the other quorum members to compare answers.<br><br>Once each member gets the other's signature, it will be time to vote on the correct signature!")
        .transition()
        .duration(2000)
        .style("opacity", 1)
        .end()
        .then(() => {
            
            const button = d3.select('.information').append('button')
                .text('Block Vote')
                .attr('class', 'step-buttons')
                .attr('id', 'block-vote')
                .style('opacity', 0)
                .on('click', () => voteBlock())
                .style('transition', 'opacity 2s');

            setTimeout(() => {
                button.style('opacity', 1);
            }, 100);
        });

    function performBlockVote() {
        const mempool = d3.selectAll(".mempool"); // Select mempool elements
        const mempoolLabel = d3.selectAll(".mempool-label"); // Select mempool label elements

        mempool.transition()
            .duration(1000)
            .style("opacity", 0)
            .remove();

        mempoolLabel.transition()
            .duration(1000)
            .style("opacity", 0)
            .remove()
            .end()
            .then(() => {
                const quorumBlocks = d3.selectAll(".quorum-block.block").nodes();
                quorumBlocks.forEach(block => {
                    animateNodeTitle(block);
                });
            });
    }

    function animateNodeTitle(block) {
        const blockWidth = +block.getAttribute("width");
        const blockHeight = +block.getAttribute("height");
        const x = +block.getAttribute("x") + (blockWidth / 2);
        const y = +block.getAttribute("y") + (blockHeight / 2);

        const nodes = d3.selectAll(".nodes circle").nodes();

        let minDist = Infinity;
        let nodeTitle = '';
        for (const node of nodes) {
            const nodeX = +node.getAttribute('cx');
            const nodeY = +node.getAttribute('cy');

            const dist = Math.hypot(nodeX - x, nodeY - y);

            if (dist < minDist) {
                minDist = dist;
                nodeTitle = node.querySelector('title').textContent;
            }
        }

        const titleText = svg.append("text")
            .attr("x", x)
            .attr("y", y)
            .attr("class", "quorum-hash")
            .attr("text-anchor", "middle")
            .attr("dominant-baseline", "middle")
            .style("opacity", 0)
            .style("font-size", "18px")
            .style("font-weight", "bold")
            .style("fill", "#FD9A6A")
            .text(nodeTitle);

            titleText.transition()
            .duration(2000)  // Increased duration
            .style("opacity", 1)
            .end()
            .then(() => {
                titleText.transition()
                        .duration(2000)
                        .style("opacity", 0)
                        .end()
                        .then(() => {
                            titleText.remove(); // remove the text element after it fades out
        
                            d3.select(block)
                                .style("fill", "#FD9A6A") // set fill color
                                .style("filter", "drop-shadow(0 0 2px #111111)") // Apply glow effect
                                .transition()
                                .duration(2000)
                                .end()
                                .then(() => {
                                    const quorumBlocks = d3.selectAll(".quorum-block.block").nodes();
                                    if (block === quorumBlocks[quorumBlocks.length - 1]) {
                                        rotateBlocks(quorumBlocks);
                                    }
                                });
                            }); 
            });
    }

    function rotateBlocks(quorumBlocks) {
        const coords = quorumBlocks.map(block => ({
            x: +block.getAttribute("x"),
            y: +block.getAttribute("y")
        }));

        quorumBlocks.forEach((block, i) => {
            const nextPos = coords[(i + 1) % coords.length];

            d3.select(block)
                .transition()
                .duration(3000)
                .attrTween("x", function () {
                    const i = d3.interpolate(+this.getAttribute("x"), nextPos.x);
                    return function (t) { return i(t); };
                })
                .attrTween("y", function () {
                    const i = d3.interpolate(+this.getAttribute("y"), nextPos.y);
                    return function (t) { return i(t); };
                })
                .end()
                .then(() => {
                    if (i === quorumBlocks.length - 1) rotateBlocks(quorumBlocks);  // Recursive call to continue rotation
                });
        });
    }
    performBlockVote();

}
