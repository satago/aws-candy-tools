Version 0.8.1
==============
- `postgresql-parameter-group-template`
  * New parameter `pgRandomPageCost` to allow setting of postgresql parameter `random-page-cost`

Version 0.8.0
==============
- `decrypt-properties.bash`
  * Instead of expecting encrypted properties on a specific `encrypted` folder we expect them where they belong
    and instead rename them to *.properties.encrypted and decrypt to *.properties in the same folder.

Version 0.7.0
==============
- `decrypt-properties.bash`
  * Encrypted properties are now expected to live inside an `encrypted` folder and are decrypted 
    to the parent directory without the previous `.decrypted` extension.
    This is so that any inner references to other properties files inside a properties file are preserved
    between the encrypted and decrypted versions, while at the same time keeping both encrypted and decrypted
    `.properties` file extension name for ide introspection and Spring validation. 

Version 0.6.22
==============
- `postgresql-template`
  * Make `AZIndex` parameter optional by setting it to `-1` (default now);
    this is to support updates from Multi-AZ to Single-AZ.

Version 0.6.21
==============
- `postgresql-template`
  * New parameters to create CloudWatch Alarm for low MIN(BurstBalance, EBSIOBalance%, EBSByteBalance%)
    - `LowIOBalanceAlarmEnabled` (default true)
    - `LowIOBalanceAlarmPeriod` (default 900 seconds)
    - `LowIOBalanceAlarmThreshold` (default 50%)
- Upgrade `aws-candy-tools` itself to use Gradle 7.2

Version 0.6.20
==============
- `postgresql-template`
  * Optional new parameter `DBReplicaInstanceIdentifier`
  * Changing `MyDBReplicaInstance.DeletionPolicy` to `Retain`

Version 0.6.19
==============
- `bin/deploy`
  * fix syntax error in `codedeploy-push-and-wait` 

Version 0.6.18
==============
- `bin/stack`:
  * new command `get-min-size` returning `MinSize` of the stack's auto-scaling group
- `bin/deploy`
  * deploying with `--auto-scaling` now changes desired capacity back to `MinSize`
    instead of original desired capacity after successful deployment;
    this is to avoid over-provisioned ASGs if one of the previous deployments failed
    leaving ASG with capacity bigger than required minimum
- `web-cluster-template`
  * udpated default AMI versions to latest
  * Change ASG's `UpdatePolicy.AutoScalingRollingUpdate.MinInstancesInService`
    from `1` to `MinSize` of auto-scaling group itself,
    so that we always have required minimum in service,
    even during CloudFormation updates.

    So now with `MinSize=2` instead of bringing one instance
    out of service and running updates on it,
    CloudFormation will have to create a new instance,
    perform updates on it, and only after that replace old instance with new,
    healthy one.

Version 0.6.17
==============
- `web-cluster-template`
  * Actually use new parameters from previous release in the template

Version 0.6.16
==============
- `web-cluster-template`
  * Expose parameters responsible for controlling ELB HealthCheck behaviour:
    - `HealthCheckIntervalSeconds` default 10 seconds
    - `HealthCheckTimeoutSeconds` default 5 seconds
    - `HealthyThresholdCount` default 2 times
    - `UnhealthyThresholdCount` default 5 times

Version 0.6.15
==============
- `postgresql-parameter-group-template`
  * Create replica parameters group only on condition CreateReadOnlyReplica=true

Version 0.6.14
==============
- `postgresql-template`
  * New parameter `BackupRetentionPeriod` with default value of 7 days

Version 0.6.13
==============
- `postgresql-template`
  * Use `EngineVersion` parameter to override value from `DBSnapshotIdentifier` if provided
  * New parameters `DBParameterGroupName` and `DBReplicaParameterGroupName`
  * All parameters that were defining parameter group values moved to own template

- `postgresql-parameter-group-template`
  * New stack extracted from `postgresql-template` to manage DB parameter groups separately from DB instances. The reason for this is CloudFormation cannot change the `family` property of parameter group (which includes engine version) once it was created, and will not try to re-create the resource also. So each DB version upgrade requires new parameter group created before DB engine can be upgraded, this cannot happen on the same CloudFormation stack.

Version 0.6.12
=============
- `postgresql-template`
  * allow using io1 storage by ror db

Version 0.6.11
=============
- `build.gradle`
  * due to the deprecation of JCenter bintray, we now publish to a general maven repository. New properties are required in your `gradle.properties` file for `nexusSnapshotRepositoryUrl`, `nexusRepositoryUrl`, as well as `nexusUsername` and `nexusPassword`

