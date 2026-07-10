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
//  6. WIND — Gentle whooshing ambient wind
//     Filtered noise with slow amplitude modulation
// ═══════════════════════════════════════════════════════════════════
function generateWind() {
  const duration = 3.0; // seconds — longer for ambient loop
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  // Pre-generate noise for consistency
  const noise = new Array(numSamples);
  for (let i = 0; i < numSamples; i++) {
    noise[i] = Math.random() * 2 - 1;
  }

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Low-pass filter via simple moving average (smoothing)
    let filtered = 0;
    const windowSize = 80; // ~1.8ms window for low-pass
    let count = 0;
    for (let j = Math.max(0, i - windowSize); j <= i; j++) {
      filtered += noise[j];
      count++;
    }
    filtered /= count;

    // Slow amplitude modulation (0.3Hz) for gentle whoosh
    const mod = 0.5 + 0.5 * Math.sin(2 * Math.PI * 0.3 * t);
    // Sub-bass rumble for depth
    const sub = 0.08 * sine(60, t);
    samples[i] = (filtered * 0.4 * mod) + sub;
  }

  return envelope(samples, 0.8, 0.8);
}

// ═══════════════════════════════════════════════════════════════════
//  7. THUNDER — Distant low rumble
//     Sharp attack followed by slow decaying low-frequency boom
// ═══════════════════════════════════════════════════════════════════
function generateThunder() {
  const duration = 3.0; // seconds — long rumble
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  // Pre-generate noise for rumble texture
  const noise = new Array(numSamples);
  for (let i = 0; i < numSamples; i++) {
    noise[i] = Math.random() * 2 - 1;
  }

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Low frequency rumble ~40-80Hz
    const lowRumble1 = 0.3 * sine(45 + 15 * (t / duration), t);
    const lowRumble2 = 0.2 * sine(65 - 10 * (t / duration), t);
    // Filtered noise (heavy low-pass) for texture
    let filtered = 0;
    const windowSize = 200; // ~4.5ms window — very low pass
    let count = 0;
    for (let j = Math.max(0, i - windowSize); j <= i; j++) {
      filtered += noise[j];
      count++;
    }
    filtered /= count;
    const noiseComponent = filtered * 0.15;

    // Envelope: sharp attack, long decay
    let env;
    if (t < 0.02) {
      env = t / 0.02; // fast attack
    } else if (t < 0.2) {
      env = 1.0; // sustain
    } else {
      env = Math.exp(-(t - 0.2) * 1.2); // slow decay
    }

    samples[i] = (lowRumble1 + lowRumble2 + noiseComponent) * env;
  }

  return envelope(samples, 0.005, 0.3);
}

// ═══════════════════════════════════════════════════════════════════
//  8. BIRD CHORUS — Gentle dawn birdsong
//     Overlapping chirps and trills at morning bird frequencies
// ═══════════════════════════════════════════════════════════════════
function generateBirdChorus() {
  const duration = 4.0; // seconds — longer ambient loop
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples).fill(0);

  // Bird call definitions: [frequency, duration_sec, start_time_sec, amplitude]
  const birds = [
    // Robin-like song: clear fluty notes
    [1800, 0.6, 0.1, 0.25],
    [2200, 0.5, 0.3, 0.20],
    [1600, 0.4, 0.5, 0.22],
    [2400, 0.7, 0.8, 0.18],
    // Sparrow-like chirps: faster, higher
    [3800, 0.15, 0.2, 0.12],
    [4200, 0.12, 0.6, 0.10],
    [3500, 0.18, 1.0, 0.11],
    [4000, 0.14, 1.4, 0.09],
    [3600, 0.16, 1.8, 0.10],
    // Thrush-like phrases: melodic
    [2800, 0.8, 1.5, 0.15],
    [2600, 0.6, 2.0, 0.13],
    [3000, 0.5, 2.5, 0.11],
    // Finishing with a few light twitters
    [5000, 0.08, 2.2, 0.06],
    [5500, 0.06, 2.5, 0.05],
    [4800, 0.1, 3.0, 0.06],
    // Background ambient bird texture
    [1500, 3.5, 0.0, 0.04],
    [2000, 3.5, 0.0, 0.03],
  ];

  birds.forEach(([freq, dur, start, amp]) => {
    const startSample = Math.floor(start * SAMPLE_RATE);
    const birdSamples = Math.floor(dur * SAMPLE_RATE);
    for (let i = 0; i < birdSamples; i++) {
      const idx = startSample + i;
      if (idx >= numSamples) break;
      const t = i / SAMPLE_RATE;
      // Each bird note has a smooth envelope
      const noteEnv = Math.sin(Math.PI * t / dur);
      // Slight frequency wobble for natural feel
      const wobble = 1 + 0.02 * Math.sin(2 * Math.PI * 6 * t);
      // Multiple overtones for richness
      let s = 0;
      s += noteEnv * sine(freq * wobble, t);
      s += 0.3 * noteEnv * sine(freq * 2 * wobble, t); // first harmonic
      // Different birds have different timbres
      samples[idx] += s * amp;
    }
  });

  // Normalize to prevent clipping
  let maxAmp = 0;
  for (let i = 0; i < numSamples; i++) {
    maxAmp = Math.max(maxAmp, Math.abs(samples[i]));
  }
  if (maxAmp > 0.9) {
    const scale = 0.85 / maxAmp;
    for (let i = 0; i < numSamples; i++) {
      samples[i] *= scale;
    }
  }

  return envelope(samples, 0.2, 0.4);
}

