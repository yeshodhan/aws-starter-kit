package com.mickoo.aws.starterkit;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.util.SecurityGroupUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * aws-starter-kit
 *
 * @author Yeshodhan Kulkarni (yeshodhan.kulkarni@gmail.com)
 * @version 1.0
 * @since 1.1
 */
public class EC2 {

    private static final Logger logger = Logger.getLogger(EC2.class.getName());

    private static final int MAX_RETRIES = 50;

    private AmazonEC2Client ec2Client;

    private static final String DEFAULT_CREDENTAIL_PROFILE = "default";

    public EC2(String region) {
        this(region, DEFAULT_CREDENTAIL_PROFILE);
    }

    public EC2(String region, String credentialProfile) {
        ec2Client = new AmazonEC2Client(new ProfileCredentialsProvider(credentialProfile));
        ec2Client.setEndpoint(String.format("ec2.%s.amazonaws.com", region));
    }

    public EC2(String region, String accessKeyId, String secretAccessKey) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        ec2Client = new AmazonEC2Client(awsCredentials);
        ec2Client.setEndpoint(String.format("ec2.%s.amazonaws.com", region));
    }

    //launch an instance
    public Instance launchInstance(RunInstancesRequest runInstancesRequest) {
        RunInstancesResult runInstancesResult = ec2Client.runInstances(runInstancesRequest);
        if(runInstancesResult == null) return null;
        if(runInstancesResult.getReservation() == null) return null;
        if(runInstancesResult.getReservation().getInstances() == null) return null;
        if(runInstancesResult.getReservation().getInstances().size() == 0) return null;
        return runInstancesResult.getReservation().getInstances().get(0);
    }

    //start an instance
    public boolean startInstance(String instanceId) {
        logger.info("Starting instance: " + instanceId);

        Instance instance = getInstance(instanceId);

        if(isStopped(instance.getState())) {
            StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
            List<String> instances = new ArrayList<String>();
            instances.add(instanceId);
            startInstancesRequest.setInstanceIds(instances);
            StartInstancesResult instancesResult = ec2Client.startInstances(startInstancesRequest);
            if(instancesResult == null) return false;
            if(instancesResult.getStartingInstances() == null) return false;
            if(instancesResult.getStartingInstances().size() == 0) return false;
        }

        int retryCount = 0;
        while(retryCount < MAX_RETRIES) {
            instance = getInstance(instanceId);
            if(isRunning(instance.getState())){
                logger.info("Instance started: " + instanceId);
                return true;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryCount++;
        }

        return false;
    }

    //stop an instance
    public boolean stopInstance(String instanceId) {

        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
        List<String> instances=  new ArrayList<String>();
        instances.add(instanceId);
        stopInstancesRequest.setInstanceIds(instances);
        StopInstancesResult stopInstancesResult = ec2Client.stopInstances(stopInstancesRequest);

        if(stopInstancesResult == null) return false;
        if(stopInstancesResult.getStoppingInstances() == null) return false;
        if(stopInstancesResult.getStoppingInstances().size() == 0) return false;
        InstanceStateChange stateChange = stopInstancesResult.getStoppingInstances().get(0);
        InstanceState instanceState = stateChange.getCurrentState();
        if(isStopped(instanceState)) return true;

        int retryCount = 0;
        while(retryCount < MAX_RETRIES) {
            Instance instance = getInstance(instanceId);
            if(isStopped(instance.getState())){
                logger.info("Instance stopped: " + instanceId);
                return true;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryCount++;
        }

        return false;

    }

    private boolean isPending(InstanceState instanceState) {
        if(instanceState == null) return false;
        logger.info("Instance State: " + instanceState.getName());
        if("pending".equals(instanceState.getName())) return true;
        return false;
    }

    private boolean isRunning(InstanceState instanceState) {
        if(instanceState == null) return false;
        logger.info("Instance State: " + instanceState.getName());
        if("running".equals(instanceState.getName())) return true;
        return false;
    }

    private boolean isStopped(InstanceState instanceState) {
        if(instanceState == null) return false;
        logger.info("Instance State: " + instanceState.getName());
        if("stopped".equals(instanceState.getName())) return true;
        return false;
    }

    //create security group
    public String createSecurityGroup(CreateSecurityGroupRequest securityGroupRequest) {
        CreateSecurityGroupResult result = ec2Client.createSecurityGroup(securityGroupRequest);
        logger.info("Security Group Created: " + result.getGroupId());
        return result.getGroupId();
    }

    public boolean doesSecurityGroupExist(String securityGroupId) {
        return SecurityGroupUtils.doesSecurityGroupExist(ec2Client, securityGroupId);
    }

    public KeyPair createKeyPair(String name) {
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
        createKeyPairRequest.setKeyName(name);
        CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
        KeyPair keyPair = createKeyPairResult.getKeyPair();
        logger.info("Key Pair: " + name + " created. Fingerprint: " + keyPair.getKeyFingerprint());
        return keyPair;
    }

    public KeyPairInfo getKeyPairInfo(String keyName) {
        DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
        List<String> keys = new ArrayList<String>();
        keys.add(keyName);
        describeKeyPairsRequest.setKeyNames(keys);
        DescribeKeyPairsResult describeKeyPairsResult = null;
        try {
            describeKeyPairsResult = ec2Client.describeKeyPairs(describeKeyPairsRequest);
        } catch (Exception e){
            logger.info("Key Pair "+ keyName +" does not exist.");
            return null;
        }
        if(describeKeyPairsResult == null) return null;
        if(describeKeyPairsResult.getKeyPairs() == null) return null;
        if(describeKeyPairsResult.getKeyPairs().size() == 0) return null;
        return describeKeyPairsResult.getKeyPairs().get(0);
    }

    public SecurityGroup getSecurityGroup(String groupName) {
        DescribeSecurityGroupsResult describeSecurityGroupsResult = ec2Client.describeSecurityGroups();
        if(describeSecurityGroupsResult == null) return null;
        if(describeSecurityGroupsResult.getSecurityGroups() == null || describeSecurityGroupsResult.getSecurityGroups().size() == 0) return null;
        for(SecurityGroup securityGroup : describeSecurityGroupsResult.getSecurityGroups()) {
            if(groupName.equals(securityGroup.getGroupName())) {
                return securityGroup;
            }
        }
        return null;
    }

    public SecurityGroup getSecurityGroupById(String securityGroupId) {
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        List<String> groupIds = new ArrayList<String>();
        groupIds.add(securityGroupId);
        describeSecurityGroupsRequest.setGroupIds(groupIds);
        DescribeSecurityGroupsResult describeSecurityGroupsResult = null;
        try {
            describeSecurityGroupsResult = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest);
        } catch (Exception e){
            logger.log(Level.SEVERE, "Security Group: " + securityGroupId + " not found - " + e.getMessage());
            return null;
        }
        if(describeSecurityGroupsResult == null) return null;
        if(describeSecurityGroupsResult.getSecurityGroups() == null || describeSecurityGroupsResult.getSecurityGroups().size() == 0) return null;
        return describeSecurityGroupsResult.getSecurityGroups().get(0);
    }

    //add an inbound rule to security group
    public void addInboundRule(AuthorizeSecurityGroupIngressRequest inboundRule) {
        ec2Client.authorizeSecurityGroupIngress(inboundRule);
    }

    //add an outbound rule to security group
    public void addOutboundRule(AuthorizeSecurityGroupEgressRequest outboundRule) {
        ec2Client.authorizeSecurityGroupEgress(outboundRule);
    }

    //creates a new elastic IP in the VPC
    public AllocateAddressResult allocateElasticIP(AllocateAddressRequest allocateAddressRequest) {
        return ec2Client.allocateAddress(allocateAddressRequest);
    }

    //associates the elastic ip to an instance in vpc
    public AssociateAddressResult associateElasticIP(AssociateAddressRequest associateAddressRequest) {
        return ec2Client.associateAddress(associateAddressRequest);
    }

    public Instance getInstance(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        List<String> instances = new ArrayList<String>();
        instances.add(instanceId);
        describeInstancesRequest.setInstanceIds(instances);
        DescribeInstancesResult result = null;
        try {
            result = ec2Client.describeInstances(describeInstancesRequest);
        } catch (Exception e){
            logger.info("Instance Id: "+ instanceId +" does not exist.");
            return null;
        }
        if(result == null || result.getReservations().size() == 0) return null;
        Reservation reservation = result.getReservations().get(0);
        if(reservation == null || reservation.getInstances().size() == 0) return null;
        return reservation.getInstances().get(0);
    }

    public Vpc getVPC(String vpcId) {
        DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
        List<String> vpcs = new ArrayList<String>();
        vpcs.add(vpcId);
        describeVpcsRequest.setVpcIds(vpcs);
        DescribeVpcsResult result = null;
        try{
            result = ec2Client.describeVpcs(describeVpcsRequest);
        } catch (Exception e){
            logger.info("Vpc Id: "+ vpcId +" does not exist.");
            return null;
        }
        if(result == null) return null;
        if(result.getVpcs() == null || result.getVpcs().size() == 0) return null;
        return result.getVpcs().get(0);
    }

    public Vpc getDefaultVPC() {
        DescribeVpcsResult result = ec2Client.describeVpcs();
        if(result == null) return null;
        if(result.getVpcs() == null || result.getVpcs().size() == 0) return null;
        for(Vpc vpc : result.getVpcs()) {
            if(vpc.getIsDefault()) return vpc;
        }
        return null;
    }

    public Vpc getFirstAvailableVPC() {
        DescribeVpcsResult result = ec2Client.describeVpcs();
        if(result == null) return null;
        if(result.getVpcs() == null || result.getVpcs().size() == 0) return null;
        for(Vpc vpc : result.getVpcs()) {
            if(vpc.getIsDefault()) return vpc;
        }
        return result.getVpcs().get(0);
    }

    public List<Subnet> getSubnets(String vpcId) {
        DescribeSubnetsResult result = ec2Client.describeSubnets();
        if(result == null) return null;
        if(result.getSubnets() == null || result.getSubnets().size() == 0) return null;
        List<Subnet> found = new ArrayList<Subnet>();
        for(Subnet subnet : result.getSubnets()) {
            if(vpcId.equals(subnet.getVpcId())) {
                found.add(subnet);
            }
        }
        return found;
    }

}
