window.onload = function() {
  setTimeout(function() {
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
        const blockNumber = Number(urlParams.get('block'));

        // Function to animate the hash value moving through the generator
        function animateHashThroughGenerator() {
          var leftMostBlock = document.querySelector('#blocks-svg').lastElementChild;
          var rect = leftMostBlock.getBoundingClientRect();

          var hashElement = document.createElement('div');
          hashElement.className = 'hash-element';
          hashElement.style.position = 'absolute';
          hashElement.style.left = `${rect.left}px`;
          hashElement.style.top = `${rect.top - rect.height - 110}px`; // Adjust the top position as needed
          hashElement.style.width = `${rect.width + 20}px`;
          hashElement.style.height = `${rect.height}px`;
          hashElement.textContent = lastHashLabel;

          document.body.appendChild(hashElement);

          anime({
            targets: textElement,
            opacity: 0,
            duration: 2000,
            easing: 'easeInOutQuad',
            complete: function() {
              hashElement.style.display = 'block';
              generatorElement.style.display = 'block';

              // Animate the hash value moving to the generator's position
              anime({
                targets: hashElement,
                top: `${generatorElement.getBoundingClientRect().top + generatorElement.getBoundingClientRect().height / 2 - hashElement.getBoundingClientRect().height / 2}px`,
                left: `${generatorElement.getBoundingClientRect().left + generatorElement.getBoundingClientRect().width / 2 - hashElement.getBoundingClientRect().width / 2}px`,
                duration: 2000,
                easing: 'easeInOutQuad',
                complete: function() {
                  // Create HTML element for the generator
                  var generatorSvg = document.createElement('div');
                  generatorSvg.className = 'generator-svg';
                  generatorSvg.style.position = 'absolute';
                  generatorSvg.style.left = `${hashElement.getBoundingClientRect().right + 20}px`; // Adjust the left position as needed
                  generatorSvg.style.top = `${hashElement.getBoundingClientRect().top}px`;
                  generatorSvg.style.width = '100px';
                  generatorSvg.style.height = '100px';
                  generatorSvg.style.border = "2px solid black";
                  generatorSvg.textContent = ' Quorum Generator';

                  document.body.appendChild(generatorSvg);

                  // Animate the hash value entering the generator
                 // Animate the hash value moving to the y-center of the generator
                 const generatorXCenter = generatorSvg.getBoundingClientRect().left + generatorSvg.getBoundingClientRect().width / 2 - hashElement.getBoundingClientRect().width / 2;
                  const generatorYCenter = generatorSvg.getBoundingClientRect().top + generatorSvg.getBoundingClientRect().height / 2 - hashElement.getBoundingClientRect().height / 2;

                  anime({
                    targets: hashElement,
                    top: `${generatorYCenter}px`,
                    left: `${generatorXCenter}px`,
                    duration: 2000,
                    easing: 'easeInOutQuad',
                    complete: function() {
                      // Hide the hash value
                      hashElement.style.display = 'none';

                      // Get the quorum members for the selected block
                      var qMembers = blocks[blockNumber].quorum;

                      var qMemberElement; // Define the variable outside the loop

                      // Create a timeline
                      var timeline = anime.timeline();

                      // Calculate the y-center of the generatorSvg for quorum members to come out from there
                      // Calculate the x and y-center of the generatorSvg for quorum members to come out from there
                      const generatorXCenter = generatorSvg.getBoundingClientRect().left + generatorSvg.getBoundingClientRect().width / 2;
                      const generatorYCenter = generatorSvg.getBoundingClientRect().top + generatorSvg.getBoundingClientRect().height / 2 - 15;

                      // Add animations for each quorum member to the timeline
                      for (var i = 0; i < qMembers.length; i++) {
                        qMemberElement = document.createElement('div');
                        qMemberElement.className = 'qMember';
                        qMemberElement.style.position = 'absolute';
                        qMemberElement.style.left = `${generatorXCenter + 15}px`; // Quorum member will start slightly to the right of the generator
                        qMemberElement.style.top = `${generatorYCenter}px`; // Quorum member will start from the y-center of the generator
                        qMemberElement.style.opacity = 0;
                        qMemberElement.textContent = qMembers[i];

                        document.body.appendChild(qMemberElement);

                        timeline.add({
                          targets: qMemberElement,
                          opacity: [0, 1],
                          translateX: [0, 100 * (i + 1)], // Push each quorum member out to the right
                          duration: 1000,
                          easing: 'easeInOutQuad'
                        });
                      }
                      timeline.add({
                        targets: '.information, .step-buttons', // Target the information div and the button
                        opacity: [0, 1], // Animate the opacity from 0 to 1
                        duration: 1000, // Duration of the animation in milliseconds
                        easing: 'easeInOutQuad', // Easing function for the animation
                        begin: function() {
                          // Update the information div
                          var infoDiv = document.querySelector('.information');
                          infoDiv.innerHTML = '<br><br><br><br><br>This animation demonstrates how the hash value is processed by the generator to produce the quorum members.<br><br>The hash value of the last block is used as a seed in a random number generator between all nodes.<br><br>This ensures that whilst the quorum is indeed random, each node generates the same quorum members to construct the next block.<br>Now the next step is to form the quorum connections, so press the button when ready!';
                          
                          var button = document.createElement("button");
                          button.className = "step-buttons"; 
                          button.id = "form-quorum";
                          button.textContent = 'Form Quorum'; 
                          button.addEventListener("click", () => formQuorum(qMembers));

                  
                          infoDiv.appendChild(button);
                  
                          // Color the quorum nodes
                          colorQuorumNodes(qMembers);
                        }
                      });
                      }

                  });
                  // This is the function that colors the quorum nodes
                  function colorQuorumNodes(quorumMembers) {
                    // Select all nodes
                    var nodes = d3.selectAll(".nodes circle");
                  
                    quorumMembers.forEach(member => {
                      // Find the corresponding node
                      nodes.each(function() {
                        // Get the id from the title element
                        var id = this.querySelector('title').textContent;
                  
                        if (id === member) {
                          d3.select(this)
                            .transition()  // Start a transition
                            .duration(1000)  // Transition over 1000ms
                            .attr("fill", "#3A86FF");  // Change the fill color to blue
                        }
                      });
                    });
                  }
                  
                }
              });
            }
          });
        }

        // Attach the animateHashThroughGenerator function to the "derive quorum" button click event
        document.getElementById("derive-quorum").addEventListener("click", animateHashThroughGenerator);
      });
  }, 1000); // 3000 milliseconds = 3 seconds
};
