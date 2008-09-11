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
package org.identityconnectors.framework.common.objects;

import org.identityconnectors.framework.api.operations.SyncApiOp;

/**
 * Callback interface provided by the application to handle results from
 * {@link SyncApiOp} in a stream-processing fashion.
 */
public interface SyncResultsHandler {

    /**
     * Called to handle a delta in the stream. Will be called multiple times,
     * once for each result. Although a callback, this is still invoked
     * synchronously. That is, it is guaranteed that following a call to
     * {@link SyncApiOp#sync(ObjectClass, SyncToken, SyncResultsHandler)} no
     * more invocations to {@link #handle(SyncDelta)} will be performed.
     * 
     * @param delta
     *            The change
     * @return True iff the application wants to continue processing more
     *         results.
     * @throws RuntimeException
     *             If the application encounters an exception. This will stop
     *             the interation and the exception will be propogated back to
     *             the application.
     */
    public boolean handle(SyncDelta delta);
}