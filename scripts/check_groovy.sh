#!/bin/bash

{ type "groovy" &> /dev/null && echo "'groovy' is INSTALLED!"; } || \
{
  echo "ERROR: 'groovy' isn't INSTALLED"
  echo "Install with SDK:"
  echo "   curl -s get.sdkman.io | bash"
  echo "   sdk install groovy"
  exit 1
}
echo
