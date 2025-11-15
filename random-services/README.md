# Random Services

This project contains multiple microservices that generate a random number between 1 and 100. Each service will have no ingress except from another application called `getting-random`. The results will be store in GCS and used to train a ML model that will be used to try to predict outputs for future runs.

## Services Overview

1. **Golang Random Service**
   - **Location**: `golang-random/`
   - **Description**: A Go application that sets up an HTTP server to respond to GET requests with a random number.


2. **JavaScript Random Service**
   - **Location**: `js-random/`
   - **Description**: A Node.js application using Express to handle GET requests and return a random number.


3. **Python Random Service**
   - **Location**: `python-random/`
   - **Description**: A Python application using Flask to serve GET requests and provide a random number.

4. **Getting Random Application**

- **Location**: `getting-random/`
- **Description**: This application interacts with the random number services and is the only service allowed to make requests to them. Outputs of this will be stored in GCS


