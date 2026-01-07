# SonarQube Setup for SafeZone

This directory contains the Docker Compose configuration for running SonarQube locally.

## Quick Start

```bash
# Start SonarQube
cd sonarqube
docker-compose up -d

# Wait for startup (about 1-2 minutes)
# Access at http://localhost:9000
```

## Default Credentials

- **Username:** admin
- **Password:** admin

> ⚠️ **Security Warning:** The default `admin` / `admin` credentials grant full administrative access. If your SonarQube instance is accessible on any network, leaving default credentials is a serious security risk. **Change the admin password immediately on first login.**

## Project Configuration

The project is configured with:
- **Project Key:** safe-zone
- **Project Name:** SafeZone E-commerce Platform

## Jenkins Integration

1. Add SonarQube token as Jenkins credential:
   - Go to Jenkins → Manage Jenkins → Credentials
   - Add "Secret text" credential with ID: `sonarqube-token`

2. The Jenkinsfile includes:
   - **SonarQube Analysis** stage: Scans code for quality issues
   - **Quality Gate** stage: Fails pipeline if quality gate fails

## Quality Gates

SonarQube uses "Sonar way" quality gate by default, which checks:
- No new bugs
- No new vulnerabilities
- No new security hotspots
- Code coverage > 80% on new code
- Duplications < 3% on new code

## Useful Links

- SonarQube Dashboard: http://localhost:9000
- Project Analysis: http://localhost:9000/dashboard?id=safe-zone
- Quality Gates: http://localhost:9000/quality_gates

