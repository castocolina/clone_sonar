#!/bin/bash

{ type "groovy" &> /dev/null && echo "'groovy' is INSTALLED!"; } || \
{
  echo "ERROR: 'groovy' isn't INSTALLED"
  echo "Please install JAVA 11+ & Groovy 3+"
  echo "Install with SDK:"
  echo "   curl -s get.sdkman.io | bash"
  echo "   sdk install groovy"
  exit 1
}

groovy -cp scripts scripts/CheckRuntime.groovy


echo
