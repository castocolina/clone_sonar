# clone_sonar

# Compose SonarQube 7.x

Repository for image and container creation with sonarqube with the purpose of clone remote base sonarqube.

#### Motivation

The main motivation is keep local sonarqube instance with replica of main sonar for faster develope and code check without larges pipelines.

## Use

### 1) Clone repository

```bash
git clone git@github.com:castocolina/clone_sonar.git && cd clone_sonar
```

### 2) Start container:

```bash
    docker-compose up
```

When news rules and plugins are installed with need sync to check differences. Please make eventually `git pull` to check differences on this repo.

```bash
    docker-compose up --force-recreate
```

If you make `git pull` and see changes on _**Dockerfile**_ exec:

```bash
    docker-compose down && docker-compose up --build
```

## Credentials

The default SonarQube admin credentials are:

- user:admin
- passwd:admin

## TIPS:

- For CLI interaction exec:

```bash
    docker exec -it clone_sonar_sonar_local_1 /bin/bash
```

- Importar los profiles, qualitygates y condiciones del sonar central

Para este ultimo paso se deben hacer lo siguiente:

> - Editar el archivo .env que contiene este repositorio. Se debe colocar la IP:puerto, las credenciales de su sonarqube.

- Instalar python 2.6.6 o superior
- Ejecutar el archivo _sync_script/test_local.sh_

## Changelog

- ...

## TODO:

- Basic
  - Check exec to compare remote plugins, quality profiles with rules, compare quality gates.
  - Check and remote plugins
  - Install remote plugins on docker build
  - Download remote not 'BuiltIn' quality profiles
  - Sync remote quality profile to local instance
  - Download remote not 'BuiltIn' quality gates
  - Sync remote quality profile to local instance
- Basic
  - Install extra local plugins
  - Create extra local quality profiles
  - Enable/Disable local extra rules for profiles
  - Configure local extra quality gates / rules

### References:

    - https://github.com/gravitational/configure
    - https://github.com/sirupsen/logrus
    - https://github.com/fatih/color
