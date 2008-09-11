/*
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * U.S. Government Rights - Commercial software. Government users 
 * are subject to the Sun Microsystems, Inc. standard license agreement
 * and applicable provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and Project Identity 
 * Connectors are trademarks or registered trademarks of Sun 
 * Microsystems, Inc. or its subsidiaries in the U.S. and other
 * countries.
 * 
 * UNIX is a registered trademark in the U.S. and other countries,
 * exclusively licensed through X/Open Company, Ltd. 
 * 
 * -----------
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License(CDDL) (the License).  You may not use this file
 * except in  compliance with the License. 
 * 
 * You can obtain a copy of the License at
 * http://identityconnectors.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * -----------
 */
package org.identityconnectors.test.common;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.identityconnectors.common.IOUtil;
import org.junit.Test;



public class IOUtilsTests {

    //=======================================================================
    // JUnit Tests
    //=======================================================================
    @Test public void quietClose() {
        // test reader
        ExceptionReader rdr = new ExceptionReader();
        IOUtil.quietClose(rdr);
        assertTrue(rdr.closeCalled);
        // test input stream
        ExceptionInputStream ins = new ExceptionInputStream();
        IOUtil.quietClose(ins);
        assertTrue(ins.closeCalled);
        // test outputstream
        ExceptionOutputStream os = new ExceptionOutputStream();
        IOUtil.quietClose(os);
        assertTrue(os.closeCalled);
        // test writer
        ExceptionWriter wrt = new ExceptionWriter();
        IOUtil.quietClose(wrt);
        assertTrue(wrt.closeCalled);
    }
    
    @Test public void resourcePath() {
        // test resource path returns the right thing..
        
    }
    
    //public static String getResourcePath(Class<?> c, String res) {
    //public static InputStream getResourceAsStream(Class<?> clazz, String res) {
    //public static byte[] getResourceAsBytes(Class<?> clazz, String res) {
    //public static String getResourceAsString(Class<?> clazz, String res, Charset charset) {
    //public static String getResourceAsString(Class<?> clazz, String res) {
 
    static class ExceptionReader extends Reader {
        boolean closeCalled;
        
        @Override
        public void close() throws IOException {
            closeCalled = true;
            throw new IOException();
        }

        @Override
        public int read(char[] arg0, int arg1, int arg2) throws IOException {
            return 0;
        }        
    }
    
    static class ExceptionInputStream extends InputStream {
        boolean closeCalled;

        @Override
        public void close() throws IOException {
            closeCalled = true;
            throw new IOException();
        }

        @Override
        public int read() throws IOException {
            return 0;
        }        
    }
    
    static class ExceptionWriter extends Writer {
        boolean closeCalled;

        @Override
        public void close() throws IOException {
            closeCalled = true;
            throw new IOException();
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(char[] arg0, int arg1, int arg2) throws IOException {
        }        
    }
    
    static class ExceptionOutputStream extends OutputStream {
        boolean closeCalled;

        @Override
        public void close() throws IOException {
            closeCalled = true;
            throw new IOException();
        }
        @Override
        public void write(int arg0) throws IOException {            
        }
    }
}