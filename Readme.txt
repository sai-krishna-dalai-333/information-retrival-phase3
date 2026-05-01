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
* output_title.txt: The ranked results using only the Title setting.
* output_title_desc.txt: The ranked results using the Title + Description setting.
* output_title_narr.txt: The ranked results using the Title + Narrative setting.

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
   To comply with project specifications, the system processes queries across three different context settings and generates three distinct output files:
   
   1. output_title.txt      (Setting 1: Main Query/Title only)
   2. output_title_desc.txt (Setting 2: Title + Description)
   3. output_title_narr.txt (Setting 3: Title + Narrative)

   All three files adhere to the required tab-separated batch-mode format:
   TOPIC    DOCUMENT    UNIQUE#    COSINE_VALUE