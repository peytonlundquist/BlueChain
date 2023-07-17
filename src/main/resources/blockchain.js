// Select the svg element for blocks
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

  // Append a new block
  var newBlock = svgBlocks.append("rect")
    .attr("class", "block")
    .attr("x", -blockSize)
    .attr("y", (heightBlocks - blockSize) - 15)
    .attr("width", blockSize)
    .attr("height", blockSize)
    .transition()
    .duration(1000)
    .attr("x", 0);

  // Append a new block label
  var newBlockLabel = svgBlocks.append("text")
    .attr("class", "block-label")
    .attr("x", -blockSize / 2)
    .attr("y" , (heightBlocks - blockSize / 1.25) -15)
    .attr("text-anchor", "middle")
    .attr("fill", "black")
    .text("block " +blockData[index].block)
    .transition()
    .duration(1000)
    .attr("x", blockSize / 2);

  var newBlockHashLabel = svgBlocks.append("text")
    .attr("class","hash-label")
    .attr("x",-blockSize/2)
    .attr("y", (heightBlocks-blockSize/2) -15) 
    .attr("text-anchor","middle")
    .attr("fill","black")
    .text("hash " + (blockData[index].hash).substring(0,4))
    .transition()
    .duration(1000)
    .attr("x",blockSize/2);

  var newBlockTxLabel = svgBlocks.append("text")
    .attr("class","tx-label")
    .attr("x",-blockSize/2)
    .attr("y", (heightBlocks-blockSize/5) - 15) 
    .attr("text-anchor","middle")
    .attr("fill","black")
    .text(blockData[index].tx_count + " TXNs")
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
}



function inQuorum(members,blockTitle) {
    if(members.includes(blockTitle)) {
        return true; 
    } return false; 
}

function updateData() {
    d3.text("network.ndjson").then(function(text) {
      var updatedBlockData = text.split("\n").filter(Boolean).map(JSON.parse);
      
      var lastBlock = updatedBlockData[updatedBlockData.length - 1];
      var quorumMembers = lastBlock.quorum;
      
      // selects nodes
      var svg = d3.select("svg");
      var nodes = svg.selectAll("circle");
      
      nodes.each(function(d) {
        // fill quorum members red and other nodes gray 
        var nodeColor = quorumMembers.includes(d.id) ? "green" : "grey";
        d3.select(this).attr('fill', nodeColor);

      });

  


      // Update the visualization
      if (updatedBlockData.length > blockData.length) {
        for (var i = blockData.length; i < updatedBlockData.length; i++) {
          addNewBlock(updatedBlockData);
        }
        blockData = updatedBlockData;
      }
    }).catch(function(error) {
      console.log("Error loading data:", error);
    });
  }
  



// Initial data load
d3.text("network.ndjson").then(function(text) {
  blockData = text.split("\n").filter(Boolean).map(JSON.parse);

  // Add the initial blocks
  for (var i = 0; i < blockData.length; i++) {
    addNewBlock(blockData);
  }

  // Start updating the data every 3 seconds
  setInterval(updateData, 3000);
}).catch(function(error) {
  console.log("Error loading data:", error);
});
