/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package org.acumos.azure.client.test.util;



import org.acumos.azure.client.utils.Component;
import org.acumos.azure.client.utils.OperationSignature;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentTest {
	
	private static Logger logger = LoggerFactory.getLogger(ComponentTest.class);
	@Test	
	public void ComponentTestObjectparameter(){
		logger.info("ComponentTest Start");
		Component component=new Component();
		OperationSignature operationSignature = new OperationSignature();
		component.setName(AzureClientTestConstants.TEST_COMPONENT_NAME);
		component.setOperationSignature(operationSignature);
		Assert.assertEquals(AzureClientTestConstants.TEST_COMPONENT_NAME, component.getName());
		Assert.assertEquals(operationSignature, component.getOperationSignature());
		logger.info("ComponentTest End");
	}

}
