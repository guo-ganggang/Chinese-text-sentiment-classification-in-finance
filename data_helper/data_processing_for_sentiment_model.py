#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 28/10/2017 下午 9:50
# @Author  : GUO Ganggang
# @email   : ganggangguo@csu.edu.cn
# @Site    : 
# @File    : data_processing_for_sentiment_model.py
# @Software: PyCharm

import codecs
import os
import re

import sys
reload(sys)
sys.setdefaultencoding('utf-8')

# 准备训练股吧文本Word2vec模型的预料，包括两次采集的，去掉重复的，并将预料集合保存文件，作为分词输入
# 第一批采集全部post四千6百万条，以及其中的三千五百万条post的comments
def bbs_corpus_summary(filePath):

    missing_url = set()

    infilePath1 = filePath + 'eastmoney_guba_post_urls_missing.csv'
    infilePath2 = filePath + 'eastmoney_guba_post_db.csv'
    infilePath3 = filePath + 'eastmoney_guba_post_urls_completed_parsed.csv'
    outfilePath = filePath + 'eastmoney_guba_ugc.csv'

    with codecs.open(infilePath1, "rb", "utf-8") as input_file1:
        for line in input_file1:
            temp = line.strip().split(',')
            if len(temp) < 3:
                continue
            # text = '_'.join(temp[1:]) + '.csv'
            missing_url.add(line.strip())
    print len(missing_url)

    with open(outfilePath, 'a') as output_file1:
        with codecs.open(infilePath2, "rb", "utf-8") as input_file:
            for line in input_file:
                temp = line.strip().split('\",\"')
                if len(temp) < 12:
                    continue
                if temp[10] == '':
                    continue
                if temp[10] in missing_url:
                    output_file1.write(temp[1].strip() + '\n')

    with open(outfilePath, 'a') as output_file:
        with codecs.open(infilePath3, "rb", "utf-8") as input_file:
            for line in input_file:
                temp = line.strip().split('	')
                title = temp[1].strip().split('_')
                output_file.write(title[0].strip() + '\n')
                if temp[4] == '0':
                    continue
                else:
                    conmments = temp[5].strip().split('&&||')
                    for comment in conmments:
                        if (comment != '') or (comment != u'空'):
                            output_file.write(comment.strip() + '\n')

# 将上一步获取的ugc文本与第二批采集的全部post两千一百万条合并，去重后保存文件中
def merge_files_dedup(filePath):
    filePath_1 = 'I:\\eastmoney_guba\\eastmoney_guba_ugc.csv'
    filePath_2 = 'D:\\paper_lab\\eastMoney_guba_ugc_parsed.csv'
    outfilePath = filePath + 'eastmoney_guba_ugc.csv'

    second_collect = set()
    with open(outfilePath, 'w') as output_file:
        with codecs.open(filePath_2, "rb", "utf-8") as input_file2:
            for line in input_file2:
                temp = line.strip().split('\t')
                if len(temp) < 11:
                    continue
                text = ','.join(temp[11:])
                second_collect.add(text.encode("utf-8").strip())
                output_file.write(text.encode("utf-8").strip() + '\n')
    print len(second_collect)

    with open(outfilePath, 'a') as output_file:
        with codecs.open(filePath_1, "rb", "utf-8") as input_file1:
            for line in input_file1:
                if line.encode("utf-8").strip() in second_collect:
                    continue
                else:
                    output_file.write(line.encode("utf-8").strip() + '\n')

