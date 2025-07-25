# distributed-content-searching

Distributed file content searching project

# How to Run the Application

## 1. Run the Bootstrap Server:

Navigate to the bootstrap-server module.
Navigate to the src/main/java directory.
Compile and run the BootstrapServer application using below commands:

- javac BootstrapServer.java
- java BootstrapServer.java

## 2. Run the Node Server Application:

Navigate to the node-server module.
Run the node-server application in docker using below command:

- docker-compose up

Please note that, you have to uncomment nodes in docker-compose.yaml to achieve required number of nodes in the network. Adding multiple nodes will increase resource usage.

## 3. Run the Web Application:

Navigate to the web module.
Build and run the web application in docker using below commands:

- docker build -t web .
- docker run -p 3000:80 -e REACT_APP_API_BASE_URL=http://localhost:5001 web

Login to the UI using http://localhost:3000
