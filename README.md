# **BlueChain**
  - A java-implemented distributed, decentralized blockchain network

## Description
This network is being developed for the purpose of student researchers, interested in developing blockchain solutions. We provide an alternate blockchain network option to expirement with, without the burden of high learning curves associated with softwares such as HyperLedger, Ethereum, etc. 

This software is not a public network costing gas fees to expirement with, nor is its implementation complex. With this, researchers or other curious people are invited expirement with and fork our software meet their use case.

## How to Use
### Prerequisites
  - Java 11
  - Maven

### Running a Local Network
  1. Navigate to the config.properties file (network/src/main/java/config.properties)
  2. Configure the network to the specifications you desire. 
  
  - **Warning:** The number of nodes your local machine can handle depends on the computing resources that the machine has. In addition, **networks over the size of 100 nodes may be subject to port exhaustion** depending on how many transcations are submitted in small time intervals. **Unexpected exceptions may occur as a result.**
  
  3. Use Maven to compile and run the NetworkLauncher (navigate back to network/)
  
    mvn clean install
    java -cp target/network-1.0-SNAPSHOT.jar NetworkLauncher
  4. Begin submitting transactions in new terminal
   - The transactions are arbitrary in content as of this version. 
   - The Client application transaction args: Client trans <portNum> <transactionContent>
   - The Client as of now simply submits a transaction to an arbitrarily specified nodewhose content is an input string. That node gossips the transaction to the rest of the network
  
    java -cp target/network-1.0-SNAPSHOT.jar Client trans 8000 1
    
### Running a cross-host Network
  For steps 1 and 2, please refer to the 'Running a Local Netork' section's steps 1 and 2
  
  3. Use Maven to compile the NetworkLauncher (navigate back to network/)
  
    mvn clean install
  
  4. Running the NetworkLauncher

  - For this step, since we plan to run our network across multiple hosts, please ensure that the networking and firewall permissions / setting allow TCP/IP connections from the ports and hosts you will specify
  - Using the NetworkLauncher java program in this context works by running the program on each host you plan on using. 
  - Every node on each host is required to submit it's public key to the Node Registry, a directory intended to be shared across hosts. This allows each node in the network to properly use DSA with one another in addition to local peer discovery.
  - It is suggested to specify a '-t <timedWaitDelayMilliseconds> for each program in order for the networks to bind but wait to connect until you have every host setup
  - An example for our first host to run

    `java -cp target/network-1.0-SNAPSHOT.jar NetworkLauncher`
    
  - In this example we specified our host needs to wait 60 seconds before trying to connect to any nodes. We then gave it the port ranges and host names of the other nodes that we will want to connect with.
  - We would run a similar argument for each other host with slightly different ordering of the arguments
