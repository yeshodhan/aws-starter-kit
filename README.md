# AWS Starter Kit
Starter Kit demonstrating how to get started with AWS Java SDK

## Installation

Clone the repository. Open terminal. Change directory to <PROJECT_ROOT>, and run the below:

```bash
cd <PROJECT_ROOT>
mvn clean install
```

Test run will fail, if aws credentials are not configured.


## Test Run
If you have configured the the AWS credentials in ~/.aws/credentials, the test run will use them.

Else, you can run the test by providing the accessKeyId and secretAccessKey in JVM arguments:
-Daws.accessKeyId=ACCESS_KEY_ID
-Daws.secretAccessKey=SECRET_ACCESS_KEY

Below is a test run log:

```
Feb 18, 2016 11:45:43 PM com.mickoo.aws.starterkit.EC2 createSecurityGroup
INFO: Security Group Created: sg-7c4fef1b
Feb 18, 2016 11:45:46 PM com.mickoo.aws.starterkit.EC2 getKeyPairInfo
INFO: Key Pair my-key-pair does not exist.
Feb 18, 2016 11:45:46 PM com.mickoo.aws.starterkit.EC2 createKeyPair
INFO: Key Pair: my-key-pair created. Fingerprint: 41:3c:3d:93:59:6d:6b:17:2f:c8:4d:8f:1d:de:46:25:e6:c2:77:ae
Feb 18, 2016 11:45:47 PM com.mickoo.aws.starterkit.EC2 startInstance
INFO: Starting instance: i-91029656
Feb 18, 2016 11:45:47 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: pending
Feb 18, 2016 11:45:47 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:45:52 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:45:57 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:46:02 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:46:08 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:46:13 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: pending
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2 isRunning
INFO: Instance State: running
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2 startInstance
INFO: Instance started: i-91029656
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2Test testInstanceLifeCycle
INFO: Private DNS: ip-172-30-0-48.us-west-2.compute.internal
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2Test testInstanceLifeCycle
INFO: Private IP: ip-172-30-0-48.us-west-2.compute.internal
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2Test testInstanceLifeCycle
INFO: Public DNS: ec2-54-187-236-174.us-west-2.compute.amazonaws.com
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2Test testInstanceLifeCycle
INFO: Public IP: 54.187.236.174
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2 allocateElasticIP
INFO: New Elastic IP Allocated. Allocation Id: eipalloc-42db2326 Public IP: 52.36.239.219
Feb 18, 2016 11:46:18 PM com.mickoo.aws.starterkit.EC2 associateElasticIP
INFO: Elastic IP Associated. Association Id: eipassoc-9c7148fb
Feb 18, 2016 11:46:19 PM com.mickoo.aws.starterkit.EC2Test testInstanceLifeCycle
INFO: New Public IP: 52.36.239.219
Feb 18, 2016 11:46:19 PM com.mickoo.aws.starterkit.EC2 disassociateElasticIP
INFO: Elastic IP Disassociated. Association Id: eipassoc-9c7148fb
Feb 18, 2016 11:46:19 PM com.mickoo.aws.starterkit.EC2 releaseElasticIP
INFO: Elastic IP Released. Allocation Id: eipalloc-42db2326
Feb 18, 2016 11:46:19 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:19 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:24 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:30 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:35 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:40 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:45 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopping
Feb 18, 2016 11:46:50 PM com.mickoo.aws.starterkit.EC2 isStopped
INFO: Instance State: stopped
Feb 18, 2016 11:46:50 PM com.mickoo.aws.starterkit.EC2 stopInstance
INFO: Instance stopped: i-91029656
```