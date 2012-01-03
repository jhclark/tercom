#!/usr/bin/env bash
set -xeo pipefail
scriptDir=$(dirname $0)
source $scriptDir/constants

dist=tercom-$version
distDir=$scriptDir/dist
mkdir -p $distDir/$dist

cd $scriptDir
ant
cp -r constants \
    LICENSE.txt \
    README.md \
    CHANGELOG.md \
    tercom-$version.jar \
    sample-data \
    $distDir/$dist

cd $distDir
tar -cvzf $dist.tgz $dist
