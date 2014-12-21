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
  1. Number of transactions **to** the same account exceeds configured threshold
  2. Number of transactions **from** the same account exceeds configured threshold
  3. Number of transactions by the same user **and** their sum total **both** exceed **any** of configured thresholds
    
  *Conditions may be changed*
  
  Every condition pertains to particular day which follows from skipping analysis of transaction on days other than current (R 1.1).
  