// ═══════════════════════════════════════════════════════════════════
//  9. RAIN — Gentle pitter-patter ambient rainfall
//     Filtered noise with random amplitude peaks for raindrop impacts
// ═══════════════════════════════════════════════════════════════════
function generateRain() {
  const duration = 4.0; // seconds — longer for ambient loop
  const numSamples = Math.floor(SAMPLE_RATE * duration);
  const samples = new Array(numSamples);

  // Pre-generate noise for rainfall texture
  const noise = new Array(numSamples);
  for (let i = 0; i < numSamples; i++) {
    noise[i] = Math.random() * 2 - 1;
  }

  // Pre-compute raindrop impact positions (random sharp spikes)
  const dropPositions = new Set();
  const numDrops = 80; // ~20 drops per second
  for (let d = 0; d < numDrops; d++) {
    const pos = Math.floor(Math.random() * numSamples);
    dropPositions.add(pos);
  }

  for (let i = 0; i < numSamples; i++) {
    const t = i / SAMPLE_RATE;
    // Band-pass filtered noise (mid frequencies for rain texture)
    // Simple FIR: difference of two moving averages
    let slow = 0;
    let fast = 0;
    const slowWindow = 120; // ~2.7ms
    const fastWindow = 20;  // ~0.45ms
    let slowCount = 0;
    let fastCount = 0;
    for (let j = Math.max(0, i - slowWindow); j <= i; j++) {
      slow += noise[j];
      slowCount++;
    }
    for (let j = Math.max(0, i - fastWindow); j <= i; j++) {
      fast += noise[j];
      fastCount++;
    }
    slow /= slowCount;
    fast /= fastCount;
    // Band-pass = fast - slow (passes mid frequencies, cuts lows)
    const bandPass = fast - slow;

    // Rain can have gentle amplitude modulation (surges)
    const surge = 0.7 + 0.3 * Math.sin(2 * Math.PI * 0.15 * t + Math.sin(2 * Math.PI * 0.07 * t));

    // Base rainfall (continuous)
    let s = bandPass * 0.35 * surge;

    // Raindrop impacts (sharp spikes at random positions)
    if (dropPositions.has(i)) {
      const spikeEnv = Math.exp(-(i % 200) / 30); // quick decay
      s += bandPass * 0.6 * spikeEnv;
    }

    // Very subtle low-end warmth
    s += 0.04 * sine(120, t);

    samples[i] = s;
  }

  return envelope(samples, 0.3, 0.3);
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
writeWav('fx_wind.wav', generateWind());
writeWav('fx_thunder.wav', generateThunder());
writeWav('fx_bird_chorus.wav', generateBirdChorus());
writeWav('fx_rain.wav', generateRain());

console.log('\n✅ All sounds generated successfully!\n');
