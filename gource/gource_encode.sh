#!/bin/bash

# 1.64383561643836 seconds/day results in 10 minute years
# 0.821917808 seconds/day results in 5 minute years
# 600 second idle results in files not touched in 1 year to drop off 

SCRIPT_DIR=`dirname $0`

OUTPUT_FILE=$1
shift

echo "Encoding video to $OUTPUT_FILE" >&2

$SCRIPT_DIR/gource.sh "$@" --hide bloom,filenames,mouse -o - | \
    ffmpeg -y -r 60 -f image2pipe -vcodec ppm -i - -vcodec libx264 -profile:v high -flags +cgop -x264opts bframes=2:keyint=30:min-keyint=1 -crf 1 -threads 0 $OUTPUT_FILE

