For assignment 4 there are 2 parts:
    -Server
    -Router

In order to run the program you will need to run the server first.

Server Instructions:
    - type "java -Djava.util.logging.config.file=logging.properties -jar rserver.jar 8887 topology.txt 0.1" to start the server 

Router instructions:
    -compile with 'jac -cp "a4.jar" *.java'
    -run with 'java -cp ".:a4.jar" Router 3 localhost 8887 10000'

How this program works is different routers interact and try to find the shortest distance between them. The text file in the server files contains the distance between routers which the server runs and interacts with the routers.

This program requires more then 1 router open up new terminals and type:
    - java -cp ".:a4.jar" Router 1 localhost 8887 10000
    - java -cp ".:a4.jar" Router 2 localhost 8887 10000
    - java -cp ".:a4.jar" Router 3 localhost 8887 10000
    - java -cp ".:a4.jar" Router 4 localhost 8887 10000
to make 4 routers for this program.

You can also type "update" in the server while it's running to update the distance vectors in the routers, the routers will then find the shortest distance to each router when you type "quit" in the server code.

