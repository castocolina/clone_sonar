# sonar_sync

# Compose SonarQube 8.x-community

The purpose of this repository is sync profiles & quality gates from remote sonarqube with tasks for image generation.

#### Motivation

The main motivation is keep local sonarqube instance with replica of main sonar for faster develope and code check without larges pipelines.

## Use

### 1) Clone repository

```bash
git clone git@github.com:castocolina/sonar_sync.git && cd sonar_sync
```

### 2) Start container:

```bash
    make up
```

When news plugins and rules are installed with need sync to check differences. Please make eventually `git pull` to check differences on this repo.

```bash
    make build
```

If you make `git pull` and see changes on _**Dockerfile**_ exec:

```bash
    make build
```

## Credentials

The default SonarQube admin credentials are:

- user:admin
- password:admin

## TIPS:

- For CLI interaction exec:

```bash
    # enter sonar_local instance
    make enter
```

## Changelog

- Delete olds or unnecessary plugins on docker build
- Install extra local plugins on docker build
- Download remote not 'BuiltIn' quality profiles
- Download remote not 'BuiltIn' quality gates
- Upload quality profiles to local instance
- Upload quality gates to local instance
- Login with token
- Merge multiple quality profiles to new one
- Merge multiple quality gates to new one

## TODO:

- Basic
  - Enable/Disable local extra rules for profiles
  - Create or update local extra metrics for gates
  - Set defaults quality profiles
  - Set defaults quality gates
  - Set system configuration
- Advance:
  - Compare remote plugins, quality profiles with rules, compare quality gates.

### References:
