#!/usr/bin/env node
/**
 * FieldMind Sound Effect Generator
 * Generates simple WAV files for the FieldMind sound design system.
 * Uses PCM 16-bit mono, 44100 Hz sample rate.
 */

import fs from 'fs';
import path from 'path';

const SAMPLE_RATE = 44100;
const BITS_PER_SAMPLE = 16;
const NUM_CHANNELS = 1;

const OUT_DIR = path.resolve('app/src/main/res/raw');

// Ensure output directory exists
fs.mkdirSync(OUT_DIR, { recursive: true });

/**
 * Write a WAV file given an array of float samples (-1.0 to 1.0).
 */
function writeWav(filename, samples) {
  const numSamples = samples.length;
  const dataSize = numSamples * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);
  const fileSize = 36 + dataSize;

  const buffer = Buffer.alloc(44 + dataSize);
  let offset = 0;

  // RIFF header
  buffer.write('RIFF', offset); offset += 4;
  buffer.writeUInt32LE(fileSize, offset); offset += 4;
  buffer.write('WAVE', offset); offset += 4;

  // fmt chunk
  buffer.write('fmt ', offset); offset += 4;
  buffer.writeUInt32LE(16, offset); offset += 4; // chunk size
  buffer.writeUInt16LE(1, offset); offset += 2;  // PCM format
  buffer.writeUInt16LE(NUM_CHANNELS, offset); offset += 2;
  buffer.writeUInt32LE(SAMPLE_RATE, offset); offset += 4;
  buffer.writeUInt32LE(SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8), offset); offset += 4; // byte rate
  buffer.writeUInt16LE(NUM_CHANNELS * (BITS_PER_SAMPLE / 8), offset); offset += 2; // block align
  buffer.writeUInt16LE(BITS_PER_SAMPLE, offset); offset += 2;

  // data chunk
  buffer.write('data', offset); offset += 4;
  buffer.writeUInt32LE(dataSize, offset); offset += 4;

  // Write PCM samples
  for (let i = 0; i < numSamples; i++) {
    // Clamp and convert float to 16-bit signed integer
    let sample = Math.max(-1, Math.min(1, samples[i]));
    let intSample = sample < 0 ? sample * 0x8000 : sample * 0x7FFF;
    intSample = Math.round(intSample);
    buffer.writeInt16LE(intSample, offset);
    offset += 2;
  }

  const outPath = path.join(OUT_DIR, filename);
  fs.writeFileSync(outPath, buffer);
  console.log(`  ✓ ${filename} (${(buffer.length / 1024).toFixed(1)} KB, ${(numSamples / SAMPLE_RATE).toFixed(2)}s)`);
}

/**
 * Generate a sine wave sample.
 */
function sine(freq, t) {
  return Math.sin(2 * Math.PI * freq * t);
}

/**
 * Apply a linear fade envelope.
 */
function envelope(samples, fadeInSec, fadeOutSec) {
  const n = samples.length;
  const fadeInSamples = Math.floor(fadeInSec * SAMPLE_RATE);
  const fadeOutSamples = Math.floor(fadeOutSec * SAMPLE_RATE);
  
  return samples.map((s, i) => {
    let env = 1.0;
    if (i < fadeInSamples) env = i / fadeInSamples;
    if (i > n - fadeOutSamples) env = (n - i) / fadeOutSamples;
    return s * env;
  });
}

// ═══════════════════════════════════════════════════════════════════
//  1. CHIME — Gentle ascending tone (like wind chime)
//     Multiple sine harmonics with slow attack/release
// ═══════════════════════════════════════════════════════════════════
function generateChime() {
  const duration = 1.0; // seconds
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Rich harmonic stack: fundamental at 523Hz (C5) with harmonics
    let s = 0;
    s += 0.35 * sine(523, t);       // C5
    s += 0.20 * sine(659, t);       // E5
    s += 0.15 * sine(784, t);       // G5
    s += 0.10 * sine(1047, t);      // C6
    s += 0.05 * sine(1319, t);      // E6
    s += 0.03 * sine(1568, t);      // G6
    samples[i] = s;
  }

  // Slow fade in/out for a gentle feel
  return envelope(samples, 0.15, 0.5);
}

