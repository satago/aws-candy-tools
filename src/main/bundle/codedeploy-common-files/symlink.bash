#!/bin/bash
set -e

LINK_TARGET=/opt/satago/current

# Remove link to previous install to re-created in the next step
# Don't fail if no symlink existed before
rm -f ${LINK_TARGET}

CURRENT_INSTALL=$(cat /opt/codedeploy-agent/deployment-root/deployment-instructions/${DEPLOYMENT_GROUP_ID}_most_recent_install)

ln -s ${CURRENT_INSTALL}/deployment-archive ${LINK_TARGET}