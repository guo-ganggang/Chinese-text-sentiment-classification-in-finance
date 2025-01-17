#!/usr/bin/env python
# -*- coding: utf-8 -*-

import numpy as np
import re
import itertools
from collections import Counter
import os
import word2vec_helpers
import time
import pickle

def load_data_and_labels(input_text_file, input_label_file):
    x_text = read_prediction_dataset_file(input_text_file)
    y = None if not os.path.exists(input_label_file) else map(int, list(open(input_label_file, "r").readlines()))
    return (x_text, y)

def load_positive_negative_data_files(positive_data_file, negative_data_file,neutral_data_file): #
    """
    Loads MR polarity data from files, splits the data into words and generates labels.
    Returns split sentences and labels.
    """
    # Load data from files
    positive_examples = read_and_clean_zh_file(positive_data_file)
    neutral_examples = read_and_clean_zh_file(neutral_data_file)
    negative_examples = read_and_clean_zh_file(negative_data_file)
    # Combine data
    x_text = positive_examples + neutral_examples + negative_examples
    # Generate labels
    positive_labels = [[1,0,0] for _ in positive_examples]
    neutral_labels = [[0,1,0] for _ in neutral_examples]
    negative_labels = [[0,0,1] for _ in negative_examples]
    y = np.concatenate([positive_labels,neutral_labels,negative_labels],axis=0)
    return [x_text, y]

def padding_sentences(input_sentences, padding_token, padding_sentence_length = None): #
    sentences = [sentence.split(' ') for sentence in input_sentences]
    max_sentence_length = padding_sentence_length if padding_sentence_length is not None else max([len(sentence) for sentence in sentences])
    print 'max_sentence_length: ' + str(max_sentence_length)
    slice_sentences = []
    for sentence in sentences:
        if len(sentence) > max_sentence_length:
            sentence = sentence[:max_sentence_length]
            slice_sentences.append(sentence)
        else:
            sentence.extend([padding_token] * (max_sentence_length - len(sentence)))
            slice_sentences.append(sentence)
    return (slice_sentences, max_sentence_length)

def batch_iter(data, batch_size, num_epochs, shuffle=True):
    '''
    Generate a batch iterator for a dataset
    '''
    data = np.array(data)
    data_size = len(data)
    num_batches_per_epoch = int((data_size - 1) / batch_size) + 1  # 每一个epoch有多少个batch_size
    for epoch in range(num_epochs):
        if shuffle:
            # Shuffle the data at each epoch
            shuffle_indices = np.random.permutation(np.arange(data_size))
            shuffled_data = data[shuffle_indices]
        else:
            shuffled_data = data
        for batch_num in range(num_batches_per_epoch):
            start_idx = batch_num * batch_size # 当前batch的索引开始
            end_idx = min((batch_num + 1) * batch_size, data_size) # 判断下一个batch是不是超过最后一个数据了
            yield shuffled_data[start_idx : end_idx]

def test():
    # Test clean_str
    print("Test")
    #print(clean_str("This's a huge dog! Who're going to the top."))
    # Test load_positive_negative_data_files
    x_text,y = load_positive_negative_data_files("pos_labeled_data_set.csv", "neu_labeled_data_set.csv", "neg_labeled_data_set.csv")
    # print(x_text)
    # print(y)
    # Test batch_iter
    #batches = batch_iter(x_text, 2, 4)
    #for batch in batches:
    #    print(batch)

def mkdir_if_not_exist(dirpath):
    if not os.path.exists(dirpath):
        os.mkdir(dirpath)

def seperate_line(line):
    return ''.join([word + ' ' for word in line])

def split_line(line):
    postid_text = line.strip().split('\t')
    if len(postid_text) < 2:
        return line.strip()
    else:
        return postid_text[1]

def read_and_clean_zh_file(input_file, output_cleaned_file = None): # 'total_train_data_set.csv'
    lines = list(open(input_file, "r").readlines())
    # lines = [clean_str(seperate_line(line.decode('utf-8'))) for line in lines]
    lines = [clean_str(line.decode('utf-8')) for line in lines]
    if output_cleaned_file is not None:
        with open(output_cleaned_file, 'w') as f:
            for line in lines:
                f.write((line + '\n').encode('utf-8'))
    return lines

def read_prediction_dataset_file(input_file, output_cleaned_file = None): # 'total_train_data_set.csv'
    lines = list(open(input_file, "r").readlines())
    lines = [clean_str(split_line(line.decode('utf-8'))) for line in lines]
    if output_cleaned_file is not None:
        with open(output_cleaned_file, 'w') as f:
            for line in lines:
                f.write((line + '\n').encode('utf-8'))
    return lines


def clean_str(string):
    """
    Tokenization/string cleaning for all datasets except for SST.
    Original taken from https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
    """
    string = string
    # string = re.sub(ur"[^\u4e00-\u9fff]", " ", string)
    #string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
    #string = re.sub(r"\'s", " \'s", string)
    #string = re.sub(r"\'ve", " \'ve", string)
    #string = re.sub(r"n\'t", " n\'t", string)
    #string = re.sub(r"\'re", " \'re", string)
    #string = re.sub(r"\'d", " \'d", string)
    #string = re.sub(r"\'ll", " \'ll", string)
    #string = re.sub(r",", " , ", string)
    #string = re.sub(r"!", " ! ", string)
    #string = re.sub(r"\(", " \( ", string)
    #string = re.sub(r"\)", " \) ", string)
    #string = re.sub(r"\?", " \? ", string)
    # string = re.sub(r"\s{2,}", " ", string)
    #return string.strip().lower()
    return string.strip()

def saveDict(input_dict, output_file):
    with open(output_file, 'wb') as f:
        pickle.dump(input_dict, f) 

def loadDict(dict_file):
    output_dict = None
    with open(dict_file, 'rb') as f:
        output_dict = pickle.load(f)
    return output_dict

if __name__ == "__main__":
    test()
