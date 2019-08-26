#!/usr/bin/env bash

set -xe

CMD=${1:-"start"}
PORT=${2:-"8983"}
CORE=${3:-"solr/core/data"}

solr/bin/solr $CMD -p $PORT -s $CORE
