There is a ready subsystem that stores all financial transactions done in some company.

The new subsystem is being built to analyse those transactions. The goal of the system is to discover suspicious transactions.

Below are the rules that describe suspicious transactions. The transaction is treated as suspicious if at least one of the rules is met:

	1. If transaction is done by one of the users with ID: '542', '1052', '2103' 
	2. If there are more than 3 transactions done by the same user on particular day on sum greater than 5.000 EUR
	3. If there are more than 2 transactions done by the same user on particular day on sum greater than 10.000 EUR
	4. If there are more than 4 transactions done to the same account by one user on particular day.
	5. If there are more than 5 transactions done from the same account on particular day.

There is also another rule to ignore transactions from users with ID: '101', '606'. Those transaction should not be analysed at all.

Your task is to implement method 'analyse' in class 'FraudAnalyser'. The method takes two parameters: all transaction and a date. 
The method should analyse all transactions with given day and return a list of suspicious transactions. Transactions that are not suspicious shouldn't be returned.
	
NOTES:
	- Take into consideration that above rules can be changed in the future therefore propose a good design. Good design should allow to extend system as requirements (rules) change.
	- Design is the most important criteria of evaluation of the solution. So full implementation is not required, a good draft would be enough.
	- Transactions are not required to be persisted to database or anything like this.
	- the average number of transaction per day is 10 000.
	- Good suggestions of API improvement are welcome and will be scored extra.
