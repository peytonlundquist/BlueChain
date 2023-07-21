window.onload = function() {
  setTimeout(function() {
    var textElement = document.querySelector('.information');
    var generatorElement = document.querySelector('.generator');
    const hashLabels = document.querySelectorAll(".hash-label");
    const lastHashLabel = hashLabels[hashLabels.length - 1].textContent;

    // Function to animate the hash value moving through the generator
    function animateHashThroughGenerator() {
      var leftMostBlock = document.querySelector('#blocks-svg').lastElementChild;
      var rect = leftMostBlock.getBoundingClientRect();

      var hashElement = document.createElement('div');
      hashElement.className = 'hash-value';
      hashElement.style.position = 'absolute';
      hashElement.style.left = `${rect.left}px`;
      hashElement.style.top = `${rect.top - rect.height - 110}px`; // Adjust the top position as needed
      hashElement.style.width = `${rect.width + 20}px`;
      hashElement.style.height = `${rect.height}px`;
      hashElement.style.border = "2px solid black";
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
            top: `${generatorElement.getBoundingClientRect().top}px`,
            left: `${generatorElement.getBoundingClientRect().left}px`,
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
              generatorSvg.textContent = 'Generator';

              document.body.appendChild(generatorSvg);

              // Animate the hash value entering the generator
              anime({
                targets: hashElement,
                left: `${generatorSvg.getBoundingClientRect().left}px`,
                duration: 2000,
                easing: 'easeInOutQuad',
                complete: function() {
                  // Hide the hash value
                  hashElement.style.display = 'none';

                  // Create HTML elements for the quorum members
                  var qMembers = ["Member 1", "Member 2", "Member 3"];
                  for (var i = 0; i < qMembers.length; i++) {
                    var qMemberElement = document.createElement('div');
                    qMemberElement.className = 'qMember';
                    qMemberElement.style.position = 'absolute';
                    qMemberElement.style.left = `${generatorSvg.getBoundingClientRect().right + i * 100}px`; // Adjust the left position as needed
                    qMemberElement.style.top = `${generatorSvg.getBoundingClientRect().top}px`;
                    qMemberElement.style.opacity = 0;
                    qMemberElement.textContent = qMembers[i];

                    document.body.appendChild(qMemberElement);

                    // Animate the quorum members coming out of the generator
                    anime({
                      targets: qMemberElement,
                      opacity: [0, 1],
                      translateX: [0, 100],
                      delay: i * 1000, // Delay each animation by 1 second
                      duration: 2000,
                      easing: 'easeInOutQuad'
                    });
                  }
                }
              });
            }
          });
        }
      });
    }

    // Attach the animateHashThroughGenerator function to the "derive quorum" button click event
    document.getElementById("derive-quorum").addEventListener("click", animateHashThroughGenerator);
  }, 1000); // 3000 milliseconds = 3 seconds
};
