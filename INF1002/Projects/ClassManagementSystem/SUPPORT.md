# Setting Up POSIX regex.h on Windows (MinGW)

This guide explains how to install `regex.h` on Windows using MinGW-w64, so your code can use POSIX regex functions that are natively available on macOS/Linux.

## Prerequisites

- MinGW-w64 installed (we used winlibs or MSYS2)
- Make sure you know your MinGW installation path

## Step 1: Find Your MinGW Path

In PowerShell, run:
```powershell
where gcc
```

If that doesn't work, try:
```powershell
Get-Command gcc | Select-Object -ExpandProperty Source
```

Your MinGW is typically in one of these locations:
- **MSYS2 ucrt64** (recommended, modern): `C:\msys64\ucrt64`
- **MSYS2 mingw64** (legacy): `C:\msys64\mingw64`
- **winlibs**: `C:\Users\<YourUsername>\Downloads\winlibs-x86_64-posix-seh-gcc-<version>\mingw64`
- Or wherever you extracted your MinGW installation

**Note:** If you're using MSYS2, `ucrt64` is the modern, recommended environment (uses Universal C Runtime). `mingw64` uses the legacy MSVCRT runtime.

**Note the path** - you'll need it in Step 4.

## Step 2: Download the 64-bit Binary Package

You need the **64-bit binary package** (NOT the source code).

