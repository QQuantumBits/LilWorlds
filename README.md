# LilWorlds

Un plugin performante e modulare per la gestione dei mondi su server Minecraft che supporta le versioni dalla 1.16 alla 1.21.5.

## Caratteristiche

### 🌍 Gestione Mondi
- **Crea mondi** con ambienti e generatori personalizzati
- **Clona mondi esistenti** con tutti i loro dati
- **Carica e scarica mondi** dinamicamente
- **Importa mondi esterni** senza problemi
- **Sistema di spawn universale** per il teletrasporto tra mondi
- **Rimozione sicura dei mondi** con conferma e backup

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

## Comandi

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

## Permessi

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

### Permessi Plugin
- `lilworlds.worlds.*` - Tutti i comandi gestione plugin
- `lilworlds.worlds.reload` - Ricarica componenti plugin
- `lilworlds.worlds.inventory` - Gestisci inventari separati
- `lilworlds.worlds.groups` - Gestisci gruppi mondi

## Generatori Personalizzati

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

## Sistema Inventari Separati

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

## Integrazione PlaceholderAPI

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

## Configurazione

Il plugin utilizza diversi file di configurazione:

### config.yml
File di configurazione principale con impostazioni generali, predefiniti, limiti e opzioni di performance.

### worlds.yml
Memorizza informazioni sui mondi gestiti (auto-generato).

### messages.yml
Messaggi personalizzabili per tutto l'output del plugin.

## Installazione

1. Scarica l'ultima release
2. Posiziona il file JAR nella cartella `plugins/` del tuo server
3. Riavvia il server
4. Configura il plugin in `plugins/LilWorlds/config.yml`
5. Crea generatori personalizzati in `plugins/LilWorlds/generators/` (opzionale)

## Requisiti

- **Server Minecraft**: 1.16 - 1.21.5
- **Java**: 8 o superiore
- **Dipendenze**: Nessuna (PlaceholderAPI è opzionale)

## Funzionalità Principali

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

## Supporto

- **Issues**: Segnala bug su GitHub
- **Documentazione**: Controlla la wiki per guide dettagliate
- **Discord**: Unisciti al nostro server community

## Licenza

Questo progetto è licenziato sotto la Licenza MIT - vedi il file LICENSE per i dettagli.

---

**LilWorlds** - Rendere la gestione dei mondi semplice, veloce e potente.