// ═══════════════════════════════════════════════════════════════════
//  2. SHUTTER — Short click like a camera shutter
//     Quick noise burst with sharp attack
// ═══════════════════════════════════════════════════════════════════
function generateShutter() {
  const duration = 0.12; // seconds
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  // Two-part shutter: mechanical click + slight resonance
  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Noise burst
    const noise = (Math.random() * 2 - 1);
    // Mechanical resonance at ~200Hz
    const resonance = 0.3 * sine(200, t);
    // Envelope: very sharp attack, quick decay
    const env = Math.exp(-t * 60);
    samples[i] = (noise * 0.5 + resonance) * env;
  }

  return samples;
}

// ═══════════════════════════════════════════════════════════════════
//  3. WATER DROP — Soft plop with descending pitch
//     Frequency sweep from ~800Hz down to ~200Hz with soft attack
// ═══════════════════════════════════════════════════════════════════
function generateWaterDrop() {
  const duration = 0.35; // seconds
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Descending frequency from 800Hz to 200Hz
    const freq = 800 - 600 * (t / duration);
    // Sine with slight FM for watery texture
    const fm = 0.08 * sine(30, t); // slow modulation
    let s = sine(freq + fm * 100, t);
    // Add a second harmonic for richness
    s += 0.3 * sine(freq * 1.5 + fm * 50, t);
    // Divide by amplitude
    s /= 1.3;
    samples[i] = s;
  }

  // Soft attack, gentle decay
  return envelope(samples, 0.01, 0.25);
}

// ═══════════════════════════════════════════════════════════════════
//  4. CRICKET — Night cricket chirp
//     Short pulsed chirps at ~4kHz with gaps
// ═══════════════════════════════════════════════════════════════════
function generateCricket() {
  const duration = 1.6; // seconds
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples).fill(0);

  // Create 4 chirps spread across the duration
  const chirpCount = 4;
  const chirpDuration = 0.08; // seconds per chirp
  const chirpSamples = Math.floor(SAMPLE_RATE * chirpDuration);
  const gapSamples = Math.floor((numSamples - chirpCount * chirpSamples) / (chirpCount + 1));

  for (let c = 0; c < chirpCount; c++) {
    const startSample = gapSamples + c * (chirpSamples + gapSamples);
    for (let i = 0; i < chirpSamples; i++) {
      const t = i / SAMPLE_RATE;
      // Cricket sounds are around 4-5kHz
      const freq = 4200 + 800 * (i / chirpSamples); // slight upward sweep
      const chirpEnv = Math.sin(Math.PI * i / chirpSamples); // smooth envelope per chirp
      const s = 0.6 * chirpEnv * sine(freq, t);
      samples[startSample + i] = s;
    }
  }

  return samples;
}

// ═══════════════════════════════════════════════════════════════════
//  5. SUCCESS — Gentle ascending arpeggio (for achievements)
// ═══════════════════════════════════════════════════════════════════
function generateSuccess() {
  const duration = 0.8;
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  const notes = [523, 659, 784, 1047]; // C5 E5 G5 C6
  const noteDuration = duration / notes.length;

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    const noteIdx = Math.min(Math.floor(t / noteDuration), notes.length - 1);
    const noteT = t - noteIdx * noteDuration;
    const freq = notes[noteIdx];
    // Gentle sine with soft envelope
    const noteEnv = Math.sin(Math.PI * noteT / noteDuration);
    let s = 0.4 * noteEnv * sine(freq, noteT);
    samples[i] = s;
  }

  return envelope(samples, 0.02, 0.3);
}

// ═══════════════════════════════════════════════════════════════════
//  Main
// ═══════════════════════════════════════════════════════════════════

console.log('\n🎵 FieldMind Sound Effect Generator\n');
console.log(`Output: ${OUT_DIR}\n`);

console.log('Generating sounds...\n');

// Use common seed for reproducibility
Math.random = ((seed) => () => {
  seed = (seed * 16807) % 2147483647;
  return (seed - 1) / 2147483646;
})(12345);

writeWav('fx_chime.wav', generateChime());
writeWav('fx_shutter.wav', generateShutter());
writeWav('fx_water_drop.wav', generateWaterDrop());
writeWav('fx_cricket.wav', generateCricket());
writeWav('fx_success.wav', generateSuccess());

console.log('\n✅ All sounds generated successfully!\n');
