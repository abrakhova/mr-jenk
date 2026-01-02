# Jenkins CI/CD Setup

This directory contains the Jenkins configuration for the mr-jenk CI/CD pipeline.

## Quick Start

```bash
# Build custom Jenkins image (includes Maven, Node.js, Docker)
cd jenkins
docker build -t mr-jenk-jenkins:latest .

# Start Jenkins
docker-compose up -d

# View logs (look for the initial admin password)
docker logs jenkins

# Access Jenkins
open http://localhost:8090
```

## Initial Setup

1. **Get the initial admin password from the logs:**
   ```bash
   docker logs jenkins 2>&1 | grep -A 2 "initial"
   ```

2. **Open http://localhost:8090 in your browser**

3. **Paste the admin password**

4. **Install suggested plugins** + these additional plugins:
   - Email Extension Plugin
   - GitHub Integration Plugin
   - Docker Pipeline

5. **Create your admin user**

---

## Creating the Pipeline Job

1. Go to **Dashboard → New Item**
2. Enter name: `mr-jenk-pipeline`
3. Select: **Pipeline**
4. Click **OK**

### Configure the Job:

**General:**
- ✅ GitHub project: `https://github.com/abrakhova/mr-jenk`

**Build Triggers:**
- ✅ GitHub hook trigger for GITScm polling
- ✅ Poll SCM: `H/5 * * * *` (backup every 5 min)

**Pipeline:**
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/abrakhova/mr-jenk.git`
- Branches to build: `*/feature/jenkins-cicd` (or `*/main`)
- Script Path: `Jenkinsfile`

---

## GitHub Webhook Setup (Auto-trigger on Push)

### 1. In GitHub Repository:
1. Go to: **Settings → Webhooks → Add webhook**
2. Configure:
   - **Payload URL:** `http://YOUR_JENKINS_URL:8090/github-webhook/`
   - **Content type:** `application/json`
   - **Secret:** (optional, for security)
   - **Events:** Select "Just the push event"
3. Click **Add webhook**

### 2. For Local Development (ngrok):
Since Jenkins runs locally, use ngrok to expose it:
```bash
# Install ngrok
brew install ngrok

# Expose Jenkins
ngrok http 8090

# Use the ngrok URL for GitHub webhook
# Example: https://abc123.ngrok.io/github-webhook/
```

---

## Email Notifications Setup

### 1. Configure SMTP in Jenkins:
1. Go to: **Manage Jenkins → System**
2. Find: **Extended E-mail Notification**
3. Configure:
   - **SMTP server:** `smtp.gmail.com` (or your SMTP)
   - **SMTP Port:** `465`
   - **Use SSL:** ✅
   - **Credentials:** Add Gmail App Password
   - **Default Recipients:** your-email@example.com

### 2. For Gmail:
1. Enable 2-Factor Authentication in Google Account
2. Generate an App Password: 
   - Go to: https://myaccount.google.com/apppasswords
   - Create password for "Mail" + "Other (Jenkins)"
3. Use your email as username, app password as password

### 3. Test Email:
1. Go to: **Manage Jenkins → System**
2. Scroll to **Extended E-mail Notification**
3. Click **Test configuration by sending test e-mail**

---

## Security Configuration

### 1. Basic Security (Already enabled):
- Matrix-based security
- Admin user with full permissions

### 2. Recommended Security Settings:
1. Go to: **Manage Jenkins → Security**
2. Configure:
   - **Security Realm:** Jenkins' own user database
   - **Authorization:** Matrix-based security
   - **CSRF Protection:** ✅ Enabled

### 3. User Permissions:
| User | Read | Build | Admin |
|------|------|-------|-------|
| admin | ✅ | ✅ | ✅ |
| developers | ✅ | ✅ | ❌ |
| viewers | ✅ | ❌ | ❌ |

### 4. Credentials Management:
1. Go to: **Manage Jenkins → Credentials**
2. Add credentials for:
   - GitHub (if private repo)
   - Docker Hub (if pushing images)
   - SMTP (for email notifications)

**Never store credentials in Jenkinsfile!** Use Jenkins Credentials.

---

## Rollback Strategy

The pipeline includes automatic rollback on failure:

1. **On Build Failure:**
   - All running containers are stopped
   - Email notification sent with error details

2. **Manual Rollback:**
   ```bash
   # Stop current deployment
   docker-compose down
   
   # Deploy previous version (find build number)
   # Go to Jenkins → mr-jenk-pipeline → Build History
   # Click on successful build → Rebuild
   ```

3. **Docker Image Rollback:**
   ```bash
   # List available images
   docker images | grep mr-jenk
   
   # Tag previous version as latest
   docker tag mr-jenk-eureka:v{OLD_VERSION} mr-jenk-eureka:latest
   
   # Restart services
   docker-compose up -d
   ```

---

## Pipeline Stages

| Stage | Description | Fails Build? |
|-------|-------------|--------------|
| Checkout | Clone Git repository | ✅ |
| Generate SSL | Create keystores & certs | ✅ |
| Build Shared | Compile shared module | ✅ |
| Build Backend | Compile 5 services (parallel) | ✅ |
| Build Frontend | npm install + ng build | ✅ |
| Test Backend | Run JUnit tests | ✅ |
| Test Frontend | (Skipped - needs Chrome) | ❌ |
| Build Docker | Build all Docker images | ✅ |
| Deploy | docker-compose up | ✅ |
| Health Check | Verify services running | ❌ |

---

## Test Reports

Test results are:
1. **Published in Jenkins UI** - Click on build → "Test Result"
2. **Archived as artifacts** - Download XML reports
3. **Trend graphs** - View test trends over time

---

## Troubleshooting

### Build fails on "Docker not found"
```bash
# Ensure Docker socket is mounted
docker exec jenkins docker ps
```

### Email not working
1. Check SMTP settings
2. For Gmail, ensure App Password is used
3. Check firewall allows outbound SMTP

### Webhook not triggering
1. Check GitHub webhook delivery logs
2. Ensure Jenkins URL is accessible from internet
3. Use ngrok for local testing

### Tests failing
```bash
# Run tests locally first
cd backend/services/user
../mvnw test
```

---

## Ports

| Port | Service |
|------|---------|
| 8090 | Jenkins Web UI |
| 50000 | Jenkins Agent |

## Data Persistence

Jenkins data is stored in Docker volume `mr-jenk-jenkins-data`.

To reset Jenkins completely:
```bash
docker-compose down -v
```

## Stop Jenkins

```bash
docker-compose down
```
