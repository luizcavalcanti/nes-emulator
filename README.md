# NES Emulator

I have some trouble understanding how the NES hardware
(and specially cartridges) works, so I'm building an as-simple-as-I-can emulator to really get it (I'm that dumb)
in java 11 (because reasons).

### What's working so far?

- I can kinda load iNES rom file format into memory
- A very initial version of CPU with a dozen instructions or so

### What I'm working on right now?

- Undesrtanding why Tetris falls into a death spiral of DEY-BNE-STA loops

### Parking lot

- Check for CHR RAM/ROM presence
- Proper logging
- Proper memory inspection