Version 0.6.10
=============
- `bin/deploy`
  * ensure changes introduced in 0.6.10 check the sub-command return code properly on all environments  

Version 0.6.9
=============
- `bin/deploy`
  * ensure after the deployment with --auto-scaling enabled we decrease the ASG size only is all instances are successfully registered in ELB (to avoid issues when deployment to the last instance fails, but CodeDeploy marks the whole deployment as successful)

Version 0.6.8
=============
- `web-cluster-template`
  * do not add empty security groups to ALB
  * add support for Cognito auth to ALB


Version 0.6.7
=============
- `web-cluster-template`
  * elasticsearch cluster version as parameter

Version 0.6.6
=============
- `web-cluster-template`
  * changed URL for downloading `get-pip.py` script


Version 0.6.5
=============
- `web-cluster-template`
  * Automatically create the S3 bucket for access logs, if enabled. When updating existing stacks, and the bucket already exists,
    need to use the `Import resources into stack` option

- `bin/stack`
  * dropped `create-access-logs-bucket` command - access log buckets are created by the stacks

Version 0.6.4
=============
- `web-cluster-template`
  * Fix `install-pip` task in cfn-init to use version for Python 2.7

Version 0.6.1
==============
- `web-cluster-template`
  * Add support for additional (up to 2) SSL certificates on the ALB HTTPS listener
  * Fix MyALBTargetGroupHealthyHostCountAlarm dimensions using ARNs instead of Full Names only
  * Fix MyDeploymentGroup reference MyALBTargetGroup without checking the condition whether the load balancer should actually be created 

Version 0.6.1
==============
- `web-cluster-template`
  * Fix issue of referencing ELBAccessLogging S3 folder even if no logging should be used 

Version 0.6.0
==============
- `web-cluster-template`
  * Classic ELBs replaces with Application Load Balancers (ELBv2)
  * Amazon Linux AMIs updated to version 2.0.20201111.0
  * AWS CLI updated to 1.18.210
  * Dropped the ELB latency alarm - it wasn't used and there isn't direct mapping to ALB
  * Added required `VpcId` parameter
  * Added required `SubnetIds` paramters
  * Added optional `HealthCheckPath` parameter
  * Dropped the `InternalELBSubnets` parameter (migrated to `SubnetIds`, now required for all stacks)
  * Dropped the `HealthCheckTarget` paramters (replaces with `HealthCheckPath` containing the path only, protocol and port same as for normal request handling)
  * Instance de-registration/registration from load balancer has been moved to CodeDeploy (dropped calling of `elb/*` scripts from appspec)

**NOTE:** Breaking changes. After the upgrade to 0.6.0 the stack's `ApplicationURL` output has a new value (for newly created ALB). Please make sure to update all references (e.g. DNS aliases).

- `bin/stack`
  * `wait-for-elb-instances <stack-name>` now checks both instance health status in both classic ELBs (created in aws-candy-tools pre 0.6.0) and Applicatiowait-for-elb-instances <stack-name>n Load Balancers (created in aws-candy-tools 0.6.0+) to simplify migration

Version 0.5.7
==============
- `transfer-sftp-template`
    * Parameter named `SubnetId` changed from single item to List `SubnetIds`

Version 0.5.6
==============
- `web-cluster-template`
    * `amazon-cloudwatch-agent` service will run as `root` user (reverted change from 0.5.5)

Version 0.5.5
==============
- `web-cluster-template`
    * new parameter `VolumeSize` to control size of the instance volume. (in gigabytes)
    * creating file `/etc/systemd/system/amazon-cloudwatch-agent.service.d/override.conf` to change the service `Restart` parameter `Always`
    * `amazon-cloudwatch-agent` service will run as `cwagent` user

Version 0.5.4
==============
- `transfer-sftp-template`
    * New CloudFormation template for creating an SFTP file server using AWS Transfer Family

Version 0.5.3
==============
- `pre-hooks.bash`
    * Fix a bug that prevented running multiple pre-hook scripts

Version 0.5.2
==============
- `postgresql-template`
    * New parameter `pgReplicaMaxStandbyDelay` to support long-running queries on replicas

Version 0.5.1
==============
- `postgresql-template`
    * New parameter `DBInstanceVPCSecurityGroupId3` to add an extra SG to an instance

Version 0.5.0
==============
- `postgresql-template`
    * new CloudFormation template to create RDS PostgreSQL database instances with optional read replicas
