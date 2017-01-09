# enrich-your-text
An all-in-one toolkit for Wikipedia extraction, keyword lookup and disambiguation. My main intent was to allow anyone interested in the topic have an easy, working out-of-the-box solution which makes it possible to test the standard lookup/disambiguation algorithms as well as to introduce new algorithms.

It consists a set of tools for preprocessing Wiki XML database [dumps](https://dumps.wikimedia.org/), putting the data into a local database then utilizing the extracted knowledge by translating a regular text file into an enhanced version with automatically annotated links to Wikipedia.

The application is based on Java and uses MongoDB for the backend. It has been tested both on Windows and Linux platforms.

# Requirements
* At least 8 GB of RAM for extraction of enwiki, 16 GB is optimal.
* After extraction process most of the data is removed
* The database should be located on an SSD drive for optimal performance. For more info on how to setup MongoDB to use a specific drive for its data folder please refer to [Windows](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/#configure-a-windows-service-for-mongodb-community-edition) or appropriate Linux (for instance, [Redhat](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-red-hat/#data-directories-and-permissions)) documentation.

# Installation
1. Install Mongo Community Server Edition from [here](https://www.mongodb.com/). Make sure that the server is started according to the instructions for [Windows](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/) and [Linux](https://docs.mongodb.com/manual/administration/install-on-linux/).
2. Install latest Java Runtime Environment from [here](https://www.java.com/en/).
3. Download the latest release of *enrich-your-text* from [here](https://github.com/mnarusze/enrich-your-text/releases/latest).
4. Unpack the archive, then run **EnrichYourText.jar**.

# Usage

## Database configuration
1. Click *Edit* -> *Database Configuration* or *Ctrl+P*.
2. Make sure that credentials (username, password, port) are correct.
3. Test the settings, then save if a *success* popup appears.

## Extraction
1. Download **"\*pages-articles.xml.bz2"** dump file of any Wikipedia from [here](https://dumps.wikimedia.org). For example, you can always find the latest version of **enwiki** at the following [link](https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2). Unpack it into a regular XML file.
2. Click *Edit* -> *Extract Wikipedia Dump* or *Ctrl+E*.
3. Point to the extracted dump file 
4. Specify the database name.
5. (Optional) Set the starting step in case an earlier extraction had to be finished. The step will start from its own beginning and requires the previous steps to be completed.
6. (Optional) Set database parameters.
   * *Add indexes* - if set, the most important indexes will be created. Indexing utilizes RAM but makes it possible to do extremely fast lookups. If not set, no indexes will be created.
   * *Force* - if set, any database with the same name which might already exist will be overwritten. If not set, an error will popup when trying an existing name.
   * *Use stemming* - if set, all labels (phrases used as links in Wikipedia) will be stemmed using [Porter Stemmer](https://tartarus.org/martin/PorterStemmer/). If not set, original text will be used. Please note it only works for English Wikipedia!
7. Run the extraction by clicking "Unpack Wikipedia". The process will take a varying amount of time depending on the size of the selected Wikipedia and the power of your machine. On a setup with i7-4900K, 16 GB DDR3 RAM and database on Samsung SSD 850 EVO 250 GB extraction of *SimpleWiki* takes around *20 minutes* while extraction of *EnWiki* takes around *3 days*.

## Importing ready to use dumps
TODO

## Enrichment
1. After extraction, return to the main application window.
2. Select the database name that interests you.
   * If no database is present, use *Reload databases* button.
3. Paste the text into the top window or open it using *File* -> *Open file* (*Ctrl+O*). If inserting text in Wikimedia format, please select a proper *Input Document Type*.
4. Set extraction parameters.
   * If the selected database uses stemming then select the stemmed versions of *Input Parsing*, *Keyphrase Lookup* and *Annotation Algorithms*.
   * By default, only change the *Disambiguation algorithm*.
   * Override the values if you know how the algorithm works. For more info, look into code.

# Development
I have developed the application using NetBeans and I recommend using this IDE. Of course, other environments like Eclipse work perfectly fine.

## Adding new algorithms
TODO
