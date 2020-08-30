#!/usr/bin/env python
# -*- coding: utf-8 -*-

import tensorflow as tf
import numpy as np
import os
import time
import datetime
import data_helpers
import word2vec_helpers
from text_cnn import TextCNN
import csv

def load_session():

    # Eval Parameters
    tf.flags.DEFINE_integer("batch_size", 64, "Batch Size (default: 64)")
    tf.flags.DEFINE_string("checkpoint_dir", "./runs/1510024202/checkpoints", "Checkpoint directory from training run")
    tf.flags.DEFINE_boolean("eval_train", True, "Evaluate on all training data")  # eval_train=开关是评价还是训练

    # Misc Parameters
    tf.flags.DEFINE_boolean("allow_soft_placement", True, "Allow device soft device placement")
    tf.flags.DEFINE_boolean("log_device_placement", False, "Log placement of ops on devices")

    FLAGS = tf.flags.FLAGS
    FLAGS._parse_flags()
    print("\nParameters:")
    for attr, value in sorted(FLAGS.__flags.items()):
        print("{}={}".format(attr.upper(), value))
    print("")

    # Evaluation
    # ==================================================
    print("\nEvaluating...\n")
    # ==================================================
    # 找到最新的时间点,应用模型
    checkpoint_file = tf.train.latest_checkpoint(FLAGS.checkpoint_dir)
    # 构造图,就是运算结构,构造好了,填入数据,连续迭代
    graph = tf.Graph()

    return FLAGS,checkpoint_file,graph

def data_raw_load(file_num):
    # Parameters
    # ==================================================

    # Data Parameters
    tf.flags.DEFINE_string("input_text_file",
                           "./data/batches/318stocks_pid_have_comments_divide_seg_clean_senti_%s" %file_num +".csv",
                           "Test text data source to evaluate.")
    tf.flags.DEFINE_string("input_label_file", "", "Label file for test text data source.")

    FLAGS = tf.flags.FLAGS
    FLAGS._parse_flags()
    print("\nParameters:")
    for attr, value in sorted(FLAGS.__flags.items()):
        print("{}={}".format(attr.upper(), value))
    print("")

    # validate
    # ==================================================

    # validate checkout point file
    # checkpoint_file = tf.train.latest_checkpoint(FLAGS.checkpoint_dir)
    # if checkpoint_file is None:
    #     print("Cannot find a valid checkpoint file!")
    #     exit(0)
    # print("Using checkpoint file : {}".format(checkpoint_file))

    # Load data
    if FLAGS.eval_train:
        x_raw, y_test = data_helpers.load_data_and_labels(FLAGS.input_text_file, FLAGS.input_label_file)
    else:
        x_raw = ["a masterpiece four years in the making", "everything is off."]
        # y_test = [1, 0]
    return x_raw

def text_embedding_batchs(x_raw,w2vModel):
    # validate word2vec model file
    # trained_word2vec_model_file = os.path.join(FLAGS.checkpoint_dir, "..", "eastmoney_guba_ugc_seg_clean_w2v_s128_w3_m10.bin")
    # trained_word2vec_model_file = "eastmoney_guba_ugc_seg_clean_w2v_s128_w3_m10.bin"
    # if not os.path.exists(trained_word2vec_model_file):
    #     print("Word2vec model file \'{}\' doesn't exist!".format(trained_word2vec_model_file))
    # print("Using word2vec model file : {}".format(trained_word2vec_model_file))

    # validate training params file
    # training_params_file = os.path.join(FLAGS.checkpoint_dir, "..", "training_params.pickle")
    training_params_file = './runs/1510024202/training_params.pickle'
    if not os.path.exists(training_params_file):
        print("Training params file \'{}\' is missing!".format(training_params_file))
    print("Using training params file : {}".format(training_params_file))

    # Load params
    params = data_helpers.loadDict(training_params_file)
    # num_labels = int(params['num_labels'])
    max_document_length = int(params['max_document_length'])

    # Get Embedding vector x_test
    sentences, max_document_length = data_helpers.padding_sentences(x_raw, '<PADDING>',
                                                                    padding_sentence_length=max_document_length)
    x_raw_embedding = np.array(word2vec_helpers.embedding_sentences(sentences,w2vModel))
    print("x_test.shape = {}".format(x_raw_embedding.shape))
    del sentences
    x_raw = np.array([text.encode('utf-8') for text in x_raw])

    return x_raw_embedding,x_raw

