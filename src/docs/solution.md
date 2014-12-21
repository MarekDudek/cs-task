Requirements
============

1. No analysis is performed on any of conditions:
  1. When date is different that current
  2. When user is whitelisted
  
  Other conditions may be specified at a later time.
  
2. Transaction may be evaluated as suspicious by it's own merits, on any of conditions:
  1. User is blacklisted
  
  Other conditions may be specified at a later time.

3. Transaction may also be evaluated as suspicious based on criteria that span the whole streak
  1. Number of transactions **to** the same account exceeds configured threshold
  2. Number of transactions **from** the same account exceeds configured threshold
  3. Number of transactions by the same user **and** their sum total exceeds configured thresholds