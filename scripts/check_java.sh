#!/bin/bash

jVersionReq=11
jVersion=$(groovy -version | cut -d ':' -f 3 | cut -d '.' -f 2)
echo "The JAVA version is $jVersion"

if [ "$jVersionReq" -gt "$jVersion" ]; then
    echo "With need java version greater than or equal to '$jVersionReq'"
    exit 1
fi

echo
