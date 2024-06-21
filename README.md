# BlueChain

<p align="center">
  <img src="https://github.com/peytonlundquist/network/blob/master/bluechainlogo.png"  width="300" height="300">
</p>


## A distributed, decentralized blockchain network and research framework implemented in Java.

## Description
This network and research framework is being developed for the purpose of student researchers, interested in developing blockchain solutions. We provide an alternate blockchain network option to expirement with, without the burden of high learning curves associated with softwares such as HyperLedger, Ethereum, etc. 

This software is not a public network costing gas fees to expirement with, nor is its implementation complex. With this, researchers or other curious people are invited expirement with and fork our software to meet their use case.

We provide:
  - Easy configurability
  - An understanable code base
  - A Framework for multiple use cases, such as defi or healthcare

This software heavily uses the following concepts in order to achieve a fully distributed, decentralized blockchain network:
  - Servent (Server + Client)
  - Java Sockets + TCP/IP
  - Multi-threading
  - Distributed Systems
  - Decentralization
  - Quorum Consensus
  
BlueChain is not production-grade software, and should not be externally hosted unless proper security has been implemented first.
  - Lack of certain securities
  - Few fail-safe mechanisms
  - Brand-New
  - Short-Lived Network

## How to Use
### Prerequisites
  - Java 11
  - Maven

### Running a Local Network
  1. Navigate to the config.properties file (network/src/main/java/config.properties)
  2. Configure the network to the specifications you desire. 
  
  - **Warning:** The number of nodes your local machine can handle depends on the computing resources that the machine has.
  
  3. Use the startNetwork shell script (navigate back to network/)
    
    ./startNetwork.sh
    
  5. Launch the client associated with the use case defined in the config file

  - To launch the client, run the following command
    
        ./startClient.sh

  - Some use cases will have special clients.
  - The health care use case has a special patient client where the patient can access all of their important information.
  Launch the shell script below to access the patient client.

        ./startPatientClient.sh
    
      
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

    `java -cp target/network-1.0-SNAPSHOT.jar NetworkLauncher -t 60000`