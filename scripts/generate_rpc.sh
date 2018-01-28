#!/usr/bin/env bash

set -e

if [ "$#" -ne 1 ]; then
    echo "Usage: generate_rpc.sh <PROTO_FILES_DIR>"
    echo "  PROTO_FILES_DIR: location of the loggregator v2 api .proto files"
    exit 1
fi

$(which protoc)

if [ "$?" -ne 0 ]; then
    echo "protoc command must be installed"
    exit 1
fi

PROTO_FILES_DIR=$1

pushd $(git rev-parse --show-toplevel)/src/main/java
    tmp_dir=$(mktemp -d)
    mkdir -p $tmp_dir/loggregator

    cp ${PROTO_FILES_DIR}/*proto $tmp_dir/loggregator

    protoc $tmp_dir/loggregator/*.proto \
        --plugin=protoc-gen-grpc-java=/usr/local/bin/protoc-gen-grpc-java \
        --java_out=. \
        --grpc-java_out=. \
        --proto_path=$tmp_dir/loggregator
popd


