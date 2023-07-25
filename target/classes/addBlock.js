
function addBlock() {
    
    stopAnimation();
    d3.selectAll("path").interrupt();
    d3.selectAll("path").transition().duration(3000).style("opacity",0).remove();

    d3.selectAll("circle").attr("fill", "#222");

    const urlParams = new URLSearchParams(window.location.search);
    var blockNumber = Number(urlParams.get('block'));
    const infoDiv = d3.select('.information')
        .style("opacity", 0)
        .html("Now each node has added the block to their ledger!<br><br>The quorum also disbands and returns to their normal functionality as a full node.<br><br>You can see the block added in at the bottom of the page.<br><br>This is now the new ledger that the nodes in the network are keeping and maintaining.<br><br>You can click the dropdown menu to go back to the live page, or you can press 'Derive Quorum' to continue with the consensus process for the next block.")
        .transition()
        .duration(3000)
        .style("opacity", 1)
        .end()
        .then(() => {
            const button = d3.select('.information').append('button')
                .text('Derive Quorum')
                .attr('class', 'step-buttons')
                .attr('id', 'derive-quorum')
                .style('opacity', 0)
                .on('click', () => deriveQuorum())
                .style('transition', 'opacity 2s');

            setTimeout(() => {
                button.style('opacity', 1);
            }, 100);
        });
          var textElement = document.querySelector('.information');
          var generatorElement = document.querySelector('.generator');
          const hashLabels = document.querySelectorAll(".hash-label");
          const lastHashLabel = hashLabels[hashLabels.length - 1].textContent;
      
          // Fetch the network.ndjson file
          fetch('network.ndjson')
            .then(response => response.text())
            .then(data => {
              // Parse the NDJSON data
              const blocks = data.split('\n').filter(Boolean).map(line => JSON.parse(line));
      
              // Get the block number from the URL parameters
              const urlParams = new URLSearchParams(window.location.search);
              var blockNumber = Number(urlParams.get('block'));

              var currentURL = window.location.href; 
                
                var newURL = currentURL.replace(`block=${blockNumber}`, `block=${blockNumber +1}`);

                blockNumber = blockNumber + 1;

            history.pushState({}, '', newURL);

            var svgBlocks = d3.select("#blocks-svg");

            // Set the dimensions of the svg element for blocks
            var widthBlocks = window.innerWidth;
            var heightBlocks = 200; // adjust this as needed
            
            svgBlocks.attr("width", widthBlocks).attr("height", heightBlocks);
            
            // Set the dimensions of each block
            var blockSize = 125;
            var blockPadding = 20;
            
            // Initialize the index for blockData
            var index = 0;
            
            // Function to add a new block
            function addNewBlock(blockData) {
              // Animate the transition of the blocks
              animateBlocks();
            
              // Store block number 
              var blockNum = blockData[index].block; 
            
              // Append a new line representing the chain
              svgBlocks.append("line")
                .attr("class", "chain")
                .attr("x1", -blockSize)
                .attr("y1", (heightBlocks - blockSize) - 15 + blockSize / 2) // Center of block
                .attr("x2", -blockSize)
                .attr("y2", (heightBlocks - blockSize) - 15 + blockSize / 2) // Center of block
                .attr("stroke", "black")
                .attr("stroke-width", 2)
                .transition()
                .duration(1000)
                .attr("x2", 0); // End line where new block begins
            
              // Append a new block
              svgBlocks.append("rect")
                .attr("class", "block")
                .attr("x", -blockSize)
                .attr("y", (heightBlocks - blockSize) - 15)
                .attr("width", blockSize)
                .attr("height", blockSize)
                .on("click", function(d) {
                  window.location.href = "consensus.html?block=" + blockNum; 
                })
                .transition()
                .duration(1000)
                .attr("x", 0);
            
              // Append a new block label
              svgBlocks.append("text")
                .attr("class", "block-label")
                .attr("x", -blockSize / 2)
                .attr("y" , (heightBlocks - blockSize / 1.25) -15)
                .attr("text-anchor", "middle")
                .attr("fill", "black")
                .text("block " + blockData[index].block)
                .transition()
                .duration(1000)
                .attr("x", blockSize / 2);
            
              svgBlocks.append("text")
                .attr("class","hash-label")
                .attr("x",-blockSize/2)
                .attr("y", (heightBlocks-blockSize/2) -15) 
                .attr("text-anchor","middle")
                .attr("fill","black")
                .text("hash " + (blockData[index].hash).substring(0,4))
                .transition()
                .duration(1000)
                .attr("x",blockSize/2);
            
              svgBlocks.append("text")
                .attr("class","tx-label")
                .attr("x",-blockSize/2)
                .attr("y", (heightBlocks-blockSize/5) - 15) 
                .attr("text-anchor","middle")
                .attr("fill","black")
                .text(blockData[index].tx_count + " txs")
                .transition()
                .duration(1000)
                .attr("x",blockSize/2);
            
              // Increment index
              index++;
            }
            
            // Animate the transition of the blocks
            function animateBlocks() {
              svgBlocks.selectAll(".block")
                .transition()
                .duration(1000)
                .attr("x", function(d, i) {
                  return (blockSize + blockPadding) * (index - i);
                });
            
              svgBlocks.selectAll(".block-label")
                .transition()
                .duration(1000)
                .attr("x", function(d, i) {
                  return (blockSize + blockPadding) * (index - i) + blockSize / 2;
                });
            
              svgBlocks.selectAll(".hash-label")
                .transition()
                .duration(1000)
                .attr("x", function(d, i) {
                  return (blockSize + blockPadding) * (index - i) + blockSize / 2;
                });
            
              svgBlocks.selectAll(".tx-label")
                .transition()
                .duration(1000)
                .attr("x", function(d, i) {
                  return (blockSize + blockPadding) * (index - i) + blockSize / 2;
                });
            
              svgBlocks.selectAll(".chain")
                .transition()
                .duration(1000)
                .attr("x1", function(d, i) {
                  return (blockSize + blockPadding) * (index - i);
                })
                .attr("x2", function(d, i) {
                  return (blockSize + blockPadding) * (index - i) - blockSize;
                });
            }
            function inQuorum(members,blockTitle) {
              if(members.includes(blockTitle)) {
                  return true; 
              } return false; 
            }
            
            
            // Initial data load
            d3.text("network.ndjson").then(function(text) {
              blockData = text.split("\n").filter(Boolean).map(JSON.parse);
             
                  var block = blockData[blockNumber];
                  
                  
                  
            
              // Add the initial blocks
              for (var i = 0; i <= blockNumber; i++) {
                addNewBlock(blockData);
              }
            
              // Start updating the data every 3 second
            }).catch(function(error) {
              console.log("Error loading data:", error);
            });
        });
}