- `web-cluster-template`
    * friendly names for CloudWatch alarms
    * dropped `DesiredCapacity` stack parameter in favour of `MinSize`
    * `Ingress8080SecurityGroupId` is now optional
    * `InstanceSecurityGroups` is now optional
    * updated default AMI versions to latest
- `bin/*`
    * executables changed bash executable to `#!/usr/bin/env bash`
- Support custom policy for stacks
    * It is now possible to define a custom policy for a stack when it's created
      by adding a file named `stack-policy.json` in the same directory as `<stack>-template.json`.
      `postgresql-template` has a default stack policy that prevents removal and replacement of DB instance.
      https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/protect-stack-resources.html

Version 0.4.12
==============
- `bin/encrypt`
    * Base64-encode secrets transparently before passing them down to `aws kms`:
      https://docs.aws.amazon.com/cli/latest/userguide/cliv2-migration.html#cliv2-migration-binaryparam
    * Drop support for AWS CLI version 1, fail with `Unknown options: --cli-binary-format, base64` 

Version 0.4.11
==============
- `bin/deploy`
    * delay each run in parallel `docker-tag-and-push-all` to avoid throttling error from AWS CLI
- `bin/set-aws-profile`
    * suggest to use `export AWS_PAGER=` as default
- `bin/stack`
    * don't use `BASTION_HOST` as jump-host when `bin/stack` is invoked from the `BASTION_HOST` itself
    * SSH'ing from bastion host to internal instances is now ignoring strict host key checking:
        `-o StrictHostKeyChecking=no -o GlobalKnownHostsFile=/dev/null -o UserKnownHostsFile=/dev/null`

Version 0.4.10
==============
- `bin/deploy`
    * new option `parallel-count` in `docker-tag-and-push-all` command:
        - max number of parallel `docker-tag-and-push` invocations at a time
        - optional, default value is 1

Version 0.4.9
=============
- Bug fix release:
    * `tarRevisions` was missing `version` suffix in the name of revision archive after 0.4.7
    * fix deprecation warnings in `tarRevisions` and `untarRevisions`
    * exclude duplicate entries from revision tarball

Version 0.4.8
=============
- `bin/deploy`
    * `ecr get-login` replaced with function `docker_login_ecr`
- `codedeploy-common-file/pull.bash`
    * `ecr get-login` replaced with function `docker_login_ecr`
- `bin/_common-functions.bash`
    * added function `docker_login_ecr` which does secure log in to ecr using `ecr get-login-password` not `ecr get-login`   
    * removed functions `docker_login_options` and `check_version` - we do not support docker versions < 17.06 any longer
    * function `aws_account_id` extracted from `bin/stack`
- `bin/stack`
    * function `aws_account_id` extracted to `bin/_common-functions.bash`   

Version 0.4.7
=============
- internal cleanup
    * upgrade Gradle wrapper to 6.5 and fix deprecation warnings

Version 0.4.6
=============
- `web-cluster-template`:
    * updated default AMI versions to latest
    * updated default aws-cli version to 1.18.68

Version 0.4.5
=============
- `appspec.yml`:
    * move `register_with_elb.sh` call from `ApplicationStart` to `ValidateService`
      to make it possible to implement custom warm-up logic in `pre-ValidateService`
      before attaching instance back to ELB
   
Version 0.4.4
=============
- `bin/deploy`:
    * ignore error when getting deployment status in `wait`. Just check again.
- `bin/stack`:
    * ignore error when getting count of instances in `wait-for-elb-instances`. Just check again.
   

Version 0.4.3
=============
- `web-cluster-template`:
    * new property `Ingress8080SecurityGroupId` allows specifying
      additional SecurityGroup to access port 8080 of EC2 instances

Version 0.4.2
=============
- `appspec.yml`
    * 'pre-hooks' for `ApplicationStop` now triggered before instance is removed from ELB

Version 0.4.1
=============
- `appspec.yml`
    * added calling 'pre-hooks' to all supported hooks
- `bin/deploy wait`
    * include name of a stack next in the status line

Version 0.4.0
=============
- Updated minimum supported version of `gradle-docker-plugin` to 6.x

Version 0.3.14
==============
- `web-cluster-template`:
    * updated default AMI versions to latest
    * updated default aws-cli version to 1.17.9
- `bin/deploy`
    * new option `--auto-scaling` in `codedeploy-push-and-wait` command:
       - changes desired capacity of associated ASG to at least 2,
       - waits for all instances to appear in ELB,
       - performs CodeDeploy push and waits for successful deployment,
       - scales instances down to original capacity
     
