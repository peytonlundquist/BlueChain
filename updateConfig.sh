#!/bin/bash

# Get number of nodes as input
NUM_NODES=$1

# Calculate the quorum (square root of NUM_NODES, rounded down)
QUORUM=$(echo "scale=0; sqrt($NUM_NODES)/1" | bc)

# Define other constants
USE=Defi
MINIMUM_TRANSACTIONS=1
STARTING_PORT=8000
MAX_CONNECTIONS=5
MIN_CONNECTIONS=3
DEBUG_LEVEL=1

# Write these values to config.properties
echo "USE=$USE" > src/main/java/config.properties
echo "NUM_NODES=$NUM_NODES" >> src/main/java/config.properties
echo "QUORUM=$QUORUM" >> src/main/java/config.properties
echo "MINIMUM_TRANSACTIONS=$MINIMUM_TRANSACTIONS" >> src/main/java/config.properties
echo "STARTING_PORT=$STARTING_PORT" >> src/main/java/config.properties
echo "MAX_CONNECTIONS=$MAX_CONNECTIONS" >> src/main/java/config.properties
echo "MIN_CONNECTIONS=$MIN_CONNECTIONS" >> src/main/java/config.properties
echo "DEBUG_LEVEL=$DEBUG_LEVEL" >> src/main/java/config.properties
