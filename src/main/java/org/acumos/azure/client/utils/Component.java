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

package org.acumos.azure.client.utils;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Component implements Serializable {

	private static final long serialVersionUID = 5749775315078650369L;

	public Component() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Component(String name, OperationSignature operationSignature) {
		super();
		this.name = name;
		this.operationSignature = operationSignature;
	}

	@JsonProperty("name")
	private String name = null;
	/*
	 * @JsonProperty("operation_signatures") private List<OperationSignature>
	 * operationSignatures = null;
	 */
	@JsonProperty("operation_signature")
	private OperationSignature operationSignature = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OperationSignature getOperationSignature() {
		return operationSignature;
	}

	public void setOperationSignature(OperationSignature operationSignature) {
		this.operationSignature = operationSignature;
	}

	@Override
	public String toString() {
		return "Component [name=" + name + ", operationSignature=" + operationSignature + "]";
	}

}
