# DiscordLink

A powerful and user-friendly Discord verification system for your Minecraft server. Seamlessly link player accounts with automatic role assignment, nickname synchronization, and enhanced security features.

## ✨ Features

### Simple Verification Process
- One-click code copying with visual feedback
- Easy-to-follow instructions
- Secure verification codes
- Automatic role assignment

### Discord Integration
- Automatic nickname synchronization
- Custom role assignment
- Configurable messages
- Slash command support
- Discord member leave detection

### Advanced Security
- Unique verification codes
- Configurable code expiry
- Anti-abuse cooldowns
- Secure data storage
- Two-Factor Authentication (2FA) system
- Server selector restrictions for unverified players

### User Experience
- Clean & modern messages
- Clickable commands
- Hover tooltips
- Detailed feedback
- Admin unlink command for staff management

## 📋 Requirements
- Minecraft 1.20.x - 1.21.4 (Paper/Purpur)
- Java 21 or higher
- Discord Bot Token
- Discord Server with admin access

## ⚙️ Setup Guide

### 1. Plugin Installation
- Upload `DiscordLink.jar` to your plugins folder
- Restart your server
- Configure `config.yml`

### 2. Discord Bot Setup
1. Create a bot at [Discord Developer Portal](https://discord.com/developers/applications)
2. Enable "Server Members Intent"
3. Add bot to your server with required permissions:
   - Manage Roles
   - Send Messages
   - Read Messages/View Channels
   - Use Slash Commands
   - Manage Nicknames (optional, for nickname sync)
4. **CRITICAL STEP:** Set up the role hierarchy correctly
   - Go to Server Settings > Roles
   - Make sure the bot's role is ABOVE the verified role in the list
   - This is REQUIRED for the bot to assign roles
   - If this step is skipped, verification will fail!

### 3. Configuration
- Set your bot token in `config.yml`
- Set your server ID
- Set your verified role ID
- Configure 2FA settings
- Configure server selector restrictions
- Customize messages (optional)

## 🎮 Commands

### Minecraft Commands
- `/verify` - Start verification process
- `/unlink` - Unlink Discord account
- `/2fa <code>` - Complete two-factor authentication
- `/discordlink unlink <player>` - Admin command to unlink a player's account

### Discord Commands
- `/verify <code>` - Complete verification process

## 🔒 Permissions
- `discordlink.verify` - Allow players to verify their account
- `discordlink.unlink` - Allow players to unlink their account
- `discordlink.admin` - Access to admin commands (including unlinking other players)
- `discordlink.bypass.2fa` - Allow players to bypass 2FA requirement
- `discordlink.bypass.serverrestriction` - Allow players to bypass server selector restrictions

## ⚡ Quick Start
1. Player types `/verify` in Minecraft
2. Clicks to copy their unique code
3. Uses `/verify <code>` in Discord
4. Gets automatically verified with role & nickname!
5. When rejoining, completes 2FA if enabled

## 🛡️ Security Features

### Two-Factor Authentication (2FA)
- Automatic verification code sent to Discord on join
- Player frozen until verification complete
- Configurable timeout and messages
- Permission-based bypass option

### Server Selector Restrictions
- Prevent unverified players from using server selector
- Clear feedback messages
- Configurable restrictions

### Discord Member Leave Detection
- Automatically unlink accounts when players leave Discord
- Server-side logging
- Prevents unauthorized access

## 🎨 Customization

Fully customize in `config.yml`:
- Message prefix
- All plugin messages
- Verification settings
- Nickname format
- Role management
- Security options
- 2FA settings
- Server selector restrictions

## 📝 Support
Join our [Discord Support Server](https://discord.gg/wMZBfMnNkM) for assistance!

## 💡 Tips
- Keep the bot's role above the verified role
- Enable nickname sync for better server management
- Use custom messages to match your server's style
- Enable debug mode for troubleshooting
- Enable 2FA for enhanced security
- Configure server selector restrictions for controlled access

## ⚠️ Common Issues

### Bot Can't Assign Roles
- **Role Hierarchy Issue** (Most Common Problem):
  - Discord uses a top-down role hierarchy system
  - The bot's role MUST be positioned ABOVE the verified role in Server Settings > Roles
  - If the bot's role is below the verified role, it cannot assign that role to users
  - To fix: Go to Server Settings > Roles and drag the bot's role higher than the verified role
  - This is a Discord permission system requirement, not a plugin limitation
- Check bot permissions (Manage Roles is required)
- Verify the correct role ID is in config.yml
- Look for error messages in the console when users try to verify

### Verification Process Failing
- If you see "Your account is already verified" in Minecraft but Discord shows errors:
  - This indicates a role assignment failure - check role hierarchy first
  - Try unlinking your account with `/unlink` and verify again
  - Check server logs for specific error messages
- Ensure verification codes are being used before they expire
- Confirm the user isn't already verified with a different Minecraft account

### Nickname Sync Not Working
- Verify bot has "Manage Nicknames" permission
- Check role hierarchy - bot's role must be above user's highest role
- **Server Owner Limitation**: Discord does not allow bots to change the server owner's nickname under any circumstances. This is a Discord limitation, not a plugin issue
- Enable debug mode for detailed logs
- Some users may have higher roles that the bot cannot modify

### Can't Find Guild/Server
- Double-check guild ID is correct (enable Developer Mode to see IDs)
- Ensure bot is in the server
- Check intents are enabled (Server Members Intent required)
- Restart Discord bot if recently invited to server

### 2FA Not Working
- Verify Discord bot is properly configured
- Check permissions are set correctly
- Ensure player has verified their account first
- Check console for error messages

## 🔑 Troubleshooting Checklist

If verification is failing, check these items in order:

1. **Discord Role Hierarchy**: The most common issue - make sure bot's role is above the verified role
2. **Bot Permissions**: Verify the bot has "Manage Roles" permission 
3. **Correct IDs**: Confirm guild_id and verified_role_id are correct in config.yml
4. **Bot Status**: Ensure the bot is online and working correctly
5. **Intents**: Verify Server Members Intent is enabled in Discord Developer Portal
6. **Console Errors**: Check server logs for specific error messages
7. **Network Issues**: Ensure your server can connect to Discord's API

## 🔄 Version History

### v1.0.1 - Enhancement Update
- Added Two-Factor Authentication (2FA) system
- Added server selector restrictions for unverified players
- Implemented admin unlink command
- Enhanced verification code button with visual feedback
- Added Discord member leave detection
- Updated to support Java 21
- Performance optimizations and bug fixes

### v1.0.0 - Initial Release
- Core verification system
- Nickname synchronization
- Role management
- Clickable codes
- Support for Minecraft 1.20.x - 1.21.4

## 📜 License
Copyright © 2024 GukDev. All rights reserved.
This plugin is proprietary software. Unauthorized distribution, modification, or commercial use is prohibited. 