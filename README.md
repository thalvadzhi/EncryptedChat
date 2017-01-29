# EncryptedChat
A basic chat application written in java, that uses AES and RSA encryption and a login.

Once a ChatServer is initialized, multiple ChatClients can be started. The "chat room" is public so everyone can see everyone's messages. All messages will be encrypted on the way from the sender client to the server and from the server to the recipient clients. 

## Installation
To generate the jar, go to the root of the repository and run `mvn clean package`. Two jar files will be generated in the `target` folder.

## Usage
* To start the ChatServer run `java -jar JAR_NAME.jar -start_server` where JAR_NAME.jar is the jar with dependecies.

* To start a ChatClient run `java -jar JAR_NAME.jar -start_client -a <ADDRESS>` where JAR_NAME.jar is the jar with dependencies and ADDRESS is the ip addres of the ChatServer(if -a option is not given it defaults to localhost).
