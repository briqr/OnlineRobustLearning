import numpy as np
import os.path


feature_file_name = '/media/datasets/flickr/CNN_features/feature_per_label_'
#feature_file = '/media/datasets/flickr/alexnet_features/features.txt'
label_file = '/media/datasets/flickr/yfcc100m_dataset'

img_list_path = '/media/datasets/flickr/CNN_features/YFCC100M_hybridCNN_gmean_fc6_'

target_features = ['travel', 'canon', 'italy', '2009', 'cruise', 'children', 'netherlands', 'architecture', 'tower', 'wedding', 'america', 'snow', 'venezia', 'utah', 'church']
target_fileoutputs = []
feature_photosid_map = dict()
for target_feature in target_features:
    out_name = feature_file_name+target_feature+'.txt'
    target_f = open(out_name, 'w+')
    target_fileoutputs.append(target_f)
    feature_photosid_map[target_feature]=set()
    i = 0

for line in open(label_file):
    url_str = line.split('\t')
    tags_str = url_str[10].strip().lower()
    photo_id = url_str[1]
    for label in target_features:
        if label in tags_str:
            feature_photosid_map[label].add(photo_id)
      
    i += 1



for f_id in range(100):
    target_ids = set()
    i = 0
    current_feature_filename = img_list_path + str(f_id) + '.txt'
    print current_feature_filename
    if(not os.path.isfile(current_feature_filename)):
        continue
    
    for line in open(current_feature_filename):
        photo_id = line.strip().split("\t")[0]
        k = 0
        for label in target_features:
            if photo_id in feature_photosid_map[label]:
                print 'found', photo_id
                target_fileoutputs[k].write(line + '\n')
            k = k+1

        i += 1