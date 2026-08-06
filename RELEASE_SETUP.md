# 🚀 Release APK Build Setup

## GitHub Actions Workflow

En automatisk workflow er nu tilføjet til `.github/workflows/build-release.yml`.
Den bygger en release APK ved hvert push til `main`, ved hver tag der starter med `v`,
og kan også trigges manuelt.

## Systemkrav

- **Android 12+** (API 31+)
- **Compile SDK**: 36
- **Target SDK**: 36
- **Min SDK**: 24 (Android 7.0+)
- **Java**: 21 (Temurin)

## Sådan bruger du workflowen

### 1. Manuel build (anbefalet til test)

1. Gå til **Actions** fanen i dit GitHub repository
2. Vælg **"Build Release APK"** workflow
3. Klik **"Run workflow"**
4. Vælg `release` som build type
5. Klik **"Run workflow"**
6. Vent ~5-10 minutter
7. Download APK fra **Artifacts** sektionen

### 2. Automatisk build ved push

Workflowen kører automatisk når du pusher til `main`.

### 3. Release build med tag

```bash
git tag -a v1.0.0 -m "First release"
git push origin v1.0.0
```

Workflowen opretter automatisk en GitHub Release med APK'en attached.

## 🔐 Signing (valgfrit men anbefalet)

For at signere APK'en med dit eget keystore:

### Trin 1: Generer et release keystore

```bash
keytool -genkey -v   -keystore my-upload-key.jks   -alias upload   -keyalg RSA   -keysize 2048   -validity 10000   -dname "CN=Age of DAV, O=AI Studio, C=DK"
```

### Trin 2: Konverter til base64

```bash
base64 -i my-upload-key.jks -o keystore.b64
```

### Trin 3: Tilføj GitHub Secrets

Gå til **Settings → Secrets and variables → Actions** og tilføj:

| Secret | Værdi |
|--------|-------|
| `KEYSTORE_BASE64` | Indholdet af `keystore.b64` |
| `STORE_PASSWORD` | Dit keystore password |
| `KEY_PASSWORD` | Dit key password |

Når disse secrets er sat, bygger workflowen automatisk en **signeret release APK**.

## 📦 Output

Workflowen producerer:
- `AgeOfDAV-v{VERSION}-{DATE}-{COMMIT}.apk`
- Uploadet som GitHub Artifact (30 dages retention)
- Ved tags: Attached til GitHub Release

## 🐛 Fejlfinding

### "No release keystore provided"
Dette er normalt — workflowen falder tilbage til debug signing. Tilføj secrets ovenfor for signeret build.

### Build fejler
Tjek **Artifacts → build-reports** for detaljerede fejllogs.

### Gradle cache problemer
Kør workflowen igen — første build downloader dependencies.
