#!/bin/bash
# Sync configs first
aws s3 sync s3://stonksbot ./
# Start the application, we could tune memory size and change GC type here if needed
java -jar stonksbot*.jar