Version 0.3.13
==============
- `aws/elb/common_function`:
    * suspending AutoScaling processes while deploying will be enabled by default now
     
Version 0.3.12
==============
- `bin/stack`: 
    * new command `get-desired-capacity` which returns current desired capacity of the stack auto scaling group
    * in `wait` and `wait-for-elb-instances` commands: improved new lines handling when not running from terminal
- `bin/deploy`: 
    * in `wait` command: improved new lines handling when not running from terminal
- `bin/httping`: 
    * improved new lines handling when not running from terminal
    

Version 0.3.11
==============
- `web-cluster-template`: fix error "No configuration found with name: asg-terminate-listener" when `GracefulShutdownEnabled=false`

Version 0.3.10
==============
- `web-cluster-template`: new parameters for finer control of instance health and termination policies
  * `HealthCheckType` whether to use EC2 (default) or ELB metrics to check instance health
  * `HealthCheckGracePeriod` the amount of time, in seconds, that ASG waits before checking the health status of an EC2 instance that has come into service
  * `TerminationPolicies` a policy or a list of policies that are used to select the instances to terminate
- `bin/stack`:
  * fix a bug in parameter value of generated command hint after `create-change-set`

Version 0.3.9
==============
- `web-cluster-template`: fixed bug of broken dependencies for stacks with graceful shutdown disabled

Version 0.3.8
==============
- `bin/stack`:
  * new command: `create-cloudformation-bucket` creates new account-wide s3 bucket to store CFN templates
  * CFN templates are now sent to s3 bucket before use as they become too big to be used by AWS from local filesystem
- `cloudformation-templates/web-cluster-template` support for graceful shutdown of ASG instances via SQS
  * new parameters:
    * `GracefulShutdownEnabled` (default false)
    * `GracefulShutdownTimeout` (default 600)
  * when `GracefulShutdownEnabled=true`:
    * new `asg-terminate-listener` systemd service will monitor stack's own SQS for ASG shutdown events; on shutdown it will try to execute all `ApplicationStop` hooks from current CodeDeploy deployment
    * `/opt/satago/asg-confirm-instance-termination.sh` can be used to signal successful termination to ASG manually
  * AMI versions => 2.0.20191024.3

Version 0.3.7
==============
- `appspec.yml`
  * added calling 'pre-hooks' before Application stop

Version 0.3.6
==============
- `bin/stack`: new commands
 * `set-desired-capacity <stack-name> <desired-capacity>` to change desired number of instances in ASG
 * `wait-for-elb-instances <stack-name>` to wait for number of healthy instances in ELB to match its ASG

Version 0.3.5
=============
- maintenance release, upgrade project's Gradle to latest (5.6.2)

Version 0.3.4
==============
- `web-cluster-template`: Prevent startup of `rpcbind.service` (and `rpc.socket`)
  * RPC is a security vulnerability, one which can be started remotely even when the service is stopped.
    It must be stopped and disabled, along with its sibling service `rpc.socket`
    which is able to remotely start it.

    This change is not compatible with `Amazon Linux 1`-based images.

Version 0.3.1
==============
- `web-cluster-template`: added support for Amazon Linux 2
  * default AMI versions now use Amazon Linux 2

Version 0.2.25
==============
- `web-cluster-template`: changing type of `ELBSecurityGroups` and `InstanceSecurityGroups` to allow blank defaults.


Version 0.2.24
==============
- `web-cluster-template`: new parameters to specify additional security groups for ELB and EC2 instances.
  * `ELBSecurityGroups`
  * `InstanceSecurityGroups`

Version 0.2.23
==============
- `web-cluster-template`: updated default AMI versions to latest

Version 0.2.22
==============
- `web-cluster-template`: changes to parameters for alarms & metrics
  * parameter `ELBAlarmActions` renamed to `AlarmActions`
  * new parameters:
    * `ELBHealthyHostCountAlarmEnabled` (default false)
    * `ELBLatencyAlarmEnabled` (default false)
    * `ELBLatencyAlarmPeriod` (60 seconds),
      `ELBLatencyAlarmStatistic` (Average),
      `ELBLatencyAlarmThreshold` (10 seconds)
    * `DiskMetricsCollectionInterval` (default 1 hour),
      `MemoryMetricsCollectionInterval` (default 5 minutes),
      `SwapMetricsCollectionInterval` (default 5 minutes)
    * `DiskUsedPercentAlarmEnabled` (default false),
      `DiskUsedPercentAlarmThreshold` (default 85%)
  * ignore `devtmpfs`, `tmpfs`, and `xfs` filesystems from disk monitoring

