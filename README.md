# SimpleDS
A Simple Deep Reinforcement Learning Dialogue System

DESCRIPTION
-----------
SimpleDS is a computational framework for training dialogue systems with deep reinforcement learning. In contrast to other dialogue systems, this system selects dialogue actions directly from raw (noisy) text or word embeddings of the last system and user responses -- from raw audio in progress. The motivation is to train dialogue agents with as little human intervention as possible.

This system runs under a client-server architecture, where the learning agent (in JavaScript) acts as the "client" and the environment (in Java) acts as the "server". They communicate by exchanging messages, where the client tells the client the action to execute, and the server tells the client the actions available, environment state and rewards observed. SimpleDS is a (spoken) dialogue system on top of [ConvNetJS](http://cs.stanford.edu/people/karpathy/convnetjs/) with support for multi-threaded and client-server processing, and fast learning via constrained search spaces.

This system has been tested with simulated and real dialogues using the Google Speech Recogniser. It has also been tested in three different languages: English, German and Spanish. SimpleDS is for experimental purposes, represents work in progress, and is therefore released without any guarantees.

SOFTWARE
--------
This system was implemented and tested under Linux and Mac OS X with the following software -- though it should run in other operating systems with minor modifications.
+ Ubuntu 14.10.4 / Mac OS X 10.10 / Windows 10
+ Java 1.8.0 or higher
+ Ant 1.9.3 or higher
+ Node 0.10.25 or higher
+ Octave 3.8.0 or higher
+ Android 4.4.3 (optional)

DOWNLOAD
--------
You can download the system directly from the command line:

>git clone https://github.com/cuayahuitl/SimpleDS.git

You can also download the system as a zip file using the following URL, 
and then unzip it in your path of preference. 
https://github.com/cuayahuitl/SimpleDS/archive/master.zip 

You should download pre-trained word vectors if you want support for word embeddings, e.g. http://nlp.stanford.edu/data/glove.6B.zip and put the text file of your choice under YourPath/SimpleDS/resources/English. Apply the same procedure for other languages.

COMPILATION
-----------
>cd YourPath/SimpleDS

>ant

EXECUTION
---------
You can run the system from two terminals:

Terminal1:YourPath/SimpleDS>ant SimpleDS

Terminal2:YourPath/SimpleDS/web/main>nodejs runclient.js (train|test) [num_dialogues] [-v|-nv]

![Alt text](https://github.com/cuayahuitl/SimpleDS/blob/master/screenshots/Screenshot-SimpleDS-WordEmbedding.png "Example screenshot of SimpleDS at test time (Dialogues=1, Verbose=true)")

For practical reasons, you can specify the number of dialogues and verbose mode from the command line. The values of these parameters would override the values specified in the file config.txt.

The outputs from the training phase consists in the learnt interaction policy (json file under the folder 'results/language'), and logged performance metrics (txt file under the 'results/language'). Depending on the config file, the metrics produce multiple rows with the following information: number of dialogues, average reward, epsilon value, number of actions per state, number of dialogues, and execution time (in hours). The outputs from the test phase are similar exept that no learnt policy is generated. In addition, executing the system in verbose mode would print out training/test dialogues -- according to the specified parameters.

PLOTTING
--------
You can visualise a learning curve of the SimpleDS agent according to number of learning steps in the x-axis and average reward + learning time in the y-axis. Learning curves can be generated for newly trained or pre-trained policies in the currently supported languages (English, German and Spanish).

>cd YourPath/SimpleDS

>octave scripts/plotdata.m results/english/simpleds-output.txt

>[From the command line, press the space bar key for termination]

or 

>cd YourPath/SimpleDS

>octave scripts/plotdata.m results/english/simpleds-output.txt results/english/simpleds-output.png

>[From the command line, press the space bar key for termination]

The latter generates an image of the plot in png (Portable Network Graphics) format. The file plotdata.m can also be used from Matlab if that software is prefered. The following learning curves (available from YourPath/results/*/*.png) can be obtained with the default parameters for the supported languages: [English](https://github.com/cuayahuitl/SimpleDS/blob/master/results/english/simpleds-output-wordembedding.png), [German](https://github.com/cuayahuitl/SimpleDS/blob/master/results/german/simpleds-output.png) and [Spanish](https://github.com/cuayahuitl/SimpleDS/blob/master/results/spanish/simpleds-output.png).

The following learning curve was generated from image-based supervised learning learning: [spectrogram](https://github.com/cuayahuitl/SimpleDS/blob/master/results/pixels/simpleds-output.png).

CONFIGURATION
-------------
The config file "YourPath/SimpleDialogueSystem/config.txt" has a number parameters number of dialogues, verbose outputs, saving frequency, etc. You may want to set Verbose=false during training and Verbose=true during testing. You may also want to set a high number of dialogues during training (e.g. Dialogues=2000) and a low one during tests (e.g. Dialogues=1). You may want to change the system/user responses if you want different verbalisations. If this is the case, then you will also want to update the demonstration dialogues in the folder YourPath/SimpleDS/data/.

REFERENCES
----------
SimpleDS has been applied to spoken dialogue systems and interactive games. See the following references for further information.

+ H. Cuayáhuitl. [Deep Reinforcement Learning for Conversational Robots Playing Games](http://eprints.lincoln.ac.uk/29060/). In IEEE RAS International Conference on Humanoid Robots (HUMANOIDS), 2017.
+ H. Cuayáhuitl, S. Yu. [Deep Reinforcement Learning of Dialogue Policies with Less Weight Updates](http://eprints.lincoln.ac.uk/27676/1/multids-interspeech2017.pdf). International Conference of the Speech Communication Association (INTERSPEECH), 2017.
+ H. Cuayáhuitl, S. Yu, A. Williamson, J. Carse. [Scaling Up Deep Reinforcement Learning for Multi-Domain Dialogue Systems](http://eprints.lincoln.ac.uk/26622/1/PID4664349.pdf). International Joint Conference on Neural Networks (IJCNN), 2017.
+ H. Cuayáhuitl, S. Yu, A. Williamson, J. Carse. [Deep Reinforcement Learning for Multi-Domain Dialogue Systems](https://arxiv.org/pdf/1611.08675.pdf). NIPS Workshop on Deep Reinforcement Learning, 2016.
+ H. Cuayáhuitl, G. Couly, G. Olalainty. [Training an Interactive Humanoid Robot Using Multimodal Deep Reinforcement Learning](https://arxiv.org/pdf/1611.08666.pdf). NIPS Workshop on the Future of Interactive Learning Machines, 2016.
+ H. Cuayáhuitl. [SimpleDS: A Simple Deep Reinforcement Learning Dialogue System](http://arxiv.org/abs/1601.04574). International Workshop on Spoken Dialogue Systems (IWSDS), 2016.
+ H. Cuayáhuitl, S. Keizer, O. Lemon. [Strategic Dialogue Management via Deep Reinforcement Learning](https://arxiv.org/pdf/1511.08099v1.pdf). NIPS Workshop on Deep Reinforcement Learning, 2015.

See ["How to apply SimpleDS to interactive systems"](https://github.com/cuayahuitl/SimpleDS/blob/master/doc/How2UseSimpleDS.txt) if you would like to use SimpleDS in your own system.

COMMENTS/QUESTIONS/COLLABORATIONS?
-------------------
Contact: [Heriberto Cuayahuitl](http://staff.lincoln.ac.uk/hcuayahuitl)

Email: h.cuayahuitl@gmail.com
