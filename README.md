## Prerequisites

Before running the project, ensure you have **Java** installed on your system.

- We developed and tested using **Java 24.0.1**, so using this version (or newer) is recommended for compatibility.
- You can check your installed version by running:
  ```bash
  java -version
  ```

## Running the Application

To compile and run the program:
1. Compile the source code
    ```bash
    javac Main.java
    ```
2. Run the compiled program:
    ```bash
    java Main
    ```

## Features of our design
1. Columnar storage for efficient data processing
2. Shared Scans: Reuses filtered row subsets across queries (`fpMonthIndexSharedScan()`)
3. Composite Indices: `yearMonthTownIndex` for O(1) time-window lookups
4. Zone Maps: Pre-built town → row mappings for instant geographical filters
5. Hash Accelerators: Direct key-value access via `filterWithHashing()`
