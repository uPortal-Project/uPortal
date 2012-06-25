#!/bin/bash
#
# Licensed to Jasig under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Jasig licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a
# copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on
# an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#


# 1.64383561643836 seconds/day results in 10 minute years
# 0.821917808 seconds/day results in 5 minute years
# 600 second idle results in files not touched in 1 year to drop off

SCRIPT_DIR=`dirname $0`

FROM_TAG=$1
shift
TO_TAG=$1
shift
TITLE=$1
shift

LOG_FILE=`mktemp -t gource_git_log.XXXXXX`

echo "Generating log file for $TITLE from $FROM_TAG to $TO_TAG in $LOG_FILE" >&2
git log \
    --pretty=format:user:%aN%n%at \
    --reverse \
    --raw \
    --encoding=UTF-8 \
    --no-renames \
    $FROM_TAG..$TO_TAG > $LOG_FILE
        
echo "Generating video for $TITLE from $FROM_TAG to $TO_TAG" >&2
gource \
    -1920x1080 \
    --max-file-lag 0.8 \
    --max-files 0 \
    --disable-bloom \
    --file-idle-time 0 \
    -a 1 \
    -s 0.821917808 \
    --logo $SCRIPT_DIR/uportal_logo.png \
    --user-image-dir $SCRIPT_DIR/../.git/avatar/ \
    --hide bloom,filenames \
    -r 60 \
    --stop-at-end \
    --title "Development on uPortal $TITLE" \
    --multi-sampling \
    --log-format git \
    $@ \
    $LOG_FILE
 
rm $LOG_FILE

