/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.archive.tests.java.util.zip;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.zip.ZipException;

//@TestTargetClass(ZipException.class)
public class ZipExceptionTest extends TestCase {

/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipException",
        args = {}
    )
*/
    public void testZipException() {
        ZipException zz = new ZipException();
        assertEquals(zz.getMessage(), null);
    }

/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipException",
        args = {java.lang.String.class}
    )
*/
    public void testZipExceptionLjava_lang_String() {
        ZipException zz = new ZipException("Test");
        assertEquals(zz.getMessage(), "Test");
    }

}
