# LilWorlds

<div align="center">

[![Latest Release](https://img.shields.io/github/v/release/QQuantumBits/LilWorlds?style=for-the-badge&logo=github&color=brightgreen)](https://github.com/QQuantumBits/LilWorlds/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/QQuantumBits/LilWorlds/total?style=for-the-badge&logo=download&color=blue)](https://github.com/QQuantumBits/LilWorlds/releases)
[![Modrinth](https://img.shields.io/modrinth/v/lilworlds?style=for-the-badge&logo=modrinth&color=00AF5C)](https://modrinth.com/plugin/lilworlds)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16--1.21.6-orange?style=for-the-badge&logo=minecraft)](https://www.minecraft.net/)
[![Java Version](https://img.shields.io/badge/Java-8+-red?style=for-the-badge&logo=openjdk)](https://openjdk.org/)

[![Discord](https://img.shields.io/discord/phVzDFAZ3v?style=for-the-badge&logo=discord&color=7289da&label=Discord)](https://discord.gg/phVzDFAZ3v)
[![Documentation](https://img.shields.io/badge/Docs-Available-brightgreen?style=for-the-badge&logo=gitbook)](https://hydr4.mintlify.app/lilworlds)
[![License](https://img.shields.io/github/license/Hydr46605/LilWorlds?style=for-the-badge&color=blue)](LICENSE)
[![bStats](https://img.shields.io/badge/bStats-Metrics-brightgreen?style=for-the-badge&logo=chartdotjs)](https://bstats.org/plugin/bukkit/LilWorlds)

[![Issues](https://img.shields.io/github/issues/QQuantumBits/LilWorlds?style=for-the-badge&logo=github)](https://github.com/QQuantumBits/LilWorlds/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/QQuantumBits/LilWorlds?style=for-the-badge&logo=github)](https://github.com/QQuantumBits/LilWorlds/pulls)
[![Stars](https://img.shields.io/github/stars/QQuantumBits/LilWorlds?style=for-the-badge&logo=github&color=yellow)](https://github.com/QQuantumBits/LilWorlds/stargazers)
[![Forks](https://img.shields.io/github/forks/QQuantumBits/LilWorlds?style=for-the-badge&logo=github)](https://github.com/QQuantumBits/LilWorlds/network/members)

</div>

---

<div align="center">
  <h3>🌍 Plugin performante e modulare per la gestione dei mondi su server Minecraft</h3>
  <p><strong>Supporta le versioni dalla 1.16 alla 1.21.6</strong></p>
</div>

---

## 📋 Indice

- [🚀 Caratteristiche](#-caratteristiche)
- [🔧 API per Sviluppatori](#-api-per-sviluppatori)
- [📦 Installazione](#-installazione)
- [⚡ Quick Start](#-quick-start)
- [🎮 Comandi](#-comandi)
- [🔐 Permessi](#-permessi)
- [🌀 Sistema Portali](#-sistema-portali)
- [⚙️ Configurazione](#️-configurazione)
- [🛠️ Generatori Personalizzati](#️-generatori-personalizzati)
- [📊 Sistema Inventari Separati](#-sistema-inventari-separati)
- [🔗 Integrazione PlaceholderAPI](#-integrazione-placeholderapi)
- [📄 Licenza](#-licenza)

---

## 🚀 Caratteristiche

### 🌍 Gestione Mondi
- **Crea mondi** con ambienti e generatori personalizzati
- **Clona mondi esistenti** con tutti i loro dati
- **Carica e scarica mondi** dinamicamente
- **Importa mondi esterni** senza problemi
- **Sistema di spawn universale** per il teletrasporto tra mondi
- **Rimozione sicura dei mondi** con conferma e backup

### 🌀 Sistema Portali Integrato
- **Portali personalizzati** - Crea portali tra qualsiasi mondo con facilità
- **Integrazione WorldEdit** - Usa le selezioni WorldEdit per creare portali rapidamente
- **Coordinate manuali** - Supporto completo per coordinate manuali senza WorldEdit
- **Gestione frame** - Costruzione e rimozione automatica dei frame dei portali
- **Sistema cooldown** - Prevenzione spam con cooldown configurabile
- **Salvataggio persistente** - Tutti i portali vengono salvati e persistono tra i riavvii

### 📦 Sistema Inventari Separati
- **Inventari separati per mondo/gruppo** - I giocatori possono avere inventari diversi in mondi diversi
- **Gruppi di mondi** - Condividi inventari tra mondi dello stesso gruppo
- **Separazione configurabile** - Scegli cosa separare: inventario, salute, esperienza, modalità di gioco, volo, effetti pozioni, posizione, ender chest
- **Gestione avanzata cache** - Timeout configurabile, auto-salvataggio, backup
- **Comandi di gestione** - Abilita/disabilita, stato, gestione gruppi

### ⚡ Focalizzato sulle Performance
- Caricamento e scaricamento mondi ottimizzato
- Operazioni asincrone dove possibile
- Gestione memoria e pulizia automatica
- Impatto minimo sul server
- Sistema di rate limiting per prevenire spam

### 🔧 Design Modulare
- Sistema generatori personalizzati con configurazione YAML
- Opzioni di configurazione complete
- Integrazione PlaceholderAPI
- Integrazione metriche bStats
- Auto-aggiunta chiavi di configurazione mancanti

### 📊 Funzionalità Avanzate
- **Generatori Personalizzati**: Crea generatori di mondi personalizzati usando file YAML
- **Logging Completo**: Traccia tutte le operazioni sui mondi e i comandi dei giocatori
- **Tab Completion**: Completamento automatico completo per tutti i comandi
- **Sistema Permessi**: Controllo granulare dei permessi
- **Gestione Errori**: Gestione errori robusta con logging dettagliato
- **Sistema Messaggi**: Messaggi completamente personalizzabili in messages.yml

---

## 🔧 API per Sviluppatori

LilWorlds 1.4.0 introduce una **API completa e potente** per sviluppatori che vogliono integrare la gestione dei mondi nei loro plugin.

### ✨ Caratteristiche API

- **🔄 Operazioni Asincrone**: Tutte le operazioni I/O utilizzano CompletableFuture
- **🏗️ Builder Pattern**: Creazione intuitiva dei mondi con method chaining
- **📡 Sistema Eventi**: Eventi personalizzati per operazioni sui mondi (cancellabili)
- **🛡️ Thread Safety**: Sincronizzazione appropriata e scheduling sul main thread
- **⚠️ Gestione Errori**: Gestione errori completa con callback
- **🔧 Funzioni Utility**: Metodi helper per operazioni comuni

### 🚀 Quick Start API

**Aggiungi la dipendenza (JitPack):**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.QQuantumBits</groupId>
        <artifactId>LilWorlds</artifactId>
        <version>v1.4.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Esempio di utilizzo:**

```java
import org.hydr4.lilworlds.api.LilWorldsAPI;

// Ottieni l'istanza API
LilWorldsAPI api = LilWorldsAPI.getInstance();

// Crea un mondo con il builder pattern
api.createWorld("myworld")
    .environment(World.Environment.NORMAL)
    .generator("superflat")
    .structures(true)
    .onSuccess(world -> {
        System.out.println("Mondo creato: " + world.getName());
    })
    .onFailure(error -> {
        System.err.println("Errore: " + error);
    })
    .buildAsync();

// Operazioni asincrone
api.getWorldManager().loadWorldAsync("myworld").thenAccept(success -> {
    if (success) {
        System.out.println("Mondo caricato!");
    }
});
```

### 📚 Documentazione API

- **[📖 Documentazione Completa](API_DOCUMENTATION.md)** - Guida completa all'API
- **[🚀 Guida Publishing](API_PUBLISHING_GUIDE.md)** - Come pubblicare e usare l'API
- **[💡 Esempi Pratici](API_EXAMPLE.java)** - Plugin di esempio completo

### 🎯 Componenti Principali

| Componente | Descrizione |
|------------|-------------|
| `LilWorldsAPI` | Punto di accesso principale all'API |
| `WorldBuilder` | Builder per creazione mondi con pattern fluent |
| `WorldManager` | Operazioni avanzate di gestione mondi |
| `WorldInfo` | Wrapper completo per informazioni sui mondi |
| `WorldUtils` | Funzioni utility per operazioni sui mondi |

---

## 📦 Installazione

### Requisiti
- **Server Minecraft**: 1.16 - 1.21.6 (Bukkit/Spigot/Paper)
- **Java**: 8 o superiore
- **Dipendenze**: Nessuna (PlaceholderAPI è opzionale)

### Passi di Installazione

1. **Scarica** l'ultima release dal [GitHub Releases](https://github.com/Hydr46605/LilWorlds/releases/latest)
2. **Posiziona** il file JAR nella cartella `plugins/` del tuo server
3. **Riavvia** il server
4. **Configura** il plugin in `plugins/LilWorlds/config.yml`
5. **Crea** generatori personalizzati in `plugins/LilWorlds/generators/` (opzionale)

---

## ⚡ Quick Start

```bash
# Crea il tuo primo mondo
/world create mymundo

# Clona un mondo esistente
/world clone world myworld_copy

# Imposta lo spawn universale
/world setuniversalspawn

# Abilita inventari separati
/worlds inventory enable

# Ricarica la configurazione
/worlds reload
```

---

## 🎮 Comandi

### Comandi Mondo (`/world` o `/w`)
- `/world info [mondo]` - Visualizza informazioni dettagliate del mondo
- `/world create <nome> [ambiente] [-g generatore]` - Crea un nuovo mondo
- `/world clone <sorgente> <destinazione>` - Clona un mondo esistente
- `/world load [mondo]` - Carica mondo/i dal disco
- `/world unload [mondo]` - Scarica mondo/i dalla memoria
- `/world remove <mondo>` - Rimuovi un mondo (con conferma)
- `/world delete <mondo>` - Alias per remove
- `/world import <nome> [ambiente] [-g generatore]` - Importa mondo esterno
- `/world setspawn` - Imposta la posizione di spawn del mondo
- `/world setuniversalspawn` - Imposta la posizione di spawn universale
- `/world config <enable|disable|set> <chiave> [valore]` - Modifica configurazione
- `/world list` - Elenca tutti i mondi con informazioni dettagliate

### Comandi Portali (`/portal`)
- `/portal create <nome> <mondo_destinazione>` - Crea portale con selezione WorldEdit
- `/portal create <nome> <mondo> <x1> <y1> <z1> <x2> <y2> <z2>` - Crea portale manualmente
- `/portal delete <nome>` - Elimina un portale
- `/portal list` - Elenca tutti i portali
- `/portal info <nome>` - Mostra informazioni dettagliate portale
- `/portal tp <nome>` - Teletrasportati attraverso un portale
- `/portal frame <create|remove> <nome>` - Gestisci frame portali
- `/portal reload` - Ricarica configurazione portali

### Comandi Plugin (`/worlds`)
- `/worlds reload [target]` - Ricarica componenti del plugin
- `/worlds rl [target]` - Alias per reload
- `/worlds inventory <azione>` - Gestisci inventari separati
- `/worlds inv <azione>` - Alias per inventory
- `/worlds groups <azione>` - Gestisci gruppi di mondi per inventari

#### Target Ricaricamento
- `all` - Ricarica tutto
- `config` - Ricarica file di configurazione
- `generators` - Ricarica generatori personalizzati
- `worlds` - Ricarica gestore mondi

#### Azioni Inventario
- `enable` - Abilita inventari separati
- `disable` - Disabilita inventari separati
- `status` - Mostra stato e configurazione
- `clear <giocatore|all>` - Pulisci cache inventari

#### Azioni Gruppi
- `list` - Elenca tutti i gruppi di mondi configurati
- `add <mondo> <gruppo>` - Aggiungi mondo a un gruppo
- `remove <mondo>` - Rimuovi mondo dal suo gruppo

---

## 🔐 Permessi

### Permessi Mondo
- `lilworlds.world.*` - Tutti i comandi mondo
- `lilworlds.world.info` - Visualizza informazioni mondo
- `lilworlds.world.create` - Crea nuovi mondi
- `lilworlds.world.clone` - Clona mondi esistenti
- `lilworlds.world.load` - Carica mondi
- `lilworlds.world.unload` - Scarica mondi
- `lilworlds.world.remove` - Rimuovi mondi
- `lilworlds.world.delete` - Alias per remove
- `lilworlds.world.import` - Importa mondi esterni
- `lilworlds.world.setspawn` - Imposta posizioni spawn mondo
- `lilworlds.world.setuniversalspawn` - Imposta spawn universale
- `lilworlds.world.config` - Modifica configurazione
- `lilworlds.world.list` - Elenca mondi

### Permessi Portali
- `lilworlds.portal.*` - Tutti i comandi portali
- `lilworlds.portal.create` - Crea nuovi portali
- `lilworlds.portal.delete` - Elimina portali
- `lilworlds.portal.list` - Elenca tutti i portali
- `lilworlds.portal.info` - Visualizza informazioni portali
- `lilworlds.portal.teleport` - Teletrasportati attraverso portali
- `lilworlds.portal.frame` - Gestisci frame portali
- `lilworlds.portal.reload` - Ricarica configurazione portali

### Permessi Plugin
- `lilworlds.worlds.*` - Tutti i comandi gestione plugin
- `lilworlds.worlds.reload` - Ricarica componenti plugin
- `lilworlds.worlds.inventory` - Gestisci inventari separati
- `lilworlds.worlds.groups` - Gestisci gruppi mondi

---

## 🌀 Sistema Portali

LilWorlds 1.5.0 introduce un sistema di portali integrato e completo che permette di creare portali personalizzati tra mondi diversi.

### ✨ Caratteristiche Portali
- **Integrazione WorldEdit**: Usa `//wand` per selezionare aree facilmente
- **Coordinate manuali**: Supporto completo senza WorldEdit
- **Salvataggio persistente**: I portali vengono salvati in `portals.yml`
- **Gestione frame**: Costruzione automatica dei frame dei portali
- **Sistema cooldown**: Prevenzione spam con cooldown di 3 secondi
- **Permessi granulari**: Controllo completo degli accessi

### 🚀 Quick Start Portali

1. **Installa WorldEdit** (opzionale ma raccomandato)
2. **Seleziona area portale**:
   ```
   //wand
   ```
   Clicca due angoli per definire l'area del portale

3. **Crea il portale**:
   ```
   /portal create mio_portale mondo_destinazione
   ```

4. **Costruisci frame** (opzionale):
   ```
   /portal frame create mio_portale
   ```

### 📝 Esempi Uso

```bash
# Crea portale con WorldEdit
//wand
# Seleziona area con il wand
/portal create spawn_nether world_nether

# Crea portale con coordinate manuali
/portal create hub_portal world_hub 10 64 10 15 70 15 0 64 0

# Gestisci portali
/portal list
/portal info spawn_nether
/portal tp spawn_nether
/portal frame create spawn_nether
/portal delete vecchio_portale
```

Per maggiori dettagli, consulta la documentazione.

---

## ⚙️ Configurazione

Il plugin utilizza diversi file di configurazione:

### config.yml
File di configurazione principale con impostazioni generali, predefiniti, limiti e opzioni di performance.

### worlds.yml
Memorizza informazioni sui mondi gestiti (auto-generato).

### messages.yml
Messaggi personalizzabili per tutto l'output del plugin.

---

## 🛠️ Generatori Personalizzati

LilWorlds supporta generatori di mondi personalizzati tramite file di configurazione YAML posizionati nella cartella `generators/`.

### Esempio Configurazione Generatore

```yaml
display-name: "Mondo Piatto Personalizzato"
description: "Un mondo piatto personalizzato con strati specifici"
type: "FLAT"

# Generazione strutture
generate-structures: true
generate-villages: true
generate-strongholds: false
generate-mineshafts: true
generate-temples: true
generate-ravines: false
generate-caves: false
generate-dungeons: true

# Biomi
biomes:
  - "PLAINS"
  - "FOREST"
  - "DESERT"

# Strati (dal basso verso l'alto)
layers:
  - "minecraft:bedrock"
  - "minecraft:stone:5"
  - "minecraft:dirt:3"
  - "minecraft:grass_block"

# Generazione minerali
ores:
  coal:
    frequency: 20
    min-height: 1
    max-height: 128
  iron:
    frequency: 15
    min-height: 1
    max-height: 64
  gold:
    frequency: 5
    min-height: 1
    max-height: 32
  diamond:
    frequency: 2
    min-height: 1
    max-height: 16

# Impostazioni personalizzate
settings:
  sea-level: 63
  spawn-x: 0
  spawn-z: 0
```

---

## 📊 Sistema Inventari Separati

LilWorlds offre un sistema avanzato di inventari separati che permette ai giocatori di avere inventari diversi in mondi diversi o gruppi di mondi.

### Configurazione Inventari

```yaml
features:
  separate-inventories:
    # Abilita la funzionalità inventari separati
    enabled: false
    
    # Cosa separare tra i mondi
    separate:
      inventory: true      # Inventario giocatore e armatura
      health: true         # Salute e livelli cibo
      experience: true     # Esperienza e livelli
      gamemode: false      # Modalità di gioco (mantieni uguale tra mondi)
      flight: true         # Impostazioni volo
      potion-effects: true # Effetti pozioni attivi
      location: false      # Posizione giocatore (spawn al mondo spawn se true)
      enderchest: true     # Contenuti ender chest
    
    # Gruppi di mondi - i giocatori condividono inventari nello stesso gruppo
    world-groups:
      survival: ["world", "world_nether", "world_the_end"]
      creative: ["creative_world", "build_world"]
      minigames: ["pvp_arena", "spleef_arena"]
    
    # Gruppo predefinito per mondi non specificati
    default-group: "default"
    
    # Impostazioni avanzate
    advanced:
      save-to-files: true        # Salva dati su file (persistente tra riavvii)
      cache-timeout: 30          # Timeout cache in minuti
      auto-save-interval: 300    # Intervallo auto-salvataggio in secondi
      backup-on-switch: false    # Backup dati giocatore prima del cambio
      clear-cache-on-unload: true # Pulisci cache allo scaricamento mondo
```

---

## 🔗 Integrazione PlaceholderAPI

LilWorlds fornisce un supporto estensivo per PlaceholderAPI:

### Placeholder Generali
- `%lilworlds_current_world%` - Nome mondo attuale
- `%lilworlds_current_world_type%` - Ambiente mondo attuale
- `%lilworlds_current_world_difficulty%` - Difficoltà mondo attuale
- `%lilworlds_current_world_pvp%` - Stato PvP mondo attuale
- `%lilworlds_current_world_players%` - Giocatori nel mondo attuale
- `%lilworlds_total_worlds%` - Totale mondi caricati
- `%lilworlds_managed_worlds%` - Conteggio mondi gestiti
- `%lilworlds_custom_generators%` - Conteggio generatori personalizzati
- `%lilworlds_version%` - Versione plugin

### Placeholder Specifici per Mondo
- `%lilworlds_world_exists_<nomemondo>%` - Controlla se il mondo esiste
- `%lilworlds_world_loaded_<nomemondo>%` - Controlla se il mondo è caricato
- `%lilworlds_world_players_<nomemondo>%` - Giocatori in un mondo specifico
- `%lilworlds_world_type_<nomemondo>%` - Ambiente mondo
- `%lilworlds_world_difficulty_<nomemondo>%` - Difficoltà mondo
- `%lilworlds_world_pvp_<nomemondo>%` - Stato PvP mondo
- `%lilworlds_world_time_<nomemondo>%` - Tempo mondo
- `%lilworlds_world_weather_<nomemondo>%` - Meteo mondo
- `%lilworlds_is_managed_<nomemondo>%` - Controlla se il mondo è gestito

### Placeholder Spawn Universale
- `%lilworlds_has_universal_spawn%` - Controlla se lo spawn universale è impostato
- `%lilworlds_universal_spawn_world%` - Nome mondo spawn universale

### Placeholder Inventari Separati
- `%lilworlds_inventory_enabled%` - Controlla se gli inventari separati sono abilitati
- `%lilworlds_inventory_group%` - Gruppo inventario del mondo attuale
- `%lilworlds_inventory_cache_size%` - Dimensione cache inventari

---

## 🏆 Funzionalità Principali

### 🔄 Auto-Aggiornamento Configurazione
- Le chiavi di configurazione mancanti vengono aggiunte automaticamente
- Preserva le impostazioni esistenti dell'utente
- Logging trasparente di tutte le modifiche

### 🎨 Sistema Messaggi Personalizzabili
- Tutti i messaggi sono configurabili in `messages.yml`
- Supporto per placeholder dinamici
- Messaggi di errore consistenti e informativi

### 🛡️ Sicurezza e Rate Limiting
- Protezione contro spam di comandi
- Console esente da rate limiting
- Conferme per operazioni pericolose

### 📊 Logging Completo
- Tracciamento di tutte le operazioni sui mondi
- Debug configurabile per risoluzione problemi
- Metriche di performance opzionali

---

## 📊 Statistiche

<div align="center">

[![bStats Servers](https://img.shields.io/bstats/servers/YOUR_PLUGIN_ID?style=for-the-badge&logo=chartdotjs&label=Servers)](https://bstats.org/plugin/bukkit/LilWorlds)
[![bStats Players](https://img.shields.io/bstats/players/YOUR_PLUGIN_ID?style=for-the-badge&logo=chartdotjs&label=Players)](https://bstats.org/plugin/bukkit/LilWorlds)

</div>

---

## 🤝 Contribuire

Contributi sono sempre benvenuti! Ecco come puoi aiutare:

1. **🍴 Fork** il repository
2. **🌿 Crea** un branch per la tua funzionalità (`git checkout -b feature/AmazingFeature`)
3. **💾 Commit** le tue modifiche (`git commit -m 'Add some AmazingFeature'`)
4. **📤 Push** al branch (`git push origin feature/AmazingFeature`)
5. **🔄 Apri** una Pull Request

### 📋 Linee Guida per Contribuire
- Segui lo stile di codice esistente
- Aggiungi test per nuove funzionalità
- Aggiorna la documentazione se necessario
- Assicurati che tutti i test passino

---

## 📄 Licenza

Questo progetto è licenziato sotto la **Licenza MIT** - vedi il file [LICENSE](LICENSE) per i dettagli.

```
MIT License

Copyright (c) 2024 Hydr4

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">

### 🌟 Se LilWorlds ti è utile, considera di dargli una stella! ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=Hydr46605/LilWorlds&type=Date)](https://star-history.com/#Hydr46605/LilWorlds&Date)

---

**LilWorlds** - *Rendere la gestione dei mondi semplice, veloce e potente.*

Made with ❤️ by [Hydr4](https://github.com/Hydr46605)

</div>
