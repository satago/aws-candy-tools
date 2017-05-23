#!/bin/bash

. $(dirname $0)/common_functions.sh

msg "Running AWS CLI with region: $(get_instance_region)"

# get this instance's ID
INSTANCE_ID=$(get_instance_id)
if [ $? != 0 -o -z "$INSTANCE_ID" ]; then
    error_exit "Unable to get this instance's ID; cannot continue."
fi

# Get current time
msg "Started $(basename $0) at $(/bin/date "+%F %T")"
start_sec=$(/bin/date +%s.%N)

msg "Automatically finding all the ELBs that this instance is registered to..."
get_elb_list $INSTANCE_ID
if [ $? != 0 ]; then
    msg "Couldn't find any, nothing to do here."

    finish_msg
    exit 0
fi

msg "Waiting for instance to register to its load balancers"
for elb in $ELB_LIST; do
    msg "Checking validity of load balancer named '$elb'"
    validate_elb $INSTANCE_ID $elb
    if [ $? != 0 ]; then
        msg "Error validating $elb; cannot continue with this LB"
        continue
    fi

    for elb in $ELB_LIST; do
        wait_for_state "elb" $INSTANCE_ID "InService" $elb
        if [ $? != 0 ]; then
            error_exit "Failed waiting for $INSTANCE_ID to return to $elb"
        fi
    done
done

finish_msg