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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * I add logging to all methods of specified classes.
 *
 * @author Ivo Woltring
 */
public class LoggingTransformer implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingTransformer.class);
    private static final String LOG_DEFINITION = "private static java.util.logging.Logger _log;";
    private static final String IF_LOG = "if (_log.isLoggable(java.util.logging.Level.INFO))";
    private static final String GET_LOGGER = "java.util.logging.Logger.getLogger(%s.class.getName());";
    private static final String[] IGNORE = {"sun.", "java.", "javax."};
    private final Set<String> classes;


    public LoggingTransformer(final Set<String> arguments) {
        this.classes = new HashSet<String>(arguments);
    }

    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
            throws IllegalClassFormatException {

        final String name = transformClassName(className);
        if (ignored(name)) {
            return classfileBuffer;
        }
        if (notInTransformList(name)) {
            LOG.trace("### " + name);
            return classfileBuffer;
        }

        LOG.info(String.format("Found wanted class [%s]", name));
        return addLoggingToClass(name, classfileBuffer);

    }

    private boolean ignored(final String name) {
        for (final String ignore : IGNORE) {
            if (name.startsWith(ignore)) {
                return true;
            }
        }
        return false;
    }

    private boolean notInTransformList(final String name) {
        return !classes.contains(name);
    }

    private String transformClassName(final String className) {
        return className.replace(File.separator, ".");
    }

    private byte[] addLoggingToClass(final String name, final byte[] classfileBuffer) {
        final ClassPool classPool = ClassPool.getDefault();
        try {
            //FIXME How do I do this without doing this hardcoded!?
            classPool.appendClassPath("/usr/local/Cellar/tomcat/7.0.26/libexec/lib/servlet-api.jar");
            classPool.appendClassPath("/usr/local/Cellar/tomcat/7.0.26/libexec/lib/jasper.jar");
        } catch (NotFoundException e) {
            System.err.println(String.format("Could not find [%s]", e.getMessage()));
        }
        CtClass ctClass = null;
        try {
            ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (!ctClass.isInterface()) {
                ctClass.addField(CtField.make(LOG_DEFINITION, ctClass), String.format(GET_LOGGER, name));
                processMethods(ctClass.getDeclaredBehaviors());
                return ctClass.toBytecode();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            System.err.printf("Could not instrument  %s,  exception : %s%n", name, e.getMessage());
        } finally {
            detach(ctClass);
        }
        return classfileBuffer;
    }

    private void detach(final CtClass ctClass) {
        if (ctClass != null) {
            ctClass.detach();
        }
    }

    private void processMethods(final CtBehavior[] methods) throws NotFoundException, CannotCompileException {
        for (final CtBehavior method : methods) {
            processMethod(method);
        }
    }

    private void processMethod(final CtBehavior method) throws NotFoundException, CannotCompileException {
        if (!method.isEmpty()) {
            doMethod(method);
        }
    }

    private void doMethod(final CtBehavior method)
            throws NotFoundException, CannotCompileException {

        final String signature = JavassistHelper.getSignature(method);
        final String returnValue = JavassistHelper.returnValue(method);

        method.insertBefore(String.format("%s_log.info(\">>> %s);", IF_LOG, signature));

        method.insertAfter(String.format("%s_log.info(\"<<< %s%s);", IF_LOG, signature, returnValue));
    }
}

