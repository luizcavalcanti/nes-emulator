![Unit Tests](https://github.com/luizcavalcanti/nes-emulator/workflows/Unit%20Tests/badge.svg)

# NES Emulator

I have some trouble understanding how the NES hardware
(and specially cartridges) works, so I'm building an as-simple-as-I-can emulator to really get it (I'm that dumb)
in java 11 (because reasons).

### How to run?

- For just running a hardcoded cart, `make run`
- For the Memory and CPU inspector/debugger, `make inspect`

### What's working so far?

- Loading iNES 1.0 rom format with some headers
- Basic mirroring for Cart PRG ROM and CPU RAM addresses
- A functional CPU with some opcodes (implementing them as needed)
- A functional MMU with most of the memory mirroring needed
- A vey basic PPU on which you can write and read bytes mindlessly, but does not render stuff yet.
- Basic inspection GUI for debugging purposes (run `make inspect`)
- Supported CPU Opcodes so far: 40 of 151
- Supported PPU Addresses so far: 4 of 8

### What I'm working on right now?

- Creating an inspection GUI (javaFX) to understand what's happening in RAM and CPU (visually)
- PPU Implementation
- MORE OPCODES! (at least enough to run Balloon Fighter)

### Parking lot

- Fake PPU
- Interrupts
- Check for CHR RAM/ROM presence
