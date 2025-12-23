# Configuration Guide

## URL Pattern Setup

The app uses URL patterns with placeholders to generate download links for different versions of your APK files.

### Pattern Syntax

Use `{version}` as a placeholder in your URL where the version identifier should be inserted.

### Examples

#### Example 1: Simple Version Numbers
If your APKs are named like: `MyApp-v1.2.3.apk`

**Configuration:**
- **App Name**: My App
- **URL Pattern**: `https://myserver.com/downloads/MyApp-{version}.apk`
- **Version Name (when downloading)**: `v1.2.3`

**Result**: Downloads from `https://myserver.com/downloads/MyApp-v1.2.3.apk`

#### Example 2: Build Numbers
If your APKs use build numbers: `app-build-12345.apk`

**Configuration:**
- **App Name**: My App
- **URL Pattern**: `https://cdn.example.com/builds/app-build-{version}.apk`
- **Version Name (when downloading)**: `12345`

**Result**: Downloads from `https://cdn.example.com/builds/app-build-12345.apk`

### Important Notes

1. **Placeholder is Required**: The URL pattern MUST contain `{version}` placeholder
2. **Case Sensitive**: The placeholder must be exactly `{version}` (lowercase, with hyphens)
3. **Valid URL**: The pattern should be a valid HTTP/HTTPS URL
4. **File Extension**: While `.apk` is typical, the app will attempt to download and install any file type

### Testing Your Pattern

Before adding to the app, you can test your URL pattern:

1. Take your pattern: `https://server.com/path/{version}.apk`
2. Mentally replace `{version}` with a real version: `https://server.com/path/1.0.0.apk`
3. Test if that URL works in a browser or curl

Example test:
```bash
curl -I https://cdn.example.com/apps/myapp-v1.2.3.apk
```

If you get a 200 OK response, the pattern is correct!

### Common Issues

**Issue**: "Failed to download: 404"
- **Solution**: The version name might be incorrect, or the file doesn't exist at that location

**Issue**: "URL must contain {version} placeholder"
- **Solution**: Make sure you included the `{version}` placeholder in your URL pattern

**Issue**: "Failed to download: Connection refused"
- **Solution**: Check that the server is accessible and that you have internet connection

### Security Considerations

- Always use HTTPS URLs when possible to ensure secure downloads
- Only download APKs from trusted sources that you control
- The app does not verify APK signatures automatically
