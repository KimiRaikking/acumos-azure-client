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
package org.acumos.azure.client.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.acumos.azure.client.transport.AzureContainerBean;
import org.acumos.azure.client.transport.AzureDeployDataObject;
import org.acumos.azure.client.transport.SingletonMapClass;
import org.acumos.azure.client.utils.AzureBean;
import org.acumos.azure.client.utils.DockerUtils;
import org.acumos.azure.client.utils.SSHShell;
import org.acumos.azure.client.utils.Utils;
import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.domain.MLPSolutionDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

public class AzureSimpleSolution implements Runnable {
	Logger logger = LoggerFactory.getLogger(AzureSimpleSolution.class);
	private Azure azure;
	private AzureDeployDataObject deployDataObject;
	private String dockerContainerPrefix;
	private String dockerUserName;
	private String dockerPwd;
	private String localEnvDockerHost;
	private String localEnvDockerCertPath;
	private ArrayList<String> list = new ArrayList<String>();
	private String dockerRegistryName;
	private String bluePrintName;
	private String bluePrintUser;
	private String bluePrintPass;
	private String networkSecurityGroup;
	private String dockerRegistryPort;
	private String uidNumStr;

	private String dataSource;
	private String dataUserName;
	private String dataPassword;
	private String dockerVMUserName;
	private String dockerVMPassword;
	private String solutionPort;

	public AzureSimpleSolution(Azure azure, AzureDeployDataObject deployDataObject, String dockerContainerPrefix,
			String dockerUserName, String dockerPwd, String localEnvDockerHost, String localEnvDockerCertPath,
			ArrayList<String> list, String bluePrintName, String bluePrintUser, String bluePrintPass,
			String networkSecurityGroup, String dockerRegistryName, String uidNumStr, String dataSource,
			String dataUserName, String dataPassword, String dockerVMUserName, String dockerVMPassword,String solutionPort) {
		this.azure = azure;
		this.deployDataObject = deployDataObject;
		this.dockerContainerPrefix = dockerContainerPrefix;
		this.dockerUserName = dockerUserName;
		this.dockerPwd = dockerPwd;
		this.localEnvDockerHost = localEnvDockerHost;
		this.localEnvDockerCertPath = localEnvDockerCertPath;
		this.list = list;
		this.bluePrintName = bluePrintName;
		this.bluePrintUser = bluePrintUser;
		this.bluePrintPass = bluePrintPass;

		this.networkSecurityGroup = networkSecurityGroup;
		this.dockerRegistryName = dockerRegistryName;
		this.uidNumStr = uidNumStr;
		this.dataSource = dataSource;
		this.dataUserName = dataUserName;
		this.dataPassword = dataPassword;
		this.dockerVMUserName = dockerVMUserName;
		this.dockerVMPassword = dockerVMPassword;
		this.solutionPort = solutionPort;

	}

