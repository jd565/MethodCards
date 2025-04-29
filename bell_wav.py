import numpy as np
import wave

# Define parameters
duration = 1.2  # shorter note duration
sample_rate = 44100
base_freq = 261.63  # Middle C (C4) for a major scale

semitone_intervals = [12, 11, 9, 7, 5, 4, 2, 0]

# Function to generate a bell-like tone for a given frequency
def generate_bell_tone(frequency, duration, sample_rate):
    t = np.linspace(0, duration, int(sample_rate * duration), False)
    envelope = np.exp(-6 * t)  # quick decay
    waveform = np.sin(2 * np.pi * frequency * t) * envelope
    # Normalize to the range [-1, 1] before converting to int16
    waveform = waveform / np.max(np.abs(waveform))
    return waveform

# Generate and save each tone
file_paths = []
for i, semitone in enumerate(semitone_intervals):
    freq = base_freq * (2 ** (semitone / 12))
    waveform = generate_bell_tone(freq, duration, sample_rate)
    filename = f"composeApp/src/commonMain/composeResources/files/major_scale_note_{i+1}.wav"

    # Use wave module to create a proper WAV file
    with wave.open(filename, 'w') as wf:
        wf.setnchannels(1)  # Mono
        wf.setsampwidth(2)  # 2 bytes (16 bits)
        wf.setframerate(sample_rate)
        # Convert to int16 and write to the file
        wf.writeframes((waveform * 32767).astype(np.int16).tobytes())

    file_paths.append(filename)

print(file_paths)
