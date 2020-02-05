#!/bin/bash

BASEDIR=$(dirname "$0")
BASEDIR=$(readlink -f $BASEDIR)

sudo -H pip install --upgrade pip
pip install poster

file="$BASEDIR/../env"

if [ -f "$file" ]
then
  echo "$file found."
  echo
  while IFS='=' read -r key value
  do
    [[ $key =~ ^#.* ]] && continue;

    if [ "$key" == "" ]; then
      continue;
    fi
    printf "${key}='${value}'\n"
    eval "export ${key}='${value}'"
  done < "$file"
else
  echo "$file not found."
fi

export SONAR_BASEDIR_SYNC=$BASEDIR

rm -rf $SONAR_TMP_SYNC
source $BASEDIR/populate_sonar.sh
#python -u $BASEDIR/upload_local.py
