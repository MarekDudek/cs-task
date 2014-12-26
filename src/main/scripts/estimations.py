#!/usr/bin/env python

from __future__ import division

# numbers
thousand = 1000
million = thousand * thousand

# basic settings
transactions = million
users = 100
whitelisted = 10
blacklisted = 10
accounts = 100
days = 3
minAmount = 100
maxAmount = 999


# estimations
estimation_based_on_blacklisted = transactions/blacklisted/days
print 'suspicious based on blaclisted:', estimation_based_on_blacklisted

transactions_per_account_per_day = transactions/users/days
print 'transactions per account per day', transactions_per_account_per_day
transactions_per_account_analysed = transactions_per_account_per_day * (1 - whitelisted / users)
print 'transactions per account analysed', transactions_per_account_analysed
# with this number allowed roughly half of transactions would be suspicious
# only single account has more than two times this number, 6000 transactions

transactions_per_user_per_account_per_day = transactions/users/accounts/days
print 'transactions per user per account per day', transactions_per_user_per_account_per_day
# with this number allowed roughly half of transactions would be suspicious
# only single combination of user and account has more than 140 transactions

avgAmount = (minAmount+maxAmount)//2
print 'average amount', avgAmount
avgSumTotal = avgAmount * transactions_per_account_per_day
print 'average sum total', avgSumTotal
# with this numbers allowed roughly half of transactions would be suspicious
# only 6700 transactions for 4000 per user and 2000000 sum total