# 上一步得到股吧ugc训练预料，通过java程序分词后，在本步骤进行clean，代码已经两次修改，处理不同文件存放类型的数据
def clean_seg_sentiments(filePath,k):
    # infilePath = filePath + 'guba_1000stocks_postid_text_seg.csv'
    # outfilePath = filePath + 'guba_1000stocks_postid_text_seg_clean.csv'


    infilePath = filePath + '318stocks_pid_have_comments_divide_seg.csv'
    outfilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean.csv'

    clean_list = ['www', '▇', '★', 'ゞ', '∷', '▌','◢◤', '▆','￡','▌▌', '▅▆', '▇◤', '▆▆','◥', \
                  "'''", "''", '．', ',', '﹍', '▁', '▂▃','＂', '／', '〜', '•',  '﹑', '´', '╬╬', '＼', \
                  '﹎', '﹏', '▃▃', '▆▇', '◥◣','︻', '.', '%', '﹌', 'ミ', '∟', '☆', '➠', '➕', '•', \
                  '▃', '█','■■','мī','ч','html','csv',']','[', u'空',u'版', u'网友',u'网页',u'博客',u'关注',\
                  '╬', 'з', 'ヽ', '｀', '︱', '┢┦', '※', '�', '▄', '┻═','┳一', '－', '≦', '〆',  \
                   '■■', '▋', '◥◤', '■■','▼', '︼', '▇▇', '┏','░░', '〖','〗', '︼']

    # values = set()
    with codecs.open(outfilePath, "w", "utf-8") as output_file:
        with codecs.open(infilePath, "rb", "utf-8") as inputStockCode:
            for line in inputStockCode:
                postid_text = line.strip().split('\t')
                if len(postid_text) < 2:
                    print 'line.strip(): ' + line.strip()
                    output_file.write(line.strip() + '\t' + 'null' + '\n')
                    continue
                postid = postid_text[0]
                text = postid_text[1]

                temp = text.strip().split(' ')
                if len(temp) == k:
                    print 'text.strip(): ' + text.strip()
                    output_file.write(postid + '\t' + 'null' + '\n')
                    continue
                clean_temp = []
                for i in range(len(temp)):
                    flag = '0'
                    for string in clean_list:
                        if string == temp[i]:
                            flag = '1'
                            break
                    if flag == '1':
                        continue
                    else:
                        clean_temp.append(temp[i])
                lenth = len(clean_temp)
                if lenth == k:
                    vec = 'null'
                else:
                    vec = " ".join(clean_temp[0:])
                output_file.write(postid + '\t' + vec + '\n')


                # if lenth >= k:
                    # if vec in values:
                    #     continue
                    # else:
                    #     output_file.write(postid + '\t' + vec + '\n')
                        # if len(values) <= 100:
                        #     values.add(vec)
                        # else:
                        #     values.clear()

# 划分打过标签的文件，根据标签1、-1和0划分为三类
def divide_small_file(filePath):
    labeled_data_set = {}
    neg_labeled_data_set = []
    neu_labeled_data_set = []
    pos_labeled_data_set = []
    infilePath = filePath + 'eastMoney_bbs_labeled_total_seg_clean.csv'

    with codecs.open(infilePath, "rb", "utf-8") as inputStockCode:
        for line in inputStockCode:
            label_text = line.strip().split('\t')
            if label_text[0] == '-1':
                neg_labeled_data_set.append(label_text[1])
            elif label_text[0] == '0':
                neu_labeled_data_set.append(label_text[1])
            else:
                pos_labeled_data_set.append(label_text[1])
    labeled_data_set['neg_labeled_data_set'] = neg_labeled_data_set
    labeled_data_set['neu_labeled_data_set'] = neu_labeled_data_set
    labeled_data_set['pos_labeled_data_set'] = pos_labeled_data_set

    for key in labeled_data_set.keys():
        print key,str(len(labeled_data_set[key]))
        outfilePath = filePath + key + '.csv'

        with open(outfilePath, 'w') as output_file:
            for line in labeled_data_set[key]:
                output_file.write(line.strip() + '\n')

# 统计出每条标注训练集中词的个数
def statistic_words_num(filePath):
    words_num_count = {}
    infilePath = filePath + 'eastMoney_bbs_labeled_total_seg_clean.csv'
    outFilePath = filePath + 'eastMoney_bbs_labeled_total_seg_clean_words_num_count.csv'
    with codecs.open(infilePath, "rb", "utf-8") as inputStockCode:
        for line in inputStockCode:
            label_text = line.strip().split('\t')
            if len(label_text) < 2:
                continue
            words_num = label_text[1].strip().split(' ')
            words_num_count[str(len(words_num))] = words_num_count.get(str(len(words_num)), 0) + 1

    lda_sort = sorted(words_num_count.iteritems(), key=lambda d: d[1], reverse=True)
    # if len(lda_sort) > 1000:
    #     top_n = 1000
    # else:
    #     top_n = len(lda_sort)

    with open(outFilePath, 'w') as output_file:
        for i in range(len(lda_sort)):
            print lda_sort[i][0], lda_sort[i][1]
            output_file.write(str(lda_sort[i][0]) + ',' + str(lda_sort[i][1]) + '\n')