1. Go to: [MSYS2 Mirror - mingw64 packages](https://mirror.msys2.org/mingw/mingw64/)
2. Search for: `libgnurx` (or scroll down to find it)
3. Download the file named: `mingw-w64-x86_64-libgnurx-<version>-any.pkg.tar.zst`
   - Make sure it ends with `-any.pkg.tar.zst` (NOT `-src.tar.xz`)
   - Example: `mingw-w64-x86_64-libgnurx-2.5.1-3-any.pkg.tar.zst`

**Direct download link (check for latest version):**
[mingw-w64-x86_64-libgnurx-2.5.1-3-any.pkg.tar.zst](https://mirror.msys2.org/mingw/mingw64/mingw-w64-x86_64-libgnurx-2.5.1-3-any.pkg.tar.zst)

## Step 3: Extract the Package

Extract the `.zst` file using:
- **7-Zip** (recommended - download from https://www.7-zip.org/ if needed)
- Or any other archive tool that supports `.zst` format

After extraction, you should see a folder structure like:
```
usr/
  └── x86_64-w64-mingw32/
      ├── bin/
      │   └── libgnurx-0.dll  (or similar DLL name)
      ├── include/
      │   └── regex.h
      └── lib/
          └── libgnurx.a
```

## Step 4: Copy Files to Your MinGW Installation

Replace `YOUR_MINGW_PATH` with your actual MinGW path from Step 1.

### Copy the header file:
```powershell
Copy-Item "path\to\extracted\usr\x86_64-w64-mingw32\include\regex.h" "YOUR_MINGW_PATH\include\regex.h"
```

**Examples:**

For MSYS2 ucrt64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\include\regex.h" "C:\msys64\ucrt64\include\regex.h"
```

For MSYS2 mingw64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\include\regex.h" "C:\msys64\mingw64\include\regex.h"
```

For winlibs:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\include\regex.h" "C:\Users\Muham\Downloads\winlibs-x86_64-posix-seh-gcc-15.2.0-mingw-w64msvcrt-13.0.0-r1\mingw64\include\regex.h"
```

### Copy the library file:
```powershell
Copy-Item "path\to\extracted\usr\x86_64-w64-mingw32\lib\libgnurx.a" "YOUR_MINGW_PATH\lib\libgnurx.a"
```

**Examples:**

For MSYS2 ucrt64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\lib\libgnurx.a" "C:\msys64\ucrt64\lib\libgnurx.a"
```

For MSYS2 mingw64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\lib\libgnurx.a" "C:\msys64\mingw64\lib\libgnurx.a"
```

For winlibs:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\lib\libgnurx.a" "C:\Users\Muham\Downloads\winlibs-x86_64-posix-seh-gcc-15.2.0-mingw-w64msvcrt-13.0.0-r1\mingw64\lib\libgnurx.a"
```

### Copy the DLL file (Required for runtime on Windows):

**Important:** You need to copy the DLL file so your program can run! The DLL should be in the `bin/` folder of the extracted package.

```powershell
Copy-Item "path\to\extracted\usr\x86_64-w64-mingw32\bin\libgnurx-0.dll" "YOUR_MINGW_PATH\bin\libgnurx-0.dll"
```

**Examples:**

For MSYS2 ucrt64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\bin\libgnurx-0.dll" "C:\msys64\ucrt64\bin\libgnurx-0.dll"
```

For MSYS2 mingw64:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\bin\libgnurx-0.dll" "C:\msys64\mingw64\bin\libgnurx-0.dll"
```

For winlibs:
```powershell
Copy-Item "C:\Users\Muham\Downloads\extracted\usr\x86_64-w64-mingw32\bin\libgnurx-0.dll" "C:\Users\Muham\Downloads\winlibs-x86_64-posix-seh-gcc-15.2.0-mingw-w64msvcrt-13.0.0-r1\mingw64\bin\libgnurx-0.dll"
```

**Note:** The DLL name might be slightly different (e.g., `libgnurx-0.dll`, `libgnurx.dll`). Check the `bin/` folder in your extracted package to find the exact name.

**For running your compiled program:** You also need the DLL in your `build/` directory (same folder as your `.exe` file) OR add the MinGW `bin` directory to your PATH. See Step 7 for details.

## Step 5: Update Your Makefile

Open your `Makefile` and add the library to your linker flags:

```makefile
LDFLAGS := -lgnurx
```

If you already have other flags, add it:
```makefile
LDFLAGS := -lgnurx -lm
```

**Note:** The flag is `-lgnurx` because the library file is named `libgnurx.a` (the `lib` prefix and `.a` suffix are removed when using `-l`).

## Step 6: Copy DLL to Build Directory (For Running Your Program)

When you compile your program, Windows needs to find the DLL at runtime. You have two options:

### Option A: Copy DLL to build directory (Recommended for development)

After compiling, copy the DLL to your `build/` folder (same folder as your `.exe`):

```powershell
Copy-Item "C:\msys64\ucrt64\bin\libgnurx-0.dll" -Destination "build\" -Force
```

Or if you still have the extracted folder:
```powershell
Copy-Item "path\to\extracted\usr\x86_64-w64-mingw32\bin\libgnurx-0.dll" -Destination "build\" -Force
```

### Option B: Add MinGW bin to PATH (Alternative)

Add the MinGW `bin` directory to your PATH environment variable so Windows can find the DLL automatically:

```powershell
$mingwPath = "C:\msys64\ucrt64\bin"
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";$mingwPath", "User")
```

**Important:** Close and reopen PowerShell for PATH changes to take effect.

## Step 7: Verify Installation

Try compiling your project:
```powershell
gcc -Iinclude -Wall -Wextra -g src\*.c -o build\main.exe -lgnurx
```

Or if you have `make` in your PATH:
```powershell
make
```

### Setting Up make.exe on Windows (Optional)

If you want to use `make` instead of calling `gcc` directly, follow these steps:

1. **Download make.exe:**
   - Go to: https://sourceforge.net/projects/ezwinports/files/
   - Search for "make" or download: `make-4.4-without-guile-w32-bin.zip`
   - Extract the ZIP file

2. **Copy make.exe to your MinGW bin folder:**
   ```powershell
   Copy-Item "path\to\extracted\make.exe" "YOUR_MINGW_PATH\bin\make.exe"
   ```
   
   **Examples:**

   For MSYS2 ucrt64:
   ```powershell
   Copy-Item "C:\Users\Muham\Downloads\make.exe" "C:\msys64\ucrt64\bin\make.exe"
   ```

   For MSYS2 mingw64:
   ```powershell
   Copy-Item "C:\Users\Muham\Downloads\make.exe" "C:\msys64\mingw64\bin\make.exe"
   ```

   For winlibs:
   ```powershell
   Copy-Item "C:\Users\Muham\Downloads\make.exe" "C:\Users\Muham\Downloads\winlibs-x86_64-posix-seh-gcc-15.2.0-mingw-w64msvcrt-13.0.0-r1\mingw64\bin\make.exe"
   ```

3. **Add MinGW to PATH (so you can use `make` from anywhere):**
   
   Open PowerShell as Administrator and run:
   ```powershell
   $mingwPath = "YOUR_MINGW_PATH\bin"
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";$mingwPath", "User")
   ```
   
   **Examples:**

   For MSYS2 ucrt64:
   ```powershell
   $mingwPath = "C:\msys64\ucrt64\bin"
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";$mingwPath", "User")
   ```

   For MSYS2 mingw64:
   ```powershell
   $mingwPath = "C:\msys64\mingw64\bin"
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";$mingwPath", "User")
   ```

   For winlibs:
   ```powershell
   $mingwPath = "C:\Users\Muham\Downloads\winlibs-x86_64-posix-seh-gcc-15.2.0-mingw-w64msvcrt-13.0.0-r1\mingw64\bin"
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";$mingwPath", "User")
   ```
   
   **Important:** Close and reopen PowerShell for the PATH changes to take effect.

4. **Verify make works:**
   ```powershell
   make --version
   ```

If compilation succeeds without linker errors, you're all set!

## Troubleshooting

### "cannot find -lgnurx" or "skipping incompatible"
- **Problem:** The library file is missing or wrong architecture
- **Solution:** 
  - Make sure you copied `libgnurx.a` to `YOUR_MINGW_PATH\lib\`
  - Make sure you downloaded the **64-bit binary** (`-any.pkg.tar.zst`), not the source code
  - Verify the file exists: `Test-Path "YOUR_MINGW_PATH\lib\libgnurx.a"`

### "regex.h: No such file or directory"
- **Problem:** The header file is missing
- **Solution:**
  - Make sure you copied `regex.h` to `YOUR_MINGW_PATH\include\`
  - Verify: `Test-Path "YOUR_MINGW_PATH\include\regex.h"`

### "skipping incompatible ...libgnurx.a"
- **Problem:** You downloaded the 32-bit version instead of 64-bit
- **Solution:** Download the `x86_64` version (64-bit), not `i686` (32-bit)

### Program runs but crashes with "DLL not found" or similar error
- **Problem:** The regex DLL is not found at runtime
- **Solution:** 
  - Make sure you copied the DLL from the extracted package's `bin/` folder to `YOUR_MINGW_PATH\bin\`
  - Copy the DLL to your `build/` directory (same folder as your `.exe`), OR
  - Add the MinGW `bin` directory to your PATH (see Step 6)
  - Verify the DLL exists: `Test-Path "C:\msys64\ucrt64\bin\libgnurx-0.dll"`

## Important Notes

- **For macOS/Linux teammates:** They don't need to do anything - `regex.h` is already available in their system!
- **Always use the binary package** (`-any.pkg.tar.zst`), not the source code (`-src.tar.xz`)
- The library must match your MinGW architecture (64-bit for `x86_64-w64-mingw32`)

## References

- MSYS2 Package Repository: https://packages.msys2.org/base/mingw-w64-libgnurx
- MSYS2 Mirror: https://mirror.msys2.org/mingw/mingw64/

