# Typeahead System Design

This document contains the system design approach for a large scale autocomplete/typeahead suggestions system.

# Requirements

## Functional Requirements

*  Get a list of top phrase suggestions based on the user input (prefix)
*  Suggestions are ordered by weighing the frequency and recency of a given phrase/word

The main two APIs will be:

*  *topSuggestions(prefix)*: returns the list of top suggestions for a given prefix
*  *acceptPhrase(phrase)*: accepts the searched phrase into the system. This phrase will later be available as suggestions for subsequent *topSuggestions(prefix)* requests.

## Non-functional Requirements

*  *High availability* - designed keeping in mind that anything that can go wrong, will go wrong. Hence each component of the system is designed 
   to self heal and manage its responsibilities without any down-time
*  *Performant* - Suggestions should be delivered to the user in a lighting speed. The response time should be quicker than the user’s type 
    speed (< 50ms). Latency expectation is single digit milliseconds latency.
*  *Scalable* - able to function efficiently, under heavy load when a large number of requests are made
*  *Durable* - previously searched phrases (for a given time-span) should be available, even if there is a hardware fault or crash


## System design considerations

The system is designed and implemented keeping in mind all of the above requirements. Hence the overall design approach that I have envisioned is extensible in a sense that every design decision made along the way is in the service of enhancing the system towards the end goal of producing large scale, distrubuted, fault tolerant and performant system.


<img src="https://github.com/SaurabhKMistry/typeahead-app/blob/master/images/Typeahead-High-Level.jpeg">

### Details of the component diagram

The components in color coded with green are the ones implemented for MVP. In the next alpha and beta stages of the product releases, the typeahead system would be evolved incrementally to what I envisioned as future final state. 

**Client** - A React UI component that interacts with typeahead system

**Auto-complete rest api** - A spring Boot powered rest end point for returning top 10 auto-suggestions (read flow)

**Accept-Suggestion rest api** - A spring Boot powered rest end point for accepting new suggestions into the typeahead system (write flow)

**Elastic Search** -  Open source search engine that powers auto-suggestions using its in built *Completion Suggester* suggest feature

**Amazon S3** - Periodic snapshot of elastic search data is backed up in Amazon S3. This is needed in case data needs to be replayed in case of disaster

**Trends Aggregator** - Talks to third party APIs to collect recent trends, news feed, etc and stores them in ES

**Apache Kafka Cluster** - Event bus to provide asynchronous write to ES for trends, news feed (fed via Aggregator component) and user suggestions


***For the purpose of the demo***, the components in green are implemented (React UI, Spring Boot auto-complete Rest API and programmatic usage of Elastic Search Completion Suggester feature capability). 

### Setup and usage of Typeahead MVP

### Prerequisite

* This setup requires installation of ```docker```. So if you don't have it installed, please install it by following [docker installation steps](https://www.thegeekdiary.com/how-to-install-docker-on-mac/)
* Java 11 and above
* Maven 2

### Step 1

Clone the source code available at https://github.com/skmistry/typeahead-react-spring-boot.git

You should see a following directory structure,

Root folder *typeahead-react-spring-boot* with 

* *typeahead-react* folder containing front end react UI component 
* *typeahead-rest-api* folder containing rest api backend component for typeahead system.

### Step 2

Go to the directory *typeahead-rest-api* and run the following command

```mvn clean install```

### Step 3

Go to the root folder *typeahead-react-spring-boot* and run the following command

```docker-compose up -d --build```

The above command will deploy and start the following services for you in a separate docker container,

* React UI component
* 2 instances of *typeahead-rest-api* on 2 separate docker containers
* Nginx load balancers which would balance the load between above 2 rest api services
* Elastic Search to which above 2 rest api services would connect for fetching typeahead suggestions

After successful execution of this command, you should have all the services of typeahead system up and running in their respective docker containers. To confirm this, run the following command,

```docker-compose ps --all```

The status column in the result of this command should show the status of all services as *UP*

### Step 4

You can interact with the system by accessing http://localhost:3000 in your browser. The search suggestions will be provided by the system as you start typing.


### Step 5

In order to gracefully shutdown all running docker containers created by docker-compose, you can run following command,

```docker-compose down```


## Tech Stack Selection Process 

### React UI

React is used to implement landing page of typeahead functionality.

There are numerous popular Javascript frameworks out there like *Angular*, *Vue.js*, *Meteor* and many others. So obvious question is why *React* over others.

