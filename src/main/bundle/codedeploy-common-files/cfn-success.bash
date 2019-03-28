#!/bin/bash
set -e

# Signal ASG that this instance is now healthy
if [[ -f /opt/satago/cfn-success ]]; then
    /opt/satago/cfn-success --soft 'CodeDeploy validation passed.'
fi
