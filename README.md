# ⚡ CodeTracker

> Ever wondered what Git actually does under the hood? I stopped wondering and built it from scratch in pure Java.

**CodeTracker (`ct`)** is a lightweight, Git-inspired version control engine written completely from zero. No external Git binaries, no magic libraries—just raw byte streams, cryptographic hashing, recursive tree traversal, and file-system manipulation.

It snapshots project directories into Merkle trees, tracks staged changes in a clean index, checks diffs across 3 separate states, and lets you travel backward and forward through project history at will.

---

## 💡 What Makes It Tick?

Most tutorials stop at saving flat files. `ct` builds the real internal mechanisms that power modern version control systems:

* **Content-Addressable Object Database:** Files aren't stored by filename; they are hashed (SHA-1), compressed into payloads, and stored under `.ct/objects/` using two-character folder sharding—just like real Git.
* **Recursive Merkle Trees:** Nested directories are turned into hierarchical tree nodes. If a subfolder has 5 levels of folders, `ct` recursively hashes and unpacks each branch accurately.
* **Two-Stage Indexing:** An intermediate staging ground (`.ct/index.json`) ensures edits on disk stay decoupled from repository snapshots until explicitly staged.
* **True Time-Travel Checkout:** Not just a viewer—`checkout` clears tracked files missing from the past commit, writes out the exact historical blobs, and recalibrates the index without blowing away untracked work.
* **Three-Way Diff Engine (`status`):** Runs simultaneous delta checks:
* 🟢 **Index vs. HEAD:** Flags staged new files, modifications, and deletions.
* 🔴 **Disk vs. Index:** Detects uncommitted local edits, unstaged deletions, and untracked rogue files.



---

## 🛠️ The Architecture at a Glance

```text
 💻 Working Directory
         │
         │  ct add <file>
         ▼
 📋 Staging Area (.ct/index.json)
         │
         │  ct commit -m "..."
         ▼
 🌳 Object Storage (.ct/objects/xx/xxxx...)
    ├── [Blob] Raw content snapshots
    ├── [Tree] Folder graphs & permissions
    └── [Commit] Pointers, metadata, & parent hashes

```

---

## 🚀 Commands in Action

| Command | What it does | Real-world example |
| --- | --- | --- |
| `ct init` | Spawns a fresh `.ct` repository skeleton | `ct init` |
| `ct add <file>` | Blobs the file payload and registers it in staging | `ct add main.py` |
| `ct commit -m` | Freezes the staged state into a tree snapshot | `ct commit -m "feat: first working build"` |
| `ct status` | Runs diagnostics across disk, index, and latest commit | `ct status` |
| `ct log` | Walks the parent commit ancestry back to day one | `ct log` |
| `ct checkout <hash>` | Rewinds your entire workspace to that exact moment | `ct checkout 92fb15e...` |

---

## 💻 Quickstart

### 1. Compile the Engine

Clone the repository and compile the source directly into a `bin/` folder:

```bash
git clone https://github.com/<your-username>/CodeTracker.git
cd CodeTracker
javac -d bin src/ct/*.java

```

### 2. Make `ct` Run Globally (Windows)

To type `ct` directly from any terminal window without prefixing `java`:

1. Create a quick batch runner inside a directory like `C:\tools\ct.bat`:
```bat
@echo off
java -cp "C:\path\to\CodeTracker\bin" ct.Main %*

```


2. Add `C:\tools` to your System `PATH` variable.
3. Open a fresh terminal and run `ct status` anywhere.

---

## 🕹️ Test Drive

```bash
# 1. Initialize a repo
ct init

# 2. Create some files
echo "System operational" > core.txt
ct status

# 3. Stage and lock it into history
ct add core.txt
ct commit -m "First snapshot"

# 4. Make changes and watch the diff engine work
echo "Updating systems" >> core.txt
echo "Rogue file" > temp.txt
ct status

# 5. Travel back in time
ct log
ct checkout <commit-hash>

```

---

## 📂 Source Code Layout

```text
src/ct/
├── Main.java                 # The CLI traffic controller
├── Commands.java             # Command implementations (init, add, commit, etc.)
├── Status_checks.java        # Three-way diff comparisons (staged vs working vs HEAD)
├── Local_file_operations.java# Disk crawler that bypasses VCS internals
├── Object_functions.java     # The object factory: Blobs, Trees, and Commits
├── TreeNode.java             # Tree graph construction for nested directories
├── Indexing.java             # Reads & writes the JSON staging ground
└── Hash_for_ct.java          # Cryptographic SHA-1 hashing utility

```

---

## 🧭 What's Next?

* [x] Complete SHA-1 object storage engine
* [x] Recursive Merkle tree builder
* [x] Accurate snapshot reconciliation (`checkout`)
* [x] Git-style 3-state `status` diagnostics
* [ ] 🎙️ **Voice-Controlled Interface** (Control commits, checkouts, and status completely hands-free)
* [ ] Lightweight branch pointers (`ct branch <name>`)

---

*Built for the curiosity of seeing how version control really works beneath the surface.*
