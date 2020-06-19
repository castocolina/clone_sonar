#!/bin/bash

BASEDIR=$(dirname "$0")
BASEDIR=$(readlink $BASEDIR)

SEPARATOR="======================================"
FILE_TO_REMOVE="$BASEDIR/to_remove"
FILE_TO_INSTALL="$BASEDIR/to_install"

if [ ! -d "$PLUGIN_DIR" ]; then
    mkdir -p $PLUGIN_DIR
fi

ls -la $PLUGIN_DIR
printf "\n\n$SEPARATOR$SEPARATOR\n"
echo "REMOVE OLDS PLUGINS"
printf "\n\n$SEPARATOR$SEPARATOR\n"

while IFS= read -r LINE
do 
    if [[ $LINE == \#* ]] || [[ $LINE == -* ]] || [[ -z $LINE ]] ; then
        continue;
    fi

    printf "\n $SEPARATOR\n $LINE \n"
    file="$PLUGIN_DIR/$LINE"
    rm -rfv $file

done < "$FILE_TO_REMOVE"

ls -la $PLUGIN_DIR

printf "\n\n$SEPARATOR$SEPARATOR\n"
echo "INSTALL PLUGINS"
printf "\n\n$SEPARATOR$SEPARATOR\n"

while IFS= read -r LINE
do 
    if [[ $LINE == \#* ]] || [[ $LINE == -* ]] || [[ -z $LINE ]] ; then
        continue;
    fi

    PURL="$LINE"
    PNAME="${PURL##*/}"
    printf "\n $SEPARATOR\n $PNAME \t $PURL \n"

    PFILE="$PLUGIN_DIR/$PNAME"
    if [ -f "$file" ]; then
        printf "  LOCAL: $PFILE ... found. \n"
    else
        printf "  LOCAL: $PFILE ... NOT found!! \n"
        curl -o "$PFILE" -fSL "$PURL"
    fi

done < "$FILE_TO_INSTALL"
