function compileMempool() {
    const svg = d3.select("#graph-svg");

    const infoDiv = d3.select('.information')
    .style("opacity", 0)
    .html('So what is the mempool?<br><br>The mempool is where transactions gather in the network after being verified and wait to be placed inside a block.<br><br>Whenever quorum members form, they will send the transactions from their mempool to one another to make sure everyone has the same transactions when constructing the next block for the network. You can see the transactions from the mempool being sent around the quorum members now.<br><br>After they all resolve the mempool, the next step is to construct a block for each member. So press construct block when ready!')
    .transition()
    .duration(3000)
    .style("opacity", 1)
    .end()
    .then(() => {
        const button = d3.select('.information').append('button')
            .text('Construct Block')
            .attr('class', 'step-buttons')
            .attr('id', 'construct-block')
            .style('opacity', 0)
            .on('click', () => constructBlock())
            .style('transition', 'opacity 2s');

        setTimeout(() => {
            button.style('opacity', 1);
        }, 100);
    });

    fetch('network.ndjson')
    .then(response => response.text())
    .then(data => {
        const blocks = data.split('\n').filter(Boolean).map(line => JSON.parse(line));
        const urlParams = new URLSearchParams(window.location.search);
        const blockNumber = Number(urlParams.get('block'));
        const txs = blocks[blockNumber+1].transactions;
        const paths = d3.selectAll("path").nodes();

        const mempools = [];  // Store mempool elements to track their size

        d3.selectAll(".nodes circle")
        .each(function() {
            const node = d3.select(this);
            const cx = +node.attr("cx");
            const cy = +node.attr("cy");
            const mempool = svg.append("g")
                .attr("class", "mempool")
                .attr("transform", `translate(${cx},${cy})`)
                .append("text")
                .attr("dx", 15)
                .attr("dy", 15)
                .text("");
            mempool.transactionCount = 0;  // Initialize a property to track each mempool's transaction count
            mempools.push(mempool);
        });


  


        

        let currentTransactionIndex = 0;
        let currentPathIndex = 0;
    
        // Create an array to track the number of transactions received by each mempool
        const mempoolTransactionCounts = new Array(mempools.length).fill(0);
    
        let transactionInterval = setInterval(() => {
            // Check if all mempools have received the correct number of transactions
            const allMempoolsComplete = mempoolTransactionCounts.every(count => count >= txs.length);
    
            if (allMempoolsComplete) {
                clearInterval(transactionInterval);
                return; // Exit the interval immediately
            }
    
            // Add new transaction if mempool size is less than txs.length for the corresponding mempool
            if (mempoolTransactionCounts[currentPathIndex] < txs.length) {
                animateTransactionText(txs[currentTransactionIndex], paths[currentPathIndex]);
                console.log(txs[currentTransactionIndex]);
    
                mempoolTransactionCounts[currentPathIndex]++; // Increment the transaction count for the current mempool
    
                currentTransactionIndex = (currentTransactionIndex + 1) % txs.length;
                currentPathIndex = (currentPathIndex + 1) % paths.length;
            }
        }, 1000);
    

        function animateTransactionText(transaction, path) {
            const text = svg.append("text")
                .attr("dy", -5)  
                .style("font-size", "14px")
                .style("fill", "black")
                .text(transaction);

            const start = path.getPointAtLength(0);
            text.attr("transform", `translate(${start.x},${start.y})`);

            text.transition()
                .duration(1000)
                .ease(d3.easeLinear)
                .attrTween("transform", function() {
                    const length = path.getTotalLength();
                    const offsetScale = 10; 
                    const ds = 1;
                    return function(t) {
                        const point1 = path.getPointAtLength(Math.max(t * length - ds, 0));
                        const point2 = path.getPointAtLength(Math.min(t * length + ds, length));
                        const tangent = {x: point2.x - point1.x, y: point2.y - point1.y};
                        const perp = {x: -tangent.y, y: tangent.x};
                        const norm = Math.sqrt(perp.x * perp.x + perp.y * perp.y);
                        perp.x /= norm;
                        perp.y /= norm;
                        const textPoint = {x: point2.x + perp.x * offsetScale, y: point2.y + perp.y * offsetScale};
                        return `translate(${textPoint.x},${textPoint.y})`;
                    };
                })
                .on('end', function() {
                    const length = path.getTotalLength();
                    const point = path.getPointAtLength(length);
                    let nearestMempool = null;
                    let minDistance = Infinity;
                
                    svg.selectAll(".mempool").each(function() {
                        const transform = d3.select(this).attr("transform");
                        const match = transform.match(/translate\(([^,]*),([^)]*)\)/);
                        const translate = [parseFloat(match[1]), parseFloat(match[2])];
                        const dx = translate[0] - point.x;
                        const dy = translate[1] - point.y;
                        const distance = dx*dx + dy*dy;
                
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestMempool = d3.select(this);
                        }
                    });

                    text.remove();

                    const oldText = nearestMempool.select("text").text();
                    const newText = oldText + transaction + "\n";
                    nearestMempool.select("text").text(newText);

                    nearestMempool.transactionCount++;
                });
        }
    });
}
