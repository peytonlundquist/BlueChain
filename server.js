const express = require('express');
const cors = require('cors'); 
const { exec } = require('child_process');

const app = express();

app.use(cors());
app.use(express.json());

app.post('/run-script', (req, res) => {
  const numNodes = req.body.numNodes;
  exec(`osascript -e 'tell app "Terminal" to do script "cd /Users/jettblack/Documents/BlueChain && ./updateConfig.sh ${numNodes}"'`, (error, stdout, stderr) => {
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
    exec(`osascript -e 'tell app "Terminal" to do script "cd /Users/jettblack/Documents/BlueChain && ./startNetwork.sh"'`, (error, stdout, stderr) => {
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
    exec(`osascript -e 'tell app "Terminal" to do script "cd /Users/jettblack/Documents/BlueChain && ./startVis.sh"'`, (error, stdout, stderr) => {
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
    exec(`osascript -e 'tell app "Terminal" to do script "cd /Users/jettblack/Documents/BlueChain && ./simulateWallets.sh"'`, (error, stdout, stderr) => {
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
