# LilWorlds Changelog

## Version 1.3.1-AsyncFix - "Synchronization Fix"

**Release Date:** June 13, 2025

### 🎯 **Critical Bug Fix**
- **FIXED:** `WorldBorderCenterChangeEvent may only be triggered synchronously` error
  - Fixed async world creation that was causing IllegalStateException
  - Implemented hybrid async/sync approach for optimal performance
  - World preparation (reflection, validation, logging) now runs asynchronously
  - Only the critical `creator.createWorld()` call runs synchronously (required by Bukkit API)
  - Maintains performance benefits while respecting Bukkit's threading requirements

### 🚀 **Performance Improvements**
- **NEW:** `createWorldAdvancedAsync()` method for better async handling
- **IMPROVED:** World creation now uses callback-based async pattern
- **OPTIMIZED:** Minimal main thread usage - only for world border events
- **MAINTAINED:** Legacy synchronous method for compatibility

### 🔧 **Technical Changes**
- Added `Consumer<Boolean>` callback pattern for async world creation
- Split world creation into async preparation and sync execution phases
- Added proper error handling for both async and sync phases
- Improved thread safety for world creation operations

---

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
