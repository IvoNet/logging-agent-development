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

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;

@SuppressWarnings({"SpellCheckingInspection", "UtilityClass"})
public class JavassistHelper {

    static String returnValue(final CtBehavior method)
            throws NotFoundException {
        String returnValue = "";
        if (methodReturnsValue(method)) {
            returnValue = "\" returns: \" + $_ ";
        }
        return returnValue;
    }

    private static boolean methodReturnsValue(final CtBehavior method)
            throws NotFoundException {

        final boolean isConstructor = method instanceof CtConstructor;
        if (isConstructor) {
            return false;
        }

        final CtClass returnType = ((CtMethod) method).getReturnType();
        final String returnTypeName = returnType.getName();

        return !"void".equals(returnTypeName);
    }

    static String getSignature(final CtBehavior method)
            throws NotFoundException {
        final CtClass[] parameterTypes = method.getParameterTypes();

        final CodeAttribute codeAttribute = method.getMethodInfo()
                .getCodeAttribute();

        final LocalVariableAttribute locals = (LocalVariableAttribute) codeAttribute
                .getAttribute("LocalVariableTable");
        final String methodName = method.getName();

        final StringBuilder sb = new StringBuilder(methodName + "(\" ");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sb.append(" + \", \" ");
            }

            final CtClass parameterType = parameterTypes[i];
            final CtClass arrayOf = parameterType.getComponentType();

            sb.append(" + \"");
            sb.append(parameterNameFor(method, locals, i));
            sb.append("\" + \"=");

            // use Arrays.asList() to render array of objects.
            if ((arrayOf != null) && !arrayOf.isPrimitive()) {
                sb.append("\"+ java.util.Arrays.asList($").append(i + 1).append(')');
            } else {
                sb.append("\"+ $").append(i + 1);
            }
        }
        sb.append("+\")\"");

        return sb.toString();
    }

    static String parameterNameFor(final CtBehavior method,
                                   final LocalVariableAttribute locals, final int i) {
        if (locals == null) {
            return Integer.toString(i + 1);
        }

        if (Modifier.isStatic(method.getModifiers())) {
            return locals.variableName(i);
        }

        // skip #0 which is reference to "this"
        return locals.variableName(i + 1);
    }

}