* Learning curve of other Javascript frameworks is steeper than React. React is extremely easy to learn. To start with this craft demonstration implementation, I did not know anything about React but within 3 days I was able to learn it and develop the entire UI with it.

* React is lightening fast. React uses virtual DOM to efficiently handle update of real html DOM. Updating html DOM is a very costly operation and majority of the slowness of page rendering comes from the time spent in updating DOM and then rendering it in the UI. Whenever react has to render any element on the UI, it compares earlier virtual DOM with the new virtual DOM and only makes those real DOM element updates that are absolutely necessary and hence it is very fast and one can experience it while developing it with React 

* React inherently supports modular and writing cleaner code. It is built from ground up on the core foundation of creating components that can work together independently. React actually nudges you towards thinking in terms of visualizing, building and managing components. Designing with React is very similar to designing using OOPs concept wherein you take into account individual components, their interaction with each other, their state transition, their individual performance etc

Hence it was not a difficult choice to go with React JS for typeahead UI development

### Elastic Search (ES)

ES is chosen as an auto-complete typeahead suggestion engine because after evaluating ES against each functional and non-functional requirements outlined above, it satisfies and even excel at each one of them.

* ES provides auto-complete functionality out of the box. It has a *suggest* feature which suggests similar looking terms based on a provided text by using a *suggester*. The ES suggester **Completion Suggester** is designed specifically to aid in the implementation of the auto complete, typeahead functionality. 

* ES is fast. ES is built on top of Lucene, and hence it excels at full-text search. ES is also a near real-time search platform, meaning the latency from the time a document is indexed until it becomes searchable is very short — typically one second. 

* ES is highly scalable and distributed by nature. The documents stored in ES are distributed across different shards, which are replicated to provide redundant 
  copies of the data in case of hardware failure. The distributed nature of ES allows it to scale out to hundreds (or even thousands) of servers and 
  handle petabytes of data. This is the kind of scale typeahead suggestions need.

I am using ES as a primary database here. This is chosen after careful consideration. 

Had this been a transactional database, I would not have chosen ES as primary persistence. However in the case of typeahead all the writes to ES are front-ended by Kafka cluster thereby writes can be buffered during the cases when ES is doing cluster updates or re-indexing or leader election. Thus insuring 
that there is no data loss. 

Also ES snapshots are taken on Amazon S3 on regular basis so this also adds final guard against data loss.

## Typeahead deployment diagram
<img src="https://github.com/SaurabhKMistry/typeahead-app/blob/master/images/Typeahead-Deployment-Diagram.jpeg" height="740px" width="580px">


## Typeahead Aggregator

We want to keep write flows async mainly because typeahead system can afford to have new accepted typeahead suggestions to be available after a certain delay of few minutes. Also we want to avoid heavy write load when ES is serving heavy read requests.

Write flow is used in below two cases,

* When user enters a new search in the search field, it is a new phrase that we want to accept so that it could be later used for further typeahead suggestions and
* Aggregator component talks to third party trends, news-feed and other similar services to read latest trends and feed it back to typeahead system so that they could be used for providing suggestions

In both of these cases, when <code>/collect-phrases</code> end point is hit, it just puts the data (phrases) in the form of messages into a Kafka topic and a Kafka-ES connector which would write that data into an index in ES.  


## Alternate Design Option

There is another option that I was considering for the typeahead system design. In real time design of system like typeahead, I would consider doing a quick POC with both the options and then choose one over the another based on data points. Nevertheless, I spent some time looking at this other option and I have coded one component of it as well which you can see already in the Github. I didn't get a chance to fully complete end to end flow with it with one million records.  

Here's a trie that stores *Pot*, *Past*, *Pass* and *Part*

<img src="https://github.com/SaurabhKMistry/typeahead-app/blob/master/images/Trie.svg">

With *Trie* whenever there is a request for auto-complete based on a given prefix, we could traverse the tree character by character until we reach the last character of the prefix and then from there reach down to all the leaf nodes to arrive at possible suggestions. We could sort these suggestions based on their scores before sending it back to the UI. Please note that every leaf node in the Trie (leaf node represent a suggestion) contains a numeric score.

This option is very powerful and efficient in a single server environment. When Trie needs to be managed across multiple servers in a distributed manner then things get complex. To handle distrubuted setup, trie could be stored in a LinkedList powered HashMap. Something similar to LinkedHashMap in Java. In this linked hash map, prefix is a mapped as a key and value is the linked list of string of all possible completion for the given prefix key.

This obviously requires more space than usual Trie in memory but it is very fast and could be mapped to Redis key value pair and hence could be made distributed. 