def prediction_save(FLAGS,checkpoint_file,graph,x_raw, x_test,filename_flag):

    with graph.as_default():
        session_conf = tf.ConfigProto(
            allow_soft_placement=FLAGS.allow_soft_placement,
            log_device_placement=FLAGS.log_device_placement)
        sess = tf.Session(config=session_conf)
        # 打开会话,准备传入构造好的graph,交给后台运算
        with sess.as_default():
            # Load the saved meta graph and restore variables
            saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
            saver.restore(sess, checkpoint_file)

            # Get the placeholders from the graph by name
            input_x = graph.get_operation_by_name("input_x").outputs[0]
            # input_y = graph.get_operation_by_name("input_y").outputs[0]
            dropout_keep_prob = graph.get_operation_by_name("dropout_keep_prob").outputs[0]

            # Tensors we want to evaluate
            predictions = graph.get_operation_by_name("output/predictions").outputs[0]

            # 以上分别为保存,输出 输入占位符 的设置

            # Generate batches for one epoch
            # 取batches,每FLAGS.batch_size个list(x_test),作一个batches
            batches = data_helpers.batch_iter(list(x_test), FLAGS.batch_size, 1, shuffle=False)

            # Collect the predictions here
            # 判断输出,用来和标签比对,即知道正确率,见下
            all_predictions = []

            for x_test_batch in batches:
                batch_predictions = sess.run(predictions, {input_x: x_test_batch, dropout_keep_prob: 1.0})
                all_predictions = np.concatenate([all_predictions, batch_predictions])
                # 循环相连接,把所有的x_test_batch跑出来的结果x_test_batch,首尾相连
                # dropout_keep_prob=1.0 不损失

    # # Print accuracy if y_test is defined
    # if y_test is not None:
    #     correct_predictions = float(sum(all_predictions == y_test))
    #     print("Total number of test examples: {}".format(len(y_test)))
    #     print("Accuracy: {:g}".format(correct_predictions/float(len(y_test))))
    #     # y_test=所有的标签,correct_predictions=正确率求和,两者求商,为正确率

    # Save the evaluation to a csv
    predictions_human_readable = np.column_stack((x_raw, all_predictions))
    out_path = os.path.join(FLAGS.checkpoint_dir, "..", "prediction/prediction_%s" % filename_flag + ".csv")
    print("Saving evaluation to {0}".format(out_path))
    with open(out_path, 'w') as f:
        csv.writer(f).writerows(predictions_human_readable)
    del predictions_human_readable

if __name__ == "__main__":

    FLAGS, checkpoint_file, graph = load_session()
    w2vModel = word2vec_helpers.load_w2v_model()
    file_num = 10 # 循环加载文件，每次手动更新
    x_raw_all = data_raw_load(str(file_num))
    count = 0
    filename_flag = 0
    new_x_raw = []
    for line in x_raw_all:
        new_x_raw.append(line)
        count += 1
        if count == 1000000:
            count = 0
            x_raw_embedding, x_raw = text_embedding_batchs(new_x_raw,w2vModel)
            new_x_raw = []
            prediction_save(FLAGS, checkpoint_file, graph, x_raw, x_raw_embedding,str(file_num) + '_' +str(filename_flag))
            del x_raw_embedding
            del x_raw
            print filename_flag
            filename_flag += 1
    if len(new_x_raw) != 0:
        x_raw_embedding, x_raw = text_embedding_batchs(new_x_raw,w2vModel)
        prediction_save(FLAGS, checkpoint_file, graph, x_raw, x_raw_embedding,str(filename_flag))
        print filename_flag


