# DiscordLink

A powerful and user-friendly Discord verification system for your Minecraft server. Seamlessly link player accounts with automatic role assignment and nickname synchronization.

## ✨ Features

### Simple Verification Process
- One-click code copying
- Easy-to-follow instructions
- Secure verification codes
- Automatic role assignment

### Discord Integration
- Automatic nickname synchronization
- Custom role assignment
- Configurable messages
- Slash command support

### Advanced Security
- Unique verification codes
- Configurable code expiry
- Anti-abuse cooldowns
- Secure data storage

### User Experience
- Clean & modern messages
- Clickable commands
- Hover tooltips
- Detailed feedback

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

### 3. Configuration
- Set your bot token in `config.yml`
- Set your server ID
- Set your verified role ID
- Customize messages (optional)

## 🎮 Commands

### Minecraft Commands
- `/verify` - Start verification process
- `/unlink` - Unlink Discord account

### Discord Commands
- `/verify <code>` - Complete verification process

## 🔒 Permissions
- `discordlink.verify` - Allow players to verify their account
- `discordlink.unlink` - Allow players to unlink their account
- `discordlink.admin` - Access to admin commands

## ⚡ Quick Start
1. Player types `/verify` in Minecraft
2. Clicks to copy their unique code
3. Uses `/verify <code>` in Discord
4. Gets automatically verified with role & nickname!

## 🎨 Customization

Fully customize in `config.yml`:
- Message prefix
- All plugin messages
- Verification settings
- Nickname format
- Role management
- Security options

## 📝 Support
Join our [Discord Support Server](https://discord.gg/wMZBfMnNkM) for assistance!

## 💡 Tips
- Keep the bot's role above the verified role
- Enable nickname sync for better server management
- Use custom messages to match your server's style
- Enable debug mode for troubleshooting

## ⚠️ Common Issues

### Bot Can't Assign Roles
- Ensure bot's role is above verified role
- Check bot permissions

### Nickname Sync Not Working
- Verify bot has "Manage Nicknames" permission
- Check role hierarchy
- Enable debug mode for details

### Can't Find Guild/Server
- Double-check guild ID
- Ensure bot is in server
- Check intents are enabled

## 🔄 Version History

### v1.0.1 - Java 21 Update
- Added support for Java 21
- Updated dependencies to latest versions
- Improved compatibility with newer Minecraft versions
- Performance optimizations

### v1.0.0 - Initial Release
- Core verification system
- Nickname synchronization
- Role management
- Clickable codes
- Support for Minecraft 1.20.x - 1.21.4

## 📜 License
Copyright © 2024 GukDev. All rights reserved.
This plugin is proprietary software. Unauthorized distribution, modification, or commercial use is prohibited. 