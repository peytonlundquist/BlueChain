// Parse block number from URL
const urlParams = new URLSearchParams(window.location.search);
const blockNum = urlParams.get('block');

// Select the svg element for blocks
var svgBlocks = d3.select("#blocks-svg");

// Set the dimensions of the svg element for blocks
var widthBlocks = window.innerWidth;
var heightBlocks = 200; // adjust this as needed

svgBlocks.attr("width", widthBlocks).attr("height", heightBlocks);

// Set the dimensions of each block
var blockSize = 125;
var blockPadding = 20;

// Function to add a new block
function addNewBlock(blockDataItem, index) {
  // Calculate position based on index
  var position = (blockSize + blockPadding) * (blockNum - index);

  // Append a new block
  var newBlock = svgBlocks.append("rect")
    .attr("class", "block")
    .attr("x", position)
    .attr("y", (heightBlocks - blockSize) - 15)
    .attr("width", blockSize)
    .attr("height", blockSize);

  // Append a new block label
  var newBlockLabel = svgBlocks.append("text")
    .attr("class", "block-label")
    .attr("x", position + blockSize / 2)
    .attr("y" , (heightBlocks - blockSize / 1.25) -15)
    .attr("text-anchor", "middle")
    .attr("fill", "black")
    .text("block " + blockDataItem.block);

  // Append a new block hash label
  var newBlockHashLabel = svgBlocks.append("text")
    .attr("class","hash-label")
    .attr("x", position + blockSize / 2)
    .attr("y", (heightBlocks - blockSize / 2) -15) 
    .attr("text-anchor","middle")
    .attr("fill","black")
    .text("hash " + blockDataItem.hash.substring(0,4));

  // Append a new block transaction label
  var newBlockTxLabel = svgBlocks.append("text")
    .attr("class","tx-label")
    .attr("x", position + blockSize / 2)
    .attr("y", (heightBlocks - blockSize / 5) - 15) 
    .attr("text-anchor","middle")
    .attr("fill","black")
    .text(blockDataItem.tx_count + " txs");
}

// Load data
d3.text("network.ndjson").then(function(text) {
  // Parse data
  var blockData = text.split("\n").filter(Boolean).map(JSON.parse);

  // Add each block up to the block number from URL
  for (let i = 0; i <= blockNum; i++) {
    addNewBlock(blockData[i], i);
  }
}).catch(function(error) {
  console.log("Error loading data:", error);
});
