# Introduction #

This project demonstrates the usage of a javaagent as a LoggingAgent

# Usage #

* open command prompt / terminal in this folder
* mvn package
* this will create an artifact folder
* java -javaagent:artifact/LoggingAgent-jar-with-dependencies.jar=nl.ivonet.coce.MyMain,nl.ivonet.code.One -jar AgentTest.jar these are all arguments
* should print somehting like:


    12:35:59.125 [main] INFO  nl.ivonet.agent.LoggingAgent - LoggingAgent invoked with args: nl.ivonet.code.MyMain,nl.ivonet.code.One
    12:35:59.137 [main] INFO  nl.ivonet.agent.LoggingTransformer - Found wanted class [nl.ivonet.code.MyMain]
    12:35:59.289 [main] INFO  nl.ivonet.agent.LoggingTransformer - Found wanted class [nl.ivonet.code.One]
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain main
    INFO: >>> main(args=[these, are, all, arguments])
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain <init>
    INFO: >>> MyMain()
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain <init>
    INFO: <<< MyMain()
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain getOne
    INFO: >>> getOne()
    jun 30, 2013 12:35:59 PM nl.ivonet.code.One toString
    INFO: >>> toString()
    jun 30, 2013 12:35:59 PM nl.ivonet.code.One toString
    INFO: <<< toString() returns: I'm One
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain getOne
    INFO: <<< getOne() returns: I'm One
    jun 30, 2013 12:35:59 PM nl.ivonet.code.One toString
    INFO: >>> toString()
    jun 30, 2013 12:35:59 PM nl.ivonet.code.One toString
    INFO: <<< toString() returns: I'm One
    I'm One
    jun 30, 2013 12:35:59 PM nl.ivonet.code.MyMain main
    INFO: <<< main(args=[these, are, all, arguments])

* The AgentTest.jar is a test jar of course. you should be able to do this by adding the agent to your own code


# Description #

The LoggingAgent.jar implements a javaagent and adds logging to specific compiled code without actually having to
recompile that code.

The logging-agent module is the actual javaagent
The Logging-agent-test-project is just a project for testing this agent as described above.

# Todo #

* Add comprehensive unit tests! (sorry it started out as a poc but ended up doing something useful)
* Fix the classpath problems when adding the LoggingAgent to a ServletContainer like tomcat. (see the LoggingTransformer FIXME)
* Make this LoggingAgent configurable through a property file
* Eliminate the JavassistHelper because it is one big codesmell (all static code -> procedural etc)

