This file contains all instructions necessary to

* install and setup the code
* do the lab work and experimentation

SETUP
-----

###0. Assumptions

* you are running the following on a machine (e.g. laptop) with **GUI capabilities** and not on a text terminal!
* running on a headless server (e.g. AWS EC2 Linux machine) **is not recommended**, even if you setup remote GUI such as XWindows.

###1. Make sure Java SE Development Kit 8 is installed (`javac` compiler required)


    % java -version
    java version "1.8.0_161"
    Java(TM) SE Runtime Environment (build 1.8.0_161-b12)
    Java HotSpot(TM) 64-Bit Server VM (build 25.161-b12, mixed mode)
    
    % javac -version
    javac 1.8.0_161


Go to [installation page](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) otherwise.

###2. Make sure Maven is installed

Maven is used to build the code and run specific Java classes.

Install from [Apache Maven Project](https://maven.apache.org/install.html) page.
Make sure `JAVA_HOME` environment variable is set correctly.

    % mvn --version
    Apache Maven 3.5.2 (138edd61fd100ec658bfa2d307c43b76940a5d7d; 2017-10-18T07:58:13Z)
    Maven home: /usr/share/apache-maven
    Java version: 1.8.0_171, vendor: Oracle Corporation
    Java home: /usr/java/jdk1.8.0_171-amd64/jre
    Default locale: en_US, platform encoding: UTF-8
    OS name: "linux", version: "4.9.81-35.56.amzn1.x86_64", arch: "amd64", family: "unix"

    
 ###3. Make sure Git is installed
 
    
 from the [Git install page](https://git-scm.com/downloads).
 
    % git --version
    git version 2.13.6
   
 
 ###4. Clone the git repository with the Reinforcement Learning codebase used in this lab
 
 Switch to the directory where you want the repository to be cloned to.
 The following command will create `reinforcement-learning` subdirectory with the codebase.
 
    % git clone https://github.com/dbatalov/reinforcement-learning
    Cloning into 'reinforcement-learning'...
    ...
    
    % ls -l
    total 4
    drwxrwxr-x 6 ec2-user ec2-user 4096 Apr 18 18:08 reinforcement-learning

###5. Build the code using Maven

    % cd reinforcement-learning
    % mvn package
    
This command will result in lengthy output that should end with a successful build.
Now you are ready to run the Rocket Lander example.

###6. Running RocketLander GUI

####6.1 Start the simulator

First, put the compiled jar file into local maven repository 

    % mvn install
    
Then, run the RocketLander simulator (this must be done from the RocketLander subdirectory)

    % cd RocketLander
    % mvn exec:java -Dexec.mainClass=com.batalov.RL.RocketLanderFrame
    
This should open a simulator window with RocketLander positioned on the ground.
Use the arrow buttons to fire the engines and get lift off!
Firing a single engine will cause the rocket to rotate in the corresponding direction without adding thrust.
Both engines must be fired to produce thrust in the direction the rocket is pointing.
Release the arrow button to stop the corresponding engine from firing.

![Simulator Screenshot](images/simulator-screenshot.png)

####6.2 Land the RocketLander

Once the RocketLander takes off, try landing it.
To land safely the rocket must not

* be going down faster than 3 m/sec
* have a horizontal speed of more than 1 m/sec
* be rotated more than 10 degrees away from a horizontal position

If you manage to land the rocket **successfully**, it will self-correct and you will be able to take off again.
If the rocket **crashes**, you must restart the simulator. 

### Congrats, you are done with setup!


Lab Work
========

###TASK 1. Observe Q-Learning landing the Rocket

The goal of this section is to configure the experiment with the right parameters, observe the training of Q-Learning model and it's convergence to a safe landing policy.

Using your favorite editor, open the file `RocketLanderExperiment` class:

    vim RocketLander/src/main/java/com/batalov/RL/RocketLanderExperiment.java

In this experiment, the rocket is only able to move up and down without rotation.
This is achieved by engines always firing together.

####1.1 Set the starting height of the rocket to be 50 meters:

                // ===== STEP 5. initialize real environment
                // define the state of the environment at the beginning of each learning session
                final RocketLander startingLander = new RocketLander();
                startingLander.getPosition().setLocation(0, 50.0);

####1.2 Set the *ceiling* to be 60 meters:

        private static final float CEILING_HEIGHT = 60.0f;
        private static boolean isTooHigh(final RocketLander lander) {
                return lander.getPosition().getY() > CEILING_HEIGHT;
        }

If the rocket flies above the virtual ceiling, the episode ends with failure and is equivalent to crashing.

####1.3 Adjust the range of valid rocket heights to be in the range of `[-1 .. 99]` meters:

                inputDescriptors.put(DESC_NAME_HEIGHT,  FixedPointInputDescriptor.newWith(-1.0f, 99.0f, PRECISION_HEIGHT));
                
            
We don't want to use 60 meters, because the rocket might be well above 60 at the end of a simulation step, before the ceiling breach is detected. 99 provides sufficient room and restricts the height to only 101 possible values, since the `PRECISION_HEIGHT` is set to 0 digits after decimal point.

####1.4 Set the learning rate and the discount factor both to 1

                // step 4. configure the model
                qlo.setAlgorithmLearningRate(modelId, 1);
                qlo.setAlgorithmDiscountFactor(modelId, 1);

####1.5 Check to make sure `reinforcement1` function is being used:

                                      reinforcement = reinforcement1(oldState, actionName, lander);

####1.6 Examine the reinforcement function definition to understand how the landing goal is defined:

        // penalize crash (or out of bounds) heavily, otherwise penalize for time wasted
        private static Double reinforcement1(final RocketLander oldState, final String actionName, final RocketLander lander) {
                return lander.crashed() || isTooHigh(lander) ? -1000.0 : -1.0;
        }

####1.7 Finally, run the experiment

    % cd RocketLander
    % mvn exec:java -Dexec.mainClass=com.batalov.RL.RocketLanderExperiment
    
On the console you will see information printed for each time step:

    episode = 3, time = 14
    s  : eng = < * -- * >, pos = [0.000000, 47.010551], vel = [0.000000, -2.753000], rot = 0.000000
    a = burn, r = -1.0
    s' : eng = < * -- * >, pos = [0.000000, 46.786201], vel = [0.000000, -1.734000], rot = 0.000000

The first line shows the episode number and the time step.
The second line shows the state of the rocket before action is chosen.
Since in this experiment the rocket is only able to move vertically, the horizontal components of position and velocity as well as the rotation always remain 0.
The state of each engine is depicted with an asterisk `*` (engine burn) or a dot `.` (engine off).
The third line shows the action chosen - both engines are to burn or to be turned off, as well as the reinforcement value obtained.
The last line shows the state of the rocket after the action has been applied and a simulation time step had passed.

At the end of the episode the end result is printed, with three possibilities:
* `^^^^^` - rocket went through the "ceiling"
* `CRASH` - rocket crashed down
* `LAND!` - successful landing

for example:

    episode = 664, time = 36, sslc = 5, result = CRASH

During each episode, we also measure and report the number of time steps that has passed since last time the Q Table had been updated.
This is reported as `sslc` or "steps since last change". This metric is an indication of convergence.
If, for example, the Q Table was not updated at all during the entire episode (i.e. `sslc` is the same as `time`), it means that no learning actually happened and the model possibly converged to optimal behavior.
"Possibly" because it is not a guarantee that during the following episode some updates might happen due to randomized nature of action selection.

It may take many episodes before the model converges (or the model may never converge) and in this particular case it takes about 7000 episodes!
So you don't wait for each episode in real-time, click on the simulator window anywhere to speed up the simulation. In this mode, only the results of the episodes are printed.
Keep clicking to toggle between real-time and fast simulation.

###TASK 2. Play with parameters

Make modifications and re-run the experiment taking note of the effect on learning.
Here are some ideas to try:

* increase/decrease the starting height of the rocket. Don't forget to change the "ceiling" and height range descriptor!
* change gravity to that of the Moon, see `GRAVITY` constant inside `RocketLander` class
* play with learning rate and discount factor, both must be in `[0..1]` range

###TASK 3. Change the goal - penalize engine burn

The "goal" of the RL agent is specified in the form of the reinforcement function.
It's an indirect way of supplying the goal since we are not specifying exactly what needs to be done.
The original reinforcement function is not really helping the agent understand what the desired state is, i.e. slowly getting to the ground.
It penalizes crashes heavily, and otherwise simply penalizes for wasting time.
This forces an exploration of the state space until it stumbles upon safe landing by accident.

How can we give stronger hints to the agent of what is the right behavior?
Clearly, when the engines are firing the rocket goes into the wrong direction.
Vice versa, when the engines are off, the rocket decelerates and eventually falls down closer to the ground.
So one simple idea is to penalize the agent for burning the engines, rather than for just wasting time.

Add a new reinforcement function `reinforcement2` that encodes this idea and compare results with the original function.
The agent should now learn to land the rocket in roughly half the time, though the optimal behavior may now take a few more time steps, since we are optimizing for less engine firing and not less time, though these are somewhat related.

###TASK 4. Change the goal - penalize engine burn when rocket goes up

When engine firing causes the rocket to go up, that's clearly the wrong behavior, but when the engines fire to slow the rocket's fall, this may actually be the right behavior depending on how close the rocket is to the ground.

Add a new reinforcement function `reinforcement3` that encodes this idea and compare results with the function in previous task.

###TASK 4. Change the goal - add fuel consumption

Depending on how you implemented the above function you are likely seeing faster convergence, though, now the engines might be burning much more frequently than necessary and it takes longer to land.
Next idea would be to model real fuel consumption of the rocket. This is a much more involved task because it requires changes to `RocketLander` class - maintaining a new state variable of fuel left.
Additionally, the Q-Learning algorithm needs to observe the amount of fuel left as another sensory value, which you need to provide to a `QLearningOrchestrator` as an explicit input with the corresponding `InputDescriptor`.
When the rocket runs out of fuel, engines cannot fire anymore and the rocket falls to the ground. Like in the previous tasks, you need to consider what kind of reinforcement function to use.

###TASK 5. BONUS EXERCISE - rotating rocket

##### WARNING: This is a difficult task and you are likely to run out of time! So this could be a good take home exercise.

For this task we want to allow the rocket to move freely not just along the vertical axis. This means allowing the two engines to act independently. To make sure the rocket does not fly too far away, you may want to establish virtual "walls" to the left and to the right of the rocket, in addition to the virtual "ceiling".

Bonus points if you can make the rocket land onto the landing pad and not just anywhere on the ground.    
 