Version 0.2.21
==============
- `web-cluster-template`: new parameters `DiskMetrics`, `MemoryMetrics`,
   and `SwapMetrics` to collect and publish corresponding metrics
   using CloudWatch agent.

   Full list of supported metrics can be found here:
   https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/metrics-collected-by-CloudWatch-agent.html

Version 0.2.20
==============
- `web-cluster-template`: new parameter `ELBAlarmActions`

    Non-blank value creates a CloudWatch alarm to watch over the
    `HealthyHostCount` metric of ELB associated with current web cluster.

Version 0.2.19
==============
- `web-cluster-template`: new default AMI versions & packages
  * AMI versions => 2018.3 (released on 2018-06-28)
  * docker => 18.03.1ce

Version 0.2.18
==============
- `web-cluster-template`: install AWS CLI using `pip install --upgrade ...`

Version 0.2.17
==============
- `web-cluster-template`: AWS CLI version 1.15.8

Version 0.2.16
==============
- `web-cluster-template`: new default AMI versions & packages
  * AMI versions => 2018.3
  * docker => 17.12.1ce
  * docker-compose => 1.21.0
  * AWS CLI => 1.14.9

Version 0.2.15
==============
- `web-cluster-template`: new default AMI versions with fixes for [Meltdown/Spectre vulnerabilities](https://aws.amazon.com/security/security-bulletins/AWS-2018-013/).

Version 0.2.14
==============
- `web-cluster-template`: new parameter `ELBReferenceSecurityPolicy` to specify one of the [predefined ELB security policies](http://docs.aws.amazon.com/elasticloadbalancing/latest/classic/elb-security-policy-table.html). Default value is `ELBSecurityPolicy-2016-08`.
- Auto-detect expired bundles when using `bin/exec`: the script now runs the `binInit` task not only when bundle is not extracted, but also if its version does not match plugin version.

Version 0.2.13
==============
- `bin/deploy`: Set Gradle project root to the location of gradle wrapper script

Version 0.2.12
==============
- Fix #6: "Gradle wrapper not found" when using `bin/deploy`
- `web-cluster-template``: fix description of DockerVersion parameter

Version 0.2.11
==============
- `web-cluster-template`: upgrade AMI versions to 2017.09.
- `web-cluster-template`: new parameter `CustomImageId` to override region default AMIs.
- `web-cluster-template`: specify `SuspendProcesses` for ASG as per [best practices for ASG rolling updates](https://aws.amazon.com/ru/premiumsupport/knowledge-center/auto-scaling-group-rolling-updates/).
- Fix `_common-functions.bash: line 25: [[: 09: value too great for base (error token is "09")`

Version 0.2.10
==============
- Support Docker 17.06 on client-side: requires AWS CLI version 1.11.91 or higher with `--no-include-email` option for Docker 17.06.
- `web-cluster-template`: new parameter `AWSCLIVersion` with default value `1.11.121`.

Version 0.2.9
=============
- Fix `yum versionlock delete docker` when no Docker is installed/versionlocked.

Version 0.2.8
=============
- Support `SKIP_ELB_WAIT` in `compose.env` to skip ELB wait during deployment validation. It can help, i.e., if you do long-running database migrations as a part of application deployment.
- Support `WAITER_TIMEOUT` in `compose.env` to override ELB health check timeout for the `InService` state, i.e. how much time in seconds we should wait before ELB becomes healthy during deployment validation.

Version 0.2.7
=============
- `yum versionlock delete docker` before installing docker of a given version, i.e. when we need to upgrade docker version, otherwise `yum` isn't able to find requested package of docker for installation. 

Version 0.2.6
=============
- Fix `docker-compose: Text file busy` error seen after `AWS::CloudFormation::Init` metadata update: `cfn-init` was failing due to error in `/opt/satago/install-codedeploy-agent` trying to install already existing package.
- Clean task can now coexist with Gradle's standard clean tasks by attaching to them.

Version 0.2.0
=============

- `web-cluster-template` now requires versions of Docker and Docker Compose to be specified as CloudFormation stack parameters.

  These versions will be fixed and won't be upgraded with `yum update` automatically.

- Added `clean` task to clean temporary folder with scripts & templates.

  Running of this task required after upgrade to new version of the plugin, this is needed to force upgrade of built-in scripts & templates on next run of `bin/exec`. At the moment there's no versioning for scripts, simply bumping version of Gradle plugin isn't enough.
