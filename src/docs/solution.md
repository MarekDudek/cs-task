Requirements
============

1. No analysis is performed on **any** of conditions:
  1. Date is different that current
  2. User is whitelisted
  
  *Other conditions may be specified at a later time.*
  
  It is assumed that no further processing of such a transaction takes place, neither individual one (R 2) nor as part of larger group (R 3). 
  
2. Transaction may be evaluated as suspicious by its own merits, on **any** of conditions:
  1. User is blacklisted
  
  *Further conditions may be added.* 

3. Transaction may also be evaluated as suspicious based on criteria that span the whole streak:
  1. Number of transactions **from** the same account exceeds configured threshold
  2. Number of transactions **to** the same account **by** the same user exceeds configured threshold
  3. Number of transactions by the same user **and** their sum total **both** exceed **any** of configured thresholds
    
  *Conditions may be changed*
  
  Every condition pertains to particular day which follows from skipping analysis of transaction on days other than current (R 1.1).
  
  
Solutions
=========

1. Simple solution
  
  Simplest solution is implemented in class `test.analyser.SimpleFraudAnalyser`. It is mostly straightforward procedural code. It supports changes to requirements well with `solution.collectors.StatsCollector` interface that abstracts over peculiarities of various suspicion raising collective conditions.
  
2. Solution with Java 8 *lambda* expressions

  It is implemented in class `test.analyser.LambdaAnalyser`. All requirements are implemented using *Streams API* and *lambda* expressions making it far less verbose and significantly shorter.
  
3. Concurrent solution with *CompletableFuture*s

  It is implemented in class `test.analyser.ConcurrentAnalyser` and is based on the previous one. It uses the same *lambda* expressions on top of non-blocking concurrent processing using `java.util.concurrent.CompletableFuture<T>` class from Java 8. It also uses parallel streams and concurrent grouping with *Streams API*.
  
  As in previous solution architecture is slightly compromised for the sake of performance. It more than makes up for it with brevity.
  

Another solution was tried, one respecting iterating nature of data source. It was discontinued as making more problems than it solved. It was supposed to immediately returned *lazy* iterator of suspicious transactions. It proved to be very difficult to implement due to its statefull nature.

Benchmarks
==========

1. All solutions were benchmarked. 
  
  Results are available in file `src/test/resources/benchmarks/graphs/analysers-history.html`. 
  
  It shows that *lambda* analyser is almost two times faster than simple solution. Despite performing the same algorithm.
  
  Concurrent version performs almost four times better then *lambda* version on eight processor machine. Far from maximum speed-up allowed by Amdahl's law but still impressive considering how little effort it took to implement.
  
2. All phases of concurrent solution were also benchmarked to find out where it could be improved.
  
  1. Collecting data from random generator.
   	
   	Results available in file `src/test/resources/benchmarks/graphs/collecting-from-generator-history.html`.
   	
   	It took more than 99% of the processing for all analysers when iterator passed was *lazy*. That is why for analyser comparison benchmark above *eager* iterators were provided.
   	
  2. Filtering out skipped transactions.
   	
   	Results available in file `src/test/resources/benchmarks/graphs/filtering-out-skipped-history.html`.
   	
   	It took about 90% of all processing time.
   	
  3. Suspicion raising with single individual analysis and three collective analyses.
   	
   	Results available in file `src/test/resources/benchmarks/graphs/analysis-phases-history.html`.
   	
   	This phase took less than 10% of time.
   	
   	Unsurprisingly individual analysis took the least amount of time and *Count from account* (R 3.1.) was the fastest from collective analyses as requiring only single grouping. 
   	
   	As to *Count by user to account* (R 3.3.) and *Count and total sum by user* (R 3.2) the former was significantly slower than the latter. Two groupings required by *Count by user to account* took more time than single grouping with two reductions in *Count and total sum by user*.
   	
   	Non-blocking versions usually take more time than counterparts using only inherent parallelism of *Streams API* but they are necessary for concurrent execution.
   	
  4. Collecting distinct elements.
   	
   	Results available in file `src/test/resources/benchmarks/graphs/collecting-analysis-results-history.html`.
   	
   	This phase takes below 1% of all time.