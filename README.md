![Unit Tests](https://github.com/luizcavalcanti/nes-emulator/workflows/Unit%20Tests/badge.svg)

# NES Emulator

I have some trouble understanding how the NES hardware
(and specially cartridges) works, so I'm building an as-simple-as-I-can emulator to really get it (I'm that dumb)
in java 11 (because reasons).

### What's working so far?

- Loading iNES 1.0 rom format with some headers
- Basic mirroring for Cart PRG ROM and CPU RAM addresses
- A functional CPU with some opcodes (implementing them as needed)
- Basic inspection GUI for debugging purposes (run `make inspect`)

Supported CPU Opcodes so far: 40 of 151

### What I'm working on right now?

- Creating an inspection GUI (javaFX) to understand what's happening in RAM and CPU (visually)
- MORE OPCODES! (at least enough to run Balloon Fighter)

### Parking lot

- Fake PPU
- Interrupts
- Check for CHR RAM/ROM presence
