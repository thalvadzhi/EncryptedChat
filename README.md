# EncryptedChat
A basic chat application written in java, that uses AES and RSA encryption and a login.

## Installation
To generate the jar, go to the root of the repository and run `mvn clean package`. Two jar files will be generated in the `target` folder.

## Usage
* To start the ChatServer run `java -jar JAR_NAME.jar -start_server` where JAR_NAME.jar is the jar with dependecies.

* To start the ChatClient run `java -jar JAR_NAME.jar -start_client -a <ADDRESS>` where JAR_NAME.jar is the jar with dependencies and ADDRESS is the ip addres of the ChatServer(if -a option is not given it defaults to localhost).
