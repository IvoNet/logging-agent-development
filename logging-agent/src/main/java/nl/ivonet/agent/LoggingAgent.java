/*
 * Copyright (c) 2013 by Ivo Wolring (http://ivonet.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.ivonet.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Add Logging to existing code without re-compiling the existing code.
 *
 * Usage example:
 * <pre>java -javaagent:LoggingAgent.jar=org.example.ClassToAddLoggingTo,org.example.AnotherClass -jar YourExecutable
 * .jar [...]</pre>
 *
 * I am declared as a javaagent and get comma separated fully qualified classes as input. (=class,class,class)
 * then you start your own code and see what happends...
 *
 * @author Ivo Woltring
 */
@SuppressWarnings("UtilityClass")
public final class LoggingAgent {

    static final Logger LOG = LoggerFactory.getLogger(LoggingAgent.class);
    private static final Pattern COMPILED_DELIMITER = Pattern.compile(",");

    private LoggingAgent() {
        //can not instantiate a Utility class
    }

    /**
     * JVM hook to statically load the javaagent at startup.
     *
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     */
    @SuppressWarnings("ProhibitedExceptionDeclared")
    public static void premain(final String args, final Instrumentation inst) throws Exception {
        LOG.info("##################################################################");
        LOG.info("######################LOGGING AGENT STARTED#######################");
        LOG.info("##################################################################");
        LOG.info("LoggingAgent invoked with args: {}", args);

        if (exists(args)) {
            final String[] argumentsList = COMPILED_DELIMITER.split(args);
            final Set<String> arguments = new HashSet<String>(Arrays.asList(argumentsList));
            inst.addTransformer(new LoggingTransformer(arguments));
//            String classpath = System.getProperty("java.class.path");
//            System.out.println("classpath = " + classpath);

        } else {
            LOG.warn("This LoggingAgent will not do anything!");
            LOG.warn("No parameters provided for classes to enhance with logging");
            LOG.warn("syntaxis: java -javaagent:LoggingAgent.jar=org.exampl.ClassToAddLoggingTo [rest of the "
                     + "commandline...]");
        }
    }

    private static boolean exists(final String args) {
        return args != null;
    }

}