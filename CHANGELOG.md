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
