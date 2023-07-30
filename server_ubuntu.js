const express = require('express');
const cors = require('cors'); 
const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

function findBlueChainDirectory(startDir) {
  const blueChainDir = 'BlueVision'; // change this for BlueChain merge 
  const absStartDir = path.resolve(startDir);

  // Check if the current directory is BlueChain directory
  if (path.basename(absStartDir) === blueChainDir) {
    return absStartDir;
  }

  // Initialize dirs as an empty array
  let dirs = [];
  
  try {
    // Get a list of all subdirectories
    dirs = fs.readdirSync(absStartDir, { withFileTypes: true })
      .filter(dirent => dirent.isDirectory())
      .map(dirent => dirent.name);
  } catch (error) {
    // If an error occurred (like EPERM), just continue to the next directory
    console.error(`Error reading directory: ${absStartDir}`);
    console.error(error);
    return null;
  }

  // Look for BlueChain in each subdirectory
  for (let dir of dirs) {
    const found = findBlueChainDirectory(path.join(absStartDir, dir));
    if (found) return found;
  }

  // BlueChain directory not found in this branch
  return null;
}

const userHomeDir = os.homedir();
const pathToBlueChain = findBlueChainDirectory(userHomeDir);

if (pathToBlueChain) {
  console.log(`Path to BlueChain: ${pathToBlueChain}`);
} else {
  console.log('Error: BlueChain directory not found.');
}


const app = express();

app.use(cors());
app.use(express.json());

app.post('/run-script', (req, res) => {
  const numNodes = req.body.numNodes;
  exec(`gnome-terminal -- bash -c "cd cd ${pathToBlueChain} && ./updateConfig.sh ${numNodes}"`, (error, stdout, stderr) => {
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
    exec(`gnome-terminal -- bash -c "cd cd ${pathToBlueChain} && ./startNetwork.sh; exec bash"`, (error, stdout, stderr) => {
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
    exec(`gnome-terminal -- bash -c "cd cd ${pathToBlueChain} && ./startVis.sh; exec bash"`, (error, stdout, stderr) => {
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
    exec(`gnome-terminal -- bash -c "cd cd ${pathToBlueChain} && ./simulateWallets.sh; exec bash"`, (error, stdout, stderr) => {
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
