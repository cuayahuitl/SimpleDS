# SimpleDS
A Simple Deep Reinforcement Learning Dialogue System

DESCRIPTION
-----------
SimpleDS is a simple dialogue system trained with deep reinforcement learning. In contrast to other dialogue systems, this system selects dialogue actions directly from raw (noisy) text of the last system and user responses. The motivation is to train dialogue agents with as little human intervention as possible.

This system runs under a client-server architecture, where the learning agent (in JavaScript) acts as the "server" and the environment (in Java) acts as the "client". They communicate by exchanging messages, where the server tells the client the action to execute, and the client tells the server the actions available, environment state and reward observed. SimpleDS is based on [ConvNetJS](http://cs.stanford.edu/people/karpathy/convnetjs/), which implements the algorithm [`Deep Q-Learning with experience replay' (Mnih, et al. 2013)](http://arxiv.org/pdf/1312.5602v1.pdf). SimpleDS is a dialogue system on top of ConvNetJS with support for multi-threaded and client-server processing, and fast learning via constrained search spaces.

This system has been tested with simulated and real dialogues using the Google Speech Recogniser. It has also been tested in three different languages: English, German and Spanish. SimpleDS is for experimental purposes, represents work in progress, and is therefore released without any guarantees.

SOFTWARE
--------
This system was implemented and tested under Linux and Mac OS X with the following software -- though it should run in other operating systems with minor modifications.
+ Ubuntu 14.10.4 / Mac OS X 10.10
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

COMPILATION
-----------
>cd YourPath/SimpleDS

>ant

EXECUTION
---------
>cd YourPath/SimpleDS

>scripts/run.sh train

>[From the command line, press Ctrl+C for termination]

or 

>cd YourPath/SimpleDS

>scripts/run.sh test

>[From the command line, press Ctrl+C for termination]

Alternatively (recommended), you can run the system from two terminals:

Terminal1:YourPath/SimpleDS>ant SimpleDS

Terminal2:YourPath/SimpleDS/web/main>nodejs runclient.js (train|test) [num_dialogues] [-v|-nv]

![Alt text](https://github.com/cuayahuitl/SimpleDS/blob/master/screenshots/Screenshot-SimpleDS.png "Example screenshot of SimpleDS at test time (Dialogues=1, Verbose=true)")

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

The latter generates an image of the plot in png (Portable Network Graphics) format. The file plotdata.m can also be used from Matlab if that software is prefered. The following learning curves (available from YourPath/results/*/*.png) can be obtained with the default parameters for the supported languages: [English](https://github.com/cuayahuitl/SimpleDS/blob/master/results/english/simpleds-output.png), [German](https://github.com/cuayahuitl/SimpleDS/blob/master/results/german/simpleds-output.png) and [Spanish](https://github.com/cuayahuitl/SimpleDS/blob/master/results/spanish/simpleds-output.png).

CONFIGURATION
-------------
The config file "YourPath/SimpleDialogueSystem/config.txt" has the following parameters:

Dialogues=Number of dialogues for training/test (positive integer)

Verbose=Shows compressed information or detailed info (false or true)

Language=Defines the (spoken) language to use (english, german, spanish)

SysResponses=Path and file name of system responses (e.g. resources/SysResponses.txt)

UsrResponses=Path and file name of system responses (e.g. resources/UsrResponses.txt)

SlotValues=Slot-value pairs of the system (e.g. resources/SlotValues.txt)

DemonstrationsPath=Path to the demonstration dialogues (e.g. data/)

DemonstrationsFile=Pointer to training instances from demonstrations (models/demonstrations.arff)

MinimumProbability=Minimum probability (>=0) for probable actions considered for action-selection

SlotsToConfirm=Number of slots to confirm (positive integer, e.g. 3)

OutputPath=The directory where the output files (policy and metrics) will be stored

NoiseLevel=Scores under this level (<=0.2) would receive distorsion to model noisy recognition

AddressPort=Address and port of the client socket (e.g. ws://localhost:8082/simpleds)

SavingFrequency=This number defines the frequency for policiy/output saving (positive integer)

NumInputs=This number defines the number of input nodes of the neural net (positive integer) 

NumActions=This number defines the number of actions of the agent (positive integer)

LearningSteps=This number defines the number of time steps during learning (positive integer)

ExperienceSize=This number defines the size of the experience replay moemory (positive integer)

BurningSteps=This number defines the time steps with random action selection (positive integer)

DiscountFactor=This number defines gamma parameter also known as discount factor (real number)

MinimumEpsilon=This number defines the minimum epsilon during learning (real number)

BatchSize=This number defines the batch size (positive integer, e.g. 32 or 64)

AndroidSupport=This variable is used to test dialogues with a real speech recogniser (true or false)

SocketServerPort=This number defines the socket port used for communication with Android (positive integer)

You may want to set Verbose=false during training and Verbose=true during tests. You may also want to set a high number of dialogues during training (e.g. Dialogues=2000) and a low one during tests (e.g. Dialogues=1). You may want to change the system/user responses if you want different verbalisations. If this is the case, then you will also want to update the demonstration dialogues in the folder YourPath/SimpleDS/data/.

REFERENCES
----------
SimpleDS has been applied to spoken dialogue systems and interactive games. See the following references for further information.

+ H. Cuayáhuitl. [SimpleDS: A Simple Deep Reinforcement Learning Dialogue System](http://arxiv.org/abs/1601.04574). International Workshop on Spoken Dialogue Systems (IWSDS), 2016.
+ H. Cuayáhuitl, S. Keizer, O. Lemon. [Strategic Dialogue Management via Deep Reinforcement Learning](http://arxiv.org/abs/1511.08099). NIPS Workshop on Deep Reinforcement Learning, 2015.

See ["How to apply SimpleDS to interactive systems"](https://github.com/cuayahuitl/SimpleDS/blob/master/doc/How2UseSimpleDS.txt) if you would like to use SimpleDS in your own system.

COMMENTS/QUESTIONS/COLLABORATIONS?
-------------------
Contact: [Heriberto Cuayahuitl](http://staff.lincoln.ac.uk/hcuayahuitl)

Email: h.cuayahuitl@gmail.com
