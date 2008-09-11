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
package org.identityconnectors.contract.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;


/**
 * Contract test of {@link UpdateApiOp} 
 */
@RunWith(Parameterized.class)
public class UpdateApiOpTests extends ObjectClassRunner {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(UpdateApiOpTests.class);
    
    private static final String MODIFIED = "modified";
    private static final String ADDED = "added";
    private static final String TEST_NAME = "Update";

    public UpdateApiOpTests(ObjectClass objectClass) {
        super(objectClass);
    }
    
    /**
     * {@inheritDoc}     
     */
    @Override
    public Class<? extends APIOperation> getAPIOperation() {
        return UpdateApiOp.class;
    }
    
    /**
     * {@inheritDoc}      
     */
    @Override
    public void testRun() {
        ConnectorObject obj = null;
        Uid uid = null;


        try {
            // create an object to update
            uid = ConnectorHelper.createObject(getConnectorFacade(), getDataProvider(),
                    getObjectClassInfo(), getTestName(), 2, getOperationOptionsByOp(CreateApiOp.class));
            assertNotNull("Create returned null Uid.", uid);

            // get by uid
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid, getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Cannot retrieve created object.", obj);

            Set<Attribute> replaceAttributes = ConnectorHelper.getAttributes(getDataProvider(),
                    getObjectClassInfo(), getTestName(), MODIFIED, 0, false, false);

            if (replaceAttributes.size() > 0 || !isObjectClassSupported()) {
                // update only in case there is something to update or when object class is not supported
                replaceAttributes.add(uid);

                assertTrue("no update attributes were found", (replaceAttributes.size() > 0));
                Uid newUid = getConnectorFacade().update(UpdateApiOp.Type.REPLACE,
                        getObjectClass(), replaceAttributes, getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                // set
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    replaceAttributes.add(newUid);
                    uid = newUid;
                }
            }

            // verify the change
            obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                    getOperationOptionsByOp(GetApiOp.class));
            assertNotNull("Cannot retrieve updated object.", obj);
            ConnectorHelper.checkObject(getObjectClassInfo(), obj, replaceAttributes);

            // ADD and DELETE update test:
            // set of *multivalue* attributes with generated values
            Set<Attribute> addDelAttrs = ConnectorHelper.getAttributes(getDataProvider(),
                    getObjectClassInfo(), getTestName(), ADDED, 0, false, true);
            if (addDelAttrs.size() > 0) {
                // uid must be present for update
                addDelAttrs.add(uid);
                Uid newUid = getConnectorFacade().update(UpdateApiOp.Type.ADD, getObjectClass(),
                        addDelAttrs, getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after ADD
                obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve updated object.", obj);
                // don't want to have two same values for UID attribute
                addDelAttrs.remove(uid);
                ConnectorHelper.checkObject(getObjectClassInfo(), obj,
                        mergeAttributeSets(replaceAttributes, addDelAttrs));
                addDelAttrs.add(uid);

                // delete added attribute values
                newUid = getConnectorFacade().update(UpdateApiOp.Type.DELETE, getObjectClass(),
                        addDelAttrs, getOperationOptionsByOp(UpdateApiOp.class));

                // Update change of Uid must be propagated to replaceAttributes
                if (!newUid.equals(uid)) {
                    replaceAttributes.remove(uid);
                    addDelAttrs.remove(uid);
                    replaceAttributes.add(newUid);
                    addDelAttrs.add(newUid);
                    uid = newUid;
                }

                // verify the change after DELETE
                obj = getConnectorFacade().getObject(getSupportedObjectClass(), uid,
                        getOperationOptionsByOp(GetApiOp.class));
                assertNotNull("Cannot retrieve updated object.", obj);
                ConnectorHelper.checkObject(getObjectClassInfo(), obj, replaceAttributes);
            }                        
        } finally {
            if (uid != null) {
                // finally ... get rid of the object
                ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(), uid,
                        false, getOperationOptionsByOp(DeleteApiOp.class));
            }
        }
    }    
    
    /**
     * Tests create of two different objects and then update one to the same
     * values as the second. Should return different Uid or throw.
     */
    @Test
    public void testUpdateToSameAttributes() {
        if (ConnectorHelper.operationSupported(getConnectorFacade(), getAPIOperation())) {
            final int createdObjectsCount = 2;

            Map<Uid, Set<Attribute>> coCreated = null;
            Uid newUid = null;
            
            try {
                // create objects with object class that is supported
                coCreated = ConnectorHelper.createObjects(getConnectorFacade(), getDataProvider(),
                        getSupportedObjectClass(), getObjectClassInfo(), getTestName(),
                        createdObjectsCount, getOperationOptionsByOp(CreateApiOp.class));
                // check that objects were created with attributes as requested
                final boolean success = ConnectorHelper.checkObjects(getConnectorFacade(),
                        getSupportedObjectClass(), getObjectClassInfo(), coCreated, getOperationOptionsByOp(GetApiOp.class));
                assertTrue("Created objects are different than requested.", success);
                
                Uid[] uids = coCreated.keySet().toArray(new Uid[0]);

                Set<Attribute> replaceAttributes = coCreated.get(uids[0]);
                replaceAttributes.add(uids[1]);
                newUid = getConnectorFacade().update(UpdateApiOp.Type.REPLACE,
                        getSupportedObjectClass(), replaceAttributes, getOperationOptionsByOp(UpdateApiOp.class));

                
                assertFalse("Update returned the same uid when tried to update to the same " +
                		"attributes as another object.", uids[0].equals(newUid));

            } catch (RuntimeException ex) {
                // ok - update could throw this exception
            } finally {
                if (newUid != null) {
                    ConnectorHelper.deleteObject(getConnectorFacade(), getSupportedObjectClass(),
                            newUid, false, getOperationOptionsByOp(DeleteApiOp.class));
                }
                // delete test objects
                if (coCreated != null) {
                    ConnectorHelper.deleteObjects(getConnectorFacade(), getSupportedObjectClass(),
                            coCreated.keySet(), getOperationOptionsByOp(DeleteApiOp.class));
                }
            }
        }
    }

    @Override
    public String getTestName() {
        return TEST_NAME;
    }
    
    /**
     * Returns new attribute set which contains all attributes from both sets. If attribute with the same name is present
     * in both sets then its values are merged.
     */
    private Set<Attribute> mergeAttributeSets(Set<Attribute> attrSet1, Set<Attribute> attrSet2) {
        Set<Attribute> attrs = new HashSet<Attribute>();
        Map<String, Attribute> attrMap2 = AttributeUtil.toMap(attrSet2);

        for (Attribute attr1 : attrSet1) {
            Attribute attr2 = attrMap2.remove(attr1.getName());
            // if attribute is present in both sets then merge its values
            if (attr2 != null) {
                AttributeBuilder attrBuilder = new AttributeBuilder();
                attrBuilder.setName(attr1.getName());
                attrBuilder.addValue(attr1.getValue());
                attrBuilder.addValue(attr2.getValue());
                attrs.add(attrBuilder.build());
            } else {
                attrs.add(attr1);
            }
        }

        // add remaining attributes from second set
        for (Attribute attr2 : attrMap2.values()) {
            attrs.add(attr2);
        }

        return attrs;
    }
}