import tensorflow as tf
import numpy as np
import json
import random
import glob
import os

def byteToVector(b):
	array = [0 for a in range(0,256)]
	array[b] = 1
	return array

def byteArrayToTensor(byteArray, length):
	tensor = [byteToVector(b) for b in byteArray]
	if len(tensor) > length:
		return tensor[0:length]
	while len(tensor) < length:
		tensor.append(byteToVector(0))
	return tensor

def flatten(arrays):
	result = []
	for array in arrays:
		for item in array:
			result.append(item)
	return result

# モデル定義
model = tf.keras.models.Sequential([
	tf.keras.layers.Flatten(),
	tf.keras.layers.Dense(100, activation=tf.nn.relu),
	tf.keras.layers.Dense(10, activation=tf.nn.relu),
	tf.keras.layers.Dense(20, activation=tf.nn.relu),
	tf.keras.layers.Dropout(0.2),
	tf.keras.layers.Dense(12, activation=tf.nn.softmax)
])
model.compile(optimizer='adam',
	loss='sparse_categorical_crossentropy',
	metrics=['accuracy'])

if os.path.exists("./main.model.index"):
	print("load")
	model.load_weights("./main.model")

# JSONファイル列挙
listFileNameJsonThread = glob.glob("json/*")

for i in range(0, len(listFileNameJsonThread)):
	
	# 1個取る
	fileNameJsonThread = listFileNameJsonThread[i]
	
	with open(fileNameJsonThread) as f:
		jsonThread = json.load(f)
		
		# 履歴
		tensors = []
		
		for jsonResponse in jsonThread:
			
			# レスのテンソルを作る
			tensor = byteArrayToTensor(jsonResponse[2], 50)
			
			# 履歴に追加
			tensors.append(tensor)
			
			# 履歴は10個まで保存
			if len(tensors) > 10:
				tensors = tensors[1:11]
			
			# 履歴が10個存在すれば
			if len(tensors) == 10:
				
				# アンカーなし→0
				# アンカーアリだが未来→11
				# アンカーありだが範囲外→11
				# アンカーありで範囲内→1～10　1の時にそのレスと同じ、10の時に9個前のレス
				if jsonResponse[1] == -1:
					y = 0
				else:
					y = -(jsonResponse[1] - jsonResponse[0]) + 1
					if y <= 0:
						y = 11
					if y > 10:
						y = 11
				
				# そのレスを含めた直近10レス分の内容を表す行列
				x = flatten(tensors)
				
				# 評価
				npx = np.array([x])
				npy = model.predict(npx)
				print("%5s; Expected: %2s; Actual: %2s; %4s / %4s at %4s / %4s (%s)" % (
					y == npy.argmax(),
					y,
					npy.argmax(),
					jsonResponse[0],
					len(jsonThread),
					i,
					len(listFileNameJsonThread),
					fileNameJsonThread))
