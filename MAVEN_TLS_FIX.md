# Maven TLS Compatibility Fix

## Issue
Maven was encountering SSL handshake failures when downloading dependencies from Maven Central:
```
Could not transfer artifact ... Received fatal alert: handshake_failure
```

## Root Cause
Java 11 with Maven 3.9.6 on Windows 10 was having TLS protocol negotiation issues with Maven Central repository.

## Solution
Set the `MAVEN_OPTS` environment variable to explicitly enable TLSv1.2:

### PowerShell (Windows)
```powershell
$env:MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
mvn clean install
```

### Command Prompt (Windows)
```cmd
set MAVEN_OPTS=-Dhttps.protocols=TLSv1.2
mvn clean install
```

### Bash (Linux/Mac)
```bash
export MAVEN_OPTS="-Dhttps.protocols=TLSv1.2"
mvn clean install
```

## Permanent Fix
To make this permanent, add to your Maven settings or system environment variables:

### Option 1: Maven settings.xml
Add to `~/.m2/settings.xml` or `C:\Users\<username>\.m2\settings.xml`:
```xml
<settings>
  <profiles>
    <profile>
      <id>default</id>
      <properties>
        <https.protocols>TLSv1.2</https.protocols>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>default</activeProfile>
  </activeProfiles>
</settings>
```

### Option 2: System Environment Variable
Add `MAVEN_OPTS=-Dhttps.protocols=TLSv1.2` to your system environment variables.

## Verification
After applying the fix, Maven successfully downloaded all dependencies:
- ✅ All dependencies downloaded successfully
- ✅ Build completed: BUILD SUCCESS
- ✅ WAR file created: target/mfa-poc.war
- ✅ 5 source files compiled successfully

## Build Output
```
[INFO] BUILD SUCCESS
[INFO] Total time:  26.021 s
[INFO] Compiling 5 source files with javac [debug target 11] to target\classes
[INFO] Building war: C:\...\target\mfa-poc.war
```

## Note
This issue is specific to certain Java 11 installations and Maven Central's TLS configuration. Java 17+ typically doesn't have this issue.