import numpy as np

def overlapped_window(stream, window_size, overlap_rate):
    X = []
    while True:
        read_size = window_size if len(X) == 0 else window_size // overlap_rate
        x = stream.read(read_size)
        if len(x) != read_size:
            raise StopIteration()
        X.extend(x)
        yield np.array(X)
        X = X[window_size//overlap_rate:]
