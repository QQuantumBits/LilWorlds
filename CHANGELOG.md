# LilWorlds Changelog

## Version 1.4.0 - "API Revolution"

**Release Date:** June 13, 2025

### 🚀 **MAJOR NEW FEATURES**

#### 🔧 **Complete Developer API System**
- **NEW:** Comprehensive API for developers with full world management capabilities
- **NEW:** `LilWorldsAPI` - Main API entry point with singleton pattern
- **NEW:** `WorldBuilder` - Fluent builder pattern for intuitive world creation
- **NEW:** `WorldManager` - Advanced world management operations with async support
- **NEW:** `WorldInfo` - Comprehensive world information wrapper
- **NEW:** `WorldUtils` - Utility functions for common world operations

#### 📡 **Event System**
- **NEW:** Custom event system for world operations (all cancellable)
- **NEW:** `WorldCreateEvent` - Fired before world creation
- **NEW:** `WorldCreatedEvent` - Fired after successful world creation
- **NEW:** `WorldDeleteEvent` - Fired before world deletion
- **NEW:** `WorldTeleportEvent` - Fired before player teleportation

#### ⚡ **Teleport Command**
- **NEW:** `/world teleport <world>` command (alias: `/w tp <world>`)
- **NEW:** Teleports players to spawn location of specified world
- **NEW:** Security validation and rate limiting
- **NEW:** Permission checking (`lilworlds.world.teleport`)
- **NEW:** Player-only command with appropriate error messages

#### 🔤 **Short Command Aliases**
- **NEW:** `/w i` (info), `/w cr` (create), `/w cl` (clone)
- **NEW:** `/w ld` (load), `/w ul` (unload), `/w rm` (remove)
- **NEW:** `/w imp` (import), `/w ls` (list), `/w ss` (setspawn)
- **NEW:** `/w sus` (setuniversalspawn), `/w cfg` (config), `/w tp` (teleport)

### 🔧 **API Features**

#### 🔄 **Async Operations**
- **NEW:** All I/O operations return `CompletableFuture`
- **NEW:** Non-blocking world loading, unloading, deletion, cloning
- **NEW:** Async world creation with callback support
- **NEW:** Thread-safe operations with proper main thread scheduling

#### 🏗️ **Builder Pattern**
- **NEW:** Fluent world creation: `api.createWorld("name").environment(NORMAL).build()`
- **NEW:** Method chaining for intuitive API usage
- **NEW:** Callback support for success/failure handling
- **NEW:** Async and sync creation methods

#### 🛡️ **Advanced Features**
- **NEW:** World size calculation and formatting
- **NEW:** World backup functionality
- **NEW:** Player evacuation from worlds
- **NEW:** Safe spawn location detection
- **NEW:** World existence checking (loaded/unloaded)

### 📚 **Documentation & Examples**

#### 📖 **Complete Documentation**
- **NEW:** `API_DOCUMENTATION.md` - Comprehensive API guide
- **NEW:** `API_PUBLISHING_GUIDE.md` - Publishing and usage guide
- **NEW:** `API_EXAMPLE.java` - Complete example plugin
- **NEW:** Maven/Gradle dependency setup instructions
- **NEW:** Best practices and performance tips

#### 🎯 **Developer Support**
- **NEW:** JitPack integration for easy dependency management
- **NEW:** GitHub Packages support
- **NEW:** Example implementations for common use cases
- **NEW:** Migration guide from internal APIs

### 🔧 **Core Plugin Improvements**

#### 💬 **Enhanced Commands**
- **IMPROVED:** All commands now support short aliases
- **IMPROVED:** Tab completion for all aliases
- **IMPROVED:** Help system shows available aliases
- **IMPROVED:** Teleport command with full validation

#### 🛡️ **Security & Performance**
- **IMPROVED:** Enhanced world name validation
- **IMPROVED:** Rate limiting for teleport operations
- **IMPROVED:** Security logging for sensitive operations
- **IMPROVED:** Efficient caching of world information

#### 📝 **Messages & UX**
- **NEW:** Teleport-specific messages in `messages.yml`
- **IMPROVED:** Command descriptions show aliases
- **IMPROVED:** Clear error messages for all scenarios
- **IMPROVED:** Consistent command structure

### 🔧 **Technical Implementation**

#### 🏗️ **API Architecture**
```
src/main/java/org/hydr4/lilworlds/api/
├── LilWorldsAPI.java              # Main API class
├── events/                        # Event system
├── utils/                         # Utility classes
└── world/                         # World management
```

#### 🔄 **Async Pattern**
- **NEW:** CompletableFuture-based async operations
- **NEW:** Proper thread management with Bukkit scheduler
- **NEW:** Error handling with callbacks and exceptions
- **NEW:** Thread-safe operations throughout

#### 📦 **Publishing Ready**
- **NEW:** Maven distribution management configured
- **NEW:** JitPack integration ready
- **NEW:** GitHub Packages support
- **NEW:** Proper versioning and artifact management

### 🎯 **Usage Examples**

#### Basic API Usage:
```java
LilWorldsAPI api = LilWorldsAPI.getInstance();
api.createWorld("myworld")
    .environment(World.Environment.NORMAL)
    .generator("superflat")
    .onSuccess(world -> System.out.println("Created!"))
    .buildAsync();
```

#### Event Handling:
```java
@EventHandler
public void onWorldCreate(WorldCreateEvent event) {
    if (event.getWorldName().startsWith("temp_")) {
        event.setCancelled(true);
    }
}
```

### 🔄 **Backward Compatibility**
- **MAINTAINED:** All existing commands work unchanged
- **MAINTAINED:** Existing configurations remain valid
- **MAINTAINED:** No breaking changes to plugin behavior
- **MAINTAINED:** Same security and performance standards

### 📊 **Version Compatibility**
- **Minecraft:** 1.16 - 1.21.5
- **Java:** 8+
- **Bukkit/Spigot/Paper:** All major implementations
- **API Version:** 1.4.0

---

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