# 划分需要预测的数据集，以及不需要预测的数据集
def filter_file(filePath):
    # infilePath = filePath + 'guba_1000stocks_postid_text_seg_clean.csv'
    # outfilePath_fir = filePath + 'guba_1000stocks_postid_text_seg_clean_senti_result_1.csv'
    # outfilePath_sec = filePath + 'guba_1000stocks_postid_text_seg_clean_senti.csv'
    #
    # no_need_prediction = set()
    # need_prediction = {}
    # with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #     for line in input_file:
    #         temp = line.strip().split('\t')
    #         if len(temp) < 2:
    #             print line.strip() + '------'
    #             # no_need_prediction.add(line.strip())
    #             continue
    #         if temp[1] == 'null':
    #             no_need_prediction.add(temp[0])
    #         else:
    #             text = ' '.join(temp[1:])
    #             need_prediction[temp[0]] = text
    #
    # print len(no_need_prediction),len(need_prediction)
    #
    # with open(outfilePath_fir, 'w') as output_file:
    #     for postid in no_need_prediction:
    #         output_file.write(str(postid) + '\t' + 'null' + '\t' + '0' + '\n')
    # with open(outfilePath_sec, 'w') as output_file:
    #     for key in need_prediction.keys():
    #         output_file.write(str(key) + '\t' + need_prediction[key] + '\n')


    infilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean.csv'
    outfilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti.csv'
    null_num = 0
    with open(outfilePath, 'w') as output_file:
        with codecs.open(infilePath, "rb", "utf-8") as input_file:
            for line in input_file:
                temp = line.strip().split('\t')
                if len(temp) < 2:
                    null_num += 1
                    continue
                if temp[1].strip() == 'null':
                    null_num += 1
                    continue
                output_file.write(line.strip() + '\n')
    print null_num

# 将一个大文件分成若干个小文件
def divide_big_file(filePath):
    # infilePath = filePath + 'guba_1000stocks_postid_text_seg_clean_senti.csv'
    # postID_text = {}
    # index_num = 0
    # with codecs.open(infilePath, "rb", "utf-8") as inputStockCode:
    #     for line in inputStockCode:
    #         temp = line.strip()
    #         if temp == '':
    #             print line.strip()
    #             continue
    #         postID_text[index_num] = temp
    #         index_num += 1
    # print index_num
    #
    # small_file = []
    # flag = 0
    # for key in postID_text.keys():
    #     small_file.append(postID_text[key])
    #     num = key + 1
    #     if ((num % 1000000) == 0) or (num == len(postID_text)):
    #         print flag
    #         outfilePath = filePath + 'batches\\guba_1000stocks_postid_text_seg_clean_senti_%s' %str(flag) +'.csv'
    #         flag += 1
    #         with open(outfilePath, 'w') as output_file:
    #             for line in small_file:
    #                 output_file.write(line.strip() + '\n')
    #         small_file = []

    infilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti.csv'
    count_index = 0
    batch_num = 0
    contents = []
    with codecs.open(infilePath, "rb", "utf-8") as input:
        for line in input:
            temp = line.strip().split('\t')
            if len(temp) < 2:
                continue
            if len(temp[1].strip().split(' ')) >63:
                content = ' '.join(temp[1].strip().split(' ')[0:63])
                contents.append(temp[0] + '\t' + content.strip())
            else:
                contents.append(line.strip())
            count_index += 1
            if count_index == 8000000:
                outfilePath = filePath + 'comments_batches_2\\318stocks_pid_have_comments_divide_seg_clean_senti_%s' % str(batch_num) + '.csv'
                with open(outfilePath, 'w') as output_file:
                    for line in contents:
                        output_file.write(line.strip() + '\n')
                print batch_num
                batch_num += 1
                contents = []
                count_index = 0

    if len(contents) != 0:
        print len(contents)
        outfilePath = filePath + 'comments_batches_2\\318stocks_pid_have_comments_divide_seg_clean_senti_%s' % str(
            batch_num) + '.csv'
        with open(outfilePath, 'w') as output_file:
            for line in contents:
                output_file.write(line.strip() + '\n')

