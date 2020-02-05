#!/bin/bash
python --version
SEPARATOR="========================================================="
###   http://200.14.166.249:9002/api_documentation

function wait_sonar(){
  until $(curl --output /dev/null --silent --head --fail -m 3 $SONAR_LOCAL_BASE_URL/sessions/login); do
    printf "\n\n$SEPARATOR\n"
    printf "..... waiting sonar! ..... -$SONAR_LOCAL_BASE_URL-\n"
    printf "$SEPARATOR\n\n"
    sleep 5
  done

  printf "\n\n$SEPARATOR\n$SEPARATOR\n"
  printf "SERVER is UP!\n"
  printf "$SEPARATOR\n$SEPARATOR\n"

  rm -rf $SONAR_TMP_SYNC
  mkdir -p $SONAR_TMP_SYNC/profiles
  mkdir -p $SONAR_TMP_SYNC/qgates

  printf "\n\n"
  python -u $SONAR_BASEDIR_SYNC/download_remote.py

  if [[ -f "tmp/remote_error" ]]; then
    printf "\n\n$SEPARATOR\n"
    printf "HUBO ERROR AL INTENTAR CONECTAR AL SERVIDOR REMOTO!\n\n"
    cat $SONAR_TMP_SYNC/remote_error
    printf "\n\n"
    exit 1
  fi

  if [[ -f "$SONAR_TMP_SYNC/remote_no_up" ]]; then
    printf "\n\n$SEPARATOR\n"
    printf "EL SERVIDOR REMOTO NO ESTA DISPONIBLE!\n\n"
    cat $SONAR_TMP_SYNC/remote_no_up
    printf "\n\n"
    exit 1
  fi

  printf "\n\n$SEPARATOR\n$SEPARATOR\n"
  printf "$SEPARATOR\n$SEPARATOR\n\n"

  python -u $SONAR_BASEDIR_SYNC/upload_local.py

  if [[ -f "$SONAR_TMP_SYNC/local_error" ]]; then
    printf "\n\n$SEPARATOR\n"
    printf "HUBO ERROR AL INTENTAR CONECTAR AL SERVIDOR LOCAL!\n\n"
    cat $SONAR_TMP_SYNC/local_error
    printf "\n\n"
    exit 1
  fi

  touch $SONAR_TMP_SYNC/.remote_to_local_ready
}

if [[ ! -f "$SONAR_TMP_SYNC/.remote_to_local_ready" ]]; then
  wait_sonar
  printf "\n\n\tSONAR LOCAL CONFIGURADO!  \n\n\n"
fi