	public void run() {
		logger.debug("<-----------------AzureSimpleSolution-----Run Started-------------------------->");
		logger.debug("<-------azure-------->" + azure);
		logger.debug("<-------deployDataObject-------->" + deployDataObject);
		logger.debug("<-------dockerContainerPrefix-------->" + dockerContainerPrefix);
		logger.debug("<-------dockerUserName-------->" + dockerUserName);
		logger.debug("<-------dockerPwd-------->" + dockerPwd);
		logger.debug("<-------localEnvDockerHost-------->" + localEnvDockerHost);
		logger.debug("<-------localEnvDockerCertPath-------->" + localEnvDockerCertPath);
		logger.debug("<-------list-------->" + list);
		logger.debug("<-------bluePrintName-------->" + bluePrintName);
		logger.debug("<-------bluePrintUser-------->" + bluePrintUser);
		logger.debug("<-------bluePrintPass-------->" + bluePrintPass);
		logger.debug("<-------networkSecurityGroup-------->" + networkSecurityGroup);
		logger.debug("<-------dockerRegistryName-------->" + dockerRegistryName);
		logger.debug("<-------uidNumStr-------->" + uidNumStr);
		logger.debug("<-------dataSource-------->" + dataSource);
		logger.debug("<-------dataUserName-------->" + dataUserName);
		logger.debug("<-------dataPassword-------->" + dataPassword);
		logger.debug("<-------dockerVMUserName-------->" + dockerVMUserName);
		logger.debug("<-------dockerVMPassword-------->" + dockerVMPassword);
		logger.debug("<-------solutionPort-------->" + solutionPort);

		logger.debug("<-------solutionId-------->" + deployDataObject.getSolutionId());
		logger.debug("<-------solutionRevisionId-------->" + deployDataObject.getSolutionRevisionId());
		logger.debug("<-------userId-------->" + deployDataObject.getUserId());

		AzureBean azureBean = new AzureBean();
		AzureContainerBean containerBean = new AzureContainerBean();
		try {
			if(dockerVMUserName!=null){
		    	dockerVMUserName=dockerVMUserName.trim();	
		    }
		    if(dockerVMPassword!=null){
		    	dockerVMPassword=dockerVMPassword.trim();	
		    }
		    logger.debug("<-------dockerVMUserName----Trim---->"+dockerVMUserName);
			logger.debug("<-------dockerVMPassword-----Trim--->"+dockerVMPassword);
			final String saName = SdkContext.randomResourceName("sa", 20);
			final Region region = Region.US_EAST;
			final String dockerContainerName = dockerContainerPrefix + System.currentTimeMillis();// "acrsample";
			String servicePrincipalClientId = deployDataObject.getClient(); // replace with a real service principal
																			// client id
			String servicePrincipalSecret = deployDataObject.getKey(); // and corresponding secret

			String portArr[] = { "8556", "8557", "8558", "8559", "8560", "8561", "8562", "8563", "8564", "8565" };
			if (servicePrincipalClientId.isEmpty() || servicePrincipalSecret.isEmpty()) {
				String envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION_2");

				if (envSecondaryServicePrincipal == null || !envSecondaryServicePrincipal.isEmpty()
						|| !Files.exists(Paths.get(envSecondaryServicePrincipal))) {
					envSecondaryServicePrincipal = System.getenv("AZURE_AUTH_LOCATION");
				}

				servicePrincipalClientId = Utils.getSecondaryServicePrincipalClientID(envSecondaryServicePrincipal);
				servicePrincipalSecret = Utils.getSecondaryServicePrincipalSecret(envSecondaryServicePrincipal);
			}

			// =============================================================
			// Create an SSH private/public key pair to be used when creating the container
			// service

			logger.debug("Creating an SSH private and public key pair");

			SSHShell.SshPublicPrivateKey sshKeys = SSHShell.generateSSHKeys("", "ACS");
			logger.debug("SSH private key value: \n" + sshKeys.getSshPrivateKey());
			logger.debug("SSH public key value: \n" + sshKeys.getSshPublicKey());

			// =============================================================
			// Create an Azure Container Service with Kubernetes orchestration

			logger.debug(
					"Creating an Azure Container Service with Kubernetes ochestration and one agent (virtual machine)");

			Date t1 = new Date();

			// =============================================================
			// Create an Azure Container Registry to store and manage private Docker
			// container images

			logger.debug("Creating an Azure Container Registry");
			Date t2 = new Date();
			t1 = new Date();

			// Get the existing Azure registry using resourceGroupName and Acr Name
			Registry azureRegistry = azure.containerRegistries().getByResourceGroup(deployDataObject.getRgName(),
					deployDataObject.getAcrName());

			t2 = new Date();
			logger.debug("Created Azure Container Registry: (took " + ((t2.getTime() - t1.getTime()) / 1000)
					+ " seconds) " + azureRegistry.id());
			Utils.print(azureRegistry);

			RegistryListCredentials acrCredentials = azureRegistry.listCredentials();
			logger.debug("azureRegistry.loginServerUrl=" + azureRegistry.loginServerUrl() + ", acrCredentials.username "
					+ acrCredentials.username() + ", acrCredentials.passwords"
					+ acrCredentials.passwords().get(0).value());
			DockerClient dockerClient = DockerUtils.createDockerClient(azure, deployDataObject.getRgName(), region,
					azureRegistry.loginServerUrl(), acrCredentials.username(),
					acrCredentials.passwords().get(0).value(), localEnvDockerHost, localEnvDockerCertPath, azureBean,
					networkSecurityGroup, dockerRegistryPort, dockerRegistryName,dockerVMUserName,dockerVMPassword);

			AuthConfig authConfig = new AuthConfig().withUsername(dockerUserName).withPassword(dockerPwd);
			logger.debug("Start pulling images from nexus::::::::");
			Iterator itr = list.iterator();
			while (itr.hasNext()) {
				String imageName = (String) itr.next();
				dockerClient.pullImageCmd(imageName).withAuthConfig(authConfig)
						// .withTag(dockerImageTag)
						.exec(new PullImageResultCallback()).awaitSuccess();
				Thread.sleep(30000);
			}

			int dockerCount = 1;
			HashMap<String, CreateContainerResponse> hmap = new HashMap<String, CreateContainerResponse>();
			HashMap<String, String> containerTagMap = new HashMap<String, String>();
			HashMap<String, String> containerImageMap = new HashMap<String, String>();
			HashMap<String, String> containerInstanceImageMap = new HashMap<String, String>();
			Iterator itr1 = list.iterator();
			while (itr1.hasNext()) {
				String imageTagVal = "";
				String imageName = (String) itr1.next();
				CreateContainerResponse dockerContainerInstance = dockerClient.createContainerCmd(imageName)
						.withName(dockerContainerName + "_" + dockerCount)
						// .withCmd("/hello")
						.exec();

				if (imageName != null && !"".equals(imageName)) {
					String tag = getTagFromImage(imageName);
					if (tag != null) {
						imageTagVal = tag;
					}
				}
				logger.debug("===imageName=======" + imageName + "========imageTagVal====" + imageTagVal);
				/*
				 * if(imageName!=null && imageName.contains(bluePrintName)){
				 * containerInstanceBluePrint=dockerContainerName+"_"+dockerCount; }
				 */
				hmap.put(dockerContainerName + "_" + dockerCount, dockerContainerInstance);
				containerTagMap.put(dockerContainerName + "_" + dockerCount, imageTagVal);
				containerImageMap.put(dockerContainerName + "_" + dockerCount, imageName);
				Thread.sleep(30000);
				dockerCount++;
			}
			logger.debug("List All Docker containers:");
			List<Container> dockerContainers = dockerClient.listContainersCmd().withShowAll(true).exec();

			for (Container container : dockerContainers) {
				logger.debug("All Docker container with images and Name %s (%s)\n" + container.getImage()
						+ "<-----container.getId()----->" + container.getId());
			}

			// =============================================================
			// Commit the new container
			// String privateRepoUrl = azureRegistry.loginServerUrl() + "/samples/" +
			// dockerContainerName;
			// logger.debug("privateRepoUrl::::::::::::::::::"+privateRepoUrl);
			HashMap<String, String> repoUrlMap = new HashMap<String, String>();
			Iterator itrContainer = hmap.entrySet().iterator();
			while (itrContainer.hasNext()) {
				String imageTagLatest = "latest";
				Map.Entry pair = (Map.Entry) itrContainer.next();
				String containerName = (String) pair.getKey();
				CreateContainerResponse dockerContainerInstance = (CreateContainerResponse) pair.getValue();

				String privateRepoUrl = azureRegistry.loginServerUrl() + "/samples/" + containerName;
				logger.debug("dockerContainerInstance.getId():::::::::::::::::" + dockerContainerInstance.getId()
						+ "===privateRepoUrl====" + privateRepoUrl);
				if (containerTagMap != null && containerTagMap.get(containerName) != null) {
					imageTagLatest = containerTagMap.get(containerName);
				}
				logger.debug("containerName======" + containerName + "==imageTagLatest======" + imageTagLatest);
				String dockerImageId = dockerClient.commitCmd(dockerContainerInstance.getId())
						.withRepository(privateRepoUrl).withTag(imageTagLatest).exec();
				logger.debug("dockerImageId::::::::::::::::::" + dockerImageId);
				repoUrlMap.put(containerName, privateRepoUrl);
				// We can now remove the temporary container instance
				dockerClient.removeContainerCmd(dockerContainerInstance.getId()).withForce(true).exec();
				Thread.sleep(5000);
			}
			// #####################################################################################
			logger.debug("<----Before Docker remoteDockerClient--------------------------->");
			DockerClient remoteDockerClient = DockerUtils.createDockerClient(azure, deployDataObject.getRgName(),
					region, azureRegistry.loginServerUrl(), acrCredentials.username(),
					acrCredentials.passwords().get(0).value(), null, localEnvDockerCertPath, azureBean,
					networkSecurityGroup, dockerRegistryPort, dockerRegistryName,dockerVMUserName,dockerVMPassword);
			logger.debug("<----After Docker remoteDockerClient--------------------------->");
			// =============================================================
			// Push the new Docker image to the Azure Container Registry
			Iterator repoContainer = repoUrlMap.entrySet().iterator();
			while (repoContainer.hasNext()) {
				Map.Entry pair = (Map.Entry) repoContainer.next();
				String containerName = (String) pair.getKey();
				String privateRepoUrl = (String) pair.getValue();

				dockerClient.pushImageCmd(privateRepoUrl).withAuthConfig(dockerClient.authConfig())
						.exec(new PushImageResultCallback()).awaitSuccess();
				Thread.sleep(50000);
			}

			logger.debug("<----Pushed Images to privaterepourl and removing imgage from local docker host---------->");
			// Remove the temp image from the local Docker host
			/*
			 * try { Iterator itr5=list.iterator(); while(itr5.hasNext()){ String
			 * imageName=(String)itr5.next();
			 * dockerClient.removeImageCmd(imageName).withForce(true).exec(); } } catch
			 * (NotFoundException e) {
			 * logger.error("Error in removing images "+e.getMessage()); }
			 */

			// =============================================================
			// Verify that the image we saved in the Azure Container registry can be pulled
			// and instantiated locally
			logger.debug("<----pull images from Azure registry to locally--------->");
			repoContainer = repoUrlMap.entrySet().iterator();
			while (repoContainer.hasNext()) {
				Map.Entry pair = (Map.Entry) repoContainer.next();
				String containerName = (String) pair.getKey();
				String privateRepoUrl = (String) pair.getValue();
				logger.debug("<----pull images from Azure registry to locally-----privateRepoUrl---->" + privateRepoUrl);
				dockerClient.pullImageCmd(privateRepoUrl).withAuthConfig(dockerClient.authConfig())
						.exec(new PullImageResultCallback()).awaitSuccess();
				Thread.sleep(50000);
			}

			logger.debug("<----remoteDockerClient with privateRepoUrl--------->");

			repoContainer = repoUrlMap.entrySet().iterator();
			while (repoContainer.hasNext()) {
				Map.Entry pair = (Map.Entry) repoContainer.next();
				String containerName = (String) pair.getKey();
				String privateRepoUrl = (String) pair.getValue();
				String tagImage = "latest";
				if (containerTagMap != null && containerTagMap.get(containerName) != null) {
					tagImage = containerTagMap.get(containerName);
				}

				String azureVMIP = azureBean.getAzureVMIP();
				final String vmUserName=dockerVMUserName;
				final String vmPassword=dockerVMPassword;
				String repositoryName = "";
				repositoryName = privateRepoUrl + ":" + tagImage;
				logger.debug("<----vmUserName-------->" + vmUserName);
				logger.debug("<----vmPassword-------->" + vmPassword);
				logger.debug("<----azureBean VM-------->" + azureBean.getAzureVMIP());
				logger.debug("<----azureBean VM-------->" + azureRegistry.loginServerUrl());
				logger.debug("<----azureBean VM-------->" + azureRegistry.loginServerUrl());
				logger.debug("<----azureBean VM-------->" + acrCredentials.passwords().get(0).value());
				//String portNumberString=
				DockerUtils.deploymentImageVM(azureVMIP, vmUserName, vmPassword, azureRegistry.loginServerUrl(),
						acrCredentials.username(), acrCredentials.passwords().get(0).value(), repositoryName);
				containerBean.setContainerIp(azureBean.getAzureVMIP());
				containerBean.setContainerPort("8557");
			}
			createDeploymentData(dataSource, dataUserName, dataPassword, containerBean,
					deployDataObject.getSolutionId(), deployDataObject.getSolutionRevisionId(),
					deployDataObject.getUserId(), uidNumStr, "DP");
		} catch (Exception e) {
			logger.error("Error in AzureSimpleSolution===========" + e.getMessage());
			try{
				createDeploymentData(dataSource, dataUserName, dataPassword, containerBean,
						deployDataObject.getSolutionId(), deployDataObject.getSolutionRevisionId(),
						deployDataObject.getUserId(), uidNumStr, "FA");
			}catch(Exception ex){
				logger.error("Error in saving data===========" +ex.getMessage());
			}
			e.printStackTrace();
		}
		String vmDetail = azureBean.getAzureVMIP() + "#8557";
		setuidHashmap(uidNumStr, vmDetail);
		logger.debug("<---------------AzureSimpleSolution-------Run End-------------------------->");
		// code in the other thread, can reference "var" variable
	}

