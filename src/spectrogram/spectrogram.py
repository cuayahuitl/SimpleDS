import matplotlib.pyplot as plt
import matplotlib.ticker
import numpy as np
import warnings
import sys
import cv2

from wavwrapper import wavfile, monowrapper
from windowing import overlapped_window

def trim(imgray):
	lower_xx = 0
	lower_yy = 0
	higher_xx = 0
	higher_yy = 0
	rows, cols = imgray.shape

	for i in range(0,rows):
		for j in range(0,cols):
			if imgray[i,j] != 255 and imgray[i,j] != 15: 
				lower_xx = i
				lower_yy = j
				break
	for i in range(0,cols):
		for j in range(0,rows):
			if imgray[j,i] != 255 and imgray[j,i] != 15: 
				higher_xx = j
				higher_yy = i
				break

        crop = imgray[higher_xx:lower_xx, lower_yy:higher_yy]
	return crop

def main(input_file=None, window_size="256", scale="log"):

    # Process command-line args.
    if input_file is None:
        sys.stderr.write("usage: python %s <input_file.wav> [window_size=int, default 1024] [scale=log|linear, default log]\n" % sys.argv[0])
        return 1
        
    window_size = int(window_size)
    
    if not scale in ['log', 'linear']:
        sys.stderr.write("error: '%s' is not a valid scale, choose 'log' or 'linear'.\n" % scale)
        return 1

    # Open wave file and load frame rate, number of channels, sample width, and number of frames.
    w = wavfile(input_file)
    
    # Catch case where there are more than 2 channels.
    if w.get_param('nchannels') > 2:
        sys.stderr.write("error: only mono and stereo tracks are supported\n")
        return 1
    
    # Catch case where there is less than one window of audio.
    if w.get_param('nframes') < window_size:
        sys.stderr.write("error: audio file is shorter than configured window size\n")
        return 1
    
    # Hann window function coefficients.
    hann = 0.5 - 0.5 * np.cos(2.0 * np.pi * (np.arange(window_size)) / window_size)
    
    # Hann window must have 4x overlap for good results.
    overlap = 4
    
    # Y will hold the DFT of each window. We use acc and bar for displaying progress.
    Y = []
    acc = 0
    
    # Process each window of audio.
    for x in overlapped_window(monowrapper(w), window_size, overlap):
        y = np.fft.rfft(x * hann)[:window_size//2]
        Y.append(y)
        acc += window_size
    
    # Normalize data and convert to dB.
    Y = np.column_stack(Y)
    Y = np.absolute(Y) * 2.0 / np.sum(hann)
    Y = Y / np.power(2.0, (8 * w.get_param('sampwidth') - 1))
    Y = (20.0 * np.log10(Y)).clip(-120)

    # Time domain: We have Y.shape[1] windows, so convert to seconds by multiplying
    # by window size, dividing by sample rate, and dividing by the overlap rate.
    t = np.arange(0, Y.shape[1], dtype=np.float) * window_size / w.get_param('framerate') / overlap
    
    # Frequency domain: There are window_size/2 frequencies represented, and we scale
    # by dividing by window size and multiplying by sample frequency.
    f = np.arange(0, window_size / 2, dtype=np.float) * w.get_param('framerate') / window_size
    
    # Plot the spectrogram.
    ax = plt.subplot(111)
    plt.pcolormesh(t, f, Y, vmin=-120, vmax=0)
    
    # Use log scale above 100 Hz, linear below.
    if scale == 'log':
        yscale = 0.25
        # Mitigation for issue 2 (https://github.com/le1ca/spectrogram/issues/2)
        if matplotlib.__version__[0:3] == '1.3':
            yscale = 1
            warnings.warn('You are using matplotlib 1.3.* (and not >= 1.4.0). Therefore linscaley must equal 1, not 0.25')
        plt.yscale('symlog', linthreshy=100, linscaley=yscale)
        ax.yaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())

    plt.axis('off')

    # Save plot as png file
    array = input_file.split(".wav")
    img_filename = array[0]+"_digits.png"
    plt.savefig(img_filename)

    # Save image with trimmed margins
    im = cv2.imread(img_filename)
    im_gray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
    im_trimmed = trim(im_gray)
    cv2.imwrite(img_filename, im_trimmed)
    plt.subplot(131),plt.imshow(im),plt.title('Original')
    plt.subplot(132),plt.imshow(im_gray),plt.title('Gray')
    plt.subplot(133),plt.imshow(im_trimmed),plt.title('Trimmed')
    plt.show()

    return 0

if __name__ == "__main__":
    sys.exit(main(*sys.argv[1:]))
