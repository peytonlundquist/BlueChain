# **JDDB Network**
  - A java-implemented distributed, decentralized blockchain network

## Description
This network is being developed for the purpose of student researchers, interested in developing blockchain solutions. We provide an alternate blockchain network option to expirement with, without the burden of high learning curves associated with softwares such as HyperLedger, Ethereum, etc. 

This software is not a public network costing gas fees to expirement with, nor is its implementation complex. With this, the students are invited to change code to meet their use case.

## How to Use
### Prerequisites
  - Java 11
  - Maven

### Running a Local Network
  1. Navigate to the config.properties file (network/src/main/java/config.properties)
  2. Configure the network to the specifications you desire. 
  
  - **Warning:** The number of nodes your local machine can handle depends on the computing resources that the machine has. In addition, networks over the size of 100 nodes may be subject to port exhaustion depending on how many transcations are submitted in small time intervals.
  
  3. Use Maven to compile and run the NetworkLauncher
  
  ```mvn clean install
     java -cp target/network-1.0-SNAPSHOT.jar NetworkLauncher
  ```
  
  4. Begin submitting transactions 
    - The transactions are arbitrary in content as of this version. 
    - The Client application transaction args: Client trans <portNum> <transactionContent>
    - The Client as of now simply submits a transaction to an arbitrarily specified nodewhose content is an input string. That node gossips the transaction to the rest of the network
  
  ```java -cp target/network-1.0-SNAPSHOT.jar Client trans 8000 1```
