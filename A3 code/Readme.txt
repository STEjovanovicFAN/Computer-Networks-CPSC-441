This program has two parts:
    -The server
    -The client 

This program sends an image from the client to the server that handels packet loss.

Start the server first by:
    -type "java -Djava.util.logging.config.file=logging.properties -jar ffserver.jar 2225 10 0.1" to start the server

Client start by:
    -compile 'javac -cp "a3.jar" *.java'
    -run 'java -cp ".:a3.jar" FastFtp localhost 2225 pic1.jpg 10 1'


