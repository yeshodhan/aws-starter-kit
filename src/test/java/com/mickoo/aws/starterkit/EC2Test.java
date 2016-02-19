package com.mickoo.aws.starterkit;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * aws-starter-kit
 *
 * @author Yeshodhan Kulkarni (yeshodhan.kulkarni@tp-link.com)
 * @version 1.0
 * @since 1.1
 */
public class EC2Test {

    private static final Logger logger = Logger.getLogger(EC2Test.class.getName());

    private static final String MY_SECURITY_GROUP = "my-security-group";

    private static final Regions MY_REGION = Regions.US_WEST_2;

    private static final String MY_KEY_PAIR = "my-key-pair";

    //This ami-id is for Ubuntu Server 14.04 LTS (HVM), SSD Volume Type in us-west-2 region.
    //The ami-id will be different if you want to start in a different region.
    private static final String MY_AMI_ID = "ami-9abea4fb";

    private static final InstanceType MY_INSTANCE_TYPE = InstanceType.T2Micro;

    private EC2 ec2;

    public EC2Test() {
        ec2 = new EC2(MY_REGION.getName());
    }


    public void basic() throws IOException {

        //create security group
        SecurityGroup securityGroup = createSecurityGroup();

        //create key pair
        KeyPairInfo keyPairInfo = createKeyPair();

        //launch an instance
        Instance instance = launchInstance();

        String instanceId = instance.getInstanceId();

        //start an instance
        boolean isStarted = startInstance(instanceId);

        //stop instance
        if(isStarted) stopInstance(instanceId);

    }

    private SecurityGroup createSecurityGroup() {
        if(!ec2.doesSecurityGroupExists(MY_SECURITY_GROUP)) {
            return ec2.getSecurityGroup(MY_SECURITY_GROUP);
        }
        CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest();
        securityGroupRequest.setGroupName(MY_SECURITY_GROUP);
        CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(securityGroupRequest);
        if(createSecurityGroupResult == null) return null;
        return ec2.getSecurityGroup(createSecurityGroupResult.getGroupId());
    }

    private KeyPairInfo createKeyPair() throws IOException {
        KeyPairInfo keyPairInfo = ec2.getKeyPairInfo(MY_KEY_PAIR);
        if(keyPairInfo == null) {
            KeyPair keyPair = ec2.createKeyPair(MY_KEY_PAIR);
            //persist this key pair somewhere
            FileUtils.write(new File("~/.ssh/"+MY_KEY_PAIR+".pem"), keyPair.getKeyMaterial());
            logger.info("New Key Pair Created: " + keyPair.getKeyFingerprint());
            logger.info("PEM: ");
            logger.info(keyPair.getKeyMaterial());
        }
        return keyPairInfo;
    }

    private Instance launchInstance() {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId(MY_AMI_ID)
        .withInstanceType(MY_INSTANCE_TYPE)
        .withMinCount(0)
        .withMaxCount(1)
        .withKeyName(MY_KEY_PAIR)
        .withSecurityGroups(MY_SECURITY_GROUP);

        return ec2.launchInstance(runInstancesRequest);
    }

    private boolean startInstance(String instanceId) {
        return ec2.startInstance(instanceId);
    }

    private boolean stopInstance(String instanceId) {
        return ec2.stopInstance(instanceId);
    }

}
