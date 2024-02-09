
GWeb  
====

# Introduction

GWeb is a very simple template for Java-based Web services revolving around the execution of database-based jobs.
It also, supports a simple user and rights management. Logged in users may create jobs an put them in an execution queue.
From there, they will eventually be executed. (Execution itself is not implemented though.)

The template lends itself well for any kind of simple and clean approach to create REST-services using Maven, JAXRS, Servlets, JDBC but not much else. In particular, I tried to avoid any kind of "annoying" frameworks, reflection and so on. Code injection is kept to a minimum and just linked JAXRS. There are a few JUnit tests that test the services underneath the REST layer.

Feel free to grab this code and change and use it for your own needs.

# License

[GWeb is free for commercial and non-commercial use.](./LICENSE.txt)

