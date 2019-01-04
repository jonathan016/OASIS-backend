# OASIS-backend
Back-end side of OASIS, Blibli.com FUTURE 3.0 Phase 1 project. Developed using Spring MVC 5

### Content
* [Related Repositories](#related-repositories)
* [Requirements](#requirements)
* [How to Run](#how-to-run)
* [TODO](#todo)

### Related Repositories
* Front End Repository
    - [https://github.com/stelli98/OASIS-frontend](https://github.com/stelli98/OASIS-frontend)

* Documentation Repository
    - [https://github.com/jonathan016/future-OASIS](https://github.com/jonathan016/future-OASIS) (To be migrated to this repository soon)

### Requirements
* [JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (Preferred)
* [Maven](https://www.mkyong.com/maven/how-to-install-maven-in-windows/)
* [Tomcat Server 9.0](https://tomcat.apache.org/download-90.cgi)
* [MongoDB Version 4.0.0](https://www.mongodb.com/download-center)
* [Redis 3.2.100](https://github.com/MicrosoftArchive/redis/releases/download/win-3.2.100/Redis-x64-3.2.100.zip)

### How to Run
- Install the requirements first
- Run command prompt (Windows)
- Clone the repository with the following command:
   ```
   git clone git@github.com:jonathan016/OASIS-backend.git
   ```
- Change directory to OASIS-backend using command:
    ```
    cd OASIS-backend
    ```
- Do a clean install by typing
    ```
    mvn clean install
    ```
- While waiting for the clean install to finish, unzip Redis anywhere you want
- Go to the extracted folder and run `redis-server.exe`
- Next, preparing MongoDB, you can copy the following configuration for MongoDB and store them in config.cfg at the MongoDB>Server>4.0>bin directory
    ``` 
    storage:
     dbPath: "C:/oasis"
    net:
     bindIp: 127.0.0.1
     port: 27017
    ```
- On the very same directory, run command prompt and type
    `mongod -f config.cfg`, this will run the database using the specified configuration
- Now go back to `OASIS-backend` directory, and find a file with `.war` extension
- Copy the `.war` file to wherever you installed Tomcat Server 9.0. Find the `webapps` folder, and put the war in there
- Now go to `conf` folder in Tomcat, and find `server.xml` file
- Around line 60-ish to 70-ish, you should see something like this
    ```xml
    <Connector port="8080" protocol="HTTP/1.1"
                   connectionTimeout="20000"
                   redirectPort="8443" />
    ```  
- Replace the port number `8080` with `8085` as our program uses the latter port in Tomcat Server
- Run the tomcat server
- It's done! You can run the sample using Postman or curl by accessing [http://localhost:8085/oasis/api/login](http://localhost:8085/oasis/api/login)
    
### TODO
* Add more unit tests
* Refactor code to follow design patterns