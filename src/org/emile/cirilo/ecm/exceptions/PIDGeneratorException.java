/*
 *  -------------------------------------------------------------------------
 *  Copyright 2014 
 *  Centre for Information Modeling - Austrian Centre for Digital Humanities
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 *  -------------------------------------------------------------------------
 */

package org.emile.cirilo.ecm.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: May 19, 2009
 * Time: 11:10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class PIDGeneratorException extends EcmException {

    public PIDGeneratorException() {
    }

    public PIDGeneratorException(String s) {
        super(s);
    }

    public PIDGeneratorException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PIDGeneratorException(Throwable throwable) {
        super(throwable);
    }
}
