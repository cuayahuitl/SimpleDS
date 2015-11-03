# SimpleDS
A Simple Deep Reinforcement Learning Dialogue System

DESCRIPTION
-----------
SimpleDS is a simple dialogue system trained with deep reinforcement learning. In contrast to other dialogue systems, this system selects dialogue actionsdirectly from raw (noisy) text of the last system and user responses. The motivation is to train dialogue agents with as little human intervention as possible. 

This system runs under a client-server architecture, where the learning agent (in JavaScript) acts as the "server" and the environment (in Java) acts as the "client". They communicate by exchanging messages, where the server tells the client the action to execute, and the client tells the server the state and reward observed. SimpleDS is based on ConvNetJS (cs.stanford.edu/people/karpathy/convnetjs/), which implements the algorithm `Deep Q-Learning with experience replay' (Mnih, et al. 2015).SimpleDS is a dialogue system on top of ConvNetJS with support for multi-threaded and client-server processing, and fast learning via constrained search spaces.

This system is for experimental purposes, represents work in progress, and is therefore released without any guarantees.

REQUIREMENTS
------------
This system was implemented and tested under Linux with the following software -- though it should run in other operating systems with minor modifications.
+ Ubuntu 14.10.4
+ Java 1.8.0
+ Ant 1.9.3 
+ Node 0.10.25
+ Octave 3.8.0

COMPILATION
-----------
>cd YourPath/SimpleDialogueSystem

>ant

EXECUTION
---------
>cd YourPath/SimpleDialogueSystem

>scripts/run.sh train

![Alt text](https://github.com/cuayahuitl/SimpleDS/blob/master/screenshots/Screenshot-SimpleDS-train.png "Example screenshot of SimpleDS during training (Dialogues=2000, Verbose=false)")

>[From the command line, press Ctrl+C for termination]

or 

>cd YourPath/SimpleDialogueSystem

>scripts/run.sh test

![Alt text](https://github.com/cuayahuitl/SimpleDS/blob/master/screenshots/Screenshot-SimpleDS-test.png "Example screenshot of SimpleDS at test time (Dialogues=1, Verbose=true)")

>[From the command line, press Ctrl+C for termination]

Alternatively, you can run the system from two terminals:

Terminal1>ant SimpleDS

Terminal2>node web/main/runclient.js [train|test]

PLOTTING
--------
You can visualise a learning curve of the SimpleDS agent according to number of learning steps in the x-axis and average reward + learning time in the y-axis.

>cd YourPath/SimpleDialogueSystem

>octave scripts/plotdata.m results/simpleds-output.txt

>[From the command line, press the space bar key for termination]

![Alt text](https://github.com/cuayahuitl/SimpleDS/blob/master/results/simpleds-output.png "Example learning curve of a SimpleDS agent")

or 

>cd YourPath/SimpleDialogueSystem

>octave scripts/plotdata.m results/simpleds-output.txt results/simpleds-output.png

>[From the command line, press the space bar key for termination]

The latter generates an image of the plot in png (Portable Network Graphics) format. 
The file plotdata.m can also be used from Matlab if that software is prefered.

CONFIGURATION
-------------
The config file "YourPath/SimpleDialogueSystem/config.txt" has the following parameters:

Dialogues=Number of dialogues for training/test (positive integer)

Verbose=Shows compressed information or detailed info (false or true)

SysResponses=Path and file name of system responses (e.g. resources/SysResponses.txt)

UsrResponses=Path and file name of system responses (e.g. resources/UsrResponses.txt)

SlotValues=Slot-value pairs of the system (e.g. resources/SlotValues.txt)

DemonstrationsPath=Path to the demonstration dialogues (e.g. data/)

DemonstrationsFile=Pointer to training instances from demonstrations (models/demonstrations.arff)

SlotsToConfirm=Number of slots to confirm (positive integer, e.g. 3)

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

You may want to set Verbose=false during training and Verbose=true during tests. You may also want to set a high number of dialogues during training (e.g. Dialogues=2000) and a low one during tests (e.g. Dialogues=1). You may want to change the system/user responses if you want different verbalisations. If this is the case, then you will also want to update the demonstration dialogues in the folder YourPath/SimpleDS/data/.

COMMENTS/QUESTIONS?
-------------------
Contact: Heriberto Cuayahuitl
Email: h.cuayahuitl@gmail.com
