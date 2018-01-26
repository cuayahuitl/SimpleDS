from collections import namedtuple
import numpy as np
import wave

class monowrapper (object):
    def __init__(self, input_stream):
        self._stream = input_stream
        self._nchans = self._stream.get_param('nchannels')
    def read(self, num_frames):
        data = self._stream.read(num_frames)
        if self._nchans > 1:
            data = (data[:,0] + data[:,1]) / 2
        else:
            data = data[:,0]
        return data

class wavfile (object):

    def __init__(self, input_file):
        self._wav = wave.open(input_file, 'r')
        self._parms = wavfile.parse_params(self._wav.getparams())
        self._dtype = wavfile.dtype_for(self._parms.sampwidth)
        
    def read(self, num_frames):
        return self.format_frames(self._wav.readframes(num_frames))
    
    def get_param(self, parm_name):
        return getattr(self._parms, parm_name)
    
    def get_params(self):
        return self._parms
    
    def normalize_frames(self, data):
        if self._parms.sampwidth == 1:
            data = (data.astype(np.int16) - np.power(2, 7)).astype(np.int8)
        return data
    
    def format_frames(self, data):
        data = np.fromstring(data, dtype=self._dtype)
        data = np.reshape(data, (len(data) // self._parms.nchannels, self._parms.nchannels))
        data = self.normalize_frames(data)
        return data
    
    @staticmethod
    def parse_params(raw_parms):
        return namedtuple('WavParams', [
            'nchannels', 'sampwidth', 'framerate',
            'nframes',   'comptype',  'compname'
        ])(*raw_parms)
    
    @staticmethod
    def dtype_for(sampwidth):
        if sampwidth == 1:
            dtype = np.uint8
        elif sampwidth == 2:
            dtype = np.int16
        else:
            raise ValueError("Sample width of %d bytes not supported" % sampwidth)
        return dtype
