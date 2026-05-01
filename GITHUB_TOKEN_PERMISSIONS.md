# GitHub Personal Access Token - Required Permissions

## Permissions Needed for Pushing Code

When creating your Personal Access Token on GitHub, you need to select the following scope:

### ✅ Required: `repo` (Full control of private repositories)

Check the **repo** checkbox. This will automatically select all sub-permissions:

```
☑ repo
  ☑ repo:status          - Access commit status
  ☑ repo_deployment      - Access deployment status
  ☑ public_repo          - Access public repositories
  ☑ repo:invite          - Access repository invitations
  ☑ security_events      - Read and write security events
```

### Why This Permission?

The `repo` scope gives you:
- **Push code** to the repository
- **Pull code** from the repository
- **Create branches** and tags
- **Manage repository** settings (if you're an admin)

## What You DON'T Need

You do NOT need to check these for basic code push operations:
- ❌ workflow
- ❌ write:packages
- ❌ delete:packages
- ❌ admin:org
- ❌ admin:public_key
- ❌ admin:repo_hook
- ❌ admin:org_hook
- ❌ gist
- ❌ notifications
- ❌ user
- ❌ delete_repo
- ❌ write:discussion
- ❌ admin:enterprise

## Step-by-Step Visual Guide

1. **On the "New personal access token" page:**
   ```
   Note: Prolifics MFA POC
   Expiration: 90 days (or your preference)
   
   Select scopes:
   ☑ repo                    ← CHECK THIS ONE!
     ☑ repo:status
     ☑ repo_deployment
     ☑ public_repo
     ☑ repo:invite
     ☑ security_events
   ☐ workflow
   ☐ write:packages
   ... (leave others unchecked)
   ```

2. **Scroll down and click "Generate token"**

3. **Copy the token immediately** (format: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)

## Security Best Practices

- ✅ Use a descriptive note (e.g., "Prolifics MFA POC - Dev Machine")
- ✅ Set an expiration date (recommended: 90 days)
- ✅ Only select the minimum permissions needed (`repo` only)
- ✅ Store the token securely (password manager)
- ✅ Never commit the token to your code
- ✅ Regenerate if compromised

## After Creating the Token

Run this command:
```bash
git push -u origin master
```

When prompted:
- **Username:** `praneel1819`
- **Password:** `ghp_your_token_here` (paste your token)

The token will be cached by Git Credential Manager, so you won't need to enter it again.