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
