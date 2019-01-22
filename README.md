# Framework to easy add/execute Emerging Pattern Mining (EPM) Algorithms.

EPM is a data mining task which tries to describe a set of data using supervised learning. The main objectives of EPM are:

* Discover emerging trends on timestamped data.
* Discover differences between multiple varaibles.
* Discover characteristical differencies between classes.

In this framework you can execute the most important EPM algorithms that exists in the literature. The framework provides both a graphical user interface (GUI) and a command-line interface (CLI) for an easy deployment of your own experiments. In addition, the framework also allows you to use the different algorithms that are available within your own machine learning processes easily. 

Right now, the supported data format is the KEEL dataset format.

# How to add a new EPM

To add a new algorithm to the framework you only have to create a new class that extends the "Model" class and override the methods learn() and predict() which are used to learn the model and predict new instances respectively.
Additionally, you must have to add an entry of the algorithm on "algorithms.xml" an example of entry with all parameters possibilities is shown in the file as a commentary. Additional information about how to use it and how to implement algorithms is available at Manual_Spanish.pdf (in spanish). This software is incompleted, and the development of new features and addition of new algorithms and documentation will be added in the near future.
