var sim;
var svg;

var nodeReg = [];
var count = 0;
var mytnode;
var opacity=0.05;
var nrad=3;
var nodedist=10;

var nodeList = [
  {id:1, r:5, x:300,y:300},
  {id:2, r:10, x:120,y:120},
  {id:3, r:15, x:240, y:120},
  {id:4, r:20,x:0,y:0}
];

var linksArray = [
  {source:0, target: 1, dist:10},
  {source:1, target: 2, dist:10},
  {source:1, target: 3, dist:10}
];

function drawGraph()
{
svg = d3.select("#vizSVG");
var width=1280;
var height=960;
console.log(svg);

     var rect = svg.append("rect")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", width)
        .attr("height", height)
        .attr('fill', '#ccc')

      rect.on("click", add, true)

      var textnode=document.createElementNS("http://www.w3.org/2000/svg","text");
      textnode.setAttributeNS(null,"x","200");
      textnode.setAttributeNS(null,"y","200");
      textnode.setAttributeNS(null,"fill","red");
      mytnode = document.createTextNode("Nodes:");
      textnode.appendChild(mytnode);
      mytnode.nodeValue="CHanged";
      
      document.getElementById("vizSVG").appendChild(textnode);
      console.log(textnode);
              
       


/*
svg.append("circle")
  .attr("cx", 2).attr("cy", 2).attr("r", 40).style("fill", "blue");
svg.append("circle")
  .attr("cx", 640).attr("cy", 480).attr("r", 40).style("fill", "red");
svg.append("circle")
  .attr("cx", 0).attr("cy", 480).attr("r", 40).style("fill", "green");
*/




   sim = d3.forceSimulation(nodeList)
  .force("link", d3.forceLink(linksArray)
                  .distance(function(l){
                    return l.dist;
                  })
                  .strength(1)
  )
  .force("charge", d3.forceManyBody().strength(-400))
  .force('center', d3.forceCenter(width/2,height/2))
  .force("collide", d3.forceCollide(10))
  .on("tick",ticked);


//  .force("x", d3.forceX(width/2).strength(0.02))
//  .force("y", d3.forceY(height/2).strength(0.02))

//sim.tick(1);
//sim.start();

var lines = svg.append("g").selectAll("line").data(linksArray);
var circles = svg.append("g").selectAll("circle").data(nodeList);

function ticked() {
    

     //console.log("Ticking");
    c = svg.selectAll('circle')
    //console.log(c);
    c.data(nodeList)
    .join('circle')
    .attr('r', function(d) { return d.r;})
    .attr('cx', function(d) {
      return d.x
    })
    .attr('cy', function(d) {
      return d.y
    }).style('fill','green');

   tl = svg.selectAll('line');
   tl.attr("x1", function(d) { return d.source.x; })
  .attr("y1", function(d) { return d.source.y; })
  .attr("x2", function(d) { return d.target.x; })
  .attr("y2", function(d) { return d.target.y; })
  .style("stroke", "black")
  .style("stroke-opacity", opacity)


  }

lines.enter()
  .append("line")
  .attr("x1", function(d) { return d.source.x; })
  .attr("y1", function(d) { return d.source.y; })
  .attr("x2", function(d) { return d.target.x; })
  .attr("y2", function(d) { return d.target.y; })
  .style("stroke", "black")
  .style("stroke-opacity", opacity);

circles.enter()
  .append("circle")
  .attr("cx", function(d) { return d.x; })
  .attr("cy", function(d) { return d.y; })
  .attr("r", function(d) { return d.r; });
}

