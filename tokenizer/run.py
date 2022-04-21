from tokenizer import tokenize
import sys
import os
from pprint import pprint
from math import log10
import numpy as np

def idf(term, D):
    numerator = float(len(D))
    denominator = float(1 + len([True for d in D if term in D[d]]))
    return log10(numerator/denominator)

def cosineSimilarity(vec1, vec2):
    size1 = 1

dataset_basic_dir = 'dataset/'
stmt_file = ''
doc_list = []
dictionary = []
doc_dict = {}
doc_vec = {}
with open(dataset_basic_dir + 'stmt0.txt', 'r') as f:
    stmt_file = f.read()

for target_file in os.listdir(dataset_basic_dir):
    with open(dataset_basic_dir + target_file, 'r') as f :
        doc_list.append(f.read())

for doc in doc_list:
    doc_dict[doc] = {}
    for token in tokenize(doc):
        token = token.value.encode('ascii','ignore')
        dictionary.append(token)
        if token not in doc_dict[doc]:
            doc_dict[doc][token] = 1
        else:
            doc_dict[doc][token] += 1

dictionary = set(dictionary)
# print(doc_dict)

number_of_doc = len(doc)

for doc in doc_dict:
    doc_vec[doc] = {}
    for term in dictionary:
        if term in doc_dict[doc]:
            doc_vec[doc][term] = idf(term, doc_dict)
        else:
            doc_vec[doc][term] = 0
# print(doc_vec)
stmt_vec = doc_vec[stmt_file]
print(stmt_vec)