# LilWorlds Changelog

## Version 1.3.0-NoTimeout - "Freedom Update"

**Release Date:** June 13, 2025

### 🎯 **Major Fixes**
- **FIXED:** World creation bug that caused `NoSuchFieldException` when using `/w create` command
  - Fixed reflection issue in `WorldManager.createWorldAdvanced()` method
  - Now properly accesses private fields using `getDeclaredField()` and `setAccessible(true)`
  - World creation with all environments (NORMAL, NETHER, THE_END) now works correctly

### 🚀 **Major Improvements**
- **REMOVED:** Annoying timeout system for rate limiting
  - Players can now perform world operations without waiting for cooldowns
  - Console operations were never rate-limited and remain unchanged
  - Security validation for world names and other inputs is still maintained

- **IMPROVED:** Confirmation system for dangerous operations
  - Confirmation prompts for world deletion and bulk unloading are **kept for safety**
  - **REMOVED:** Timeout expiration on confirmations - they no longer expire
  - Users can now take their time to confirm dangerous operations
  - Clearer confirmation messages with better formatting

### 🔧 **Technical Changes**
- Updated `SecurityUtils.checkRateLimit()` to always return `true`
- Updated `SecurityUtils.canPerformOperation()` to bypass rate limiting
- Modified world deletion confirmation to not expire
- Modified bulk world unload confirmation to not expire
- Improved reflection handling in `WorldManager` for better compatibility

### 🛡️ **Security & Safety**
- **MAINTAINED:** All input validation and security checks
- **MAINTAINED:** Confirmation requirements for dangerous operations
- **MAINTAINED:** Permission system and access controls
- **MAINTAINED:** Security logging for audit trails

### 📝 **User Experience**
- World creation is now instant and reliable
- No more frustrating cooldown messages
- Confirmation prompts are clearer and don't pressure users with timeouts
- Better error messages and user feedback

### 🔄 **Backwards Compatibility**
- All existing commands and permissions remain the same
- Configuration files are fully compatible
- No breaking changes for existing users

---

### 🎉 **Version Name: "Freedom Update"**
This update focuses on removing artificial limitations while maintaining safety. Users now have the freedom to manage worlds without arbitrary timeouts, while still being protected from accidental deletions through the improved confirmation system.

### 🐛 **Bug Reports**
If you encounter any issues with this version, please report them on the GitHub repository with detailed information about your server setup and the specific error messages.