const express = require('express');
const cors = require('cors'); 
const { exec } = require('child_process');

const app = express();

app.use(cors());
app.use(express.json());

app.post('/run-script', (req, res) => {
  const numNodes = req.body.numNodes;
  exec(`gnome-terminal -- bash -c "cd /home/jettblack/Documents/BlueChain && ./updateConfig.sh ${numNodes}"`, (error, stdout, stderr) => {
    if (error) {
      console.log(`error: ${error.message}`);
      return;
    }
    if (stderr) {
      console.log(`stderr: ${stderr}`);
      return;
    }
    console.log(`stdout: ${stdout}`);
  });

  setTimeout(() => {
    exec(`gnome-terminal -- bash -c "cd /home/jettblack/Documents/BlueChain && ./startNetwork.sh; exec bash"`, (error, stdout, stderr) => {
      if (error) {
        console.log(`error: ${error.message}`);
        return;
      }
      if (stderr) {
        console.log(`stderr: ${stderr}`);
        return;
      }
      console.log(`stdout: ${stdout}`);
    });
  }, 10000); 

  setTimeout(() => {
    exec(`gnome-terminal -- bash -c "cd /home/jettblack/Documents/BlueChain && ./startVis.sh; exec bash"`, (error, stdout, stderr) => {
      if (error) {
        console.log(`error: ${error.message}`);
        return;
      }
      if (stderr) {
        console.log(`stderr: ${stderr}`);
        return;
      }
      console.log(`stdout: ${stdout}`);
    });
  }, 15000);

  setTimeout(() => {
    exec(`gnome-terminal -- bash -c "cd /home/jettblack/Documents/BlueChain && ./simulateWallets.sh; exec bash"`, (error, stdout, stderr) => {
      if (error) {
        console.log(`error: ${error.message}`);
        return;
      }
      if (stderr) {
        console.log(`stderr: ${stderr}`);
        return;
      }
      console.log(`stdout: ${stdout}`);
    });
  }, 20000);
  
  res.send('Scripts are running.');
});

app.listen(3000, () => {
  console.log('Server is running on port 3000');
});
