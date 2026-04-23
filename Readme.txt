Information Retrieval System - Phase 3
======================================

This project implements an information retrieval system using TF-IDF weighting and the vector space model (VSM) for ranking documents based on their relevance to user queries.

File Structure
--------------
* IRSystem.java: The main Java application to run the information retrieval system.
* Topic.java: The model class used to hold the parsed query details.
* Porter.java: The Java class used to stem words to their root form.
* stopwordlist.txt: A file containing a list of stopwords to be excluded during text processing.
* documents/: A folder containing the documents to be indexed and searched.
* topics.txt: A file containing the queries to be processed.
* vsm_output.txt: The output file where the ranked results for each query are saved.

How to Run the Program
----------------------

1. Prerequisites:
   Ensure you have the Java Development Kit (JDK) installed on your system. Open your terminal or command prompt and navigate to the project directory:

   cd information-retrival-phase3


2. Compile the Code:
   Compile the Java source files using the javac command:

   javac *.java


3. To Run the Program:
   Execute the compiled main class to process the queries and rank the documents:

   java IRSystem


4. View the Results:
   The system will process the documents and queries in batch mode. The results will be saved in vsm_output.txt in the format:

   TOPIC    DOCUMENT    UNIQUE#    COSINE_VALUE