## 1.6.0 (2025-07-10)

### **Update 1.6.0: Thread-Safety & Stability**

### ️ **CRITICAL BUG FIXES**

#### **Asynchronous Operations**
- **FIXED:** Critical server errors and instability caused by performing Bukkit API calls (world loading, unloading, deletion) on asynchronous threads.
- **FIXED:** Potential server freezes when deleting worlds due to slow file operations blocking the main thread.
- **IMPROVED:** All world management operations are now fully thread-safe, preventing data corruption and unexpected crashes.

### **TECHNICAL IMPROVEMENTS**

#### ️ **Architecture**
- **IMPROVED:** Refactored `WorldManager` and `LilWorldsAPI` to correctly separate heavy, non-API tasks (like file deletion) from main-thread-only Bukkit calls.
- **IMPROVED:** The internal list of managed worlds now uses a `ConcurrentHashMap` to guarantee safe access from multiple threads.
- **NEW:** The `deleteWorld` process now returns a `CompletableFuture`, providing a more robust and modern asynchronous API for developers.

### ️ **IMPLEMENTATION**

#### ⚙️ **Correct Asynchronous Pattern**
The API now safely handles threading, allowing developers to call asynchronous methods without worrying about server stability.
```java
// Example: Asynchronously and safely deleting a world
lilWorldsAPI.deleteWorldAsync("world_to_delete").thenAccept(success -> {
    if (success) {
        player.sendMessage("World deleted successfully!");
    } else {
        player.sendMessage("Error: Could not delete the world.");
    }
});
```

### **COMPATIBILITY**

#### ️ **Backward Support**
- **MAINTAINED:** Fully backward compatible. No changes are required for existing code that uses the API.
- **MAINTAINED:** All command syntax and behavior remain identical for users.
- **MAINTAINED:** No configuration migrations are needed.

### **UPDATE SUMMARY**
1.  **Fixed Critical Threading Issues:** Resolved major stability problems by ensuring all Bukkit API calls run on the main server thread.
2.  **Eliminated Server Lag:** Moved slow file deletion operations to a separate thread, preventing server freezes.
3.  **Enhanced Reliability:** Made the plugin significantly more stable and safe for production environments.