function add() {
//  console.log("clicked");
//  console.log("nodes");
  console.log(nodeList.length);
  var n = {
          x: 100,
          y: 100, r: 2
        }


        var l = {
          index: linksArray.length,
          source: n,
          target: nodeList[Math.floor(nodeList.length * Math.random())]
        }
        linksArray.push(l);
        nodeList.push(n);
        sim.nodes(nodeList);
        sim.force("link", d3.forceLink(linksArray).distance(nodedist).strength(1.0))
        sim.alpha(1);
        sim.restart();
        reDraw();
}


    function reDraw() {
        var update_nodes = svg.selectAll("circle")
          .data(nodeList);
        update_nodes.exit().remove();
        nodes = update_nodes.enter()
          .append("circle")
          .attr("cx", 100)
          .attr("cy", 300)
          .attr("r", 25)
          .merge(update_nodes);

        var update_links = svg.selectAll("line")
          .data(linksArray);
        update_links.exit().remove()
        links = update_links.enter()
          .append("line")
          .attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; })
          .style("stroke", "black")
          .style("stroke-opacity", opacity)
          .merge(update_links)
      }


// start processing events from the web server.
function StartContent()
{
  console.log("initialize");
  launchEvents();
}


// This code processes received events and draws
// the required shapes on the board
// wait on events from the Web Server
function launchEvents(){
   if(typeof(EventSource)!=="undefined")
   {
        //create the source for updating event sources
        var eSource = new EventSource("blockchain.php");

        //detect message receipt
        eSource.onmessage = function(event)
        {
          //write the received data to the page
          //document.getElementById("serverData").innerHTML = event.data;
          //console.log(event.data);
          //console.log("next");
          var svgcode = JSON.parse(event.data);
          console.log(svgcode)
          //processBlock(svgcode);
          processBlock(svgcode);
        };
   }
   else
   {
     document.getElementById("serverData").innerHTML="Whoops! Your browser doesn't receive server-sent events.";
   }
}

function addNode(blockid,xi,yi)
{
//  console.log("clicked");
//  console.log("nodes");
  var n = { id: blockid, x: xi, y: yi, r: nrad };
  nodeList.push(n);
  return nodeList.length-1;
}

function newBlock(aid,srid)
{
   newblock = { "type": "node", "id": aid, "quorum": "no", "targets": [srid]};
   return(newblock);
}


// have recevied a blockchain object
// check if we need to update the registry with a new object
function processBlock(blockobj)
{
  // first we add or update a node
  // if node already exists update its data as needed
  if (blockobj.id in nodeReg)
  {
      //console.log("Node Again");
      //console.log(blockobj.id);
      // get this object out of the registry.
      var targs = blockobj.targets;
      blockobj = nodeReg[blockobj.id];
      blockobj.targets = targs;
  }
  else // add the new node to the registry and nodesList
  {
    // first we add the node
    // this is a new node update the graph.
    console.log("New Node");
    // in here we might calculate an initial x,y position.
    var mypos = addNode(blockobj.id,200,200);
    blockobj.position = mypos;
    nodeReg[blockobj.id] = blockobj;
  }


  // now we conect it to it's targets
  for (tar in blockobj.targets )
  {
      console.log(blockobj.targets[tar]);
      nid = blockobj.targets[tar];
      //console.log(nid);
      if (nid in nodeReg)
      {
          targc = nodeReg[nid];
          // we should add a connection edge.
          //console.log(blockobj);
          //console.log("target present "+nid);
           var l = {
              index: linksArray.length,
              source: blockobj.position,
              target: nodeReg[nid].position
           }
           //console.log(l);
           linksArray.push(l);
      }
      else
      {
           ablockobj = newBlock(nid,blockobj.id);
           //console.log(ablockobj);
           //  here we could calculate an x,y position.
           var mypos = addNode(blockobj.id,550,50);
           ablockobj.position = mypos;
           nodeReg[ablockobj.id] = ablockobj;

           var l = {
              index: linksArray.length,
              source: blockobj.position,
              target: mypos
           }
           //console.log("Add Link");
           //console.log(l);
           linksArray.push(l);
      }
  }
  
  if ((count %50) == 0)
  {
  sim.nodes(nodeList);
  sim.force("link", d3.forceLink(linksArray).distance(nodedist).strength(1.0))
  sim.alpha(1);
  sim.restart();
  reDraw();
  }
  count = count + 1;
  mytnode.nodeValue= nodeList.length.toString() + ":" + count.toString() +":" + linksArray.length.toString();
}