# Distributed Database System with Sharding and Raft Consensus

This project demonstrates a distributed database system built using Java Spring Boot, MySQL, Docker, simulated sharding and Raft consensus. It includes performance simulations comparing transaction processing with and without sharding and consensus.


## Key Features

- **Sharding:** Partitioned database into multiple shards to boost scalability
- **Raft Consensus:** Simulated Raft consensus algorithm ensuring consistency across nodes
- **Performance Simulation:** Transaction simulator demonstrating metrics for 100K transactions


## Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **MySQL**
- **Docker & Docker Compose**
- **Gradle**
- **HTTP Client** (Java built-in)


## How to Run Locally

### Step 1: Install Dependencies

Install Java, MySQL, Docker, and Docker Compose:

```
brew install openjdk@17 mysql docker docker-compose

```

Set Java environment variables:

```
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v17)' >> ~/.zshrc
source ~/.zshrc
```

Verify Java installation:

```
java -version
```

### Step 2: Configure MySQL Locally

Run MySQL and create the database:

```
mysql -u root -p
CREATE DATABASE distributed_db;
CREATE USER 'yourUser@localhost' IDENTIFIED BY 'yourPassword';
GRANT ALL PRIVILEGES ON distributed_db.* TO 'yourUser@localhost';
FLUSH PRIVILEGES;
```

### Step 3: Configure Properties

Update `application.properties` in `src/main/resources`:

```
server.port=${SERVER_PORT:8084}
raft.node.id=${RAFT_NODE_ID:n1}
raft.peers=${RAFT_PEERS:n1,n2,n3}
spring.application.name=distributeddb
spring.datasource.url=jdbc:mysql://localhost:3306/distributed_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=yourUser
spring.datasource.password=yourPassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
node.urls=${NODE_URLS:http://localhost:8084/orders}
```

### Step 4: Build the Project Jar

Navigate to project root directory and run:

```
./gradlew clean build

```

### Step 5: Run the Application Locally (with simulation)

Run the Spring Boot application:
```
./gradlew bootRun

```


---


## Running with Docker Compose (Multiple Nodes)

### Step 1: Configure Prod Properties

Create `application-prod.properties` in `src/main/resources`:

```
server.port=${SERVER_PORT:8084}
raft.node.id=${RAFT_NODE_ID:n1}
raft.peers=${RAFT_PEERS:n1,n2,n3}
spring.application.name=distributeddb
spring.datasource.url=jdbc:mysql://mysql:3306/distributed_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=yourUser
spring.datasource.password=yourPassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
node.urls=${NODE_URLS:http://node1:8081/orders,http://node2:8082/orders,http://node3:8083/orders}
```

### Step 2: Build the Jar File
```
./gradlew clean build

```

### Step 3: Start Docker Compose Environment

Launch all services (MySQL + nodes):

```
docker-compose up --build

```

This command starts:

- MySQL container on port `3306`
- Node containers (`node1`:`8081`, `node2`:`8082`, `node3`:`8083`)
- Simulator container (`simulator`:`8084`) running simulations


---


**Output Example:**

<img width="700" alt="Screenshot 2025-03-06 at 1 02 00 PM" src="https://github.com/user-attachments/assets/63dcca09-15da-40e5-95bb-4adde74112fc" />


---


## Performance Metrics

| Scenario              | Avg Latency | Throughput  | Success Rate |
|-----------------------|-------------|-------------|--------------|
| With Sharding & Raft  | 14.32 ms    | 6978 tx/sec | 100% ✅      |
| Without Optimization  | 15.84 ms    | 6312 tx/sec | 100% ✅      |


---


## Troubleshooting

- Restart MySQL service before node initialization
- Check port availability (8080-8084)
- Verify environment variables in application.properties
