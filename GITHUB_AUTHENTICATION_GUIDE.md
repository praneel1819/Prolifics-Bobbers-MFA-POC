# GitHub Authentication Guide

## Creating a Personal Access Token (PAT)

Follow these steps to create a GitHub Personal Access Token for pushing code:

### Step 1: Go to GitHub Settings
1. Log in to GitHub at https://github.com
2. Click your profile picture in the top-right corner
3. Click **Settings**

### Step 2: Access Developer Settings
1. Scroll down in the left sidebar
2. Click **Developer settings** (at the bottom)

### Step 3: Create Personal Access Token
1. Click **Personal access tokens**
2. Click **Tokens (classic)**
3. Click **Generate new token** → **Generate new token (classic)**

### Step 4: Configure Token
1. **Note**: Enter a description like "Prolifics MFA POC"
2. **Expiration**: Choose your preferred expiration (e.g., 90 days)
3. **Select scopes**: Check the following:
   - ✅ **repo** (Full control of private repositories)
     - This includes: repo:status, repo_deployment, public_repo, repo:invite, security_events

### Step 5: Generate and Copy Token
1. Scroll down and click **Generate token**
2. **IMPORTANT**: Copy the token immediately (it won't be shown again!)
3. Save it securely (you'll need it for the next step)

### Step 6: Use Token to Push Code

Once you have your token, run this command in the terminal:

```bash
git push -u origin master
```

When prompted:
- **Username**: Enter your GitHub username (praneel1819)
- **Password**: Paste your Personal Access Token (NOT your GitHub password)

## Alternative: Using Git Credential Manager

Windows users can use Git Credential Manager to store credentials:

```bash
git config --global credential.helper manager-core
```

Then when you push, a window will pop up for authentication.

## Troubleshooting

If you still have issues:
1. Verify the repository exists: https://github.com/praneel1819/Prolifics-Bobbers-MFA-POC
2. Ensure you have write access to the repository
3. Check that your token has the correct permissions

## Next Steps

After creating your token:
1. Keep this guide open
2. Follow the steps above to create your PAT
3. Come back and let me know when you're ready to push
4. I'll help you complete the push operation