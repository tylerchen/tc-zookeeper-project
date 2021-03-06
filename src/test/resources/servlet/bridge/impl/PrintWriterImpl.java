/*
 * Copyright 2013 by Maxim Kalina
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.iff.netty.servlet.bridge.impl;

import java.io.OutputStream;
import java.io.PrintWriter;

public class PrintWriterImpl extends PrintWriter {

    private boolean flushed = false;

    public PrintWriterImpl(OutputStream out) {
        super(out);
    }

    @Override
    public void flush() {
        super.flush();
        this.flushed = true;
    }

    public boolean isFlushed() {
        return flushed;
    }
}
