# tokyomap-oauth

<img alt="GitHub top language" src="https://img.shields.io/github/languages/top/xyzsince2014/tokyomap-oauth">
<img alt="GitHub tag (latest by date)" src="https://img.shields.io/github/v/tag/xyzsince2014/tokyomap-oauth">

The OIDC authorisation server for https://www.tokyomap.live

## Prerequisites
- Java 8 (Amazon Corretto 8 recommended)
- Maven 3.8+
- Docker
- IntelliJ IDEA (optional, for development)

### Initial Setup (first time only)
```bash
# 1. Clone the repository
git clone https://github.com/xyzsince2014/tokyomap-oauth.git
cd tokyomap-oauth

# 2. Open in IntelliJ IDEA
# File → Open → Select the `app` folder
# IntelliJ will auto-detect the Maven project

# 3. Load Maven dependencies (in IntelliJ)
# Right-click on app/pom.xml → Maven → Reload Project
# Or: View → Tool Windows → Maven → Click reload icon
```

### Build and Run
```bash
# 0. Run postgres and redis containers beforehand

# 1. Build the WAR file
cd ~/workspace/tokyomap-oauth
./app/build-war.sh develop

# 2. Build Docker image (packages the WAR + Tomcat)
./docker-build.sh

# 3. Run Tomcat in Docker container
./docker-run.sh

# 4. Access the application
# http://localhost:8080
```

### Build and Run
```bash
# 0. Run postgres and redis containers beforehand

# 1. Build the WAR file
cd ~/workspace/tokyomap-oauth
./app/build-war.sh develop

# 2. Build Docker image (packages the WAR + Tomcat)
./docker-build.sh

# 3. Run Tomcat in Docker container
./docker-run.sh

# 4. Access the application
```

### Troubleshooting
If IntelliJ shows import errors:
1. File → Invalidate Caches → Invalidate and Restart
2. Right-click pom.xml → Maven → Reload Project
