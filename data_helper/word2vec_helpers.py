#!/usr/bin/env python
# -*- coding: utf-8 -*-

import multiprocessing
import time
import gensim
from gensim.models import Word2Vec
from gensim.models.word2vec import LineSentence
from gensim.models.word2vec import Word2Vec
from gensim.corpora.dictionary import Dictionary
import pickle
from keras.preprocessing import sequence
from sklearn.cross_validation import train_test_split
import pandas as pd

import numpy as np
# np.random.seed(1337)  # For Reproducibility

# 训练模型
def generate_word2vec_files(input_file, output_model_file, output_vector_file, size, window , min_count):
    start_time = time.time()

    # trim unneeded model memory = use(much) less RAM
    # model.init_sims(replace=True)
    model = Word2Vec(LineSentence(input_file), size = size, window = window, min_count = min_count, workers = multiprocessing.cpu_count())
    model.save(output_model_file)
    model.wv.save_word2vec_format(output_vector_file, binary=False)

    end_time = time.time()
    print("used time : %d s" % (end_time - start_time))

# 加载训练好的模型，计算相关词汇与相似度
def load_w2v_model(filePath):

    ipath = filePath + "eastmoney_guba_post_w2v_vector\\guba_ugc_w2v_s128_w3_m10.vector"
    # opath = "similarity_vector_v2.csv"
    model = gensim.models.Word2Vec.load_word2vec_format(ipath, binary=False)
    results = model.most_similar(u"诱多", topn=50)
    for e in results:
        print e[0], e[1]
    print u'利好与下跌的相似度：'
    similarityResults_1 = model.similarity(u'看空', u'下跌')
    print similarityResults_1

    print u'利好与上涨的相似度：'
    similarityResults_2 = model.similarity(u'利多', u'上涨')
    print similarityResults_2

# 创建词语字典，并返回word2vec模型中词语的索引，词向量
def create_dictionaries(p_model):
    gensim_dict = Dictionary()
    gensim_dict.doc2bow(p_model.vocab.keys(), allow_update=True)
    w2index = {v: k + 1 for k, v in gensim_dict.items()}  # 词语的索引，从1开始编号
    w2vec = {word: p_model[word] for word in w2index.keys()}  # 词语的词向量
    return w2index, w2vec

# 加载训练好的模型，功能：利用大语料生成词语的索引字典、词向量，然后保存为pkl文件
def index_word_vector(filePath):
    ipath = filePath + "eastmoney_guba_post_w2v_vector\\guba_ugc_w2v_s128_w3_m10.vector"
    model = gensim.models.Word2Vec.load_word2vec_format(ipath, binary=False)
    # 索引字典、词向量字典
    index_dict, word_vectors = create_dictionaries(model)
    # 存储为pkl文件
    pkl_name = filePath + 'guba_ugc_w2v_s128_w3_m10.pkl'
    output = open(pkl_name, 'wb')
    pickle.dump(index_dict, output)  # 索引字典
    pickle.dump(word_vectors, output)  # 词向量字典
    output.close()

# 训练集向量化表示
def text_to_index_array(p_new_dic, p_sen):  # 文本转为索引数字模式
    new_sentences = []
    for sen in p_sen:
        new_sen = []
        for word in sen:
            try:
                new_sen.append(p_new_dic[word])  # 单词转索引数字
            except:
                new_sen.append(0)  # 索引字典里没有的词转为数字0
        new_sentences.append(new_sen)

    return np.array(new_sentences)

# 读入训练集数据，并返回特征与标签列表,读取语料分词文本，转为句子列表（句子为词汇的列表）
def read_data_by_pd(input_file):
    selected = ['label','text']
    df = pd.read_csv(input_file,names=selected,sep = '\t')
    # non_selected = list(set(df.columns) - set(selected))
    # df = df.drop(non_selected, axis=1)
    # df = df.dropna(axis=0, how='any', subset=selected)
    # df = df.reindex(np.random.permutation(df.index))

    maxlen = 0
    for row_num in range(len(df['text'])):
        temp = df['text'][row_num]
        if len(temp) > maxlen:
            maxlen = len(temp)
    print u'训练集中最大行词的个数是： ' + str(maxlen)

    allsentences = list(df['text'].values)
    labels = list(df['label'].values)

    return allsentences,labels,maxlen

def embedding_sentences(train_data_set_path,w2v_model_path,vocab_dim):
    # 读取大语料文本
    f = open(w2v_model_path, 'rb')  # 预先训练好的
    index_dict = pickle.load(f)  # 索引字典，{单词: 索引数字}
    word_vectors = pickle.load(f)  # 词向量, {单词: 词向量(128维长的数组)}
    new_dic = index_dict
    print u"Setting up Arrays for Keras Embedding Layer..."
    n_symbols = len(index_dict) + 1  # 索引数字的个数，因为有的词语索引为0，所以+1
    embedding_weights = np.zeros((n_symbols,vocab_dim))  # 创建一个n_symbols * 128的0矩阵
    for w, index in index_dict.items():  # 从索引为1的词语开始，用词向量填充矩阵
        embedding_weights[index, :] = word_vectors[w]  # 词向量矩阵，第一行是0向量（没有索引为0的词语，未被填充）

    allsentences,labels,maxlen = read_data_by_pd(train_data_set_path)

    # 划分训练集和测试集，此时都是list列表
    X_train_l, X_test_l, y_train_l, y_test_l = train_test_split(allsentences, labels, test_size=0.2)

    # 转为数字索引形式
    X_train = text_to_index_array(new_dic, X_train_l)
    X_test = text_to_index_array(new_dic, X_test_l)
    print u"训练集shape： ", X_train.shape
    print u"测试集shape： ", X_test.shape

    y_train = np.array(y_train_l)  # 转numpy数组
    y_test = np.array(y_test_l)

    # 将句子截取相同的长度maxlen，不够的补0
    print('Pad sequences (samples x time)')
    X_train = sequence.pad_sequences(X_train, maxlen=maxlen)
    X_test = sequence.pad_sequences(X_test, maxlen=maxlen)
    print('X_train shape:', X_train.shape)
    print('X_test shape:', X_test.shape)

    return n_symbols, embedding_weights, X_train, y_train, X_test, y_test,maxlen


# Linux 环境下调用程序
def run_main():
    input_file = 'eastmoney_guba_post_seg_w2v.csv'
    output_model_file = 'guba_ugc_w2v_s128_w3_m10.bin'
    output_vector_file  = 'guba_ugc_w2v_s128_w3_m10.vector'
    # vectors = embedding_sentences([['first', 'sentence'], ['second', 'sentence']], embedding_size=4, min_count=1)
    generate_word2vec_files(input_file, output_model_file, output_vector_file, size=128, window=3, min_count=10)

if __name__ == '__main__':
    pass
    # run_main()
    # filePath = 'D:\\data\\paper_data\\raw_data_from_DB\\'
    # input_file =filePath + 'eastmoney_guba_post_seg_w2v.csv'
    # output_model_file =filePath +  'guba_ugc_w2v_s128_w3_m10.bin'
    # output_vector_file  =filePath +  'guba_ugc_w2v_s128_w3_m10.vector'
    # generate_word2vec_files(input_file, output_model_file, output_vector_file, size=128, window=3, min_count=10)
    # load_w2v_model(filePath)
    # index_word_vector(filePath)