	public void setuidHashmap(String uidNumStr, String vmDetails) {
		logger.debug("<---------------setuidHashmap-------Run Start-------------------------->" + uidNumStr
				+ "====vmDetails======" + vmDetails);
		HashMap<String, String> singlatonMap = SingletonMapClass.getInstance();
		singlatonMap.put(uidNumStr, vmDetails);
		logger.debug("<---------------setuidHashmap-------Run End-------------------------->" + singlatonMap);
	}

	public CommonDataServiceRestClientImpl getClient(String datasource, String userName, String password) {
		CommonDataServiceRestClientImpl client = new CommonDataServiceRestClientImpl(datasource, userName, password);
		return client;
	}

	public void createDeploymentData(String dataSource, String dataUserName, String dataPassword,
			AzureContainerBean containerBean, String solutionId, String solutionRevisionId, String userId,
			String uidNumber, String deploymentStatusCode) throws Exception {
		logger.debug("<---------Start createDeploymentData ------------------------->");
		logger.debug("<---------dataSource-------->" + dataSource);
		logger.debug("<-------dataUserName-------------->" + dataUserName);
		logger.debug("<--------dataPassword------------->" + dataPassword);
		logger.debug("<---------solutionId------------------->" + solutionId);
		logger.debug("<--------solutionRevisionId-------------------->" + solutionRevisionId);
		logger.debug("<------userId--------------->" + userId);
		logger.debug("<------uidNumber--------------->" + uidNumber);
		logger.debug("<------deploymentStatusCode--------------->" + deploymentStatusCode);
		ObjectMapper mapper = new ObjectMapper();
		CommonDataServiceRestClientImpl client = getClient(dataSource, dataUserName, dataPassword);
		if (solutionId != null && solutionRevisionId != null && userId != null && uidNumber != null) {
			MLPSolutionDeployment mlp = new MLPSolutionDeployment();
			mlp.setSolutionId(solutionId);
			mlp.setUserId(userId);
			mlp.setRevisionId(solutionRevisionId);
			mlp.setDeploymentId(uidNumber);
			mlp.setDeploymentStatusCode(deploymentStatusCode);
			String azureDetails = mapper.writeValueAsString(containerBean);
			mlp.setDetail(azureDetails);
			logger.debug("<---------azureDetails------------------------->" + azureDetails);
			MLPSolutionDeployment mlpDeployment = client.createSolutionDeployment(mlp);
			logger.debug("<---------mlpDeployment------------------------->" + mlpDeployment);
		}
		logger.debug("<---------End createDeploymentData ------------------------->");
	}

	public String getTagFromImage(String imageName) {
		String imageTag = null;
		final int endColon = imageName.lastIndexOf(':');
		if (endColon < 0) {
			imageTag = null;
		} else {
			final String tag = imageName.substring(endColon + 1);
			if (tag.indexOf('/') < 0) {
				imageTag = tag;
			} else {
				imageTag = null;
			}
		}
		return imageTag;
	}

}
