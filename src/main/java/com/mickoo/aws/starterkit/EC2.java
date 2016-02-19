package com.mickoo.aws.starterkit;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.ec2.util.SecurityGroupUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * aws-starter-kit
 *
 * @author Yeshodhan Kulkarni (yeshodhan.kulkarni@tp-link.com)
 * @version 1.0
 * @since 1.1
 */
public class EC2 {

    AmazonEC2Client ec2Client;

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
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
        List<String> instances = new ArrayList<String>();
        instances.add(instanceId);
        startInstancesRequest.setInstanceIds(instances);
        StartInstancesResult instancesResult = ec2Client.startInstances(startInstancesRequest);
        if(instancesResult == null) return false;
        if(instancesResult.getStartingInstances() == null) return false;
        if(instancesResult.getStartingInstances().size() == 0) return false;
        InstanceStateChange stateChange = instancesResult.getStartingInstances().get(0);
        InstanceState instanceState = stateChange.getCurrentState();
        if(isRunning(instanceState)) return true;

        int MAX_RETRIES = 50;

        int retryCount = 0;
        while(retryCount < MAX_RETRIES) {
            Instance instance = getInstance(instanceId);
            if(isRunning(instance.getState())){
                break;
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

    private boolean isRunning(InstanceState instanceState) {
        if(instanceState != null && "running".equals(instanceState.getName())) return true;
        return false;
    }

    private boolean isStopped(InstanceState instanceState) {
        if(instanceState != null && "stopped".equals(instanceState.getName())) return true;
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

        int MAX_RETRIES = 50;

        int retryCount = 0;
        while(retryCount < MAX_RETRIES) {
            Instance instance = getInstance(instanceId);
            if(isStopped(instance.getState())){
                break;
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

    //create security group
    public CreateSecurityGroupResult createSecurityGroup(CreateSecurityGroupRequest securityGroupRequest) {
        return ec2Client.createSecurityGroup(securityGroupRequest);
    }

    public boolean doesSecurityGroupExists(String securityGroupName) {
        return SecurityGroupUtils.doesSecurityGroupExist(ec2Client, securityGroupName);
    }

    public KeyPair createKeyPair(String name) {
        CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
        createKeyPairRequest.setKeyName(name);
        CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);
        return createKeyPairResult.getKeyPair();
    }

    public KeyPairInfo getKeyPairInfo(String keyName) {
        DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();
        List<String> keys = new ArrayList<String>();
        keys.add(keyName);
        describeKeyPairsRequest.setKeyNames(keys);
        DescribeKeyPairsResult describeKeyPairsResult = ec2Client.describeKeyPairs(describeKeyPairsRequest);
        if(describeKeyPairsResult == null) return null;
        if(describeKeyPairsResult.getKeyPairs() == null) return null;
        if(describeKeyPairsResult.getKeyPairs().size() == 0) return null;
        return describeKeyPairsResult.getKeyPairs().get(0);
    }

    public SecurityGroup getSecurityGroup(String groupName) {
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        List<String> groups = new ArrayList<String>();
        groups.add(groupName);
        describeSecurityGroupsRequest.setGroupNames(groups);
        DescribeSecurityGroupsResult describeSecurityGroupsResult = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest);
        if(describeSecurityGroupsResult != null && describeSecurityGroupsResult.getSecurityGroups() != null && describeSecurityGroupsResult.getSecurityGroups().size() > 0) {
            return describeSecurityGroupsResult.getSecurityGroups().get(0);
        }
        return null;
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
        DescribeInstancesResult result = ec2Client.describeInstances(describeInstancesRequest);
        if(result == null || result.getReservations().size() == 0) return null;
        Reservation reservation = result.getReservations().get(0);
        if(reservation == null || reservation.getInstances().size() == 0) return null;
        Instance instance = reservation.getInstances().get(0);
        return instance;
    }

}
