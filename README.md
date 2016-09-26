# Framework to easy add/execute Emerging Pattern Mining (EPM) Algorithms.

EPM is a data mining task which tries to describe a set of data using supervised learning. The main objectives of EPM are:

* Discover emerging trends on timestamped data.
* Discover differences between multiple varaibles.
* Discover characteristical differencies between classes.

In this framework you can execute the most important EPM algorithms that exists in the literature. The supported data format is the KEEL dataset format.

# How to add a new EPM

To add a new algorithm to the framework you only have to create a new class that extends the "Model" class and override the methods learn() and predict() which are used to learn the model and predict new instances respectively.
Additionally, you must have to add an entry of the algorithm on "algorithms.xml" an example of entry with all parameters possibilities is shown in the file as a commentary.