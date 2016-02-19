package com.mickoo.aws.starterkit;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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
public class EC2Test {

    private static final Logger logger = Logger.getLogger(EC2Test.class.getName());

    private static final String DEFAULT_SECURITY_GROUP = "default";

    private static final String MY_SECURITY_GROUP = "my-security-group";
    private static final String MY_SECURITY_GROUP_DESC = "My Security Group description";

    private static final Regions MY_REGION = Regions.US_WEST_2;

    private static final String MY_KEY_PAIR = "my-key-pair";

    //This ami-id is for Ubuntu Server 14.04 LTS (HVM), SSD Volume Type in us-west-2 region.
    //The ami-id will be different if you want to start in a different region.
    private static final String MY_AMI_ID = "ami-9abea4fb";

    private static final InstanceType MY_INSTANCE_TYPE = InstanceType.T2Micro;

    private EC2 ec2;

    public EC2Test() {

        String accessKeyId = System.getProperty("aws.accessKeyId", null);
        String secretAccessKey = System.getProperty("aws.secretAccessKey", null);

        if(accessKeyId != null && secretAccessKey != null) {
            ec2 = new EC2(MY_REGION.getName(), accessKeyId, secretAccessKey);
        } else {
            ec2 = new EC2(MY_REGION.getName());
        }

    }

    @Test
    public void testInstanceLifeCycle() throws Exception {

        //launch an instance
        Instance instance = launchInstance();

        //start an instance
        boolean isStarted = ec2.startInstance(instance.getInstanceId());

        //get the instance again
        instance = ec2.getInstance(instance.getInstanceId());

        logger.info("Private DNS: " + instance.getPrivateDnsName());
        logger.info("Private IP: " + instance.getPrivateDnsName());
        logger.info("Public DNS: " + instance.getPublicDnsName());
        logger.info("Public IP: " + instance.getPublicIpAddress());

        //stop instance
        if(isStarted) ec2.stopInstance(instance.getInstanceId());

    }

    private Instance launchInstance() throws InterruptedException, IOException {

        //get default vpc. if not, get any available vpc.
        Vpc availableVPC = ec2.getFirstAvailableVPC();
        if(availableVPC == null) throw new RuntimeException("Default VPC not found.");

        //create the security group in the vpc
        SecurityGroup securityGroup = createSecurityGroup(availableVPC.getVpcId());
        if(securityGroup == null) throw new RuntimeException("Security group must exist");

        //create a key pair
        KeyPairInfo keyPairInfo = createKeyPair();

        //get all subnets from the vpc
        List<Subnet> subnets = ec2.getSubnets(availableVPC.getVpcId());
        if(subnets == null || subnets.size() == 0) throw new RuntimeException("No subnets found.");

        //select a random subnet to launch the instance
        Subnet someRandomSubnet = subnets.get(0);


        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        //any ami id
        runInstancesRequest.setImageId(MY_AMI_ID);

        //instance type
        runInstancesRequest.setInstanceType(MY_INSTANCE_TYPE);

        //set no. of instances
        runInstancesRequest.setMinCount(1);
        runInstancesRequest.setMaxCount(1);

        //specify the key pair name
        runInstancesRequest.setKeyName(keyPairInfo.getKeyName());

        //make sure to specify securityGroupIds and not groupName
        List<String> securityGroupsIds = new ArrayList<String>();
        securityGroupsIds.add(securityGroup.getGroupId());
        runInstancesRequest.setSecurityGroupIds(securityGroupsIds);

        //set subnet id
        runInstancesRequest.setSubnetId(someRandomSubnet.getSubnetId());

        //launch instance
        return ec2.launchInstance(runInstancesRequest);
    }

    private SecurityGroup createSecurityGroup(String vpcId) throws InterruptedException {
        SecurityGroup securityGroup = ec2.getSecurityGroup(MY_SECURITY_GROUP);
        if(securityGroup != null) return securityGroup;
        CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest();
        securityGroupRequest.setGroupName(MY_SECURITY_GROUP);
        securityGroupRequest.setDescription(MY_SECURITY_GROUP_DESC);
        securityGroupRequest.setVpcId(vpcId);
        String groupId = ec2.createSecurityGroup(securityGroupRequest);
        if(groupId == null) return null;
        Thread.sleep(2000);
        return ec2.getSecurityGroupById(groupId);
    }

    private KeyPairInfo createKeyPair() {
        KeyPairInfo keyPairInfo = ec2.getKeyPairInfo(MY_KEY_PAIR);
        if(keyPairInfo == null) {
            KeyPair keyPair = ec2.createKeyPair(MY_KEY_PAIR);
            try{
                //persist this key pair somewhere
                FileUtils.write(new File("~/.ssh/" + MY_KEY_PAIR + ".pem"), keyPair.getKeyMaterial());
            } catch (Exception e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                logger.info(keyPair.getKeyMaterial());
            }
            keyPairInfo = ec2.getKeyPairInfo(MY_KEY_PAIR);
        }
        return keyPairInfo;
    }



}
