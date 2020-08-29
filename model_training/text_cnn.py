#!/usr/bin/env python
# -*- coding: utf-8 -*-

import tensorflow as tf
import numpy as np


class TextCNN(object):
    '''
    A CNN for text classification
    Uses and embedding layer, followed by a convolutional, max-pooling and softmax layer.
    '''
    def __init__(
        self, sequence_length, num_classes,#vocab_size,
        embedding_size, filter_sizes, num_filters, l2_reg_lambda=0.0): # 把train.py中TextCNN里定义的参数传进来

			# Placeholders for input, output, dropout
			# input_x输入语料,待训练的内容,维度是sequence_length,"N个词构成的N维向量"
			self.input_x = tf.placeholder(tf.float32, [None, sequence_length, embedding_size], name = "input_x")
			# input_y输入语料,待训练的内容标签,维度是num_classes,"正面 || 负面"
			self.input_y = tf.placeholder(tf.float32, [None, num_classes], name = "input_y")
			# dropout_keep_prob dropout参数,防止过拟合,训练时用
			self.dropout_keep_prob = tf.placeholder(tf.float32, name = "dropout_keep_prob")

			# Keeping track of l2 regularization loss (optional)
			l2_loss = tf.constant(0.0)

			# Embedding layer
			# 指定运算结构的运行位置在cpu非gpu,因为"embedding"无法运行在gpu
			# 通过tf.name_scope指定"embedding"
			# with tf.device('/cpu:0'), tf.name_scope("embedding"):  # 指定cpu
			# 	self.W = tf.Variable(tf.random_uniform([vocab_size, embedding_size], -1.0, 1.0), name="W")  # 定义W并初始化
			# 	self.embedded_chars = tf.nn.embedding_lookup(self.W, self.input_x)
			# 	self.embedded_chars_expanded = tf.expand_dims(self.embedded_chars, -1)  # 加一个维度，转换为4维的格式
			self.embedded_chars = self.input_x
			self.embedded_chars_expended = tf.expand_dims(self.embedded_chars, -1)

			# Create a convolution + maxpool layer for each filter size
			pooled_outputs = []
			# filter_sizes卷积核尺寸,枚举后遍历
			for i, filter_size in enumerate(filter_sizes):
				with tf.name_scope("conv-maxpool-%s" % filter_size):
					# Convolution layer
					# 4个参数分别为filter_size高h，embedding_size宽w，channel为1，filter个数
					filter_shape = [filter_size, embedding_size, 1, num_filters]
					W = tf.Variable(tf.truncated_normal(filter_shape, stddev=0.1), name="W")
					b = tf.Variable(tf.constant(0.1, shape=[num_filters]), name="b")
					conv = tf.nn.conv2d(
						self.embedded_chars_expended,
						W,
						strides=[1,1,1,1],
						padding="VALID",# 这里不需要padding
						name="conv")
					# Apply nonlinearity
					# 可以理解为,正面或者负面评价有一些标志词汇,这些词汇概率被增强，即一旦出现这些词汇,倾向性分类进正或负面评价,
					# 该激励函数可加快学习进度，增加稀疏性,因为让确定的事情更确定,噪声的影响就降到了最低。
					h = tf.nn.relu(tf.nn.bias_add(conv, b), name = "relu")
					# Maxpooling over the outputs
					# 池化,池化可以减少输出维数，同时一定程度上保持了最突出的信息。
					pooled = tf.nn.max_pool(
						h,
						ksize=[1, sequence_length - filter_size + 1, 1, 1],
						strides=[1,1,1,1],
						padding="VALID",# 这里不需要padding
						name="pool")
					pooled_outputs.append(pooled)

			# Combine all the pooled features
			num_filters_total = num_filters * len(filter_sizes)
			self.h_pool = tf.concat(pooled_outputs, 3)
			self.h_pool_flat = tf.reshape(self.h_pool, [-1, num_filters_total])

			# Add dropout
			# drop层,防止过拟合,参数为dropout_keep_prob
			# 过拟合的本质是采样失真,噪声权重影响了判断，如果采样足够多,足够充分,噪声的影响可以被量化到趋近事实,也就无从过拟合。
			# 即数据越大,drop和正则化就越不需要。
			with tf.name_scope("dropout"):
				self.h_drop = tf.nn.dropout(self.h_pool_flat, self.dropout_keep_prob)

			# Final (unnomalized) scores and predictions
			# 输出层
			with tf.name_scope("output"):
				W = tf.get_variable(
					"W",
					shape = [num_filters_total, num_classes], #前面连扁平化后的池化操作
					initializer = tf.contrib.layers.xavier_initializer()) # 定义初始化方式
				b = tf.Variable(tf.constant(0.1, shape=[num_classes], name = "b"))
				# 损失函数导入
				l2_loss += tf.nn.l2_loss(W)
				l2_loss += tf.nn.l2_loss(b)
				# xw+b
				self.scores = tf.nn.xw_plus_b(self.h_drop, W, b, name = "scores")
				self.predictions = tf.argmax(self.scores, 1, name = "predictions")

			# Calculate Mean cross-entropy loss
			with tf.name_scope("loss"):
				# loss，交叉熵损失函数
				losses = tf.nn.softmax_cross_entropy_with_logits(logits = self.scores, labels = self.input_y)
				self.loss = tf.reduce_mean(losses) + l2_reg_lambda * l2_loss

			# Accuracy
			with tf.name_scope("accuracy"):
				# 准确率，求和计算算数平均值
				correct_predictions = tf.equal(self.predictions, tf.argmax(self.input_y, 1))
				self.accuracy = tf.reduce_mean(tf.cast(correct_predictions, "float"), name = "accuracy")
