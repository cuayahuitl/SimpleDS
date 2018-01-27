import pyaudio
import wave
import sys

class AudioRecorder(object):

	def __init__(self, filename):
		self.record(filename)

	def record(self, filename, FORMAT=pyaudio.paInt16, CHANNELS=2, SAMPLE_RATE=8000, CHUNK=1024, DURATION_SECS=1):
		audio = pyaudio.PyAudio()
		stream = audio.open(format=FORMAT, channels=CHANNELS, rate=SAMPLE_RATE, input=True, frames_per_buffer=CHUNK)

		frames = []
		for i in range(0, int(SAMPLE_RATE / CHUNK * DURATION_SECS)):
			data = stream.read(CHUNK)
			frames.append(data)

		stream.stop_stream()
		stream.close()
		audio.terminate()

		waveFile = wave.open(filename, 'wb')
		waveFile.setnchannels(CHANNELS)
		waveFile.setsampwidth(audio.get_sample_size(FORMAT))
		waveFile.setframerate(SAMPLE_RATE)
		waveFile.writeframes(b''.join(frames))
		waveFile.close()

filename = sys.argv[1] if len(sys.argv) == 2 else None
recorder = AudioRecorder(filename)
