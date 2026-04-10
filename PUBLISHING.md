# Publishing to Maven Central

This guide explains how to publish the Payvessel Android SDK to Maven Central.

## Prerequisites

### 1. Create a Sonatype Account

1. Go to [Sonatype JIRA](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Create an account
3. Create a new project ticket requesting access to `com.payvessel` namespace
4. Wait for approval (usually 1-2 business days)

### 2. Generate GPG Keys

```bash
# Generate a new GPG key
gpg --full-generate-key

# List keys to get key ID (last 8 characters of the key)
gpg --list-keys --keyid-format SHORT

# Export public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export private key (base64 encoded for CI)
gpg --armor --export-secret-keys YOUR_KEY_ID | base64
```

### 3. Configure Credentials

#### Option A: Local `gradle.properties`

Add to `~/.gradle/gradle.properties`:

```properties
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password
signing.keyId=ABCD1234
signing.password=your_gpg_password
signing.key=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
```

#### Option B: Environment Variables

```bash
export OSSRH_USERNAME=your_sonatype_username
export OSSRH_PASSWORD=your_sonatype_password
export SIGNING_KEY_ID=ABCD1234
export SIGNING_PASSWORD=your_gpg_password
export SIGNING_KEY="-----BEGIN PGP PRIVATE KEY BLOCK-----..."
```

## Publishing

### Publish to Staging

```bash
cd payvessel-android-sdk
./gradlew :payvessel:publishReleasePublicationToSonatypeRepository
```

### Release from Staging

1. Go to [Sonatype Nexus](https://s01.oss.sonatype.org/)
2. Login with your credentials
3. Go to "Staging Repositories"
4. Find your repository (usually named `compayvessel-XXXX`)
5. Click "Close" and wait for validation
6. Click "Release"

### Automated Release (Optional)

Add the Nexus Publish plugin for automated staging:

```kotlin
// root build.gradle.kts
plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("ossrhUsername") as String?)
            password.set(findProperty("ossrhPassword") as String?)
        }
    }
}
```

Then run:
```bash
./gradlew :payvessel:publishToSonatype closeAndReleaseSonatypeStagingRepository
```

## GitHub Actions CI/CD

Create `.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [published]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Publish to Maven Central
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
          SIGNING_KEY: ${{ secrets.SIGNING_KEY }}
          SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
        run: ./gradlew :payvessel:publishReleasePublicationToSonatypeRepository
```

Add secrets to your GitHub repository settings.

## After Publishing

Once published, developers can use:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.payvessel:payvessel-android:1.0.0")
}
```

The package will be available at:
- https://search.maven.org/artifact/com.payvessel/payvessel-android
- https://central.sonatype.com/artifact/com.payvessel/payvessel-android

## Troubleshooting

### Common Issues

1. **"Invalid signature"** - Make sure GPG key is uploaded to keyserver
2. **"Missing POM elements"** - Check that all required POM fields are set
3. **"401 Unauthorized"** - Verify Sonatype credentials
4. **"Namespace not claimed"** - Create Sonatype JIRA ticket for namespace

### Verify POM

```bash
./gradlew :payvessel:generatePomFileForReleasePublication
cat payvessel/build/publications/release/pom-default.xml
```
