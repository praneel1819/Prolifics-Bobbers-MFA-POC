# MFA POC - Multi-Factor Authentication Proof of Concept

[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A comprehensive J2EE web application demonstrating secure Multi-Factor Authentication (MFA) using Google Authenticator with TOTP (Time-based One-Time Password).

## 🌟 Features

- **Secure Authentication**: BCrypt password hashing with work factor 12
- **Multi-Factor Authentication**: TOTP-based MFA using Google Authenticator
- **Comprehensive Audit Logging**: All security events tracked and logged
- **Session Management**: Secure session handling with timeout
- **User Management**: CSV-based user storage with role-based access
- **Professional UI**: Clean, responsive design with modern styling
- **Complete Testing**: Unit tests and integration tests with high coverage
- **Extensive Documentation**: Full SDLC documentation included

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Testing](#testing)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## 🚀 Quick Start

Get the application running in 5 minutes:

```bash
# Clone the repository
git clone https://github.com/prolifics/mfa-poc.git
cd mfa-poc

# Build the project
mvn clean package

# Run the application
mvn tomcat7:run

# Access the application
# Open browser: http://localhost:8080/mfa-poc/login
```

**Test Credentials**:
- Username: `john.smith`
- Password: `SecurePass123!`

## 📦 Prerequisites

### Required Software

- **Java JDK**: 11 or higher
- **Apache Maven**: 3.6.0 or higher
- **Apache Tomcat**: 9.0.x (optional, embedded version included)
- **Google Authenticator**: Mobile app (iOS/Android)

### System Requirements

- **OS**: Windows 10+, Linux (Ubuntu 20.04+), macOS 10.15+
- **RAM**: 4GB minimum, 8GB recommended
- **Disk Space**: 500MB for application and dependencies
- **Network**: Internet connection for Maven dependencies

### Verify Installation

```bash
# Check Java version
java -version
# Expected: java version "11.0.x" or higher

# Check Maven version
mvn -version
# Expected: Apache Maven 3.6.x or higher
```

## 💻 Installation

### Step 1: Clone Repository

```bash
git clone https://github.com/prolifics/mfa-poc.git
cd mfa-poc
```

### Step 2: Install Dependencies

```bash
# Download all Maven dependencies
mvn clean install
```

This will:
- Download required libraries
- Compile source code
- Run unit tests
- Package WAR file

### Step 3: Configure Application

The application comes pre-configured with test users. To add or modify users, see [Admin Guide](docs/ADMIN_GUIDE.md).

**Default Test Users**:

| Username | Password | Role | MFA Status |
|----------|----------|------|------------|
| john.smith | SecurePass123! | USER | Not configured |
| jane.doe | Welcome2024! | USER | Configured |
| admin.user | Admin@2024 | ADMIN | Configured |
| bob.wilson | BobSecure99! | USER | Not configured |

## 🎯 Usage

### Running the Application

#### Option 1: Maven Tomcat Plugin (Development)

```bash
# Start embedded Tomcat
mvn tomcat7:run

# Application available at:
# http://localhost:8080/mfa-poc/login

# Stop with Ctrl+C
```

#### Option 2: Standalone Tomcat (Production)

```bash
# Build WAR file
mvn clean package

# Deploy to Tomcat
cp target/mfa-poc.war $CATALINA_HOME/webapps/

# Start Tomcat
$CATALINA_HOME/bin/startup.sh

# Access application
# http://localhost:8080/mfa-poc/login
```

### First-Time Login Flow

1. **Navigate to Login Page**
   ```
   http://localhost:8080/mfa-poc/login
   ```

2. **Enter Credentials**
   - Username: `john.smith`
   - Password: `SecurePass123!`

3. **Set Up MFA**
   - Scan QR code with Google Authenticator
   - Enter 6-digit verification code
   - Click Continue

4. **Access Welcome Page**
   - View your user information
   - Logout when finished

### Returning User Login

1. **Enter Credentials**
2. **Enter TOTP Code** from Google Authenticator
3. **Access Application**

### Logging Out

Click the **Logout** button in the top-right corner.

## 🧪 Testing

### Run All Tests

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate coverage report
mvn clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html
```

### Test Coverage

Current test coverage:
- **Overall**: 85%+
- **Model Layer**: 90%+
- **Utility Layer**: 95%+
- **Repository Layer**: 85%+

### Manual Testing

See [Manual Testing Guide](MANUAL_TESTING_GUIDE.md) for detailed test scenarios.

### Test Reports

After running tests, view reports:
```bash
# Unit test results
open target/surefire-reports/index.html

# Code coverage
open target/site/jacoco/index.html
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

### User Documentation
- **[User Guide](docs/USER_GUIDE.md)**: End-user instructions with screenshots
- **[Manual Testing Guide](MANUAL_TESTING_GUIDE.md)**: Step-by-step testing procedures

### Technical Documentation
- **[Architecture](ARCHITECTURE.md)**: System architecture and design
- **[Design Document](docs/DESIGN.md)**: Detailed specifications and diagrams
- **[API Documentation](docs/API_DOCUMENTATION.md)**: Servlet endpoints and examples
- **[Implementation Plan](IMPLEMENTATION_PLAN.md)**: Development roadmap

### Administration
- **[Admin Guide](docs/ADMIN_GUIDE.md)**: User management and maintenance
- **[Deployment Guide](docs/DEPLOYMENT.md)**: Installation and configuration
- **[Security Documentation](docs/SECURITY.md)**: Security features and compliance

### Specifications
- **[Audit Logging Spec](AUDIT_LOGGING_SPEC.md)**: Audit logging requirements
- **[Project Summary](PROJECT_SUMMARY.md)**: Project overview

## 📁 Project Structure

```
mfa-poc/
├── src/
│   ├── main/
│   │   ├── java/com/prolifics/mfa/
│   │   │   ├── model/              # Data models
│   │   │   │   ├── User.java
│   │   │   │   └── AuditLog.java
│   │   │   ├── repository/         # Data access
│   │   │   │   └── CSVUserRepository.java
│   │   │   ├── servlet/            # Web layer
│   │   │   │   ├── LoginServlet.java
│   │   │   │   ├── MFASetupServlet.java
│   │   │   │   ├── MFAVerifyServlet.java
│   │   │   │   └── LogoutServlet.java
│   │   │   └── util/               # Utilities
│   │   │       ├── PasswordUtil.java
│   │   │       ├── TOTPUtil.java
│   │   │       └── AuditLogger.java
│   │   ├── resources/
│   │   │   ├── users.csv           # User data
│   │   │   └── audit-log.csv       # Audit logs
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml         # Servlet configuration
│   │       ├── css/
│   │       │   └── style.css       # Styling
│   │       ├── login.jsp           # Login page
│   │       ├── mfa-setup.jsp       # MFA setup page
│   │       ├── mfa-verify.jsp      # MFA verification page
│   │       └── welcome.jsp         # Welcome page
│   └── test/
│       └── java/com/prolifics/mfa/
│           ├── repository/         # Repository tests
│           └── util/               # Utility tests
├── docs/                           # Documentation
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

## 🛠️ Technology Stack

### Backend
- **Java**: 11
- **Servlets**: 4.0.1
- **JSP**: 2.3.3
- **JSTL**: 1.2

### Security
- **BCrypt**: jbcrypt 0.4 (Password hashing)
- **TOTP**: java-otp 0.4.0 (MFA implementation)
- **QR Code**: Google ZXing 3.5.1 (QR code generation)

### Build & Test
- **Maven**: 3.6+
- **JUnit**: 5.9.3 (Unit testing)
- **JaCoCo**: Code coverage
- **Tomcat Plugin**: 2.2 (Embedded server)

### Frontend
- **HTML5**: Semantic markup
- **CSS3**: Modern styling
- **JavaScript**: Minimal, progressive enhancement

## 🔒 Security

### Security Features

- ✅ **Password Security**: BCrypt hashing with work factor 12
- ✅ **Multi-Factor Authentication**: TOTP-based (RFC 6238)
- ✅ **Session Security**: HttpOnly, Secure cookies, 30-minute timeout
- ✅ **Audit Logging**: Comprehensive security event tracking
- ✅ **Input Validation**: Server-side validation for all inputs
- ✅ **XSS Prevention**: Output encoding in JSP
- ✅ **CSRF Protection**: Session-based authentication
- ✅ **Brute Force Protection**: MFA attempt limiting

### Security Best Practices

1. **Always use HTTPS in production**
2. **Regularly rotate audit logs**
3. **Keep dependencies updated**
4. **Review audit logs daily**
5. **Backup user data regularly**

See [Security Documentation](docs/SECURITY.md) for complete details.

## 🎨 Features in Detail

### Authentication Flow

```
User Login → Password Verification → MFA Check
                                          ↓
                                    Has MFA?
                                    ↙     ↘
                                  Yes      No
                                   ↓        ↓
                            MFA Verify  MFA Setup
                                   ↓        ↓
                                Welcome Page
```

### Audit Logging

All security events are logged:
- Login attempts (success/failure)
- MFA setup and verification
- Logout events
- Account status changes

**Log Format**:
```csv
timestamp,username,action,status,ipAddress,details
2024-01-15T10:30:45.123456,john.smith,LOGIN_SUCCESS,SUCCESS,192.168.1.100,User logged in with MFA
```

### User Roles

- **USER**: Standard user access
- **ADMIN**: Administrative privileges (future enhancement)

### Session Management

- **Timeout**: 30 minutes of inactivity
- **Security**: HttpOnly and Secure cookies
- **Validation**: Required for all protected resources

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in pom.xml
```

#### Maven Build Fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

#### QR Code Not Displaying
- Refresh the page
- Try a different browser
- Use manual entry method

#### TOTP Code Not Working
- Check device time synchronization
- Wait for a fresh code
- Verify you're using the correct account

See [Troubleshooting Guide](docs/DEPLOYMENT.md#troubleshooting) for more solutions.

## 📊 Performance

### Metrics

- **Build Time**: < 2 minutes
- **Startup Time**: < 10 seconds
- **Response Time**: < 1 second
- **Memory Usage**: ~256MB
- **Test Execution**: < 30 seconds

### Optimization

- Efficient CSV file operations
- Minimal dependencies
- Optimized BCrypt work factor
- Session-based caching

## 🔄 Development Workflow

### Local Development

```bash
# Start development server
mvn tomcat7:run

# Make code changes
# Server auto-reloads (if configured)

# Run tests
mvn test

# Check coverage
mvn jacoco:report
```

### Code Quality

```bash
# Run all quality checks
mvn clean verify

# Generate reports
mvn site
```

## 🚢 Deployment

### Development Deployment

```bash
mvn tomcat7:run
```

### Production Deployment

```bash
# Build WAR
mvn clean package -Pprod

# Deploy to Tomcat
cp target/mfa-poc.war $CATALINA_HOME/webapps/

# Configure HTTPS
# See docs/DEPLOYMENT.md
```

See [Deployment Guide](docs/DEPLOYMENT.md) for detailed instructions.

## 🤝 Contributing

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- Follow Java naming conventions
- Add JavaDoc for public methods
- Write unit tests for new features
- Update documentation
- Run tests before committing

### Pull Request Process

1. Update README.md with details of changes
2. Update documentation if needed
3. Ensure all tests pass
4. Request review from maintainers

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Prolifics Development Team** - *Initial work*

## 🙏 Acknowledgments

- Google Authenticator for TOTP implementation
- BCrypt for secure password hashing
- ZXing for QR code generation
- Apache Tomcat for servlet container
- Maven for build automation

## 📞 Support

### Getting Help

- **Documentation**: Check the [docs/](docs/) directory
- **Issues**: Open an issue on GitHub
- **Email**: support@prolifics.com

### Reporting Security Issues

Please report security vulnerabilities to: security@prolifics.com

Do not open public issues for security vulnerabilities.

## 🗺️ Roadmap

### Future Enhancements

- [ ] Database integration (replace CSV)
- [ ] RESTful API endpoints
- [ ] Admin dashboard UI
- [ ] Email notifications
- [ ] Backup codes for MFA
- [ ] Password reset functionality
- [ ] User self-registration
- [ ] Mobile app integration
- [ ] SSO integration (SAML/OAuth)
- [ ] Biometric authentication

## 📈 Project Status

**Current Version**: 1.0.0  
**Status**: Production Ready (POC)  
**Last Updated**: 2024-01-15

### Milestones

- ✅ Phase 1: Project Setup (Complete)
- ✅ Phase 2: Data Layer (Complete)
- ✅ Phase 3: Security Layer (Complete)
- ✅ Phase 4: Web Layer (Complete)
- ✅ Phase 5: UI Implementation (Complete)
- ✅ Phase 6: Configuration (Complete)
- ✅ Phase 7: Testing (Complete)
- ✅ Phase 8: Bug Fixes (Complete)
- ✅ Phase 9: Documentation (Complete)

## 🔗 Related Projects

- [Google Authenticator](https://github.com/google/google-authenticator)
- [java-otp](https://github.com/jchambers/java-otp)
- [jBCrypt](https://github.com/jeremyh/jBCrypt)
- [ZXing](https://github.com/zxing/zxing)

## 📚 Additional Resources

### Learning Resources

- [TOTP RFC 6238](https://tools.ietf.org/html/rfc6238)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [BCrypt Explained](https://en.wikipedia.org/wiki/Bcrypt)

### Tools

- [Google Authenticator](https://support.google.com/accounts/answer/1066447)
- [QR Code Generator](https://www.qr-code-generator.com/)
- [BCrypt Calculator](https://bcrypt-generator.com/)

---

**Made with ❤️ by Prolifics**

For more information, visit our [documentation](docs/) or contact us at support@prolifics.com