# 预测后的结果集是文本+预测值的存放格式，结合postID+文本的存放格式，调整为postID+预测值的格式
def match_files(filePath):
    # # 读入按文本 与 预测结果方式存放的结果集
    # folder_path = filePath + 'result_data_set\\'
    # outfilePath = filePath + 'guba_1000stocks_text_prediction_senti.csv'
    # with open(outfilePath, 'w') as output_file:
    #     for file_name in os.listdir(folder_path):
    #         print file_name
    #         infilePath = folder_path + file_name
    #         with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #             for line in input_file:
    #                 temp = line.strip().split(',')
    #                 if len(temp) < 2:
    #                     print line.strip()
    #                     continue
    #                 if temp[1] == '0.0':
    #                     output_file.write(temp[0] + ',' + '1' + '\n')
    #                 elif temp[1] == '1.0':
    #                     output_file.write(temp[0] + ',' + '0' + '\n')
    #                 else:
    #                     output_file.write(temp[0] + ',' + '-1' + '\n')
    #
    #
    # # postid 与 文本的字典类型
    # infilePath_fir = filePath + 'guba_1000stocks_postid_text_seg_clean_senti.csv'
    # postID_text = {}
    # with codecs.open(infilePath_fir, "rb", "utf-8") as inputStockCode:
    #     for line in inputStockCode:
    #         temp = line.strip().split('\t')
    #         if len(temp) < 2:
    #             print line.strip()
    #             continue
    #         postID_text[temp[0]] = temp[1]
    # print len(postID_text)
    #
    # # 预测值 与 文本的字典类型
    # infilePath_sec = filePath + 'guba_1000stocks_text_prediction_senti.csv'
    # text_prediction_value = {}
    # with codecs.open(infilePath_sec, "rb", "utf-8") as inputStockCode:
    #     for line in inputStockCode:
    #         temp = line.strip().split(',')
    #         if len(temp) < 2:
    #             print line.strip()
    #             continue
    #         text_prediction_value[temp[0]] = temp[1]
    # print len(text_prediction_value)
    #
    #
    # # 将结果集写入文件中
    # outfilePath =  filePath + 'guba_1000stocks_postid_text_seg_clean_senti_result_2.csv'
    # with open(outfilePath, 'w') as output_file:
    #     for key in postID_text.keys():
    #         text = postID_text[key]
    #         sentiment_value = text_prediction_value[text]
    #         output_file.write(str(key) + ',' + text + ',' + sentiment_value + '\n')
    # # 读入按文本 与 预测结果方式存放的结果集
    # postID_prediction_values = {}
    # folder_path = filePath + 'result_data_set\\'
    # for file_name in os.listdir(folder_path):
    #     infilePath = folder_path + file_name
    #     with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #         for line in input_file:
    #             temp = line.strip().split(',')
    #             if len(temp) < 2:
    #                 print line.strip()
    #                 continue
    #             key = postID_text.keys()[postID_text.values().index(temp[0])]
    #             postID_prediction_values[key] = line.strip()
    #             del postID_text[key]
    # print len(postID_prediction_values)

    # # 将分词后内容为null的post及其预测结果追加到另一个文件尾
    # infilePath = filePath + 'guba_1000stocks_postid_text_seg_clean_senti_result_1.csv'
    # outfilePath = filePath + 'guba_1000stocks_postid_text_seg_clean_senti_result_2.csv'
    #
    # postID_content = {}
    # with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #     for line in input_file:
    #         temp = line.strip().split('\t')
    #         if len(temp) < 3:
    #             print line.strip()
    #             continue
    #         content = ','.join(temp[1:])
    #         postID_content[temp[0]] = content
    # print len(postID_content)
    #
    # with open(outfilePath, 'a') as output_file:
    #     for key in postID_content.keys():
    #         output_file.write(str(key) + ',' + postID_content[key] + '\n')

    # 读入每支股票相关post的所有评论预测的结果，并按postID进行统计，将持积极态度与消极态度的

    # # 合并预测结果，并变换预测标记值
    # folder_path = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_result\\'
    # outfilePath = filePath + '318stocks_pid_have_comments_predict_senti_result_summary.csv'
    # with open(outfilePath, 'w') as output_file:
    #     for file_name in os.listdir(folder_path):
    #         print file_name
    #         infilePath = folder_path + file_name
    #         with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #             for line in input_file:
    #                 temp = line.strip().split(',')
    #                 if len(temp) < 2:
    #                     print line.strip()
    #                     continue
    #                 if temp[1] == '0.0':
    #                     output_file.write(temp[0] + ',' + '1' + '\n')
    #                 elif temp[1] == '1.0':
    #                     output_file.write(temp[0] + ',' + '0' + '\n')
    #                 else:
    #                     output_file.write(temp[0] + ',' + '-1' + '\n')

    # # 由于在预测前，将文本进行了63个词的截取，因此，匹配时需要截取后的文件结合
    # folder_path = filePath + 'comments_batches\\'
    # outfilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_cut63.csv'
    # with open(outfilePath, 'w') as output_file:
    #     for file_name in os.listdir(folder_path):
    #         print file_name
    #         infilePath = folder_path + file_name
    #         with codecs.open(infilePath, "rb", "utf-8") as input_file:
    #             for line in input_file:
    #                 temp = line.strip()
    #                 if temp == '':
    #                     print '---------'
    #                     continue
    #                 output_file.write(temp + '\n')

    # # 预测值 与 文本的字典类型
    # infilePath_sec = filePath + '318stocks_pid_have_comments_predict_senti_result.csv'
    # text_prediction_value = {}
    # with codecs.open(infilePath_sec, "rb", "utf-8") as inputStockCode:
    #     for line in inputStockCode:
    #         temp = line.strip().split(',')
    #         if len(temp) < 2:
    #             print line.strip()
    #             continue
    #         text_prediction_value[temp[0]] = temp[1]
    # print len(text_prediction_value)
    #
    # # postid 与 文本的字典类型
    # infilePath_fir = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_cut63.csv'
    # outfilePath = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_result.csv'
    # with open(outfilePath, 'w') as output_file:
    #     with codecs.open(infilePath_fir, "rb", "utf-8") as inputStockCode:
    #         for line in inputStockCode:
    #             temp = line.strip().split('\t')
    #             if len(temp) < 2:
    #                 print line.strip()
    #                 continue
    #             sentiment_value = text_prediction_value[temp[1]]
    #             output_file.write(line.strip() + '\t' + sentiment_value + '\n')
    # del text_prediction_value

    # 统计每条postID包含的积极消极态度条数
    infilePath_thir = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_result.csv'
    postIDs_stati = {}
    postids = set()
    with codecs.open(infilePath_thir, "rb", "utf-8") as inputStockCode:
        for line in inputStockCode:
            temp = line.strip().split('\t')
            if len(temp) < 3:
                print line.strip()
                continue
            if temp[2].strip() == '0':
                continue
            if temp[0].strip() not in postids:
                postIDs_stati[temp[0]] = [0,0]
                if temp[2].strip() == '1':
                    postIDs_stati[temp[0]][0] += 1
                else:
                    postIDs_stati[temp[0]][1] += 1
            else:
                if temp[2].strip() == '1':
                    postIDs_stati[temp[0]][0] += 1
                else:
                    postIDs_stati[temp[0]][1] += 1
            postids.add(temp[0].strip())
    print len(postIDs_stati),len(postids)

    outfilePath_sta = filePath + '318stocks_pid_have_comments_divide_seg_clean_senti_result_stati.csv'
    with open(outfilePath_sta, 'w') as outputFile:
        for key in postIDs_stati.keys():
            pos_neg = ','.join(str(x)for x in postIDs_stati[key])
            outputFile.write(key.strip() + ',' + pos_neg + '\n')




if __name__ == '__main__':

    # filePath = 'D:\\paper_lab\\merge_guba_ugc_text_corpus\\'
    # merge_files_dedup(filePath)


    filePath = 'G:\\eastmoney_guba_1000stocks_ugc\\'
    # k = 0
    # clean_seg_sentiments(filePath,k)

    # divide_small_file(filePath)

    # filePath = 'D:\\paper_lab\\labeled_summary_modify_model\\'
    # statistic_words_num(filePath)

    # filePath = 'D:\\paper_lab\\guba_1000stocks_ugc_sentiment_prediction\\'
    # filter_file(filePath)

    # divide_big_file(filePath)

    match_files(filePath)