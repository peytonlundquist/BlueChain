window.onload = function() {
    setTimeout(function() {
      var textElement = document.querySelector('.information');
      var generatorElement = document.querySelector('.generator');
      const hashLabels = document.querySelectorAll(".hash-label");
      const lastHashLabel = hashLabels[hashLabels.length - 1].textContent;
  
      var infoElement = document.querySelector('.information');
  
      document.getElementById("derive-quorum").addEventListener("click", function() {
        var leftMostBlock = document.querySelector('#blocks-svg').lastElementChild;
        var rect = leftMostBlock.getBoundingClientRect();
  
        var hashElement = document.createElement('div');
        hashElement.className = 'hash-value';
        hashElement.style.position = 'absolute';
        hashElement.style.left = `${rect.left}px`;
        hashElement.style.top = `${rect.top - rect.height - 110}px`; // Adjust the top position as needed
        hashElement.style.width = `${rect.width + 20}px`;
        hashElement.style.height = `${rect.height}px`;
        hashElement.style.border = "2px solid back"; 
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
              anime({
                targets: hashElement,
                left: `${generatorElement.getBoundingClientRect().left}px`,
                top: `${generatorElement.getBoundingClientRect().top}px`,
                duration: 2000,
                easing: 'easeInOutQuad',
                complete: function() {
                  // Code to show the quorum members goes here
                }
              });
          }
        });
      });
    }, 1000); // 3000 milliseconds = 3 seconds
  };
  